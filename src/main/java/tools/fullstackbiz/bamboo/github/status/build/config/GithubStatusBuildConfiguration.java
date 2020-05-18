package tools.fullstackbiz.bamboo.github.status.build.config;
import com.atlassian.bamboo.specs.yaml.MapNode;
import com.atlassian.bamboo.specs.yaml.ListNode;
import com.atlassian.bamboo.specs.yaml.Node;
import com.atlassian.bamboo.plan.PlanHelper;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugins.git.GitHubRepository;
import com.atlassian.bamboo.plugins.git.GitRepository;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.bamboo.specs.api.exceptions.PropertiesValidationException;

import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class GithubStatusBuildConfiguration extends BuildConfiguration {
    static final String CONFIG_PREFIX = "custom.bamboo.github.status.";
    public static final String REPOSITORIES_KEY = CONFIG_PREFIX + "repositories";
    public static final String STAGES_EXCLUDED_KEY = CONFIG_PREFIX + "stages.excluded";
    
    private interface YamlTags {
        String YAML_ROOT = "fullstackbiz.bamboo.github.status";
        String REPOSITORIES = "repositories";
        String STAGES_EXCLUDED = "stages-excluded";
    }

    public ExcludedStages getExcludedStages() {
        return new ExcludedStages(getString(STAGES_EXCLUDED_KEY));
    }

    public LinkedList<Repository> getRepositories(ImmutablePlan plan) {
        LinkedList<Repository> repositories = new LinkedList<>();
        int i = 0;
        for (PlanRepositoryDefinition r : getPlanRepositories(plan)) {
            repositories.add(new Repository(i, r.getName()));
            i++;
        }
        return repositories;
    }

    public static GithubStatusBuildConfiguration from(BuildConfiguration config) {
        GithubStatusBuildConfiguration c = new GithubStatusBuildConfiguration();
        c.addConfiguration(config);
        return c;
    }

    public static GithubStatusBuildConfiguration from(Map<String, String> config) {
        GithubStatusBuildConfiguration c = new GithubStatusBuildConfiguration();
        config.forEach(c::setProperty);
        return c;
    }

    public static GithubStatusBuildConfiguration fromYaml(@NotNull Node node) throws PropertiesValidationException {
        if (node instanceof MapNode) {
            GithubStatusBuildConfiguration c = new GithubStatusBuildConfiguration();
            MapNode mapNode = (MapNode) node;
            final Optional<StringNode> yamlConfig = mapNode.getOptionalString(YamlTags.YAML_ROOT);
            if (yamlConfig.isPresent() && yamlConfig instanceof MapNode) {
                yamlConfig.getOptionalList(YamlTags.REPOSITORIES, MapNode.class)
                    .ifPresent(repositoriesMaps -> repositoriesMaps.asListOf(MapNode.class).stream()
                    .map(this::parseEnabledRepos)
                    .forEach(c::setProperty));
               
                return c;
            }
        }
        return null;
    }

    private String parseEnabledRepos(int position) {
       return String.format("%s.id_%s", REPOSITORIES_KEY, position);
    }

    public static LinkedList<PlanRepositoryDefinition> getPlanRepositories(ImmutablePlan plan) {
        return PlanHelper.getPlanRepositoryDefinitions(plan)
                .stream()
                .filter(e -> (e.asLegacyData().getRepository() instanceof GitHubRepository || e.asLegacyData().getRepository() instanceof GitRepository))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean isRepositoryEnabled(PlanRepositoryDefinition repoToCheck) {
        return getBoolean(String.format("%s.id_%s", REPOSITORIES_KEY, repoToCheck.getPosition()));
    }
}
