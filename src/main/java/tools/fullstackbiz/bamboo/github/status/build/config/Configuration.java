package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plan.configuration.MiscellaneousPlanConfigurationPlugin;
import com.atlassian.bamboo.specs.api.exceptions.PropertiesValidationException;
import com.atlassian.bamboo.specs.api.validators.common.ValidationContext;
import com.atlassian.bamboo.specs.yaml.BambooYamlParserUtils;
import com.atlassian.bamboo.specs.yaml.MapNode;
import com.atlassian.bamboo.specs.yaml.Node;
import com.atlassian.bamboo.specs.yaml.StringNode;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.v2.build.ImportExportAwarePlugin;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration.REPOSITORIES_KEY;

public class Configuration extends BaseBuildConfigurationAwarePlugin
        implements MiscellaneousPlanConfigurationPlugin, ImportExportAwarePlugin<Settings, GithubStatusSettings> {
    private static final String YAML_KEY = "ghstatus";
    private static final Logger log = LoggerFactory.getLogger(GithubStatusBuildConfiguration.class);

    private final TemplateRenderer templateRenderer;

    public Configuration(@ComponentImport(value = "TemplateRenderer") TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @NotNull
    @Override
    public Set<String> getConfigurationKeys() {
        return Collections.singleton(REPOSITORIES_KEY);
    }

    @NotNull
    @Override
    public Settings toSpecsEntity(HierarchicalConfiguration buildConfiguration) {
        GithubStatusBuildConfiguration config = GithubStatusBuildConfiguration.from((BuildConfiguration) buildConfiguration);
        // config.getRepositories(plan)
        return new Settings();
    }

    @Override
    public void addToBuildConfiguration(GithubStatusSettings specsProperties,
                                        @NotNull HierarchicalConfiguration buildConfiguration) {
        specsProperties.validate();
        final int[] i = {-1};
        specsProperties.repositories.forEach(repository -> {
            buildConfiguration.addProperty(String.format("%s.id_%s", REPOSITORIES_KEY, ++i[0]), true);
        });
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

    @NotNull
    public Settings fromYaml(@NotNull Node node) throws PropertiesValidationException {
        log.debug("node:" + node.toString());
        if (node instanceof MapNode) {
            GithubStatusBuildConfiguration c = new GithubStatusBuildConfiguration();
            MapNode mapNode = (MapNode) node;
            if (mapNode.getOptionalNode(YamlTags.YAML_ROOT).isPresent()) {
                Optional<MapNode> yamlConfig = mapNode.getOptionalMap(YamlTags.YAML_ROOT);
                yamlConfig.ifPresent(root -> root.getOptionalList(YamlTags.REPOSITORIES, MapNode.class)
                        .ifPresent(list -> list.asListOf(StringNode.class)
                                .stream()
                                .map(this::parseEnabledRepos)
                                .forEach(prop -> c.setProperty(prop, true))));

                return new Settings();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Node toYaml(@NotNull GithubStatusSettings settings) {
        final Map<String, String> result = new HashMap<>();
        result.put(YAML_KEY, settings.toString());
        return BambooYamlParserUtils.asNode(result, ValidationContext.of(YAML_KEY));
    }

    private interface YamlTags {
        String YAML_ROOT = "githubstatus";
        String REPOSITORIES = "repositories";
        String STAGES_EXCLUDED = "stages-excluded";
    }

    private String parseEnabledRepos(StringNode position) {
        return String.format("%s.id_%s", REPOSITORIES_KEY, position.get());
    }
}
