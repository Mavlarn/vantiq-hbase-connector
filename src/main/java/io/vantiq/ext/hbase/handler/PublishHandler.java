package io.vantiq.ext.hbase.handler;

import io.vantiq.ext.hbase.HBaseClient;
import io.vantiq.ext.hbase.HBaseConnector;
import io.vantiq.ext.sdk.ExtensionServiceMessage;
import io.vantiq.ext.sdk.ExtensionWebSocketClient;
import io.vantiq.ext.sdk.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class PublishHandler extends Handler<ExtensionServiceMessage> {

    static final Logger LOG = LoggerFactory.getLogger(PublishHandler.class);

    private HBaseConnector connector;

    public PublishHandler(HBaseConnector connector) {
        this.connector = connector;
    }

    @Override
    public void handleMessage(ExtensionServiceMessage message) {
        LOG.debug("Publish called with message " + message.toString());
        String replyAddress = ExtensionServiceMessage.extractReplyAddress(message);
        ExtensionWebSocketClient client = connector.getVantiqClient();

        if ( !(message.getObject() instanceof Map) ) {
            client.sendQueryError(replyAddress, "io.vantiq.videoCapture.handler.PublishHandler",
                    "Request must be a map", null);
        }

        Map<String, ?> request = (Map<String, ?>) message.getObject();

        publish(request);

    }

    private void publish(Map<String, ?> request) {

        // Gather query results, or send a query error if an exception is caught
        try {
            HBaseClient hBaseClient = connector.gethBaseClient();

            String tableName = (String)request.get("tableName");
            String deviceId = (String)request.get("deviceId");
            String serviceId = (String)request.get("serviceId");
            List<String[]> datas = (List<String[]>)request.get("datas");
            Date createdDate = new Date();
            String pkStr = "pk";

            byte[] rowKey = hBaseClient.getRowKey(deviceId, serviceId, createdDate.toString(), pkStr.getBytes());

            hBaseClient.add(tableName, rowKey, datas);

            LOG.debug("Added records: ");
        } catch (Exception e) {
            LOG.error("An unexpected error occurred when executing publish.", e);
            LOG.error("Request was: {}", request);
        }
    }

}
