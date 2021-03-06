package com.tjfintech.common.utils;


import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClassIpcr.MALLTOKEN;


@Slf4j
public class PostTest {
    public static String sendPostToJson(String linkUrl, Map<String, Object> map) {
        log.info(linkUrl);
        UtilsClass.globalResponse = ""; //请求执行前重置该变量
        JSONObject jsonObject = JSONObject.fromObject(map);
        String jsonString = jsonObject.toString();
        log.info(jsonString);
        String resultStr = "";
        try {
            // 创建url资源
            URL urlv = new URL(linkUrl);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) urlv.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);


            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.addRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            //conn.setRequestProperty("Charset", "UTF-8");
            conn.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            conn.addRequestProperty("user-agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.8 Safari/537.36");
            //转换为字节数组
            byte[] data = jsonString.getBytes("UTF-8");
            // 设置文件长度
            conn.addRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.addRequestProperty("Content-Type", "application/json; charset=UTF-8");
            //String strJson = new String(jsonString.getBytes("GBK"),"UTF-8");

            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write(data);
            out.flush();
            out.close();

            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                //log.info("连接成功");
                // 请求返回的数据
                InputStream in = conn.getInputStream();
                try {
                    byte[] data1 = readStream(in);
//                    byte[] data1 = new byte[in.available()];
//                    in.read(data1);
                    // 转成字符串
                    resultStr = new String(data1);
                    JSONObject jsonObject3 = JSONObject.fromObject(resultStr);
                    Iterator keys = jsonObject3.keys();
                    String key = null;
                    Object value = null;
                    while (keys.hasNext()) {
                        key = (String) keys.next();
                        value = jsonObject3.get(key);
//                        if(key.equals("State") && !value.equals(200)){
//                            throw new Exception("状态码不为200");
//                        }
                    }
                } catch (Exception e1) {
                    log.error("发生错误", e1);
                }
            } else {
                log.error("连接异常");
            }
            conn.disconnect();
        } catch (Exception e) {
            resultStr = e.getMessage();
        }
        UtilsClass.globalResponse = resultStr;
        return resultStr;
    }

    public static byte[] readStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toByteArray();
    }

    public static String postMethod(String linkUrl, Map<String, Object> map) {
        log.info(linkUrl);
        UtilsClass.globalResponse = ""; //请求执行前重置该变量
        JSONObject jsonObject = JSONObject.fromObject(map);
        String jsonString = jsonObject.toString();
        log.info(jsonString);
        RequestEntity se = null;
        try {
            se = new StringRequestEntity(jsonString, "application/json", "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(linkUrl);

        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);

        postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 30000);
        postMethod.setRequestEntity(se);
        postMethod.setRequestHeader("Content-Type", "application/json");

        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String responseMsg = "";
        try {
            httpClient.executeMethod(postMethod);
            responseMsg = postMethod.getResponseBodyAsString().trim();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
        }
        UtilsClass.globalResponse = responseMsg;
        return responseMsg;
    }

    public static String postMethodIpcrMall(String linkUrl, Map<String, Object> map) {
        log.info(linkUrl);
        UtilsClass.globalResponse = ""; //请求执行前重置该变量
        JSONObject jsonObject = JSONObject.fromObject(map);
        String jsonString = jsonObject.toString();
        log.info(jsonString);
        RequestEntity se = null;
        try {
            se = new StringRequestEntity(jsonString, "application/json", "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(linkUrl);
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 30000);
        postMethod.setRequestEntity(se);
        postMethod.addRequestHeader("Content-Type", "application/json; charset=utf-8");
        postMethod.addRequestHeader("token", MALLTOKEN);

        postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String responseMsg = "";
        try {
            httpClient.executeMethod(postMethod);
//            responseMsg = postMethod.getResponseBodyAsString().trim();
            InputStream inputStream = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String str;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            responseMsg = new String(stringBuffer);
            br.close();
//            return responseMsg ;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            postMethod.releaseConnection();
        }
        UtilsClass.globalResponse = responseMsg;
        return responseMsg;
    }
}
