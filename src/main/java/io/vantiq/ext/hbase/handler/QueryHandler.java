package io.vantiq.ext.hbase.handler;

import io.vantiq.ext.hbase.HBaseConnector;
import io.vantiq.ext.sdk.ExtensionServiceMessage;
import io.vantiq.ext.sdk.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryHandler extends Handler<ExtensionServiceMessage> {

    static final Logger LOG = LoggerFactory.getLogger(PublishHandler.class);

    private HBaseConnector extension;

    public QueryHandler(HBaseConnector extension) {
        this.extension = extension;
    }

    @Override
    public void handleMessage(ExtensionServiceMessage msg) {
        LOG.info("Query NOT supported");
    }
}
