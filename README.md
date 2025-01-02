

### 一个Yarn客户端命令行

> 方便在没有hadoop client环境下调用yarn api

#### 前置条件：
1. 执行环境节点和大数据平台之间网络畅通，执行节点有jdk环境
2. 提前准备好大数据相关配置文件（core-site.xml、hdfs-site.xml、yarn-site.xml），确保这些文件放置在统一目录下。
3. 如果大数据平台开启kerberos，请准备好kerberos相关文件（krb5.conf、keytab文件、principle名称）

#### 执行命令格式如下:

- 启动程序
```shell
# 无Kerberos认证集群
java -Dloader.path=lib -cp hadoop-yarn-tools-*.jar com.example.YARNInteractiveClient <bigdata_config_path> false 
# 示例
java -Dloader.path=lib -cp hadoop-yarn-tools-*.jar com.example.YARNInteractiveClient /home/devops/project/code/bigdata-examples/yarn-example/cdh5_conf false

# 有Kerberos认证集群
java -Dloader.path=lib -cp hadoop-yarn-tools-*.jar com.example.YARNInteractiveClient <bigdata_config_path> true <principle_name> <keytab_path> <krb5_conf_path>
# 示例
java -Dloader.path=lib -cp hadoop-yarn-tools-*.jar com.example.YARNInteractiveClient /home/devops/project/code/bigdata-examples/yarn-example/cdh6_conf true pipeace@PIPEACE.COM /home/devops/kerberos/pipeace/pipeace-init.keytab /etc/krb5.conf
```

- 交互命令
```bash
# 帮助
> help
Available commands:
  yarn application -list [-appStates <states>] - List applications (optionally filter by states)
    <states>: Comma-separated list of application states (e.g., RUNNING, FINISHED, FAILED)
  yarn application -status <application_id> - Get application status
  yarn application -kill <application_id> - Kill an application
  yarn node -list - List all nodes
  yarn queue -status <queue_name> - Get queue status
  yarn logs -applicationId <application_id> [> <file_path>] - Get application logs (optionally save to file)
  exit - Exit the program



# 获取作业列表，可根据状态过滤
> yarn application -list
> yarn application -list -appStates FINISHED
> yarn application -list -appStates RUNNING

# 获取yarn节点列表
> yarn node -list 

# 获取作业日志
> yarn logs -applicationId application_1732873473669_0058 > /home/devops/temp/application_1732873473669_0058

```

#### 常见问题
1. 出现如下空指针异常
   ```shell
   java.io.IOException:
   Exception in thread "main" java.lang.NullPointerException
       at org.apache.hadoop.yarn.logaggregation.LogCLIHelpers.dumpAllContainersLogs(LogCLIHelpers.java:214)
   ```

排查点
 - 确认appId正确、确认相关配置文件正确、路径引用正确
 - 确认该作业当前非running状态，running状态的作业无法获取聚合日志，仅完成或失败的作业才可以获取日志
 - 确认core-site.xml包含必要配置项，详细参考下面，需要在yarn-site.xml加一些配置,如下：
   ```text
   <property>
    <name>yarn.log-aggregation.file-formats</name>
    <value>TFile</value>
   </property>
   
   <property>
    <name>yarn.log-aggregation.file-controller.TFile.class</name>
    <value>org.apache.hadoop.yarn.logaggregation.filecontroller.tfile.LogAggregationTFileController</value>
   </property>
   
   <property>
    <name>yarn.log-aggregation.TFile.remote-app-log-dir-suffix</name>
    <value>logs</value>
   </property>
   ```


