# hadoop-yarn-tools 智能体说明

## 仓库定位

- `feature-springboot-deploy` 分支已改为单模块 Java 8 Maven Spring Boot Web 常驻服务，入口是 `com.example.YarnToolsApplication`。
- 当前分支不再保留 `YARNInteractiveClient` / `YarnLogs` CLI 入口；不要按 main 分支的交互式命令行客户端结构修改当前分支。
- Web 接口目前包括 `POST /api/yarn/logs/download` 和 `GET /api/yarn/applications`，二者都通过 `clusterId` 读取服务端白名单集群配置。

## 构建与验证

- 仓库没有 Maven Wrapper；使用系统 Maven 命令，不要写成 `./mvnw`。
- `pom.xml` 将 `maven.compiler.source`/`target` 固定为 `8`；新增代码不要使用 Java 8 之后的语言特性。
- 常用验证命令：`mvn clean test`，当前有 Spring Boot Test / JUnit 5 / Mockito 测试。
- 完整打包命令：`mvn clean package`。分发包是 `target/hadoop-yarn-tools-1.1.0.zip`，内部为核心 thin jar、`libs/` 依赖目录、根目录 `application.yml` 和根目录 `run.sh` / `stop.sh`。
- 当前未配置 JaCoCo、Checkstyle、Spotless、formatter、pre-commit 或 codegen；不要假设这些流程存在。

## 运行与发布

- 这是常驻 HTTP 服务；开发时可用 `mvn spring-boot:run`，部署时解压 zip 后在分发包根目录用 `./run.sh` 启动、`./stop.sh` 停止。
- `src/main/resources/application.yml` 通过 `yarn.logs.clusters.*` 配置服务端集群白名单；前端请求不能传 `yarn-config-dir`、keytab、`krb5.conf` 等本地路径。
- `default` 集群的配置目录可通过 `YARN_CONFIG_DIR` 覆盖；Kerberos 集群通过 `YARN_KERBEROS_PRINCIPAL`、`YARN_KERBEROS_KEYTAB_PATH`、`YARN_KERBEROS_KRB5_PATH` 覆盖。
- `GET /api/yarn/applications` 默认受 `yarn.logs.max-applications` 限制，环境变量 `YARN_MAX_APPLICATIONS` 可覆盖。
- Hadoop 客户端固定加载 `core-site.xml`、`hdfs-site.xml`、`yarn-site.xml`；真实配置文件和 Kerberos 凭据不能提交。
- `.github/workflows/release.yml` 只在推送 `v*` tag 时运行 `mvn clean package` 并上传 `target/*.zip`；其中 `env.VERSION` 未在 workflow 内设置，修改发布流程前先确认该变量来源。

## 修改注意事项

- `yarn application -kill` 会真实终止集群应用；任何手工验证都不要连生产集群执行破坏性命令。
- YARN 日志聚合依赖外部集群状态和配置；README 已说明 running 状态应用通常不能拉取聚合日志。
- Hadoop `UserGroupInformation` 和 `java.security.krb5.conf` 是 JVM 全局状态；所有新增 Hadoop/YARN 调用都应复用 `YarnClusterConnectionService` 的统一认证边界。
- 当前工作区可能出现未跟踪的 `.idea/`、`.omo/` 或 `target/`；除非用户明确要求，不要把这些本地/生成文件纳入提交或作为架构事实。
