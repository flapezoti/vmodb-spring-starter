package vmodb.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds application.yml's `vmodb.*` keys, replacing VMODB's own raw Properties-file loading
 * (ConfigUtils.loadProperties()) as the source of truth for a VMS's bootstrap options.
 */
@ConfigurationProperties(prefix = "vmodb")
public class VmodbProperties {

    /** Packages VMODB's own Reflections-based scan looks in for @Microservice/@VmsTable classes. */
    private String[] packages = new String[0];

    private String host = "0.0.0.0";
    private int port;

    private int networkThreadPoolSize = 0;
    private int networkBufferSize = 0;
    private int soBufferSize = 0;
    private int networkSendTimeout = 0;
    private int vmsThreadPoolSize = 0;
    private int numVmsWorkers = 1;
    private int maxSleep = 0;
    private boolean logging = false;
    private boolean checkpointing = false;
    private int maxRecords = 0;

    public String[] getPackages() { return packages; }
    public void setPackages(String[] packages) { this.packages = packages; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getNetworkThreadPoolSize() { return networkThreadPoolSize; }
    public void setNetworkThreadPoolSize(int v) { this.networkThreadPoolSize = v; }

    public int getNetworkBufferSize() { return networkBufferSize; }
    public void setNetworkBufferSize(int v) { this.networkBufferSize = v; }

    public int getSoBufferSize() { return soBufferSize; }
    public void setSoBufferSize(int v) { this.soBufferSize = v; }

    public int getNetworkSendTimeout() { return networkSendTimeout; }
    public void setNetworkSendTimeout(int v) { this.networkSendTimeout = v; }

    public int getVmsThreadPoolSize() { return vmsThreadPoolSize; }
    public void setVmsThreadPoolSize(int v) { this.vmsThreadPoolSize = v; }

    public int getNumVmsWorkers() { return numVmsWorkers; }
    public void setNumVmsWorkers(int v) { this.numVmsWorkers = v; }

    public int getMaxSleep() { return maxSleep; }
    public void setMaxSleep(int v) { this.maxSleep = v; }

    public boolean isLogging() { return logging; }
    public void setLogging(boolean v) { this.logging = v; }

    public boolean isCheckpointing() { return checkpointing; }
    public void setCheckpointing(boolean v) { this.checkpointing = v; }

    public int getMaxRecords() { return maxRecords; }
    public void setMaxRecords(int v) { this.maxRecords = v; }
}
