package in.ashwanthkumar.gocd.artifacts.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.ashwanthkumar.gocd.hocon.HoconUtils;
import in.ashwanthkumar.utils.collections.Lists;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JanitorConfiguration {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private String server;
    private String username;
    private String password;
    private String artifactStorage;
    private Integer defaultPipelineVersions;
    private List<PipelineConfig> pipelines;
    private Set<String> pipelineNames;
    private String pipelinePrefix;
    private int deletedLogsInDays;
    private boolean removeLogs;
    private boolean forceRemoveOldPipelineLogs;


    public static JanitorConfiguration load(String file) {
        return load(ConfigFactory.parseFile(new File(file)));
    }

    public static JanitorConfiguration load(Config config) {
        Config envThenSystem = ConfigFactory.systemEnvironment().withFallback(ConfigFactory.systemProperties());
        config = config.resolveWith(envThenSystem);

        config = config.getConfig("gocd.janitor");

        final JanitorConfiguration janitorConfiguration = new JanitorConfiguration()
                .setServer(config.getString("server"))
                .setArtifactStorage(config.getString("artifacts-dir"))
                .setDefaultPipelineVersions(config.getInt("pipeline-versions"))
                .setPipelinePrefix(HoconUtils.getString(config, "pipeline-prefix", ""))
                .setRemoveLogs(HoconUtils.getBoolean(config, "remove-logs", false))
                .setDeletedLogsInDays(HoconUtils.getInteger(config, "delete-logs-older-than-days", 0))
                .setForceRemoveOldPipelineLogs(HoconUtils.getBoolean(config, "force-remove-old-pipeline-logs", false));

        if (config.hasPath(USERNAME) && config.hasPath(PASSWORD)) {
            janitorConfiguration.setUsername(config.getString(USERNAME))
                                .setPassword(config.getString(PASSWORD));
        }

        List<PipelineConfig> pipelines = Lists.map(config.getConfigList("pipelines"), config1 -> PipelineConfig.fromConfig(janitorConfiguration.getDefaultPipelineVersions(), config1));

        return janitorConfiguration.setPipelines(pipelines);
    }

    public boolean isRemoveLogs() {
        return removeLogs;
    }

    public int getDeletedLogsInDays() {
        return deletedLogsInDays;
    }

    private JanitorConfiguration setDeletedLogsInDays(Integer deletedLogsInDays) {
        this.deletedLogsInDays = deletedLogsInDays;
        return this;
    }

    public boolean isForceRemoveOldPipelineLogs() {
        return forceRemoveOldPipelineLogs;
    }

    public String getServer() {
        return server;
    }

    public JanitorConfiguration setServer(String server) {
        this.server = server;
        return this;
    }

    public String getArtifactStorage() {
        return artifactStorage;
    }

    public JanitorConfiguration setArtifactStorage(String artifactStorage) {
        this.artifactStorage = artifactStorage;
        return this;
    }

    public List<PipelineConfig> getPipelines() {
        return pipelines;
    }

    public JanitorConfiguration setPipelines(List<PipelineConfig> pipelines) {
        this.pipelines = pipelines;
        setPipelineNames();
        return this;
    }

    public JanitorConfiguration setRemoveLogs(Boolean removeLogs) {
        this.removeLogs = removeLogs;
        return this;
    }

    public JanitorConfiguration setForceRemoveOldPipelineLogs(Boolean forceRemoveOldPipelineLogs) {
        this.forceRemoveOldPipelineLogs = forceRemoveOldPipelineLogs;
        return this;
    }
    public String getUsername() {
        return username;
    }

    public JanitorConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JanitorConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getDefaultPipelineVersions() {
        return defaultPipelineVersions;
    }

    public JanitorConfiguration setDefaultPipelineVersions(Integer defaultPipelineVersions) {
        this.defaultPipelineVersions = defaultPipelineVersions;
        return this;
    }

    public JanitorConfiguration setPipelinePrefix(String pipelinePrefix) {
        this.pipelinePrefix = pipelinePrefix;
        return this;
    }

    public boolean hasPipeline(String pipeline) {
        return pipelineNames.contains(pipeline);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JanitorConfiguration that = (JanitorConfiguration) o;
        return Objects.equals(server, that.server) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(artifactStorage, that.artifactStorage) &&
                Objects.equals(pipelines, that.pipelines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, username, password, artifactStorage, pipelines);
    }

    void setPipelineNames() {
        this.pipelineNames = new HashSet<>(Lists.map(pipelines, pipelineConfig -> pipelineConfig.getName()));
    }

    public String getPipelinePrefix() {
        return pipelinePrefix;
    }
}
