package io.vantiq.ext.hbase.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vantiq.ext.hbase.HBaseClient;
import io.vantiq.ext.hbase.HBaseConnector;
import io.vantiq.ext.sdk.ExtensionServiceMessage;
import io.vantiq.ext.sdk.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ConfigHandler extends Handler<ExtensionServiceMessage> {

    static final Logger LOG = LoggerFactory.getLogger(HBaseConnector.class);

    private static final String CONFIG = "config";
    private static final String HBASE_SERVER_HOST = "hbase_server_host";
    private static final String HBASE_SERVER_PORT = "hbase_server_port";
    private static final String HBASE_USER = "hbase_user";
    private static final String HBASE_PASSWORD = "hbase_password";


    private HBaseConnector connector;
    private ObjectMapper om = new ObjectMapper();

    public ConfigHandler(HBaseConnector connector) {
        this.connector = connector;
    }

    /**
     * topics: [
     *  {
     *      queue: "topic1", protobuf_name: "face"
     *  }
     * ]
     * @param message   A message to be handled
     */
    @Override
    public void handleMessage(ExtensionServiceMessage message) {
        LOG.warn("No configuration need for source:{}", message.getSourceName());
        Map<String, Object> configObject = (Map) message.getObject();
        Map<String, String> topicConfig;

        // Obtain entire config from the message object
        if ( !(configObject.get(CONFIG) instanceof Map)) {
            LOG.error("Configuration failed. No configuration suitable for HBase Connector.");
            failConfig();
            return;
        }
        topicConfig = (Map) configObject.get(CONFIG);

        String hbaseServer = topicConfig.get(HBASE_SERVER_HOST);
        String port = topicConfig.get(HBASE_SERVER_PORT);
        HBaseClient hBaseClient = new HBaseClient(hbaseServer, port);
        this.connector.sethBaseClient(hBaseClient);

    }

    /**
     * Closes the source {@link HBaseConnector} and marks the configuration as completed. The source will
     * be reactivated when the source reconnects, due either to a Reconnect message (likely created by an update to the
     * configuration document) or to the WebSocket connection crashing momentarily.
     */
    private void failConfig() {
//        connector.close();
    }

}
