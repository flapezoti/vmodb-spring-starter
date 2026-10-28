package vmodb.spring;

import dk.ku.di.dms.vms.modb.api.query.parser.Parser;
import dk.ku.di.dms.vms.modb.api.query.statement.SelectStatement;
import dk.ku.di.dms.vms.sdk.embed.client.VmsApplicationOptions;
import dk.ku.di.dms.vms.sdk.embed.client.VmsPreparedApplication;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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
     *       return VmodbBootstrap.repository(prepared, "products", IProductRepository.class);
     *   }
     * The standard way to obtain a repository through this starter. repositoryInterface is
     * required so Spring Data's own {@literal @}org.springframework.data.jpa.repository.Query
     * annotation is recognized on the interface's methods, alongside VMODB's own
     * {@literal @}Query (if a method carries both, Spring's wins). Either way the query string is
     * parsed and executed by VMODB's own query engine.
     *
     * One real limitation: VMODB binds a query's :named placeholders to the method's arguments by
     * POSITION, not by name the way Spring Data's own @Query normally does -- placeholders must
     * be declared in the same order as the method's parameters.
     */
    public static <T> T repository(VmsPreparedApplication prepared, String table, Class<T> repositoryInterface) {
        Map<String, SelectStatement> extraQueries = new HashMap<>();
        for (Method method : repositoryInterface.getDeclaredMethods()) {
            Query springQuery = method.getAnnotation(Query.class);
            if (springQuery == null) continue;
            extraQueries.put(method.getName(), Parser.parse(springQuery.value()));
        }
        try {
            @SuppressWarnings("unchecked")
            T repository = (T) prepared.buildRepositoryProxy(table, repositoryInterface, extraQueries);
            if (repository == null) {
                throw new IllegalStateException("No repository found for table '" + table + "'");
            }
            return repository;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build repository proxy for table '" + table + "'", e);
        }
    }
}
