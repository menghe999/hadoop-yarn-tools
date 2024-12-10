

### 一个可以替代 `yarn logs -applicationId application_xxx` 的工具

#### 前置条件：
1. 执行环境节点和大数据平台之间网络畅通，执行节点有jdk环境
2. 提前准备好大数据相关配置文件（core-site.xml、hdfs-site.xml、yarn-site.xml）
3. 如果大数据平台开启kerberos，请准备好kerberos相关文件（krb5.conf、keytab文件、principle名称）

#### 执行命令格式如下:

格式1
```shell
java -jar -Dloader.path=lib hadoop-yarn-tools--*.jar \
--appid <appid> \
--user <yarn_application_user> \
--yarn_config_dir <yarn_config_dir> \
--log_out_dir <log_out_dir>
```

格式2（kerberos）
```shell
java -jar -Dloader.path=lib hadoop-yarn-tools--*.jar \
--appid <appid> \
--user <yarn_application_user> \
--yarn_config_dir <yarn_config_dir> \
--log_out_dir <log_out_dir> \
--kerberos_enable true \
--k_principle <principle> \
--k_keytab_path <keytab_path> \
--k_krb5_path <krb5_path>
```

说明:
- `<appid>` 填写失败或已经停止的yarn作业的application_ID
- `<yarn_application_user>` 填写启动yarn作业的用户
- `<yarn_config_dir>` 填写一个本地目录，确保目录内包含core-site.xml、hdfs-site.xml、yarn-site.xml
- `<log_out_dir>` 填写一个本地目录(确保目录存在)，作业日志将会输出到该目录内
- `<principle>` kerberos的principle名称
- `<keytab_path>` 本地文件，keytab秘钥文件绝对路径
- `<krb5_path>` 本地文件，krb5.conf秘钥文件绝对路径

样例如下：

参考1
```shell
/usr/java/jdk1.8.0_231-amd64/bin/java -jar -Dloader.path=lib hadoop-yarn-tools--*.jar \
--appid application_1725270875614_0197 \
--user hdfs \
--yarn_config_dir /home/dev/hadoop-yarn-tools/yarn_conf \
--log_out_dir /home/dev/hadoop-yarn-tools/out
```

参考2
```shell
/usr/java/jdk1.8.0_231-amd64/bin/java -jar -Dloader.path=lib hadoop-yarn-tools--*.jar \
--appid application_1722940168678_16065 \
--user cc_test \
--yarn_config_dir /home/dev/hadoop-yarn-tools/cdh5_conf \
--log_out_dir /home/dev/hadoop-yarn-tools/cdh5_out
```

参考3 kerberos安全
```shell
/usr/java/jdk1.8.0_231-amd64/bin/java -jar -Dloader.path=lib hadoop-yarn-tools--*.jar \
--appid application_1724810243358_0011 \
--user dev \
--yarn_config_dir /home/dev/hadoop-yarn-tools/cdh6_conf \
--log_out_dir /home/dev/hadoop-yarn-tools/cdh6_out \
--kerberos_enable true \
--k_principle dev@HADOOP.COM \
--k_keytab_path /home/dev/kerberos/dev.keytab \
--k_krb5_path /home/dev/kerberos/krb5.conf
```

正常运行结果
```
result: 0
application logs agg successful, please see dir <log_out_dir>
```

#### 常见问题
1. 出现如下空指针异常
   ```shell
   java.io.IOException:
   Exception in thread "main" java.lang.NullPointerException
       at org.apache.hadoop.yarn.logaggregation.LogCLIHelpers.dumpAllContainersLogs(LogCLIHelpers.java:214)
       at cn.com.bsfit.YarnLogs.main(YarnLogs.java:65)
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

