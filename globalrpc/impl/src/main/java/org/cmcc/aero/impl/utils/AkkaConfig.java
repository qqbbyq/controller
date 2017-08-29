package org.cmcc.aero.impl.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by zhuyuqing on 2017/8/28.
 */

public class AkkaConfig {

  //place your config under the projectxxxx/configuration/factory/
  private static final String configDir = "./configuration/factory/";

  static Logger LOG = LoggerFactory.getLogger("org.cmcc.aero.impl.utils");

  //use name to get the config object
  public static Config get(String configName) {
    File file = new File(configDir + configName);
    LOG.info("Akka Config in: {}", file.getAbsolutePath());
    Config config = ConfigFactory.parseFile(file);
    LOG.info("Akka Config has:{}", config);
    return config;
  }

  //use new config to override the old config
  public static Config merge(Config oldConfig, Config newConfig) {
    Config config = oldConfig.withFallback(newConfig);
    LOG.info("Akka Config after merge: {}", config);
    return oldConfig.withFallback(newConfig);
  }


}
