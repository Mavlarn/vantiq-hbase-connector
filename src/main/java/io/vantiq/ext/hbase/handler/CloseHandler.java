package io.vantiq.ext.hbase.handler;

import io.vantiq.ext.hbase.HBaseClient;
import io.vantiq.ext.hbase.HBaseConnector;
import io.vantiq.ext.sdk.ExtensionWebSocketClient;
import io.vantiq.ext.sdk.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vantiq.ext.hbase.ConnectorConstants.CONNECTOR_CONNECT_TIMEOUT;
import static io.vantiq.ext.hbase.ConnectorConstants.RECONNECT_INTERVAL;

public class CloseHandler extends Handler<ExtensionWebSocketClient> {

    static final Logger LOG = LoggerFactory.getLogger(CloseHandler.class);

    private HBaseConnector connector;

    public CloseHandler(HBaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public void handleMessage(ExtensionWebSocketClient client) {

        LOG.info("Close handler: {}", client);
        HBaseClient hBaseClient = connector.gethBaseClient();
        if (hBaseClient != null) {
            hBaseClient.close();
        }

        // reconnect
        boolean sourcesSucceeded = false;
        while (!sourcesSucceeded) {
            client.initiateFullConnection(connector.getVantiqUrl(), connector.getVantiqToken());
            sourcesSucceeded = connector.checkConnectionFails(client, CONNECTOR_CONNECT_TIMEOUT);
            if (!sourcesSucceeded) {
                try {
                    Thread.sleep(RECONNECT_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.error("An error occurred when trying to sleep the current thread. Error Message: ", e);
                }
            }
        }


    }
}
