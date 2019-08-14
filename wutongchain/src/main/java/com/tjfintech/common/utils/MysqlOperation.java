package com.tjfintech.common.utils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import javax.sql.*;

@Slf4j
public class MysqlOperation {

    //DBPath = "root:root@tcp(10.1.3.246:3306)/wallet0703?charset=utf8"
    public static String mysqlIP ="10.1.3.246";
    public static String mysqlUrl="jdbc:mysql://"+mysqlIP;

    public static String driver="com.mysql.jdbc.Driver"; //连接driver

    public static String mysqlName="root"; //连接登录账户名
    public static String mysqlPwd="root"; //连接登录账户密码


    public String createDatabase(String database) throws Exception{
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

    public String delDatabase(String database) throws Exception {
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