package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.action;

import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.plugins.git.GitHubRepository;
import com.atlassian.bamboo.plugins.git.GitRepository;
import com.atlassian.bamboo.repository.Repository;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import org.kohsuke.github.GHCommitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config.GithubStatusBuildConfiguration;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service.GitHubService;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service.GithubServiceInterface;

import java.net.MalformedURLException;
import java.net.URL;


abstract class AbstractGitHubStatusAction {
    private static final Logger log = LoggerFactory.getLogger(AbstractGitHubStatusAction.class);
    private final PlanManager planManager;
    private GithubServiceInterface gitHubService;

    AbstractGitHubStatusAction(PlanManager planManager, GithubServiceInterface gitHubService) {
        this.planManager = planManager;
        this.gitHubService = gitHubService;
    }

    void pushUpdate(StageExecution stageExecution, GHCommitState status, String description) {
        ImmutableChain chain = getChain(stageExecution);
        ChainExecution chainExecution = stageExecution.getChainExecution();
        PlanResultKey planResultKey = chainExecution.getPlanResultKey();

        assert chain != null;
        GithubStatusBuildConfiguration config = GithubStatusBuildConfiguration.from(chain.getBuildDefinition().getCustomConfiguration());

        if (config.getExcludedStages().contains(stageExecution.getName())) {
            log.debug("Skipping ignored stage: {}", stageExecution.getName());
            return;
        }

        for (PlanRepositoryDefinition repo : GithubStatusBuildConfiguration.getPlanRepositories(chain)) {
            if (shouldUpdateRepo(chain, repo, config)) {
                String sha = chainExecution.getBuildChanges().getVcsRevisionKey(repo.getId());
                if (sha != null) {
                    log.info("Publishing build status for stage: {}", stageExecution.getName());
                    gitHubService.setStatus(
                            repo.asLegacyData().getRepository(),
                            status,
                            sha,
                            planResultKey.getKey(),
                            stageExecution.getName(),
                            description
                    );
                }
            } else {
                log.debug("Should not update repo: {}", repo.getName());
            }
        }
    }

    void pushUpdate(StageExecution stageExecution, GHCommitState status) {
        pushUpdate(stageExecution, status, null);
    }

    protected ImmutableChain getChain(StageExecution stageExecution) {
        ChainExecution chainExecution = stageExecution.getChainExecution();
        PlanResultKey planResultKey = chainExecution.getPlanResultKey();
        PlanKey planKey = planResultKey.getPlanKey();
        return (ImmutableChain) planManager.getPlanByKey(planKey);
    }

    private boolean shouldUpdateRepo(ImmutableChain chain, final PlanRepositoryDefinition repo, GithubStatusBuildConfiguration config) {
        PlanRepositoryDefinition repoToCheck = repo;
        if (chain.hasMaster()) {
            repoToCheck = GithubStatusBuildConfiguration.getPlanRepositories(chain.getMaster())
                    .stream()
                    .filter(e -> e.getName().equals(repo.getName()) && isTargetGithubRepository(e))
                    .findFirst()
                    .orElse(repo);
        }

        return config.isRepositoryEnabled(repoToCheck);
    }

    private boolean isTargetGithubRepository(PlanRepositoryDefinition repositoryDefinition) {
        Repository repository = repositoryDefinition.asLegacyData().getRepository();
        try {
            GitRepository gitRepository = (repository instanceof GitHubRepository) ? ((GitHubRepository) repository).getGitRepository() : ((GitRepository) repository).getGitRepository();
            URL repositoryUrl = new URL(gitRepository.getAccessData().getRepositoryUrl());
            URL githubUrl = new URL(((GitHubService) gitHubService).gitHubEndpoint);
            return repositoryUrl.getHost().toLowerCase().equals(githubUrl.getHost().toLowerCase());
        } catch (MalformedURLException e) {
            log.warn("Can't update build status as repository has invalid configuration");
        } catch (ClassCastException e) {
            log.warn("Configured repository isn't hosted on GitHub");
        }
        return false;
    }
}
