package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.action;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreStageAction;
import com.atlassian.bamboo.plan.PlanManager;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHCommitState;
import com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service.GithubServiceInterface;

public class GitHubStatusPreStage extends AbstractGitHubStatusAction implements PreStageAction {
    GitHubStatusPreStage(PlanManager planManager, GithubServiceInterface gitHubService) {
        super(planManager, gitHubService);
    }

    @Override
    public void execute(@NotNull StageExecution stageExecution) {
        pushUpdate(stageExecution, GHCommitState.PENDING);
    }
}
