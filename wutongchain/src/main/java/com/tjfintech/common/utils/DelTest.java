package com.tjfintech.common.utils;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.java.tar.gz.FileUtil.log;

public class DelTest {
    public static String doDel(String url) {
        log.info(url);
        /*
         * 使用 GetMethod 来访问一个 URL 对应的网页,实现步骤: 1:生成一个 HttpClinet 对象并设置相应的参数。 2:生成一个
         * GetMethod 对象并设置响应的参数。 3:用 HttpClinet 生成的对象来执行 GetMethod 生成的Get 方法。 4:处理响应状态码。
         * 5:若响应正常，处理 HTTP 响应内容。 6:释放连接。
         */
        /* 1 生成 HttpClinet 对象并设置参数 */
        BufferedReader in = null;
        HttpClient httpClient = new HttpClient();
        // 设置 Http 连接超时为5秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);

        /* 2 生成 GetMethod 对象并设置参数 */
//        GetMethod getMethod = new GetMethod(url);
        DeleteMethod deleteMethod = new DeleteMethod(url);
//         设置 get 请求超时为 5 秒
        deleteMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 10000);

        deleteMethod.addRequestHeader("X-Timestamp", "1607310768");
        deleteMethod.addRequestHeader("X-Nonce", "dQv0Aoghch");
        deleteMethod.addRequestHeader("X-Sign", "3045022100a39ec7d0c0a505c8121ccdb187e4bac48fe1af86a1d8fb95af5bdb4551dd5e8f02201b1d25a48fb93d369c9e8810c48c098fe287b73bc1ef7b766ce9f23241d85ae3");

        // 设置请求重试处理，用的是默认的重试处理：请求三次
        deleteMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
        String response = "";
        /* 3 执行 HTTP GET 请求 */
        try {
            int statusCode = httpClient.executeMethod(deleteMethod);
            /* 4 判断访问的状态码 */
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + deleteMethod.getStatusLine());
            }
            /* 5 处理 HTTP 响应内容 */
            // HTTP响应头部信息，这里简单打印
            Header[] headers = deleteMethod.getResponseHeaders();
            // for (Header h : headers)
            // System.out.println(h.getName() + "------------ " + h.getValue());
            // 读取 HTTP 响应内容，这里简单打印网页内容\
            InputStream responseStream = deleteMethod.getResponseBodyAsStream();
            in = new BufferedReader(new InputStreamReader(responseStream));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            response = sb.toString();
            //log.info(response);

        } catch (
                HttpException e) {
            // 发生致命的异常，可能是协议不对或者返回的内容有问题
            System.out.println("Please check your provided http address!");
            e.printStackTrace();
        } catch (
                IOException e) {
            // 发生网络异常
            e.printStackTrace();
        } finally {
            /* 6 .释放连接 */
            deleteMethod.releaseConnection();
        }
        return response;
    }
}
