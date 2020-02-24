package in.ashwanthkumar.gocd.actions;

import in.ashwanthkumar.gocd.artifacts.config.JanitorConfiguration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Most of the methods are copied from FileUtils class of Apache Commons IO.
 */
public class DeleteAction implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteAction.class);

    public static final String COULD_NOT_DELETE = "Could not delete '%s'.";

    private final Set<String> whiteList;
    private final JanitorConfiguration janitorConfiguration;

    public DeleteAction(JanitorConfiguration janitorConfiguration, Set<String> whiteList) {
        this.whiteList = whiteList;
        this.janitorConfiguration = janitorConfiguration;
    }

    public DeleteAction(JanitorConfiguration janitorConfiguration, String... paths) {
        whiteList = new HashSet<>(Arrays.asList(paths));
        this.janitorConfiguration = janitorConfiguration;
    }

    @Override
    public long invoke(File pipelineDir, String version, boolean dryRun, boolean force) {
        File versionDir = new File(pipelineDir.getAbsolutePath() + "/" + version);
        DirectoryStats stats = getDirectoryStats(versionDir, force);

        if (stats.fileCount > 0) {
            String path = versionDir.getAbsolutePath();
            String displaySize = FileUtils.byteCountToDisplaySize(stats.size);

            if (dryRun) {
                LOG.info("[DRY RUN] Will remove {}, size = {}", path, displaySize);
            } else {
                LOG.info("Deleting {}, size = {}", path, displaySize);

                try {
                    deleteDirectory(versionDir, force);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        return stats.size;
    }

    private DirectoryStats getDirectoryStats(File directory, boolean force) {
        DirectoryStats stats = new DirectoryStats();

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                try {
                    if (!FileUtils.isSymlink(file) && isNotWhiteListed(file)) {

                        if (file.isDirectory()) {
                            if (!force && (janitorConfiguration.getDeletedLogsInDays() > 0 && isDirLog(file) && !checkDateOfLog(file))) {
                                //NOACTION
                            } else {
                                stats.add(getDirectoryStats(file, force));
                            }
                        } else {
                            stats.add(file);
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }

        return stats;
    }

    private void deleteDirectory(File path, boolean force) throws IOException {
        if (isNotWhiteListed(path)) {
            File[] files = path.listFiles();
            if (files == null) throw new IOException("Couldn't list files inside " + path.getAbsolutePath());
            for (File file : files) {
                if (isNotWhiteListed(file)) {
                    if (file.isDirectory()) {
                        if (!force && (janitorConfiguration.getDeletedLogsInDays() > 0 && isDirLog(file) && !checkDateOfLog(file))) {
                            continue;
                        }
                        deleteDirectory(file, force);
                    } else {
                        Files.delete(file.toPath());
                    }
                }
            }
        }
        deleteEmptyDirectory(path);
    }

    private void deleteEmptyDirectory(final File dir) throws IOException {
        if (dir.isDirectory() && dir.list().length == 0 && !dir.delete()) {
            throw new IOException(String.format(COULD_NOT_DELETE, dir.getAbsolutePath()));
        }
    }

    private boolean isNotWhiteListed(File file) {
        return !this.whiteList.contains(file.getName());
    }

    private boolean isDirLog(File file) {
        return file.getName().contains("cruise-output");
    }

    private boolean checkDateOfLog(File file) {
        long diff = new Date().getTime() - file.lastModified();
        return diff < janitorConfiguration.getDeletedLogsInDays() * 24 * 60 * 60 * 1000;

    }

    private class DirectoryStats {
        private long size = 0;
        private long fileCount = 0;

        void add(DirectoryStats directoryStats) {
            size += directoryStats.size;
            fileCount += directoryStats.fileCount;
        }

        void add(File file) {
            size += file.length();
            fileCount += 1;
        }
    }
}
