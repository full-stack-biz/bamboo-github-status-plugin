package tools.fullstackbiz.bamboo.github.status.action;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.chains.BuildExecution;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.ChainStageResult;
import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PostStageAction;
import com.atlassian.bamboo.plan.PlanManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.commons.lang.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHCommitState;
import tools.fullstackbiz.bamboo.github.status.service.GithubServiceInterface;

import java.util.Collection;
import java.util.Collections;

public class GitHubStatusPostStage extends AbstractGitHubStatusAction implements PostStageAction {
    GitHubStatusPostStage(@ComponentImport PlanManager planManager, GithubServiceInterface gitHubService) {
        super(planManager, gitHubService);
    }

    @Override
    public void execute(@NotNull ChainResultsSummary chainResultsSummary,
                        @NotNull ChainStageResult chainStageResult,
                        @NotNull StageExecution stageExecution) {

        String description = "";
        int failed = 0;
        int success = 0;

        for (BuildExecution s : stageExecution.getBuilds()) {
            failed += ((Collection<?>) ObjectUtils.defaultIfNull(s.getBuildContext().getBuildResult().getFailedTestResults(), Collections.emptySet())).size();
            success += ((Collection<?>) ObjectUtils.defaultIfNull(s.getBuildContext().getBuildResult().getSuccessfulTestResults(), Collections.emptySet())).size();
        }

        if (failed > 0) {
            description = String.format("%d of %d tests failed. ", failed, failed + success);
        } else if (success > 0) {
            description = "All tests are passing. ";
        }

        int time = (int) stageExecution.getChainExecution().getElapsedTime();
        int minutes = time / (60 * 1000);
        int seconds = (time / 1000) % 60;
        description = description.concat(String.format("Execution time: %d:%02d", minutes, seconds));

        pushUpdate(stageExecution, statusOf(stageExecution), description);
    }

    private static GHCommitState statusOf(StageExecution stageExecution) {
        if (stageExecution.isSuccessful()) {
            return GHCommitState.SUCCESS;
        } else if (stageExecution.getBuilds().stream().anyMatch(e -> e.getBuildState() == BuildState.UNKNOWN)) {
            return GHCommitState.ERROR;
        } else {
            return GHCommitState.FAILURE;
        }
    }
}
