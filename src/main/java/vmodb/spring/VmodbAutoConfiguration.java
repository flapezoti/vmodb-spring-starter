package vmodb.spring;

import dk.ku.di.dms.vms.modb.common.transaction.ITransactionManager;
import dk.ku.di.dms.vms.sdk.embed.client.VmsApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;

/**
 * Spring integration for a VMODB VMS, except for the calls to VmsApplication.prepare(...) and
 * VmsPreparedApplication#complete(...), which must be made from the application's own package
 * (VMODB resolves @Microservice classes by the direct caller's package; see VmodbBootstrap). The
 * application declares the VmsApplication bean itself, with its @Microservice instance(s)
 * constructed by Spring:
 *
 *   {@literal @}Bean
 *   VmsPreparedApplication preparedVms(VmodbProperties props) throws Exception {
 *       return VmsApplication.prepare(VmodbBootstrap.buildOptions(props));
 *   }
 *
 *   {@literal @}Bean
 *   VmsApplication vmsApplication(VmsPreparedApplication prepared, MyService myService) throws Exception {
 *       return prepared.complete(Map.of(MyService.class.getName(), myService), myHandlerBuilder);
 *   }
 *
 * The lifecycle and transaction manager beans below are then registered around the resulting
 * VmsApplication, regardless of how it was constructed.
 */
@AutoConfiguration
@EnableConfigurationProperties(VmodbProperties.class)
public class VmodbAutoConfiguration {

    @Bean
    @ConditionalOnBean(VmsApplication.class)
    public SmartLifecycle vmodbLifecycle(VmsApplication vmsApplication) {
        return new SmartLifecycle() {
            private volatile boolean running = false;

            @Override
            public void start() {
                vmsApplication.start();
                this.running = true;
            }

            @Override
            public void stop() {
                vmsApplication.close();
                this.running = false;
            }

            @Override
            public boolean isRunning() {
                return this.running;
            }
        };
    }

    @Bean
    @ConditionalOnBean(VmsApplication.class)
    public ITransactionManager vmodbTransactionManager(VmsApplication vmsApplication) {
        return vmsApplication.getTransactionManager();
    }
}
