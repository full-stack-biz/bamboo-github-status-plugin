package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config;

import com.atlassian.bamboo.plan.PlanHelper;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugins.git.GitHubRepository;
import com.atlassian.bamboo.plugins.git.GitRepository;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;

import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class GithubStatusBuildConfiguration extends BuildConfiguration {
    static final String CONFIG_PREFIX = "custom.tools.fullstackbiz.bamboo.github.status.";
    public static final String REPOSITORIES_KEY = CONFIG_PREFIX + "repositories";
    public static final String STAGES_EXCLUDED_KEY = CONFIG_PREFIX + "stages.excluded";

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

    public static LinkedList<PlanRepositoryDefinition> getPlanRepositories(ImmutablePlan plan) {
        return PlanHelper.getPlanRepositoryDefinitions(plan)
                .stream()
                .filter(e -> (e.asLegacyData().getRepository() instanceof GitHubRepository || e.asLegacyData().getRepository() instanceof GitRepository))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public ExcludedStages getExcludedStages() {
        return new ExcludedStages(getString(STAGES_EXCLUDED_KEY));
    }

    public LinkedList<Repository> getRepositories(ImmutablePlan plan) {
        LinkedList<Repository> repositories = new LinkedList<>();
        int i = 0;
        for (PlanRepositoryDefinition r : getPlanRepositories(plan)) {
            repositories.add(new Repository(i, r.getName(), isRepositoryEnabled(r)));
            i++;
        }
        return repositories;
    }

    public boolean isRepositoryEnabled(PlanRepositoryDefinition repoToCheck) {
        return getBoolean(String.format("%s.id_%s", REPOSITORIES_KEY, repoToCheck.getPosition()));
    }
}
