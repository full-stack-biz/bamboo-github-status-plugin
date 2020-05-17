package tools.fullstackbiz.bamboo.github.status.build.config;

import com.atlassian.extras.common.org.springframework.util.StringUtils;

import java.util.HashSet;

public class ExcludedStages extends HashSet<String> {
    public ExcludedStages(String excludedStages) {
        super();
        StringUtils.commaDelimitedListToSet(excludedStages).forEach(e -> add((String)e));
    }

    @Override
    public String toString() {
        return StringUtils.arrayToCommaDelimitedString(this.toArray());
    }
}
