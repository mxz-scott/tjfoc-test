package com.tjfintech.common.utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import javax.sql.*;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class MysqlOperation {

    //DBPath = "root:root@tcp(10.1.3.246:3306)/wallet0703?charset=utf8"
//    public String mysqlIP = "10.1.3.164";
//    public String mysqlUrl = "jdbc:mysql://" + mysqlIP;

    public static String driver = "com.mysql.jdbc.Driver"; //连接driver

    public static String mysqlName="root"; //连接登录账户名
    public static String mysqlPwd="root"; //连接登录账户密码


    public String createDatabase(String mysqlIP,String database) throws Exception{
        String mysqlUrl = "jdbc:mysql://" + mysqlIP;
        Connection connection = null;
        Statement sta = null;
        try {
                //注册JDBC驱动
                Class.forName(driver);
                //打开连接
                log.info("连接mysql数据库："+mysqlUrl);
                connection = (Connection) DriverManager.getConnection(mysqlUrl, mysqlName, mysqlPwd);
                //创建database实例并默认utf-8编码格式
                String addDatabase ="create database "+database+" default character set utf8;";
                sta = (Statement) connection.createStatement();
                int eff = sta.executeUpdate(addDatabase);
                return "create OK";
            }catch (SQLException e) {
                e.printStackTrace();
                return e.toString();
            }
            catch (Exception e1){
                e1.printStackTrace();
                return "Class.forName Error";
            }
            finally {
                try {
                    connection.close();
                }catch (SQLException e2){
                    e2.printStackTrace();
                }
                try {
                    if(sta != null) sta.close();
                }catch (SQLException e3){
                    e3.printStackTrace();
                }

            }
    }

//    @Test
    public String calCountOfTableDatabase(String mysqlIP,String database,String table) throws Exception{
        String mysqlUrl = "jdbc:mysql://" + mysqlIP;
        Connection connection = null;
        Statement sta = null;
        String count = "";
        try {
            //注册JDBC驱动
            Class.forName(driver);
            //打开连接
            log.info("连接mysql数据库：" + mysqlUrl);
            connection = (Connection) DriverManager.getConnection(mysqlUrl, mysqlName, mysqlPwd);
            //创建database实例并默认utf-8编码格式
            String getTableCount ="select count(*) from " + database + "." + table + ";";
            sta = (Statement) connection.createStatement();
            ResultSet rs  = sta.executeQuery(getTableCount);
            while(rs.next()){
                count = rs.getString(1);
                log.info("get result :" + count);
            }
            return count;
        }catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        catch (Exception e1){
            e1.printStackTrace();
            return "Class.forName Error";
        }
        finally {
            try {
                connection.close();
            }catch (SQLException e2){
                e2.printStackTrace();
            }
            try {
                if(sta != null) sta.close();
            }catch (SQLException e3){
                e3.printStackTrace();
            }

        }
    }

    public String queryTableKeyValue(String mysqlIP,String database,String table,String key,String checkValue) throws Exception{
        String mysqlUrl = "jdbc:mysql://" + mysqlIP;
        Connection connection = null;
        Statement sta = null;
        String queryResult = "";
        try {
            //注册JDBC驱动
            Class.forName(driver);
            //打开连接
            log.info("连接mysql数据库：" + mysqlUrl);
            connection = (Connection) DriverManager.getConnection(mysqlUrl, mysqlName, mysqlPwd);
            //创建database实例并默认utf-8编码格式
            String queryKeyValue ="select c" + key + " from " + database + "." + table + ";";
            sta = (Statement) connection.createStatement();
            ResultSet rs  = sta.executeQuery(queryKeyValue);
            while(rs.next()){
                queryResult = rs.getString(1);
                log.info("get result :" + queryResult);
            }
            return queryResult;
        }catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        }
        catch (Exception e1){
            e1.printStackTrace();
            return "Class.forName Error";
        }
        finally {
            try {
                connection.close();
            }catch (SQLException e2){
                e2.printStackTrace();
            }
            try {
                if(sta != null) sta.close();
            }catch (SQLException e3){
                e3.printStackTrace();
            }

        }
    }


    public String delDatabase(String mysqlIP,String database) throws Exception {
        String mysqlUrl = "jdbc:mysql://" + mysqlIP;
        Connection connection = null;
        Statement sta = null;
        try {
            //注册JDBC驱动
            Class.forName(driver);
            //打开连接
            log.info("连接mysql数据库：" + mysqlUrl);
            connection = (Connection) DriverManager.getConnection(mysqlUrl, mysqlName, mysqlPwd);
            //创建database实例并默认utf-8编码格式
            String delDatabase = "DROP DATABASE " + database + ";";
            sta = (Statement) connection.createStatement();
            int eff = sta.executeUpdate(delDatabase);
            return "create OK";
        } catch (SQLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (Exception e1) {
            e1.printStackTrace();
            return "Class.forName Error";
        } finally {
            try {
                connection.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
            try {
                if (sta != null) sta.close();
            } catch (SQLException e3) {
                e3.printStackTrace();
            }
        }
    }
}
