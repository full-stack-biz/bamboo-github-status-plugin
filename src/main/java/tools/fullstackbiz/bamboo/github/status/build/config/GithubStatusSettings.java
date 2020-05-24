package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.bamboo.specs.api.model.AtlassianModuleProperties;
import com.atlassian.bamboo.specs.api.model.plan.configuration.PluginConfigurationProperties;
import com.atlassian.bamboo.specs.api.validators.common.ImporterUtils;
import com.atlassian.bamboo.specs.api.validators.common.ValidationContext;

import java.util.LinkedList;

public class GithubStatusSettings implements PluginConfigurationProperties {
    public static final ValidationContext VALIDATION_CONTEXT = ValidationContext.of("Github Status");

    LinkedList<Repository> repositories;

    private GithubStatusSettings() {
    }

    public GithubStatusSettings(LinkedList<Repository> repositories) {
        this.repositories = repositories;
    }

    @Override
    public AtlassianModuleProperties getAtlassianPlugin() {
        return new AtlassianModuleProperties("custom.bamboo.github.status:repositories");
    }

    @Override
    public final void validate() {
        ImporterUtils.checkThat(VALIDATION_CONTEXT, this.repositories.size() > 0, "Should have repositories");
    }
}
