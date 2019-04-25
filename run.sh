#!/usr/bin/env bash
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="Main" -Dexec.args="-se 127.0.0.1:9200 -si source_index_name -de 127.0.0.1:9201 -di dest_index_name" -DLOG_HOME=/home/web_server/work/es-migrate-tool/logs
