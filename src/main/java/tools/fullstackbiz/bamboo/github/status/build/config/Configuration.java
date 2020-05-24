package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plan.configuration.MiscellaneousPlanConfigurationPlugin;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.v2.build.ImportExportAwarePlugin;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration.REPOSITORIES_KEY;

public class Configuration extends BaseBuildConfigurationAwarePlugin
        implements MiscellaneousPlanConfigurationPlugin, ImportExportAwarePlugin<Settings, GithubStatusSettings> {

     private final TemplateRenderer templateRenderer;

    @NotNull
    @Override
    public Set<String> getConfigurationKeys() {
        return Collections.singleton(REPOSITORIES_KEY);
    }

    @NotNull
    @Override
    public Settings toSpecsEntity(HierarchicalConfiguration buildConfiguration) {
        return new Settings(buildConfiguration.getString(REPOSITORIES_KEY));
    }

    public Configuration(@ComponentImport(value = "TemplateRenderer") TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @Override
    public boolean isApplicableTo(@NotNull ImmutablePlan immutablePlan) {
        return immutablePlan instanceof TopLevelPlan;
    }

    @Override
    public boolean isApplicableTo(Plan plan) {
        return plan instanceof TopLevelPlan;
    }

    @Override
    protected void populateContextForEdit(@NotNull Map<String, Object> context,
                                          @NotNull BuildConfiguration buildConfiguration,
                                          Plan plan) {
        GithubStatusBuildConfiguration config = GithubStatusBuildConfiguration.from(buildConfiguration);
        context.put("repositories", config.getRepositories(plan));
        context.put("excluded", config.getExcludedStages());
        context.put("repositoriesKey", REPOSITORIES_KEY);
        context.put("excludedStagesKey", GithubStatusBuildConfiguration.STAGES_EXCLUDED_KEY);
    }
}
