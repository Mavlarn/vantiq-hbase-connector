package io.vantiq.ext.hbase;

import io.vantiq.ext.hbase.handler.*;
import io.vantiq.ext.sdk.ExtensionWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.vantiq.ext.hbase.ConnectorConstants.CONNECTOR_CONNECT_TIMEOUT;
import static io.vantiq.ext.hbase.ConnectorConstants.RECONNECT_INTERVAL;

public class HBaseConnector {

    static final Logger LOG = LoggerFactory.getLogger(HBaseConnector.class);

    ExtensionWebSocketClient vantiqClient = null;
    String sourceName;
    String vantiqUrl;
    String vantiqToken;
    String homeDir;

    private VantiqUtil vantiqUtil = new VantiqUtil();
    private HBaseClient hBaseClient;

    public HBaseConnector(String sourceName, Map<String, String> connectionInfo) {
        if (connectionInfo == null) {
            throw new RuntimeException("No VANTIQ connection information provided");
        }
        if (sourceName == null) {
            throw new RuntimeException("No source name provided");
        }

        this.vantiqUrl = connectionInfo.get(ConnectorConstants.VANTIQ_URL);
        this.vantiqToken = connectionInfo.get(ConnectorConstants.VANTIQ_TOKEN);
        this.homeDir = connectionInfo.get(ConnectorConstants.VANTIQ_HOME_DIR);
        this.sourceName = sourceName;
    }


    public void start() throws IOException {

        vantiqClient = new ExtensionWebSocketClient(sourceName);

        vantiqClient.setConfigHandler(new ConfigHandler(this));
        vantiqClient.setReconnectHandler(new ReconnectHandler(this));
        vantiqClient.setCloseHandler(new CloseHandler(this));
        vantiqClient.setPublishHandler(new PublishHandler(this));
        vantiqClient.setQueryHandler(new QueryHandler(this));

        boolean sourcesSucceeded = false;
        while (!sourcesSucceeded) {
            vantiqClient.initiateFullConnection(vantiqUrl, vantiqToken);

            sourcesSucceeded = checkConnectionFails(vantiqClient, CONNECTOR_CONNECT_TIMEOUT);
            if (!sourcesSucceeded) {
                try {
                    Thread.sleep(RECONNECT_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.error("An error occurred when trying to sleep the current thread. Error Message: ", e);
                }
            }
        }
    }

    public ExtensionWebSocketClient getVantiqClient() {
        return vantiqClient;
    }

    public VantiqUtil getVantiqUtil() {
        return this.vantiqUtil;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getVantiqUrl() {
        return vantiqUrl;
    }

    public String getVantiqToken() {
        return vantiqToken;
    }

    public HBaseClient gethBaseClient() {
        return hBaseClient;
    }

    public void sethBaseClient(HBaseClient hBaseClient) {
        this.hBaseClient = hBaseClient;
    }

    /**
     * Waits for the connection to succeed or fail, logs and exits if the connection does not succeed within
     * {@code timeout} seconds.
     *
     * @param client    The client to watch for success or failure.
     * @param timeout   The maximum number of seconds to wait before assuming failure and stopping
     * @return          true if the connection succeeded, false if it failed to connect within {@code timeout} seconds.
     */
    public boolean checkConnectionFails(ExtensionWebSocketClient client, int timeout) {
        boolean sourcesSucceeded = false;
        try {
            sourcesSucceeded = client.getSourceConnectionFuture().get(timeout, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            LOG.error("Timeout: full connection did not succeed within {} seconds: {}", timeout, e);
        }
        catch (Exception e) {
            LOG.error("Exception occurred while waiting for webSocket connection", e);
        }
        if (!sourcesSucceeded) {
            LOG.error("Failed to connect to all sources.");
            if (!client.isOpen()) {
                LOG.error("Failed to connect to server url '" + vantiqUrl + "'.");
            } else if (!client.isAuthed()) {
                LOG.error("Failed to authenticate within " + timeout + " seconds using the given authentication data.");
            } else {
                LOG.error("Failed to connect within 10 seconds");
            }
            return false;
        }
        return true;
    }
}
