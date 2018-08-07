package com.seamfix.brprinterapp.config;

import com.seamfix.brprinterapp.utils.ImageHelper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.seamfix.brprinterapp.utils.StegUtil;

/**
 * Created by rukevwe on 8/2/2018.
 */

@Log4j
public class AppConfig {

    private static final Properties versionProps = getVersionProps();

    private static final Properties coreProps = getCoreProps();

    private static final Properties stegProps = getStegProps();

    private static Properties getVersionProps() {
        Properties prop = new Properties();
        try {
            prop.load(AppConfig.class.getResourceAsStream("/com/seamfix/brprinterapp/config/version.properties"));
        } catch (IOException e) {
            log.error("Error while loading version props", e);
        }

        return prop;
    }

    public static String getAppVersion() {
        return (String) versionProps.get("app.version");
    }

    private static Properties getCoreProps() {
        Properties prop = new Properties();
        try {
            prop.load(AppConfig.class.getResourceAsStream("/com/seamfix/bioregistra/config/config-core.properties"));
        } catch (IOException e) {
            log.error("Error while loading core props", e);
        }

        return prop;
    }

    private static Properties getStegProps() {
        Properties prop = new Properties();
        try {
            File stegStage = new File(FileUtils.getTempDirectory(), "8e6474be-339a-steg-449f-stage-aca065595806");
            if (stegStage.exists()) {
                FileUtils.deleteDirectory(stegStage);
            }
            stegStage.mkdirs();

            File stegImageDir = new File(stegStage, "steg-image");
            File stegImage = new File(stegImageDir, "image.png");
            FileUtils.copyInputStreamToFile(AppConfig.class.getResourceAsStream(ImageHelper.SEAMFIX_LOGO_SEC), stegImage);

            StegUtil stegUtil = new StegUtil();
            stegUtil.reveal(stegImageDir, stegStage, null);

            File output = new File(stegStage, "steg-prep.properties");


            try (InputStream is = FileUtils.openInputStream(output)) {
                prop.load(is);
            } catch (IOException e) {
                log.error("Error while loading core props", e);
            }

            FileUtils.deleteDirectory(stegStage);
        } catch (Exception e) {
            log.error("getStegProps Error ", e);
        }

        return prop;
    }

    public static String getDbUser() {
        return getStegProp("db.user");
    }

    public static String getDbPassword() {
        return getStegProp("db.password");
    }
    public static String getStegProp(String key) {
        return (String) stegProps.get(key);
    }
}
