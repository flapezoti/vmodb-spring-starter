package vmodb.spring;

import dk.ku.di.dms.vms.sdk.embed.client.VmsApplicationOptions;

import java.util.Properties;

/**
 * Builds a VmsApplicationOptions from Spring-bound VmodbProperties, using the
 * VmsApplicationOptions.build(Properties, host, port, packages) overload, which does not use
 * VMODB's static ConfigUtils.loadProperties() singleton.
 *
 * Unlike VmsApplication.build(), this does not depend on the caller's package, so it can live in
 * a shared library. VmsApplication.build() cannot: it calls ConfigUtils.getCallerPackage() and
 * keeps only the @Microservice classes in the direct caller's package.
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
}
