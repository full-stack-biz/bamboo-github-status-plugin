package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service;

import com.atlassian.bamboo.v2.build.repository.RepositoryV2;
import org.kohsuke.github.GHCommitState;

public interface GithubServiceInterface {
    void setStatus(RepositoryV2 repo, GHCommitState status, String sha, String planResultKey, String context);

    void setStatus(RepositoryV2 repo, GHCommitState status, String sha, String planResultKey, String context, String description);
}
