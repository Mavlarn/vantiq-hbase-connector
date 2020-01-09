# vantiq-hbase-connector
hbase-connector for VANTIQ


## Usage
1. register HBase Source 
2. create a source in VANTIQ
3. start the connector

## register
Create a config file named *hbaseSource.json*:
```
{
   "name" : HBaseSource",
   "baseType" : "EXTENSION",
   "verticle" : "service:extensionSource",
   "config" : {}
}
```

And run:
```
vantiq -s <profileName> load sourceimpls hbaseSource.json
```

## Create source
In VANTIQ, you should see a new Source type named *HBaseSource*, create a new source with this type, and config:
```
{
    "hbase_server_host": "localhost",
    "hbase_server_port": "2181"
}
```

## start rabbitMQ
Start rabbitMQ server.

## Package and Start connector
At first, package the connector with:
```
# package
mvn package -Dmaven.test.skip=true 

# and run
java -jar target/hbase-connector-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Test
To send add record to hbase, you run the procedure as below:
```
var theData = []
push(theData, ["family", "column1Name", "value1"])
push(theData, ["family", "column2Name", "value2"])
push(theData, ["family", "column3Name", "value3"])
PUBLISH { "tableName": "theTableName", "serviceId": "s1", "deviceId": "d1", datas: theData } TO SOURCE iot_kafka USING { topic: "topic_test" }
```

