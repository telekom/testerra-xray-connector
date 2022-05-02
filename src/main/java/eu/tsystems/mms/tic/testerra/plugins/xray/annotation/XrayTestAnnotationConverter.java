package eu.tsystems.mms.tic.testerra.plugins.xray.annotation;

import eu.tsystems.mms.tic.testerra.plugins.xray.config.XrayConfig;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.AnnotationConverter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class XrayTestAnnotationConverter implements AnnotationConverter<XrayTest>, Loggable {
    @Override
    public Map<String, Object> toMap(XrayTest annotation) {
        XrayConfig config = XrayConfig.getInstance();
        Map<String, Object> map = new HashMap<>();
        List<String> ticketUrls = Arrays.stream(annotation.key())
                .filter(StringUtils::isNotBlank)
                .map(config::getIssueUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(URI::toString)
                .collect(Collectors.toList());

        map.put("ticketUrls", ticketUrls);
        return map;
    }
}
