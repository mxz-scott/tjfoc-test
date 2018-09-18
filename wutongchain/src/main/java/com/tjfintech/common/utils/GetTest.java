package com.tjfintech.common.utils;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
@Slf4j
public class GetTest {
    public static String SendGetTojson(String url) {
            BufferedReader in = null;
            try {
                URL realUrl = new URL(url);
                // 打开和URL之间的连接
                URLConnection connection = realUrl.openConnection();
                // 设置通用的请求属性
                HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setRequestProperty("Charset", "UTF-8");
                // 设置文件类型:
                connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                connection.setRequestProperty("accept", "*/*");
                connection.setRequestProperty("connection", "Keep-Alive");
                connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                // 建立实际的连接
                connection.connect();
                // 定义 BufferedReader输入流来读取URL的响应
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (Exception e) {
                log.error("发生错误",e);

            }
            // 使用finally块来关闭输入流
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    log.error("发生错误",e2);
                }
            }
            return null;
        }

        public static   String ParamtoUrl(Map<String,Object> map){
        String param="";
        if(map.equals(null)){
            return null;
        }else {
            for(Map.Entry<String, Object> entry:map.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(param.equals("")){
                    param=key+"="+value.toString();
                }else {
                    param=param+"&"+key+"="+value.toString();
                }
            }}
            return param;
        }


    }
