import com.atlassian.bamboo.deployments.configuration.CustomEnvironmentConfigPluginExporter;
import com.atlassian.bamboo.specs.api.builders.deployment.configuration.EnvironmentPluginConfiguration;
import com.atlassian.bamboo.specs.api.exceptions.PropertiesValidationException;
import com.atlassian.bamboo.specs.yaml.MapNode;
import com.atlassian.bamboo.specs.yaml.Node;
import com.atlassian.bamboo.specs.yaml.StringNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration;

import java.util.Optional;

import static tools.fullstackbiz.bamboo.github.status.build.config.GithubStatusBuildConfiguration.REPOSITORIES_KEY;

public class CustomEnvironmentConfigExporterImpl implements CustomEnvironmentConfigPluginExporter {
    private static final Logger log = LoggerFactory.getLogger(GithubStatusBuildConfiguration.class);

    private interface YamlTags {
        String YAML_ROOT = "githubstatus";
        String REPOSITORIES = "repositories";
        String STAGES_EXCLUDED = "stages-excluded";
    }

    @NotNull
    public EnvironmentPluginConfiguration fromYaml(@NotNull Node node) throws PropertiesValidationException {
        log.debug("node:" + node.toString());
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

                return c;
            }
        }
        return null;
    }

    private String parseEnabledRepos(StringNode position) {
        return String.format("%s.id_%s", REPOSITORIES_KEY, position.get());
    }
}