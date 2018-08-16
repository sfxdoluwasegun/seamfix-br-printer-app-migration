package com.seamfix.brprinterapp.service;

import com.seamfix.brprinterapp.config.AppConfig;
import com.seamfix.brprinterapp.pojo.enums.ConnectionType;
import com.seamfix.brprinterapp.utils.AuthCrypter;
import com.seamfix.brprinterapp.utils.SessionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashMap;
import java.util.Map;

@Log4j
@Getter
@Setter
@ToString
public class ServiceGenerator {

    private ConnectionType connectionType;
    private String httpIp;
    private int httpPort;
    private Map<String, String> headers;
    private String customUrl;

    private ServiceGenerator(ConnectionType connectionType, String httpIp, int httpPort, Map<String, String> headers) {
        this.connectionType = connectionType;
        this.httpIp = httpIp;
        this.httpPort = httpPort;
        this.headers = headers;
    }

    public static ServiceGenerator getInstance() {
        return new ServiceGenerator(AppConfig.getConnectionType(), AppConfig.getHttpIp(), AppConfig.getHttpPort(), getGeneratedHeaders());
    }

    private static HashMap<String, String> getGeneratedHeaders() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String clientType = "windows";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String tag = SessionUtils.getCurrentTagValue();

        String delimiter = ":";
        String concat = tag + delimiter + timestamp;
        log.debug("Concat headers: " + concat);
        String auth = null;
        try {
            auth = AuthCrypter.encrypt(concat, AppConfig.getAuthSeed());
        } catch (Exception e) {
            log.error("getGeneratedHeaders", e);
        }

        HashMap<String, String> generatedHeaders = new HashMap<>();
        generatedHeaders.put("Content-Type", "application/x-www-form-urlencoded");
//        generatedHeaders.put("sc-auth-key", "57662cef-cd4a-4e5b-87dc-f0ef7481ef84");

        generatedHeaders.put("br-client-type", clientType);
        generatedHeaders.put("br-time", timestamp);
        generatedHeaders.put("br-tag", tag);
        generatedHeaders.put("br-auth-key", auth);

        log.debug("Generating headers took: " + stopWatch.getTime() + "ms!");
        log.debug("Auth: " + auth);
        log.debug("time: " + timestamp);



        return generatedHeaders;
    }

    public String constructUrl() {
        return StringUtils.isBlank(customUrl) ? (connectionType + "://" + httpIp + ":" + httpPort) : customUrl;
    }
}
