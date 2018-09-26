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
    public String Balance(String key,String tokenType){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("key",key);
        map.put("tokentype",tokenType);
        param= GetTest.ParamtoUrl(map);
        return GetTest.SendGetTojson(MultiSign.SDKADD+"/utxo/balance"+"?"+param);
    }
    /**单签账号向其他地址转账
     *
     *
     */
    public String Transfer(List<Map> token,String priKey,String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Data", data);
        map.put("Token", token);

        return PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/transfer", map);

    }

    /**
     * 发行token
     */
    public String issueToken(String priKey,String tokenType,String amount,String data){
        Map<String, Object> map = new HashMap<>();
        map.put("PriKey", priKey);
        map.put("TokenType", tokenType);
        map.put("Amount", amount);
        map.put("Data",data);
        return PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/genaddress", map);
    }




    /**
     * 创建1/1单签账号
     */
    public void genAddress(String publicKey){
        Map<String, Object> map = new HashMap<>();
        map.put("PubKey", publicKey);
        log.info(PostTest.sendPostToJson(MultiSign.SDKADD+"/utxo/genaddress", map));
    }
    /**
     * 转账操作的TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  Map<String,Object>  constructToken(String toAddr, String tokenType, String amount){
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("TokenType",tokenType);
        amountMap.put("Amount",amount);
        amountMap.put("ToAddr",toAddr);
        return amountMap;
    }




    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
