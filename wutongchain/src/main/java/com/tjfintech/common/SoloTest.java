package com.tjfintech.common;

import com.tjfintech.common.untils.GetTest;
import com.tjfintech.common.untils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

@Slf4j
public class SoloTest {
    private static String   TOKENTYPE;
    private static final String USER1_1="32XBduKk48RJfoA2wJuQ7doaJimMymEmZLYYbYHyH5Eyi5Hs68";
    private static final String USER1_2="SogjzbsX6RsWwRKqdhTQeVTWQhy2SyNGfPy9LHEz6p3famV3wCe";
    private static final String USER3_3="SsUTN9RmWgrD8E48MuJY1pdLw4QDo7GJgK8fn8k7DFzpvG3pwqw";
    private static final String USER_COLLET="Soirv9ikykVHArKCdJqVNegxxqZWUj1g4ixFFYbBLMExy4zUTUe";
    private static final String PUBKEY="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFaTA4d0pVQ1RHQWd4Yzg2ZzRQNVBUUncwSTZQMAo4MXdCZzJYWVc0Zkx1TElvZDJxR0NIR0N2dTFBWi9nQUVTRS9OT1Q2eDdMRFhVVjRPTE9Kdk5oUXBRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    MultiTest multiTest=new MultiTest();
    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }
    @Test
    public void runSoloTest()throws  Exception{
        TOKENTYPE=multiTest.SoloInit();
        Thread.sleep(1000*3);
        multiTest.TestTransfer(TOKENTYPE,USER_COLLET,USER1_1);
        Thread.sleep(1000*5);
        TestSoloQuery(TOKENTYPE);
        log.info("单签向1/2账号转账");
        TestSoloTransfer(TOKENTYPE,USER1_2);
        Thread.sleep(1000*10);
        log.info("查询单签地址余额:20");
        TestSoloQuery(TOKENTYPE);
        log.info("单签向3/3账号转账");
        TestSoloTransfer(TOKENTYPE,USER3_3);
        Thread.sleep(1000*10);
        log.info("查询单签地址余额:10");
        TestSoloQuery(TOKENTYPE);

    }


    /**查询单签地址余额
     * @param tokenType 币种
     */
    public void TestSoloQuery(String tokenType){
        String param;
        String priKey= MultiTest.PRIKEY0;
        Map<String,Object>map=new HashMap<>();
        map.put("key",priKey);
        map.put("tokentype",tokenType);
        param= GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(MultiTest.SDKADD+"/utxo/balance"+"?"+param));
    }
    /**单签账号向其他地址转账
     *
     * @param TokenType
     * @param ToAddr
     */
    public void TestSoloTransfer(String TokenType,String ToAddr) {
        String PriKey = MultiTest.PRIKEY0;
        String Data = "使用单签账户私钥发起对多签账户转账-无密码";
        String Amount = "10.0";
        Map<String, Object> token = new HashMap<>();
        token.put("ToAddr", ToAddr);
        token.put("TokenType", TokenType);
        token.put("Amount", Amount);
        List<Map> TokenObjects = new ArrayList<Map>();
        TokenObjects.add(token);
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Token", TokenObjects);

        log.info(PostTest.sendPostToJson(MultiTest.SDKADD+"/utxo/transfer", map));

    }





    @Test
    /**
     * 创建1/1单签账号
     */
    public void Create1_1User(){
        Map<String, Object> map = new HashMap<>();
        map.put("Pubkey", PUBKEY);
        log.info(PostTest.sendPostToJson(MultiTest.SDKADD+"/utxo/genaddress", map));

    }

    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
