package io.vantiq.ext.hbase;

import io.vantiq.ext.sdk.AbstractConnectorMain;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Map;

import static io.vantiq.ext.hbase.ConnectorConstants.*;


public class HBaseConnectorMain extends AbstractConnectorMain {

    public static void main(String[] argv) throws IOException {

        CommandLine cmd = parseCommand(argv);
        Map<String, String> connectInfo = constructConfig(cmd);
        HBaseConnector connector = new HBaseConnector(connectInfo.get(VANTIQ_SOURCE_NAME), connectInfo);
        connector.start();
    }

}
