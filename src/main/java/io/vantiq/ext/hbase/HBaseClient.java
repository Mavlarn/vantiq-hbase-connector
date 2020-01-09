/*
 * Copyright (c) 2018 Vantiq, Inc.
 *
 * All rights reserved.
 * 
 * SPDX: MIT
 */

package io.vantiq.ext.hbase;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.MD5Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Controls the connection and interaction with the Vantiq server. Initialize it and call start() and it will run 
 * itself. start() will return a boolean describing whether or not it succeeded, and will wait up to 10 seconds if
 * necessary.
 */
public class HBaseClient {

    private final Logger LOG = LoggerFactory.getLogger(HBaseClient.class);

    private static Connection conn;

    /**
     * Creates a new HBaseClient with the settings given.
     * @param serverHosts           The hbase servers.
     */
    public HBaseClient(String serverHosts, String port) {

        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", serverHosts);
        conf.set("hbase.zookeeper.property.clientPort", port);
        try {
            conn = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            LOG.error("HBaseClient init error", e);
        }
    }

    /**
     * 添加数据
     *
     * @param tableName
     *            表名
     * @param rowKey
     *            行号
     * @param datas
     *            数据
     * @throws IOException
     */
    public void add(String tableName, byte[] rowKey, List<String[]> datas) throws IOException {

        Table table = conn.getTable(TableName.valueOf(tableName));
        try {
            Put list = new Put(rowKey);
            for (String[] data : datas) {
                list.addColumn(data[0].getBytes(), data[1].getBytes(), data[2].getBytes());
            }
            table.put(list);
        } finally {
            if (table != null) {
                IOUtils.closeQuietly(table);
            }
        }
    }

    public static final byte BLANK_STR = 0x1F;

    public byte[] coverLeft(String source, int length) {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(Bytes.toBytes(source));
        while (buffer.hasRemaining()) {
            buffer.put(BLANK_STR);
        }
        return buffer.array();
    }

    public byte[] getRowKey(String deviceId, String serviceId, String createDate, byte[] pk){

        String hashStr= MD5Hash.getMD5AsHex(deviceId.getBytes()).substring(0, 8);
        String deviceHex= hashStr+deviceId;
        byte[] deviceByte=coverLeft(deviceHex,20);
        byte[] serviceByte=coverLeft(serviceId,12);
        byte[] createDateByte=Bytes.toBytes(createDate);
        ByteBuffer rowKeyBuffer=ByteBuffer.allocate(50);
        rowKeyBuffer.put(deviceByte);
        rowKeyBuffer.put(serviceByte);
        rowKeyBuffer.put(createDateByte);
        rowKeyBuffer.put(pk);
        return rowKeyBuffer.array();
    }

    /**
     * Closes hbase connection
     */
    public void close() {
        try {
            conn.close();
        } catch (IOException e) {
            LOG.error("CLose connection error", e);
        }
    }

}
