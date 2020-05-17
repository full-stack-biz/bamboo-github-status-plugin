package tools.fullstackbiz.bamboo.github.status.service;

import com.atlassian.bamboo.repository.Repository;
import org.kohsuke.github.GHCommitState;

public interface GithubServiceInterface {
    void setStatus(Repository repo, GHCommitState status, String sha, String planResultKey, String context);
    void setStatus(Repository repo, GHCommitState status, String sha, String planResultKey, String context, String description);
}
