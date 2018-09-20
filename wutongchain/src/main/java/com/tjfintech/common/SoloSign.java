package com.tjfintech.common;

import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import com.tjfintech.common.utils.GetTest;
import java.util.*;

@Slf4j
public class SoloSign {

    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }
//    @Test
//    public void runSoloTest()throws  Exception{
//        TOKENTYPE=multiTest.SoloInit();
//        Thread.sleep(1000*3);
//        multiTest.TestTransfer(TOKENTYPE,USER_COLLET,USER1_1);
//        Thread.sleep(1000*5);
//        TestSoloQuery(TOKENTYPE);
//        log.info("单签向1/2账号转账");
//        TestSoloTransfer(TOKENTYPE,USER1_2);
//        Thread.sleep(1000*10);
//        log.info("查询单签地址余额:20");
//        TestSoloQuery(TOKENTYPE);
//        log.info("单签向3/3账号转账");
//        TestSoloTransfer(TOKENTYPE,USER3_3);
//        Thread.sleep(1000*10);
//        log.info("查询单签地址余额:10");
//        TestSoloQuery(TOKENTYPE);
//
//    }


    /**查询单签地址余额
     * @param tokenType 币种
     */
    public void balance(String key,String tokenType){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("key",key);
        map.put("tokentype",tokenType);
        param= GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(MultiSign.SDKADD+"/utxo/balance"+"?"+param));
    }
    /**单签账号向其他地址转账
     *
     *
     */
    public void Transfer(String token,String priKey,String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Data", data);
        map.put("Token", token);

        log.info(PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/transfer", map));

    }

    /**
     * 发行token
     */
    public void issueToken(String priKey,String tokenType,String amount,String data){
        Map<String, Object> map = new HashMap<>();
        map.put("PriKey", priKey);
        map.put("TokenType", tokenType);
        map.put("Amount", amount);
        log.info(PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/genaddress", map));
    }




    /**
     * 创建1/1单签账号
     */
    public void genAddress(String publicKey){
        Map<String, Object> map = new HashMap<>();
        map.put("PubKey", publicKey);
        log.info(PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/genaddress", map));
    }

    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
