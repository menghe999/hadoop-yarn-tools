

### 一个Yarn客户端命令行

> 方便在没有hadoop client环境下调用yarn api

### Spring Boot 日志下载接口

> `feature-springboot-deploy` 分支新增 HTTP 下载方式，服务端预先配置 YARN 集群和 Kerberos 凭据，前端只传集群标识、应用 ID 和提交用户。

#### 服务端配置示例

```yaml
yarn:
  logs:
    clusters:
      default:
        yarn-config-dir: /opt/hadoop/conf
        kerberos-enabled: false
      secure:
        yarn-config-dir: /opt/secure-hadoop/conf
        kerberos-enabled: true
        principal: yarn-client@EXAMPLE.COM
        keytab-path: /opt/security/yarn-client.keytab
        krb5-path: /etc/krb5.conf
```

不要让前端请求直接传入 `yarn-config-dir`、`keytab-path` 或 `krb5-path`，真实配置文件和凭据只允许保存在服务端受控环境中。

#### 下载请求

```bash
curl -X POST 'http://localhost:8080/api/yarn/logs/download' \
  -H 'Content-Type: application/json' \
  -o application_1732873473669_0058.zip \
  -d '{
    "clusterId": "default",
    "applicationId": "application_1732873473669_0058",
    "appOwner": "devops"
  }'
```

接口会在服务端临时目录中拉取聚合日志；如果结果是单文件则直接下载，如果结果包含多个文件或目录则以 ZIP 返回。YARN 聚合日志仍依赖集群侧配置和应用状态，running 状态应用通常不能获取完整聚合日志。

#### 队列应用查询请求

```bash
# 查询 root.default 队列下所有状态的应用
curl 'http://localhost:8080/api/yarn/applications?clusterId=default&queue=root.default'

# 查询 root.default 队列下 RUNNING 状态的应用
curl 'http://localhost:8080/api/yarn/applications?clusterId=default&queue=root.default&state=RUNNING'
```

`state` 为可选参数，支持 Hadoop YARN 状态：`NEW`、`NEW_SAVING`、`SUBMITTED`、`ACCEPTED`、`RUNNING`、`FINISHED`、`FAILED`、`KILLED`。响应会返回本次查询条件和应用摘要列表，摘要字段包含 `applicationId`、`name`、`user`、`queue`、`state`、`finalStatus`、`applicationType`、`progress`、`trackingUrl`、`startedTime`、`finishedTime`。

应用列表接口会按服务端配置的 `yarn.logs.max-applications` 限制单次返回数量，默认 `1000`，部署时可通过环境变量 `YARN_MAX_APPLICATIONS` 覆盖，避免一次性查询和返回过大的 ResourceManager 数据集。

#### Swagger 调试入口

服务启动后可通过 Swagger UI 调用接口：

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs/yarn`

#### Spring Boot 启动方式

```shell
mvn spring-boot:run

# 或打包后解压分发包，以常驻进程方式启动
mvn clean package
unzip target/hadoop-yarn-tools-1.1.0.zip
cd hadoop-yarn-tools-1.1.0
bin/run.sh

# 停止服务
bin/stop.sh
```

分发包内的 `hadoop-yarn-tools-1.1.0.jar` 只包含本项目核心代码和资源，运行时依赖位于同级 `libs/` 目录，外置配置位于 `config/application.yml`。`run.sh` 会切换到分发包根目录，并使用 `jar + libs/*` 作为 classpath 启动 `com.example.YarnToolsApplication`，日志默认写入 `logs/hadoop-yarn-tools.log`，进程号默认写入 `run/hadoop-yarn-tools.pid`。

默认启动配置会随分发包打包到 `config/application.yml`，部署时可直接修改该文件，也可通过环境变量覆盖 `YARN_CONFIG_DIR`、`YARN_SECURE_CONFIG_DIR`、`YARN_KERBEROS_PRINCIPAL`、`YARN_KERBEROS_KEYTAB_PATH`、`YARN_KERBEROS_KRB5_PATH`、`YARN_MAX_APPLICATIONS`。

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
