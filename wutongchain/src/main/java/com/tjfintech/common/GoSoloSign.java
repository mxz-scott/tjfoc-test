package com.tjfintech.common;

import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import com.tjfintech.common.utils.GetTest;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoSoloSign implements SoloSign {

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
        String result=GetTest.SendGetTojson(SDKADD+"/utxo/balance"+"?"+param);
        log.info(result);
        return result ;
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
       String result=PostTest.sendPostToJson(SDKADD+"/utxo/transfer", map);
       log.info(result);
        return result ;

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
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/issuetoken", map);
        log.info(result);
        return result;
    }

    /**
     * 发行token
     */
    public String issueTokenV2(String tokenType,String amount,String data){
        Map<String, Object> map = new HashMap<>();
        map.put("TokenType", tokenType);
        map.put("Amount", amount);
        map.put("Data",data);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/issuetoken", map);
        log.info(result);
        return result;
    }




    /**
     * 创建1/1单签账号
     */
    public String genAddress(String publicKey){
        Map<String, Object> map = new HashMap<>();
        map.put("PubKey", publicKey);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/genaddress", map);
        log.info(result);
        return result;
    }
    /**
     * 转账操作的TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */

   public List<Map> constructToken(String toAddr,String tokenType,String amount){
       Map<String,Object>amountMap=new HashMap<>();
       amountMap.put("TokenType",tokenType);
       amountMap.put("Amount",amount);
       amountMap.put("ToAddr",toAddr);
       List<Map>list=new ArrayList<>();
       list.add(amountMap);
       return list;
   }
    public List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList){
        List<Map>list=new ArrayList<>();
        for (int i=0;i<mapList.size();i++){
            list.add(mapList.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("TokenType",tokenType);
        amountMap.put("Amount",amount);
        amountMap.put("ToAddr",toAddr);

        list.add(amountMap);
        return list;
    }



    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
