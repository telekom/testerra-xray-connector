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
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class XrayTestAnnotationConverter implements AnnotationConverter<XrayTest>, Loggable {
    @Override
    public Map<String, Object> toMap(XrayTest annotation) {
        XrayConfig config = XrayConfig.getInstance();
        URI restServiceUri = config.getRestServiceUri();
        URI baseUrl;
        try {
            baseUrl = new URI(restServiceUri.getScheme(), restServiceUri.getUserInfo(), restServiceUri.getHost(), restServiceUri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            baseUrl = URI.create("example.com");
            log().error(e.getMessage());
        }
        final URI finalBaseUrl = baseUrl;
        Map<String, Object> map = new HashMap<>();
        List<String> ticketUrls = Arrays.stream(annotation.key())
                .filter(StringUtils::isNotBlank)
                .map(ticketKey -> {
                    return String.format("%s/browse/%s", finalBaseUrl, ticketKey);
                })
                .collect(Collectors.toList());

        map.put("ticketUrls", ticketUrls);
        return map;
    }
}
