package com.tjfintech.common.performanceTest;


import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.PostTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PRIKEY6;
import static com.tjfintech.common.utils.UtilsClass.PUBKEY6;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class HaiGuanTest {
     static TestBuilder testBuilder= TestBuilder.getInstance();
     static   Store store =testBuilder.getStore();



    @Test
    public void VerifyHaiguanData() throws Exception {

        String HaiGuanPRIKEY = getKeyPairsFromFile(certPath+"/haiguankey.pem");
        String HaiGuanPWD = "dcjet";

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        log.info(Integer.toString(blockHeight));

        int jSDK = 0, kSDK = 0, a = 0;
        int jSYNC = 0, kSYNC = 0;
        int b = 0, bok = 0;
        int c = 0;

        for (int i = 0; i <= blockHeight; i++) {

            //获取交易列表
            String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getString("txs");
            txsList = txsList.substring(2);
            txsList = StringUtils.substringBefore(txsList, "\"]");
            log.info(txsList);

            String[] txs = txsList.split("\",\"");

            for (String tx : txs) {
                JSONObject txjson = JSONObject.fromObject(store.GetTxDetail(tx)).getJSONObject("Data").getJSONObject("Header");
               int type = txjson.getInt("Type");
               int subType = txjson.getInt("SubType");

               if ( type == 0  && subType == 1 ) { //隐私存证
                   a++;

                   //使用SDK解密
                   String response3= store.GetStorePostPwd(tx,HaiGuanPRIKEY,HaiGuanPWD);

                   if (response3.contains("200")){
                       //log.info("SDK解密结果：" + response3);
                       jSDK++;
                   }else {
                       log.info("SDK未解密结果：" + response3);
                       kSDK++;
                   }

                   //使用同步工具解密
                   String data = JSONObject.fromObject(GetStorePost(tx)).getString("Data");
                   //log.info("data: " + data);
                   String response5 = Decode(data, HaiGuanPRIKEY, HaiGuanPWD);

                   if (response5.contains("200")){
                       //log.info("SYNC解密结果：" + response3);
                       jSYNC++;
                   }else {
                       log.info("SYNC未解密结果：" + response5);
                       kSYNC++;
                   }


               } else if (type == 0  && subType == 0){ //普通存证
                    b++;
                   String response4= store.GetStore(tx);
                   if (response4.contains("200")){
                       //log.info("普通存证：" + response4);
                       bok++;
                   }

               } else { //其他类型交易
                    c++;
               }

            }
        }
        log.info("隐私存证个数：" + a);
        log.info("SDK解密个数：" + jSDK + " , SDK未解密个数：" + kSDK);
        log.info("SYNC解密个数：" + jSYNC + " , SYNC未解密个数：" + kSYNC);

        log.info("普通存证个数：" + b + " ， 普通存证成功获取个数：" + bok);

        log.info("其他类型交易个数：" + c);
    }


    public String Decode(String data, String priKey, String pwd) {
        Map<String, Object> map = new HashMap<>();
        map.put("Data", data);
        map.put("PriKey", priKey);
        map.put("Pwd", pwd);
        String result = PostTest.sendPostToJson( "http://10.1.3.165:9999/decode", map);
//        log.info(result);
        return result;
    }

    /***
     * 获取隐私存证,只传哈希
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public String GetStorePost(String Hash) {
        Map<String, Object> map = new HashMap<>();
        map.put("Hash", Hash);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/getstore" + param, map);
//        log.info(result);
        return result;
    }

}


