package com.example;

import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.apache.hadoop.yarn.logaggregation.LogCLIHelpers;
import org.apache.hadoop.yarn.util.ConverterUtils;

import java.io.File;
import java.io.IOException;

/**
 * get the yarn application agg logs by java api, it's more useful when yarn application failed and user no hadoop node permission to execution `yarn logs -applicationId xx `
 * <p> create time: 9/19/24</p>
 * @author mengh mailTo: mengh@bsfit.com.cn
 * @since v1.0
 */
public class YarnLogs {
    private static final String APP_ID = "appid";
    private static final String USER = "user";
    private static final String YARN_CONFIG_DIR = "yarn_config_dir";
    private static final String LOG_OUT_DIR = "log_out_dir";
    private static final String KERBEROS_ENABLE = "kerberos_enable";
    private static final String K_PRINCIPLE = "k_principle";
    private static final String K_KEYTAB_PATH = "k_keytab_path";
    private static final String K_KRB5_PATH = "k_krb5_path";

    private static Options getOptions() {
        Options options = new Options();
        Option appid = new Option(APP_ID, APP_ID, true, "application id");
        Option user = new Option(USER, USER, true, "application submit user");
        Option yarnConfigDir = new Option(YARN_CONFIG_DIR, YARN_CONFIG_DIR, true, "yarn config dir");
        Option logOutDir = new Option(LOG_OUT_DIR, LOG_OUT_DIR, true, "log out dir");
        Option kerberosEnable = new Option(KERBEROS_ENABLE, KERBEROS_ENABLE, true, "enable or disable kerberos");
        Option kerberosPrinciple = new Option(K_PRINCIPLE, K_PRINCIPLE, true, "kerberosPrinciple");
        Option kerberosKeytabPath = new Option(K_KEYTAB_PATH, K_KEYTAB_PATH, true, "kerberosKeytabPath");
        Option kerberosKrb5Path = new Option(K_KRB5_PATH, K_KRB5_PATH, true, "kerberosKrb5Path");


        options.addOption(appid);
        options.addOption(user);
        options.addOption(yarnConfigDir);
        options.addOption(logOutDir);
        options.addOption(kerberosEnable);
        options.addOption(kerberosPrinciple);
        options.addOption(kerberosKeytabPath);
        options.addOption(kerberosKrb5Path);
        return options;
    }

    public static void main(String[] args) throws IOException, ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();
        CommandLine cmd = parser.parse(options, args);


        String applicationId = cmd.getOptionValue(APP_ID);
        String user = cmd.getOptionValue(USER);
        String yarnConfigPath = cmd.getOptionValue(YARN_CONFIG_DIR);
        String logOutPath = cmd.getOptionValue(LOG_OUT_DIR);

        String inputParams = String.format(">>>>>>> APP_ID:%s,USER:%s,YARN_CONFIG_DIR:%s,LOG_OUT_DIR:%s", applicationId, user, yarnConfigPath, logOutPath);
        System.out.println(inputParams);


        boolean enableKerberos = Boolean.valueOf(cmd.getOptionValue(KERBEROS_ENABLE, "false"));
        if (enableKerberos) {
            String principle = cmd.getOptionValue(K_PRINCIPLE);
            String keytabPath = cmd.getOptionValue(K_KEYTAB_PATH);
            String krb5Path = cmd.getOptionValue(K_KRB5_PATH);
            System.out.println(String.format(">>>>>>> K_PRINCIPLE:%s,K_KEYTAB_PATH:%s,K_KRB5_PATH:%s", principle, keytabPath, krb5Path));
            loginKerberos(principle, keytabPath, krb5Path);
        }

        ApplicationId appId = ConverterUtils.toApplicationId(applicationId);
        LogCLIHelpers logCliHelper = new LogCLIHelpers();
        Configuration config = new Configuration();
        config.addResource(new Path(yarnConfigPath + File.separator + "core-site.xml"));
        config.addResource(new Path(yarnConfigPath + File.separator + "hdfs-site.xml"));
        config.addResource(new Path(yarnConfigPath + File.separator + "yarn-site.xml"));

        logCliHelper.setConf(config);
        ContainerLogsRequest containerLogsRequest = new ContainerLogsRequest();
        containerLogsRequest.setAppId(appId);
        containerLogsRequest.setAppOwner(user);
        containerLogsRequest.setBytes(Long.MAX_VALUE);
        containerLogsRequest.setOutputLocalDir(logOutPath);

        int result = logCliHelper.dumpAllContainersLogs(containerLogsRequest);
        System.out.println(String.format(">>>>>>> result: %d", result));
        if (result == 0) {
            System.out.println(String.format(">>>>>>> application logs agg successful, please see dir [%s]", logOutPath));
        } else {
            System.out.println(">>>>>>> application logs agg failed");
        }
    }


    /**
     * login kerberos
     * @param principle user principle
     * @param keytabPath absolute keytab path
     * @param krb5Path absolute krb5.conf path
     */
    private static void loginKerberos(String principle, String keytabPath, String krb5Path) {
        System.setProperty("java.security.krb5.conf", krb5Path);
        try {
            Configuration entries = new Configuration();
            entries.set("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(entries);
            UserGroupInformation.loginUserFromKeytab(principle, keytabPath);
            System.out.println(UserGroupInformation.getCurrentUser());
            System.out.println(">>>>>>> login kerberos successful");
        } catch (IOException e) {
            System.out.println(">>>>>>> login kerberos failed, IOException");
            System.out.println(">>>>>>> " + e.getCause().getMessage());
            System.exit(0);
        }

    }

}
