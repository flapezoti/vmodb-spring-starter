# vmodb-spring-starter

A Spring Boot auto-configuration for VMODB virtual microservices (VMSes). It configures a VMS
from `application.yml` and starts and stops it with Spring's own lifecycle, instead of
hand-written bootstrap code. It uses VMODB's public API only; VMODB itself has no Spring
dependency.

`@Microservice` instances are constructed by Spring itself — real constructor injection, full
bean lifecycle — rather than by VMODB's own reflection, via `vmodb-fork`'s two-phase
`VmsApplication.prepare(...)` / `VmsPreparedApplication#complete(...)` split (see
`vmodb-fork/README.md`).

## What it provides

- `VmodbProperties` — binds the `vmodb.*` keys of `application.yml` (host, port, packages to scan,
  and VMODB's runtime tunables) with `@ConfigurationProperties`.
- `VmodbBootstrap.buildOptions(props)` — turns `VmodbProperties` into a `VmsApplicationOptions`.
- `VmodbBootstrap.repository(prepared, table)` — looks up a repository VMODB built for the given
  table, typed for use in a `@Bean` method. A repository is available as soon as
  `VmsApplication.prepare(...)` returns, before any `@Microservice` instance is constructed —
  which is the point: the application's own `@Microservice` bean needs the repository as a
  constructor argument.
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

Declare the beans in the application:

```java
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    VmsPreparedApplication preparedVms(VmodbProperties props) throws Exception {
        return VmsApplication.prepare(VmodbBootstrap.buildOptions(props));
    }

    @Bean
    IMyRepository myRepository(VmsPreparedApplication prepared) {
        return VmodbBootstrap.repository(prepared, "my_table");
    }

    // A genuine Spring bean: constructed by Spring's own dependency resolution, not by VMODB's
    // reflection. MyService itself carries no Spring annotation at all, and can take any other
    // genuine Spring bean as a constructor parameter, the same way any other @Bean method can.
    @Bean
    MyService myService(IMyRepository myRepository) {
        return new MyService(myRepository);
    }

    @Bean
    VmsApplication vmsApplication(VmsPreparedApplication prepared, MyService myService) throws Exception {
        return prepared.complete(Map.of(MyService.class.getName(), myService),
                (transactionManager, repoLookup) ->
                        new MyHttpHandler(transactionManager, (IMyRepository) repoLookup.apply("my_table")));
    }
}
```

The `vmsInstances` map passed to `complete(...)` must be keyed by `Class#getName()`, one entry per
`@Microservice` class found when `prepare(...)` scanned the application's package.

`VmsApplication.prepare(...)`/`VmsPreparedApplication#complete(...)` cannot be hidden inside the
starter itself: VMODB resolves `@Microservice`/`@VmsTable` classes by the *direct caller's*
package (`ConfigUtils.getCallerPackage()`), so those calls must sit in a class within the
application's own package, not in a shared library. Everything else — options binding, lifecycle,
`ITransactionManager` — is auto-configured.

The lifecycle and `ITransactionManager` beans are registered automatically. `VmsApplication` and
any repository/service bean can be injected anywhere in the application like ordinary Spring
beans — for example a `@RestController` can take `IMyRepository` as a constructor parameter
directly. `VmsApplication` itself also exposes `lastTidFinished()`, the latest committed
transaction id.

## Building

Requires JDK 21. VMODB's `sdk-embed` and `modb-api` (`1.0-SNAPSHOT`) must be installed in the
local Maven repository:

```
JAVA_HOME=<jdk21> mvn clean install
```
