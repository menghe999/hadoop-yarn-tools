package com.example;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.logaggregation.ContainerLogsRequest;
import org.apache.hadoop.yarn.logaggregation.LogCLIHelpers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;

public class YARNInteractiveClient {

    private static YarnClient yarnClient;

    public static void main(String[] args) {
        // 初始化YarnClient
        // 创建Hadoop配置对象并加载配置文件
        Configuration conf = loadHadoopConfig(args[0]);

        boolean kerberosEnabled = Boolean.parseBoolean(args[1]); // 是否启用Kerberos认证
        String principal = null;
        String keytab = null;
        String krb5Conf = "/etc/krb5.conf"; // 默认krb5.conf路径

        if (kerberosEnabled) {
            if (args.length < 4) {
                System.err.println("Kerberos enabled requires <principal> and <keytab> arguments.");
                System.exit(1);
            }
            principal = args[2];
            keytab = args[3];
            if (args.length >= 5) {
                krb5Conf = args[4]; // 自定义krb5.conf路径
            }
        }


        // 启用Kerberos认证
        if (kerberosEnabled) {
            // 设置krb5.conf路径
            System.setProperty("java.security.krb5.conf", krb5Conf);
            System.out.println("Using krb5.conf: " + krb5Conf);
            enableKerberos(conf, principal, keytab);
        }

        yarnClient = YarnClient.createYarnClient();
        yarnClient.init(conf);
        yarnClient.start();

        // 启动交互式命令行
        startInteractiveShell(conf);

        // 关闭YarnClient
        yarnClient.stop();
    }


    private static Configuration loadHadoopConfig(String configPath) {
        Configuration conf = new Configuration();
        // 加载Hadoop配置文件
        conf.addResource(new Path(configPath + "/core-site.xml"));
        conf.addResource(new Path(configPath + "/hdfs-site.xml"));
        conf.addResource(new Path(configPath + "/yarn-site.xml"));
        System.out.println("Loaded Hadoop configuration from: " + configPath);
        return conf;
    }
    private static void enableKerberos(Configuration conf, String principal, String keytab) {
        try {

            // 设置Kerberos配置
            conf.set("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(conf);

            // 使用Keytab文件登录
            UserGroupInformation.loginUserFromKeytab(principal, keytab);
            System.out.println("Kerberos认证成功，用户: " + UserGroupInformation.getLoginUser());
        } catch (IOException e) {
            System.err.println("Kerberos认证失败: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void startInteractiveShell(Configuration conf) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                System.out.flush(); // 刷新输出缓冲区
                String input = reader.readLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    break;
                }

                // 解析并执行命令
                executeYarnCommand(conf, input);
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    private static void executeYarnCommand(Configuration conf, String command) {
        String[] tokens = command.split(" ");
        if (tokens.length < 2 || !tokens[0].equals("yarn")) {
            System.out.println("Invalid command. Commands must start with 'yarn'.");
            return;
        }

        String mainCommand = tokens[1];
        switch (mainCommand) {
            case "application":
                handleApplicationCommand(tokens);
                break;
            case "node":
                handleNodeCommand(tokens);
                break;
            case "queue":
                handleQueueCommand(tokens);
                break;
            case "logs":
                handleLogsCommand(conf,tokens);
                break;
            default:
                System.out.println("Unknown command: " + mainCommand);
                printHelp();
        }
    }

    private static void handleApplicationCommand(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Usage: yarn application -list | -status <application_id> | -kill <application_id>");
            return;
        }

        String subCommand = tokens[2];
        switch (subCommand) {
            case "-list":
                listApplications();
                break;
            case "-status":
                if (tokens.length < 4) {
                    System.out.println("Usage: yarn application -status <application_id>");
                } else {
                    getApplicationStatus(tokens[3]);
                }
                break;
            case "-kill":
                if (tokens.length < 4) {
                    System.out.println("Usage: yarn application -kill <application_id>");
                } else {
                    killApplication(tokens[3]);
                }
                break;
            default:
                System.out.println("Unknown application command: " + subCommand);
        }
    }

    private static void handleNodeCommand(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Usage: yarn node -list");
            return;
        }

        String subCommand = tokens[2];
        if (subCommand.equals("-list")) {
            listNodes();
        } else {
            System.out.println("Unknown node command: " + subCommand);
        }
    }

    private static void handleQueueCommand(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println("Usage: yarn queue -status <queue_name>");
            return;
        }

        String subCommand = tokens[2];
        if (subCommand.equals("-status") && tokens.length >= 4) {
            getQueueStatus(tokens[3]);
        } else {
            System.out.println("Unknown queue command: " + subCommand);
        }
    }

    private static void handleLogsCommand(Configuration conf,String[] tokens) {
        if (tokens.length < 4) {
            System.out.println("Usage: yarn logs -applicationId <application_id> [> <file_path>]");
            return;
        }

        String subCommand = tokens[2];
        if (subCommand.equals("-applicationId")) {
            String appId = tokens[3];
            String outputFile = null;

            // 检查是否有重定向符号 ">"
            if (tokens.length >= 6 && tokens[4].equals(">")) {
                outputFile = tokens[5];
            }
            System.out.println(String.format(">>>>>>> appId: %s, outputFile: %s", appId, outputFile));
            getApplicationLogs(conf, appId, outputFile);
        } else {
            System.out.println("Unknown logs command: " + subCommand);
        }
    }

    private static void listApplications() {
        try {
            List<ApplicationReport> apps = yarnClient.getApplications();
            System.out.println("Applications:");
            for (ApplicationReport app : apps) {
                System.out.println("ID: " + app.getApplicationId() +
                        ", Name: " + app.getName() +
                        ", State: " + app.getYarnApplicationState());
            }
        } catch (YarnException | IOException e) {
            System.err.println("Failed to list applications: " + e.getMessage());
        }
    }

    private static void getApplicationStatus(String appIdStr) {
        try {
            ApplicationId appId = ApplicationId.fromString(appIdStr);
            ApplicationReport report = yarnClient.getApplicationReport(appId);
            System.out.println("Application ID: " + report.getApplicationId());
            System.out.println("Name: " + report.getName());
            System.out.println("State: " + report.getYarnApplicationState());
            System.out.println("Final Status: " + report.getFinalApplicationStatus());
        } catch (YarnException | IOException e) {
            System.err.println("Failed to get application status: " + e.getMessage());
        }
    }

    private static void killApplication(String appIdStr) {
        try {
            ApplicationId appId = ApplicationId.fromString(appIdStr);
            yarnClient.killApplication(appId);
            System.out.println("Application " + appId + " killed successfully.");
        } catch (YarnException | IOException e) {
            System.err.println("Failed to kill application: " + e.getMessage());
        }
    }

    private static void listNodes() {
        try {
            List<NodeReport> nodes = yarnClient.getNodeReports();
            System.out.println("Nodes:");
            for (NodeReport node : nodes) {
                System.out.println("ID: " + node.getNodeId() +
                        ", State: " + node.getNodeState() +
                        ", Address: " + node.getHttpAddress());
            }
        } catch (YarnException | IOException e) {
            System.err.println("Failed to list nodes: " + e.getMessage());
        }
    }

    private static void getQueueStatus(String queueName) {
        try {
            QueueInfo queueInfo = yarnClient.getQueueInfo(queueName);
            System.out.println("Queue: " + queueInfo.getQueueName());
            System.out.println("State: " + queueInfo.getQueueState());
            System.out.println("Capacity: " + queueInfo.getCapacity());
            System.out.println("Current Capacity: " + queueInfo.getCurrentCapacity());
        } catch (YarnException | IOException e) {
            System.err.println("Failed to get queue status: " + e.getMessage());
        }
    }

    private static void getApplicationLogs(Configuration conf, String appIdStr, String outputFile) {
        try {
            ApplicationId appId = ApplicationId.fromString(appIdStr);
            LogCLIHelpers logCliHelper = new LogCLIHelpers();
            logCliHelper.setConf(conf);
            ContainerLogsRequest containerLogsRequest = new ContainerLogsRequest();
            containerLogsRequest.setAppId(appId);
            containerLogsRequest.setBytes(Long.MAX_VALUE);
            containerLogsRequest.setOutputLocalDir(outputFile);

            int result = logCliHelper.dumpAllContainersLogs(containerLogsRequest);
            System.out.println(String.format(">>>>>>> result: %d", result));
            if (result == 0) {
                System.out.println(String.format(">>>>>>> application logs agg successful, please see dir [%s]", outputFile));
            } else {
                System.out.println(">>>>>>> application logs agg failed");
            }
        } catch (IOException e) {
            System.err.println("Failed to get application logs: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  yarn application -list - List all applications");
        System.out.println("  yarn application -status <application_id> - Get application status");
        System.out.println("  yarn application -kill <application_id> - Kill an application");
        System.out.println("  yarn node -list - List all nodes");
        System.out.println("  yarn queue -status <queue_name> - Get queue status");
        System.out.println("  yarn logs -applicationId <application_id> [> <file_path>] - Get application logs (optionally save to file)");
        System.out.println("  exit - Exit the program");
    }
}