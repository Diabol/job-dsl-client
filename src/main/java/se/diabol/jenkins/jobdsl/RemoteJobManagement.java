package se.diabol.jenkins.jobdsl;

import hudson.util.VersionNumber;
import javaposse.jobdsl.dsl.*;

import java.net.URL;

public class RemoteJobManagement extends AbstractJobManagement {

    private JenkinsClient client;

    public RemoteJobManagement(URL url, String username, String password) {
        client = new JenkinsClient(url,username, password);
    }

    @Override
    public String getConfig(String jobName) throws JobConfigurationNotFoundException {
        System.out.println("Get: " + jobName);
        return null;
    }

    @Override
    public boolean createOrUpdateConfig(String jobName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        try {
            if (client.itemExists(jobName, true)) {
                client.updateItem(jobName, config, true);
            } else {
                client.createItem(jobName, config, true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void createOrUpdateView(String viewName, String config, boolean ignoreExisting) throws NameNotProvidedException, ConfigurationMissingException {
        try {
            if (client.itemExists(viewName, false)) {
                client.updateItem(viewName, config, false);
            } else {
                client.createItem(viewName, config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String createOrUpdateConfigFile(ConfigFile configFile, boolean ignoreExisting) {
        return null;
    }

    @Override
    public Integer getVSphereCloudHash(String name) {
        return null;
    }

    @Override
    public String getConfigFileId(ConfigFileType type, String name) {
        return null;
    }

    @Override
    public String getCredentialsId(String credentialsDescription) {
        return null;
    }

    @Override
    public void requireMinimumPluginVersion(String pluginShortName, String version) {

    }

    @Override
    public VersionNumber getPluginVersion(String pluginShortName) {
        return null;
    }



}
