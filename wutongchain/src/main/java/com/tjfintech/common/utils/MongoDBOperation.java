package com.tjfintech.common.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;


@Slf4j
public class MongoDBOperation {
    public String createDatabase(String mongoIP,String database) throws Exception{
        //初始化mongodb客户端
        MongoClient mgClient = new MongoClient(mongoIP,27017);
        //连接到mongodb数据库,若指定数据库不存在则会在插入文档时创建
        MongoDatabase mgDatabase = mgClient.getDatabase(database);

        MongoCollection<Document>  collection = mgDatabase.getCollection("bc_block");
        Document doc = new Document();
        collection.insertOne(doc);

//        mgClient.listDatabaseNames();
        for(String str : mgClient.listDatabaseNames()){
            log.info("database name:"+str);
        }
        return "";
    }

    public long calCountOfTableDatabase(String mongoIP,String database,String table) throws Exception{
        //初始化mongodb客户端
        MongoClient mgClient = new MongoClient(mongoIP,27017);
        //连接到mongodb数据库
        MongoDatabase mgDatabase = mgClient.getDatabase(database);

        MongoCollection<Document>  collection = mgDatabase.getCollection(table);
        log.info("tx_finish table data counts:" + collection.count());
        return collection.count();
    }

    public String delDatabase(String mongoIP,String database) throws Exception {
        try {
            //初始化mongodb客户端
            MongoClient mgClient = new MongoClient(mongoIP, 27017);
            //删除数据库
            mgClient.dropDatabase(database);
            return "del OK";
        } catch (MongoException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public String queryTableKeyValue(String mongoIP,String database,String table,String key,String checkValue) throws Exception{
        //初始化mongodb客户端
        MongoClient mgClient = new MongoClient(mongoIP,27017);
        //连接到mongodb数据库
        MongoDatabase mgDatabase = mgClient.getDatabase(database);

        MongoCollection<Document>  collection = mgDatabase.getCollection(table);

        log.info("tx_finish table data counts:" + collection.count());

        return "";
    }

}
