package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

import com.atlassian.bamboo.specs.api.model.AtlassianModuleProperties;
import com.atlassian.bamboo.specs.api.model.plan.configuration.PluginConfigurationProperties;
import com.atlassian.bamboo.specs.api.validators.common.ImporterUtils;
import com.atlassian.bamboo.specs.api.validators.common.ValidationContext;

import java.util.LinkedList;

public class GithubStatusProperties implements PluginConfigurationProperties {
    public static final ValidationContext VALIDATION_CONTEXT = ValidationContext.of("Github Status");
    public static final String REPOSITORIES = GithubStatusBuildConfiguration.REPOSITORIES_KEY;

    LinkedList<Repository> repositories;

    private GithubStatusProperties() {
    }

    public GithubStatusProperties(LinkedList<Repository> repositories) {
        this.repositories = repositories;
    }

    @Override
    public AtlassianModuleProperties getAtlassianPlugin() {
        return new AtlassianModuleProperties("com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status:status");
    }

    @Override
    public final void validate() {
        ImporterUtils.checkThat(VALIDATION_CONTEXT, this.repositories.size() > 0, "Should have repositories");
    }
}
