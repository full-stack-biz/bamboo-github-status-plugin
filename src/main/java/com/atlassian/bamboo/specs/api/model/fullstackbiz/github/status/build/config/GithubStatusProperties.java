package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

import com.atlassian.bamboo.specs.api.codegen.annotations.Builder;
import com.atlassian.bamboo.specs.api.model.AtlassianModuleProperties;
import com.atlassian.bamboo.specs.api.model.plan.configuration.PluginConfigurationProperties;
import com.atlassian.bamboo.specs.api.validators.common.ImporterUtils;
import com.atlassian.bamboo.specs.api.validators.common.ValidationContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Builder(GithubStatusSettings.class)
public class GithubStatusProperties implements PluginConfigurationProperties {
    public static final ValidationContext VALIDATION_CONTEXT = ValidationContext.of("Github Status");
    public static final String REPOSITORIES = GithubStatusBuildConfiguration.REPOSITORIES_KEY;

    List<Repository> repositories;

    private GithubStatusProperties() {
        this.repositories = new LinkedList<Repository>();
    }

    public GithubStatusProperties(List<Repository> repositories) {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.repositories);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GithubStatusProperties other = (GithubStatusProperties) obj;
        return Objects.equals(this.repositories, other.repositories);
    }
}
