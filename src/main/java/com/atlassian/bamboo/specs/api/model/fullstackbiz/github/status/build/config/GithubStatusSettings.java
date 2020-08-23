package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

import com.atlassian.bamboo.specs.api.builders.plan.configuration.PluginConfiguration;

import java.util.LinkedList;

public class GithubStatusSettings extends PluginConfiguration<GithubStatusProperties> {

    private LinkedList<Repository> repositories;

    public GithubStatusSettings() {
        this.repositories = new LinkedList<Repository>();
    }

    public GithubStatusSettings(LinkedList<Repository> repositories) {
        this.repositories = repositories;
    }

    public GithubStatusSettings repositories(LinkedList<Repository> repositories) {
        this.repositories = repositories;
        return this;
    }

    @Override
    protected GithubStatusProperties build() {
        return new GithubStatusProperties(repositories);
    }

}
