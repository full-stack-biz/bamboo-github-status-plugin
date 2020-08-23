package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.action;

import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreChainAction;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableChain;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.build.config.GithubStatusBuildConfiguration;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service.GithubServiceInterface;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHCommitState;

public class PreBuildAction extends AbstractGitHubStatusAction implements PreChainAction {
    PreBuildAction(PlanManager planManager, GithubServiceInterface gitHubService) {
        super(planManager, gitHubService);
    }

    @Override
    public void execute(@NotNull ImmutableChain chain, @NotNull ChainExecution chainExecution) throws InterruptedException, Exception {
        GithubStatusBuildConfiguration config = GithubStatusBuildConfiguration.from(chain.getBuildDefinition().getCustomConfiguration());

        chainExecution.getStages()
                .stream()
                .filter((StageExecution stageExecution) -> !config.getExcludedStages().contains(stageExecution.getName()))
                .forEach((StageExecution stageExecution) -> {
                    pushUpdate(stageExecution, GHCommitState.PENDING);
                });
    }
}
