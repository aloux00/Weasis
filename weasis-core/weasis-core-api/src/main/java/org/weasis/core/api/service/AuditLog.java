package org.weasis.core.api.service;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.internal.Activator;

public class AuditLog {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuditLog.class);

    public static final String LOG_LEVEL = "org.apache.sling.commons.log.level";
    public static final String LOG_FILE = "org.apache.sling.commons.log.file";
    public static final String LOG_FILE_NUMBER = "org.apache.sling.commons.log.file.number";
    public static final String LOG_FILE_SIZE = "org.apache.sling.commons.log.file.size";
    public static final String LOG_PATTERN = "org.apache.sling.commons.log.pattern";
    public static final String LOG_LOGGERS = "org.apache.sling.commons.log.names";

    public enum LEVEL {
        DEBUG, INFO, WARN, ERROR, FATAL;

        public static LEVEL getLevel(String level) {
            try {
                return LEVEL.valueOf(level);
            } catch (Exception e) {
            }
            return INFO;
        }
    };

    // TODO activate audit log for the functionalities usage of Weasis
    // static {
    // AuditLog.createOrUpdateLogger("audit.log", new String[] { "org.weasis.core.api.service.AuditLog" }, "DEBUG",
    // AbstractProperties.WEASIS_PATH + File.separator + "log" + File.separator + "audit.log",
    // "{0,date,dd.MM.yyyy HH:mm:ss.SSS} *{4}* {5}", null, null);
    // }

    public static void createOrUpdateLogger(String loggerKey, String[] loggerVal, String level, String logFile,
        String pattern, String nbFiles, String logSize) {
        if (loggerKey != null && loggerVal != null && loggerVal.length > 0) {
            BundleContext bundleContext = Activator.getBundleContext();
            if (bundleContext != null) {
                ServiceReference configurationAdminReference =
                    bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
                if (configurationAdminReference != null) {
                    ConfigurationAdmin confAdmin =
                        (ConfigurationAdmin) bundleContext.getService(configurationAdminReference);
                    if (confAdmin != null) {
                        try {
                            Dictionary<String, Object> loggingProperties;
                            Configuration logConfiguration =
                                getLogConfiguration(confAdmin, "(" + loggerKey + "=" + loggerVal[0] + ")");
                            if (logConfiguration == null) {
                                logConfiguration =
                                    confAdmin.createFactoryConfiguration(
                                        "org.apache.sling.commons.log.LogManager.factory.config", null);
                                loggingProperties = new Hashtable<String, Object>();
                                loggingProperties.put(LOG_LOGGERS, loggerVal);
                                // add this property to give us something unique to re-find this configuration
                                loggingProperties.put(loggerKey, loggerVal[0]);
                            } else {
                                loggingProperties = logConfiguration.getProperties();
                            }
                            loggingProperties.put(LOG_LEVEL, level == null ? "INFO" : level);
                            if (logFile != null) {
                                loggingProperties.put(LOG_FILE, logFile);
                            }
                            if (nbFiles != null) {
                                loggingProperties.put(LOG_FILE_NUMBER, nbFiles);
                            }
                            if (logSize != null) {
                                loggingProperties.put(LOG_FILE_SIZE, logSize);
                            }
                            if (pattern != null) {
                                loggingProperties.put(LOG_PATTERN, pattern);
                            }
                            // org.apache.sling.commons.log.pattern={0,date,dd.MM.yyyy HH:mm:ss.SSS} *{4} {1}* [{2}] {3}
                            // {5}
                            logConfiguration.update(loggingProperties);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static Configuration getLogConfiguration(ConfigurationAdmin confAdmin, String filter) throws IOException {
        Configuration logConfiguration = null;
        try {
            Configuration[] configs = confAdmin.listConfigurations(filter);
            if (configs != null && configs.length > 0) {
                logConfiguration = configs[0];
            }
        } catch (InvalidSyntaxException e) {
            // ignore this as we'll create what we need
        }
        return logConfiguration;
    }
}
