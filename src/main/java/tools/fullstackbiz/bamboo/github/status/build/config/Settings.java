package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.bamboo.specs.api.builders.plan.configuration.PluginConfiguration;

import java.util.LinkedList;

public class Settings extends PluginConfiguration<GithubStatusSettings> {

    private LinkedList<Repository> repositories;

    public Settings() {

    }

    public Settings(LinkedList<Repository> repositories) {
        this.repositories = repositories;
    }

    public Settings repositories(LinkedList<Repository> repositories) {
        this.repositories = repositories;
        return this;
    }

    @Override
    protected GithubStatusSettings build() {
        return new GithubStatusSettings(repositories);
    }

}
