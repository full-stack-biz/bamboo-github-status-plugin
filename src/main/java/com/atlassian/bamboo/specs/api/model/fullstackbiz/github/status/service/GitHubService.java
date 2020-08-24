package com.atlassian.bamboo.specs.api.model.fullstackbiz.github.status.service;

import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.crypto.instance.SecretEncryptionService;
import com.atlassian.bamboo.plugins.git.GitHubRepository;
import com.atlassian.bamboo.plugins.git.GitRepository;
import com.atlassian.bamboo.utils.BambooUrl;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.repository.RepositoryV2;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

@ExportAsService({GitHubService.class})
@Named("gitHubService")
public class GitHubService implements GithubServiceInterface {
    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    public final String gitHubEndpoint =
            new SystemProperty(
                    false,
                    "atlassian.bamboo.github.api.base.url",
                    "ATLASSIAN_BAMBOO_GITHUB_API_BASE_URL"
            ).getValue("https://api.github.com");
    private final SecretEncryptionService encryptionService;
    private final BambooUrl bambooUrl;

    @Inject
    @Autowired
    public GitHubService(@ComponentImport AdministrationConfigurationAccessor adminConfigAccessor, @ComponentImport SecretEncryptionService encryptionService) {
        bambooUrl = new BambooUrl(adminConfigAccessor);
        this.encryptionService = encryptionService;
    }

    @Override
    public void setStatus(RepositoryV2 repo, GHCommitState status, String sha, String planResultKey, String context) {
        setStatus(repo, status, sha, planResultKey, context, null);
    }

    public void setStatus(RepositoryV2 repo, GHCommitState status, String sha, String planResultKey, String context, String description) {
        String url = bambooUrl.withBaseUrlFromConfiguration("/browse/" + planResultKey);
        try {
            GHRepository repository = getGHRepository(repo);
            sha = repository.getCommit(sha).getSHA1();
            repository.createCommitStatus(sha, status, url, description, context);
            log.info("GitHub status for commit {} ({}) set to {}.", sha, context, status);
        } catch (Exception ex) {
            log.error("Failed to update GitHub status", ex);
        }
    }

    private GHRepository getGHRepository(RepositoryV2 repo) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        String oauthAccessToken;
        String username;
        String repositoryUrl;
        if (repo instanceof GitHubRepository) {
            GitHubRepository gitHubRepository = (GitHubRepository) repo;
            try {
                oauthAccessToken = gitHubRepository.getClass().getDeclaredMethod("getPassword").invoke(gitHubRepository).toString();
            } catch (NoSuchMethodException ex) {
                oauthAccessToken = encryptionService.decrypt(
                        gitHubRepository.getClass().getDeclaredMethod("getEncryptedPassword").invoke(gitHubRepository).toString());
            }
            username = gitHubRepository.getUsername();
            repositoryUrl = gitHubRepository.getRepository();
        } else {
            GitRepository gitRepository = (GitRepository) repo;
            oauthAccessToken = gitRepository.getAccessData().getPassword();
            username = gitRepository.getAccessData().getUsername();
            repositoryUrl = gitRepository.getAccessData().getRepositoryUrl();
            repositoryUrl = getRelativePath(repositoryUrl);
        }

        log.info(String.format("Connecting to github ... username = %s, repositoryUrl = %s", username, repositoryUrl));

        try {
            GitHub gitHub = GitHub.connectToEnterpriseWithOAuth(gitHubEndpoint, username, oauthAccessToken);
            return gitHub.getRepository(repositoryUrl);
        } catch (HttpException ex) {
            GitHub gitHub = GitHub.connectToEnterprise(gitHubEndpoint, username, oauthAccessToken);
            return gitHub.getRepository(repositoryUrl);
        }

    }

    private String getRelativePath(String url) throws MalformedURLException {
        return new URL(url)
                .getPath()
                .replaceFirst("^/", "")
                .replace(".git", "");
    }
}
