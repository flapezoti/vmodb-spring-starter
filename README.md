# vmodb-spring-starter

A Spring Boot auto-configuration for VMODB virtual microservices (VMSes). It configures a VMS
from `application.yml` and starts and stops it with Spring's own lifecycle, instead of
hand-written bootstrap code. It uses VMODB's public API only; VMODB itself has no Spring
dependency.

## What it provides

- `VmodbProperties` — binds the `vmodb.*` keys of `application.yml` (host, port, packages to scan,
  and VMODB's runtime tunables) with `@ConfigurationProperties`.
- `VmodbBootstrap.buildOptions(props)` — turns `VmodbProperties` into a `VmsApplicationOptions`.
- `VmodbAutoConfiguration` — registers a `SmartLifecycle` bean that calls `VmsApplication.start()`
  and `.close()`, and an `ITransactionManager` bean taken from the VMS. Both are created when a
  `VmsApplication` bean exists.

`VmodbAutoConfiguration` is listed in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, so Spring Boot
loads it when the jar is on the classpath.

## Usage

Add the starter and `spring-boot-starter` as dependencies and describe the VMS in
`application.yml`:

```yaml
vmodb:
  host: 0.0.0.0
  port: 18081
  packages:
    - com.example.product
  network-thread-pool-size: 4
  vms-thread-pool-size: 4
```

Declare the `VmsApplication` bean in the application:

```java
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    VmsApplication vmsApplication(VmodbProperties props) throws Exception {
        return VmsApplication.build(VmodbBootstrap.buildOptions(props), (transactionManager, repoLookup) ->
                new MyHttpHandler(transactionManager, (IMyRepository) repoLookup.apply("my_table")));
    }
}
```

The lifecycle and `ITransactionManager` beans are registered automatically. The `VmsApplication`
can be injected anywhere in the application, for example to obtain a repository with
`vms.getRepositoryProxy("my_table")` or the latest committed transaction id with
`vms.lastTidFinished()`.

## Building

Requires JDK 21. VMODB's `sdk-embed` and `modb-api` (`1.0-SNAPSHOT`) must be installed in the
local Maven repository:

```
JAVA_HOME=<jdk21> mvn clean install
```
