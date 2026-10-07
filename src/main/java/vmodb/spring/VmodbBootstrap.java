package vmodb.spring;

import dk.ku.di.dms.vms.sdk.embed.client.VmsApplicationOptions;
import dk.ku.di.dms.vms.sdk.embed.client.VmsPreparedApplication;

import java.util.Properties;

/**
 * Builds a VmsApplicationOptions from Spring-bound VmodbProperties, using the
 * VmsApplicationOptions.build(Properties, host, port, packages) overload, which does not use
 * VMODB's static ConfigUtils.loadProperties() singleton.
 *
 * Unlike VmsApplication.prepare()/.build(), this does not depend on the caller's package, so it
 * can live in a shared library. Those cannot: they call ConfigUtils.getCallerPackage() and keep
 * only the @Microservice classes in the direct caller's package.
 */
public final class VmodbBootstrap {

    private VmodbBootstrap() {}

    public static VmsApplicationOptions buildOptions(VmodbProperties props) {
        Properties properties = new Properties();
        properties.setProperty("network_buffer_size", String.valueOf(props.getNetworkBufferSize()));
        properties.setProperty("so_buffer_size", String.valueOf(props.getSoBufferSize()));
        properties.setProperty("network_send_timeout", String.valueOf(props.getNetworkSendTimeout()));
        properties.setProperty("network_thread_pool_size", String.valueOf(props.getNetworkThreadPoolSize()));
        properties.setProperty("vms_thread_pool_size", String.valueOf(props.getVmsThreadPoolSize()));
        properties.setProperty("num_vms_workers", String.valueOf(props.getNumVmsWorkers()));
        properties.setProperty("max_sleep", String.valueOf(props.getMaxSleep()));
        properties.setProperty("logging", String.valueOf(props.isLogging()));
        properties.setProperty("checkpointing", String.valueOf(props.isCheckpointing()));
        properties.setProperty("max_records", String.valueOf(props.getMaxRecords()));

        return VmsApplicationOptions.build(properties, props.getHost(), props.getPort(), props.getPackages());
    }

    /**
     * Looks up a repository built by VMODB, for exposing as a typed @Bean in the application:
     *   {@literal @}Bean IProductRepository productRepository(VmsPreparedApplication prepared) {
     *       return VmodbBootstrap.repository(prepared, "products");
     *   }
     * A repository is available as soon as VmsApplication.prepare(...) returns, before any
     * @Microservice instance is constructed -- which is the point, since the application's own
     * @Microservice bean needs the repository as a constructor argument. See
     * VmsPreparedApplication.
     */
    @SuppressWarnings("unchecked")
    public static <T> T repository(VmsPreparedApplication prepared, String table) {
        Object repository = prepared.getRepositoryProxy(table);
        if (repository == null) {
            throw new IllegalStateException("No repository found for table '" + table + "'");
        }
        return (T) repository;
    }
}
