package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

import com.atlassian.bamboo.specs.api.builders.plan.configuration.PluginConfiguration;

import java.util.LinkedList;

public class Settings extends PluginConfiguration<GithubStatusProperties> {

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
    protected GithubStatusProperties build() {
        return new GithubStatusProperties(repositories);
    }

}
