package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.bamboo.plan.Plan;
import com.atlassian.bamboo.plan.TopLevelPlan;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.plan.configuration.MiscellaneousPlanConfigurationPlugin;
import com.atlassian.bamboo.specs.api.exceptions.PropertiesValidationException;
import com.atlassian.bamboo.specs.api.validators.common.ValidationContext;
import com.atlassian.bamboo.specs.yaml.BambooYamlParserUtils;
import com.atlassian.bamboo.specs.yaml.MapNode;
import com.atlassian.bamboo.specs.yaml.Node;
import com.atlassian.bamboo.specs.yaml.StringNode;
import com.atlassian.bamboo.template.TemplateRenderer;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.v2.build.BaseBuildConfigurationAwarePlugin;
import com.atlassian.bamboo.v2.build.ImportExportAwarePlugin;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration.REPOSITORIES_KEY;
import static tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration.STAGES_EXCLUDED_KEY;

public class Configuration extends BaseBuildConfigurationAwarePlugin
        implements MiscellaneousPlanConfigurationPlugin, ImportExportAwarePlugin<Settings, GithubStatusSettings> {

    private final TemplateRenderer templateRenderer;

    public Configuration(@ComponentImport(value = "TemplateRenderer") TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    @NotNull
    @Override
    public Set<String> getConfigurationKeys() {
        return new HashSet<>(Arrays.asList(REPOSITORIES_KEY, STAGES_EXCLUDED_KEY));
    }

    @NotNull
    @Override
    public Settings toSpecsEntity(HierarchicalConfiguration buildConfiguration) {
        LinkedList<Repository> repositories = new LinkedList<>();
        for (Iterator it = buildConfiguration.getKeys(REPOSITORIES_KEY); it.hasNext(); ) {
            String key = (String) it.next();
            Repository repo = new Repository(Integer.parseInt(key.replace(REPOSITORIES_KEY + ".id_", "")), key.replace(REPOSITORIES_KEY + ".", ""));
            repo.setEnabled(buildConfiguration.getBoolean(key));
            repositories.add(repo);
        }
        return new Settings(repositories);
    }

    @Override
    public void addToBuildConfiguration(GithubStatusSettings specsProperties,
                                        @NotNull HierarchicalConfiguration buildConfiguration) {
        specsProperties.validate();
        final int[] i = {-1};
        specsProperties.repositories.forEach(repository -> {
            buildConfiguration.setProperty(String.format("%s.id_%s", REPOSITORIES_KEY, ++i[0]), true);
        });
    }

    @Override
    public boolean isApplicableTo(@NotNull final ImmutablePlan plan) {
        return plan instanceof ImmutableTopLevelPlan;
    }

    @Override
    protected void populateContextForEdit(@NotNull Map<String, Object> context,
                                          @NotNull BuildConfiguration buildConfiguration,
                                          Plan plan) {
        GithubStatusBuildConfiguration config = GithubStatusBuildConfiguration.from(buildConfiguration);
        context.put("repositories", config.getRepositories(plan));
        context.put("excluded", config.getExcludedStages());
        context.put("repositoriesKey", REPOSITORIES_KEY);
        context.put("excludedStagesKey", STAGES_EXCLUDED_KEY);
    }

    @NotNull
    @Override
    public ErrorCollection validate(@NotNull final BuildConfiguration buildConfiguration) {
        ErrorCollection errorCollection = new SimpleErrorCollection();
//        buildConfiguration.getKeys("custom.tools.fullstackbiz.bamboo.github.status.repositories").forEach(key -> {
//            if(buildConfiguration.getBoolean(key)) {}
//        });
        return errorCollection;

//        String suggestedOwner = buildConfiguration.getString(OWNER_OF_PLAN);
//
//
//        if (!planOwnershipConfigService.isEnforcementEnabled()) {
//            return errorCollection;
//        } else {
//            if (!planOwnershipValidatorService.isSuggestedOwnerValid(suggestedOwner)) {
//                errorCollection.addError(OWNER_OF_PLAN,
//                        planOwnershipValidatorService.getReasonForSuggestedOwner(suggestedOwner));
//            }
//            return errorCollection;
//        }
    }

    @Nullable
    @Override
    public Settings fromYaml(@NotNull Node node) throws PropertiesValidationException {
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
        final Map<String, Object> config = new LinkedHashMap<>();
        config.put(YamlTags.REPOSITORIES, settings.repositories.stream()
                .collect(Collectors.toMap(
                        Repository::getName,
                        Repository::getEnabled)
                ));
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put(YamlTags.YAML_ROOT, config);
//        result.put(YamlTags.STAGES_EXCLUDED, config);
        return BambooYamlParserUtils.asNode(result, ValidationContext.of(YamlTags.YAML_ROOT));
    }

    private interface YamlTags {
        String YAML_ROOT = "github-status";
        String REPOSITORIES = "repositories";
        String STAGES_EXCLUDED = "stages-excluded";
    }

    private String parseEnabledRepos(StringNode position) {
        return String.format("%s.id_%s", REPOSITORIES_KEY, position.get());
    }
}
