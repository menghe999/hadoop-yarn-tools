

### 一个Yarn客户端命令行

> 方便在没有hadoop client环境下调用yarn api

### Spring Boot 日志下载接口

> `feature-springboot-deploy` 分支新增 HTTP 下载方式，服务端预先配置 YARN 集群和 Kerberos 凭据，前端只传集群标识和应用 ID。

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
    "clusterId": "default_hdp",
    "applicationId": "application_1732873473669_0058"
  }'
```

接口会先在服务端认证边界内查询 YARN 应用报告，通过 `ApplicationReport.getUser()` 获取提交用户，再在服务端临时目录中拉取聚合日志；如果结果是单文件则直接下载，如果结果包含多个文件或目录则以 ZIP 返回。YARN 聚合日志仍依赖集群侧配置和应用状态，running 状态应用通常不能获取完整聚合日志。

#### 队列应用查询请求

```bash
# 查询 root.default 队列下所有状态的应用
curl 'http://localhost:8080/api/yarn/applications?clusterId=default_hdp&queue=root.default'

# 查询 root.default 队列下 RUNNING 状态的应用
curl 'http://localhost:8080/api/yarn/applications?clusterId=default_hdp&queue=root.default&state=RUNNING'
```

`state` 为可选参数，支持 Hadoop YARN 状态：`NEW`、`NEW_SAVING`、`SUBMITTED`、`ACCEPTED`、`RUNNING`、`FINISHED`、`FAILED`、`KILLED`。响应会返回本次查询条件和应用摘要列表，摘要字段包含 `applicationId`、`name`、`user`、`queue`、`state`、`finalStatus`、`applicationType`、`progress`、`trackingUrl`、`startedTime`、`finishedTime`。

应用列表接口会按服务端配置的 `yarn.logs.max-applications` 限制单次返回数量，默认 `1000`，部署时可通过环境变量 `YARN_MAX_APPLICATIONS` 覆盖，避免一次性查询和返回过大的 ResourceManager 数据集。

#### 应用详情查询请求

```bash
curl 'http://localhost:8080/api/yarn/applications/application_1732873473669_0058?clusterId=default_hdp'
```

应用详情接口按 `applicationId` 查询单个 YARN 应用，响应包含 `applicationId`、`attemptId`、`name`、`user`、`queue`、`state`、`finalStatus`、`applicationType`、`progress`、`trackingUrl`、`originalTrackingUrl`、`diagnostics`、`host`、`rpcPort`、提交/启动/完成时间、`applicationTags`、`logAggregationStatus` 和资源用量信息。响应不会返回服务端本地 Hadoop 配置目录、Kerberos principal、keytab 或 `krb5.conf` 路径。

#### 应用 kill 请求（破坏性操作）

```bash
# 仅用于测试或非生产集群；该请求会真实终止非终态 YARN 应用
# 服务端需显式设置 YARN_APPLICATION_KILL_ENABLED=true 后才允许提交 kill
curl -X POST 'http://localhost:8080/api/yarn/applications/application_1732873473669_0058/kill' \
  -H 'Content-Type: application/json' \
  -d '{
    "clusterId": "default_hdp"
  }'
```

kill 接口默认关闭，必须由服务端显式设置 `YARN_APPLICATION_KILL_ENABLED=true` 后才允许提交请求。接口会先查询应用当前状态；如果应用已经是 `FINISHED`、`FAILED` 或 `KILLED`，接口会返回 400 并拒绝提交 kill。非终态应用会向 ResourceManager 提交 `Killed by hadoop-yarn-tools API` 诊断说明，响应包含 `clusterId`、`applicationId`、`previousState`、`killRequested` 和 `message`。不要对生产集群或未知业务应用执行该接口验证。

#### YARN 队列运行时信息查询请求

```bash
curl 'http://localhost:8080/api/yarn/queues?clusterId=default_hdp'
```

队列接口返回 ResourceManager 当前运行时视角下的队列信息，不读取或暴露服务端本地配置文件内容。队列字段包含 `queueName`/`name`、`state`、`capacity`、`currentCapacity`、`maximumCapacity`、`accessibleNodeLabels`、`defaultNodeLabelExpression`、抢占开关、统计信息、按节点标签分区的队列配置和 `childQueues` 子队列。

#### YARN NodeManager 节点查询请求

```bash
# 查询 RUNNING 节点
curl 'http://localhost:8080/api/yarn/nodes?clusterId=default_hdp&state=RUNNING'

# 查询所有状态节点
curl 'http://localhost:8080/api/yarn/nodes?clusterId=default_hdp'

# 查询单个节点详情
curl 'http://localhost:8080/api/yarn/nodes/nm-host.example.com:45454?clusterId=default_hdp'
```

节点接口返回 ResourceManager 视角下的 NodeManager 节点报告，`state` 可选，支持 Hadoop YARN 节点状态名称。节点字段包含 `nodeId`、`host`、`port`、`state`、`httpAddress`、`rackName`、`numContainers`、健康报告、资源使用/容量、节点标签、节点属性、资源利用率、退役超时和节点更新类型。节点详情接口从节点列表中过滤 `nodeId`，找不到时返回客户端错误。

#### 集群配置摘要查询请求

```bash
curl 'http://localhost:8080/api/yarn/clusters'
```

响应示例：

```json
{
  "maxApplications": 1000,
  "clusters": [
    {
      "clusterId": "default_hdp",
      "kerberosEnabled": false,
      "authType": "SIMPLE",
      "yarnConfigDirConfigured": true,
      "principalConfigured": false,
      "principalMasked": null,
      "keytabPathConfigured": false,
      "krb5PathConfigured": false
    },
    {
      "clusterId": "secure",
      "kerberosEnabled": true,
      "authType": "KERBEROS",
      "yarnConfigDirConfigured": true,
      "principalConfigured": true,
      "principalMasked": "yarn-client@***",
      "keytabPathConfigured": true,
      "krb5PathConfigured": true
    }
  ]
}
```

集群摘要接口只读取服务端配置并按 `clusterId` 字典序返回，不连接真实 YARN 集群。响应不会返回原始 `yarn-config-dir`、`keytab-path`、`krb5-path` 或未脱敏 principal，只提供是否已配置的布尔值和脱敏后的 `principalMasked`。

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
./run.sh

# 停止服务
./stop.sh
```

分发包内的 `hadoop-yarn-tools-1.1.0.jar` 只包含本项目核心代码和资源，运行时依赖位于同级 `libs/` 目录，外置配置位于根目录 `application.yml`。`run.sh` 会切换到分发包根目录，并使用 `jar + libs/*` 作为 classpath 启动 `com.example.YarnToolsApplication`，日志默认写入 `logs/hadoop-yarn-tools.log`，进程号默认写入 `run/hadoop-yarn-tools.pid`。

默认启动配置会随分发包打包到根目录 `application.yml`，部署时可直接修改该文件，也可通过环境变量覆盖 `YARN_CONFIG_DIR`、`YARN_SECURE_CONFIG_DIR`、`YARN_KERBEROS_PRINCIPAL`、`YARN_KERBEROS_KEYTAB_PATH`、`YARN_KERBEROS_KRB5_PATH`、`YARN_MAX_APPLICATIONS`。

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
