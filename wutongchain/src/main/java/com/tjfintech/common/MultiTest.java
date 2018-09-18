package com.tjfintech.common;


import com.tjfintech.common.untils.GetTest;
import com.tjfintech.common.untils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.*;

@Slf4j
public class MultiTest  {
    private static final String PRIKEY1 = "LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWhra283bEx2ZWtmQUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUxvc2VwNnI2azhsSXM0Tk1DNndGM2NFZ2FBbg0KeE4wWDRadHJsc2pCVG5TOXhjYnM3Wk9lcjFwY25aby9RZ2JqRWtGeThaYVBjSyt5d0NLcDRaMDVnbWgwU2M4Nw0KTVdNbGZvd1pJbXcvSHRoOHQ5Y0Z3eFRZMktiZkJEaWQ1SFpwVGRpRGU2R2tVa3hsajRnQkZhM29xMjg4UnVpOA0KOTIwY3FvQmwrWlZKKy8rZkFlaTA2b1ZqdEJzdWp0SmRjWnd6eGlxMjdzK0V3VUptV2NxaWliTWVqZGtDUWZvdQ0Kam9tQkphajZwS3pwdEhQNnIrbHkNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
    private static final String PWD1 = "123";
    private static final String PRIKEY2 = "LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWcvQTR4em8xYTR2QUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUNVSmx2MFNsQ1FIVWM0Z25YVnVsODBFZ2FDNg0KRnhGZDFEUlB5ZURBR1VlY2RnK0owSndQWm9YVk5MVXZqbGUwZU5GRFRManZlTlN4K2lYd0Y5c1o3cTZJWjN5Rg0KZHgzUzBDU3o3SFBzb29EWUJ6cUg0R09qOU1FZkpYU0hMdGU0WjRUclZsRVJkSVNzVGExb0p0WUV0R2Vmcy9hdQ0KZWRMRDJMS0RZOWY1cFA5WHRGUm1iL210TW9BaHVUdVFGU3V2a0huWFhmSFhjQ3pTb2dybjNsNHZPeDFHbm8xeA0KNnQxL1BnVERMSytTUnFhbHpGNmUNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
    private static final String PWD2 = "124";
    private static final String PRIKEY3 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tDQpNSUdUQWdFQU1CTUdCeXFHU000OUFnRUdDQ3FCSE05VkFZSXRCSGt3ZHdJQkFRUWd6cVBtZnpIQWVmdXpXM1pWDQp1K2MzaFZVbVdrMldtUk1qWGJZNVhGS0lIOWlnQ2dZSUtvRWN6MVVCZ2kyaFJBTkNBQVRHZlhVSklyWnVtZVJxDQpSUmRxdTUybkJMK3I3ZTU4K05qUU9GVWV4OGVuVHBidWh2RlpmL0cvcXpQRFF4L1I2aCszWWw2UkRFVitIckVXDQpZR2RKZEpSZg0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQ==";
    private static final String PUBKEY1 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRXhuMTFDU0syYnBua2FrVVhhcnVkcHdTL3ErM3UNCmZQalkwRGhWSHNmSHAwNlc3b2J4V1gveHY2c3p3ME1mMGVvZnQySmVrUXhGZmg2eEZtQm5TWFNVWHc9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    private static final String PUBKEY2 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWtiRmlaOW9VaWFaMmh3dTVsS3FYNkQ1OHdXOVYNCmNEQ1BjUEJQWThyTlVTQitNR1ZxMUlyUk8vVVBMaXRqc0RtcWN2MzdKdmVSTC9Ba0FWM1hDd2JGM3c9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    private static final String PUBKEY3 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRXZsTDZtVThFNkU1cTBRVk11NlNTbXRMMzhrdjQNCnVadnhVcG9YUlMwcHNsMkV6UHF2YTIxLzJiTzM5eW5RaHRPTTgrd0lWbExKY3ByNXZnOG1PYkVYdXc9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    public static final String PRIKEY0="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3BRRjEzT01KaERQVVM3bnEKTVVYQUZNK01mUlV3MFc3bFVRQnNvOW12WWZ1Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFTTFR6QWxRSk1ZQ0RGegp6cURnL2s5TkhEUWpvL1R6WEFHRFpkaGJoOHU0c2loM2FvWUljWUsrN1VCbitBQVJJVDgwNVBySHNzTmRSWGc0CnM0bTgyRkNsCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    private static String TOKENTYPE ;
    private static String RECYCLETYPE;
    public static final String SDKADD="http://10.1.3.165:9990";
    private static final String USER1_1="32XBduKk48RJfoA2wJuQ7doaJimMymEmZLYYbYHyH5Eyi5Hs68";
    private static final String USER1_2="SogjzbsX6RsWwRKqdhTQeVTWQhy2SyNGfPy9LHEz6p3famV3wCe";
    private static final String USER3_3="SsUTN9RmWgrD8E48MuJY1pdLw4QDo7GJgK8fn8k7DFzpvG3pwqw";
    private static final String USER_COLLET="Soirv9ikykVHArKCdJqVNegxxqZWUj1g4ixFFYbBLMExy4zUTUe";


    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }
/*@Test
     public void  run()throws Exception{
    StoreTest storeTest=new StoreTest();
    int count=0;
        //while (true)
         {runTest();
        storeTest.runSDK3Test();
        // Thread.sleep(1000*5);
         count++;
         log.info("第"+count+"次测试");
         }

     }*/

    @Test
    public void runTest() throws Exception {

        TOKENTYPE="cx-"+Random(6);
        log.info("\n发行Token\n");
        String response = TestissueToken(TOKENTYPE);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第一次签名\n");
        Thread.sleep(1000*3);
        String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
        Thread.sleep(1000*3);
        JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
        String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第二次签名\n");
        String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
        JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
        String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
        log.info("\n第三次签名\n");
        TestSign(signHash2, PRIKEY3);
        Thread.sleep(1000*6);
        log.info("\n查询归集账号的余额\n");
        TestQuery(TOKENTYPE);
        log.info("\n转账\n");
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2);
        Thread.sleep(1000*5);
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2, USER1_1);
        Thread.sleep(1000 * 5);
        log.info("\n查询账号余额,用户 user1_2:40 \n");
        TestUserQuery(USER1_2,PRIKEY3);
//        log.info("\n核对私钥正确性\n");
//        CheckPrikey(PRIKEY1,PWD1);
//        log.info("\n核对私钥错误性");
//        CheckPrikey(PRIKEY1,PWD2);
//        CheckPrikey(PRIKEY1,"");
//        log.info("----------------------------------------------------");
//        CheckPrikey(PRIKEY3,PWD2);
//        CheckPrikey("",PWD1);
    }

    @Test
    /**
     * 测试回收操作-需要修改tokenType
     */
    public  void RecycleTest()throws Exception{
        RECYCLETYPE="cx-AZiE2f";
        log.info("回收1个token");
        TestRecycle();
        log.info("执行转账操作，冻结后会报错");
        TestUserTransfer(RECYCLETYPE,USER1_2,USER3_3);
        Thread.sleep(1000*10);
        log.info("查询零地址token余额.冻结前为1，冻结后为2");
        TestQueryZero();
    }
    /**
     * 创建3/3多签地址
     * @author chenxu
     * @version 1.0
     */
    @Test
    public void Create3_3User() {
        int M = 3;
        String SubAccountType = "Pubkey";
        String Format = "pem";
        Map<String, Object> map = new HashMap<>();
        List<String> PubkeysObjects = new ArrayList<String>();
        PubkeysObjects.add(PUBKEY1);
        PubkeysObjects.add(PUBKEY2);
        PubkeysObjects.add(PUBKEY3);
        JSONArray jsonArray = JSONArray.fromObject(PubkeysObjects);
        map.put("Args", jsonArray);
        map.put("SubAccountType", SubAccountType);
        map.put("M", M);
        map.put("Format", Format);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/genmultiaddress", map));


    }
    /**
     * 创建1/2多签地址
     * @author chenxu
     * @version 1.0
     */
    @Test
    public void Create1_2User(){
        int M =1;
        String PubKey1=PUBKEY1;
        String PubKey2=PUBKEY3;
        String SubAccountType = "Pubkey";
        String Format = "pem";
        Map<String, Object> map = new HashMap<>();
        List<String> PubkeysObjects = new ArrayList<String>();
        PubkeysObjects.add(PubKey1);
        PubkeysObjects.add(PubKey2);
        JSONArray jsonArray = JSONArray.fromObject(PubkeysObjects);
        map.put("Args", jsonArray);
        map.put("SubAccountType", SubAccountType);
        map.put("M", M);
        map.put("Format", Format);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/genmultiaddress", map));

    }
    /**
     * 创建单签无密码地址
     * @author chenxu
     * @version 1.0
     */
    @Test
    public void Create1_1User(){
        Map<String, Object> map = new HashMap<>();
        map.put("Pubkey", "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFaTA4d0pVQ1RHQWd4Yzg2ZzRQNVBUUncwSTZQMAo4MXdCZzJYWVc0Zkx1TElvZDJxR0NIR0N2dTFBWi9nQUVTRS9OT1Q2eDdMRFhVVjRPTE9Kdk5oUXBRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==");
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/genaddress", map));

    }

    /**
     * 查询用户余额
     * @param addr    用户地址
     * @param priKey  用户私钥
     */
    public void TestUserQuery(String addr,String priKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", addr);
        map.put("PriKey", priKey);
        map.put("tokentype", TOKENTYPE);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/balance", map));
    }

    /**
     * 查询归集地址余额
     * @param tokenType    币种类型
     */
    public void TestQuery(String tokenType) {

        //查询归集地址余额
        String MultiAddr = USER_COLLET;
        String Prikey = PRIKEY0;
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("PriKey", Prikey);
        map.put("tokentype",tokenType);

        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/balance", map));
    }

    /**
     * 使用3/3账户发行Token申请
     * @param TokenType   币种类型
     * @return
     */
    public String TestissueToken(String TokenType) {
        //发币
        String MultiAddr = USER3_3;
        String Amount = "200.111";
        String Data = "发币，" + TokenType + ",数量:" + Amount;
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/issuetoken", map);
        log.info(response);
        return response;
    }

    /**
     * 签名多签发行Token交易-不带密码
     * @param Tx     交易ID
     * @param Prikey  签名所用私钥
     * @return    返回未经过处理内容
     */
    public String TestSign(String Tx, String Prikey) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", Prikey);
        map.put("Tx", Tx);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        log.info(response);
        return response;

    } /**
     签名多签发行Token交易-带密码
     * @param Tx     交易ID
     * @param Prikey  签名所用私钥
     * @param Pwd  签名所用私钥密码
     * @return    返回未经过处理内容
     */

    public String TestSign(String Tx, String Prikey, String Pwd) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", Prikey);
        map.put("Pwd", Pwd);
        map.put("Tx", Tx);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        log.info(response);
        return response;

    }

    /**
     * 双方转账
     * @param TokenType 币种
     * @param FromAddr  发起方-归集地址
     * @param ToAddr    接收方
     */
    public void TestTransfer(String TokenType,String FromAddr,String ToAddr) {
        String PriKey = PRIKEY0;
        String Data = "使用归集账户私钥发起1/2多签转账-无密码";
        String Amount = "30.0";
        Map<String, Object> token = new HashMap<>();
        Map<String, String> AmountList = new HashMap<>();
        AmountList.put("TokenType", TokenType);
        AmountList.put("Amount", Amount);
        List<Map> AmountObjects = new ArrayList<Map>();
        AmountObjects.add(AmountList);
        token.put("ToAddr", ToAddr);
        token.put("AmountList", AmountObjects);
        List<Map> TokenObjects = new ArrayList<Map>();
        TokenObjects.add(token);
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", FromAddr);
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Token", TokenObjects);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer", map));

    }
    /**
     * 一方同时向多方转账
     * @param TokenType 币种
     * @param FromAddr  发起方-归集地址
     * @param ToAddr1    接收方
     * @param ToAddr2  第二个接收方
     */

    public void TestTransfer(String TokenType,String FromAddr,String ToAddr1,String ToAddr2) {
        String PriKey =PRIKEY0;
        String Data = "使用归集账户私钥发起多账户转账-无密码";
        String Amount = "10.0";
        Map<String, Object> token1 = new HashMap<>();
        Map<String,Object> token2 = new HashMap<>();
        Map<String, String> AmountList = new HashMap<>();
        AmountList.put("TokenType", TokenType);
        AmountList.put("Amount", Amount);
        List<Map> AmountObjects = new ArrayList<Map>();
        AmountObjects.add(AmountList);
        token1.put("ToAddr", ToAddr1);
        token1.put("AmountList", AmountObjects);
        token2.put("ToAddr", ToAddr2);
        token2.put("AmountList", AmountObjects);
        List<Map> TokenObjects = new ArrayList<Map>();
        TokenObjects.add(token1);
        TokenObjects.add(token2);
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", FromAddr);
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Token", TokenObjects);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer", map));

    }

    /**
     * 普通1/2地址向外转账
     * @param TokenType
     * @param FromAddr
     * @param ToAddr
     */
    public void TestUserTransfer(String TokenType,String FromAddr,String ToAddr){
        String PriKey = PRIKEY3;
        String Data = "使用1/2账户私钥发起1/2多签转账-无密码";
        String Amount = "2.0";
        Map<String, Object> token = new HashMap<>();
        Map<String, String> AmountList = new HashMap<>();
        AmountList.put("TokenType", TokenType);
        AmountList.put("Amount", Amount);
        List<Map> AmountObjects = new ArrayList<Map>();
        AmountObjects.add(AmountList);
        token.put("ToAddr", ToAddr);
        token.put("AmountList", AmountObjects);
        List<Map> TokenObjects = new ArrayList<Map>();
        TokenObjects.add(token);
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", FromAddr);
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Token", TokenObjects);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer", map));

    }


    /**
     * 用于生成随机数
     * @param length    随机数的长度
     * @return     返回由数字跟大小写字母组成的随机数
     */
    public static String Random(int length) {
        char[] str= new char[length];
        int i = 0;
        int num=3;//数字的个数
        while (i < length) {
            int f = (int) (Math.random() * num);
            if (f == 0)
                str[i] = (char) ('A' + Math.random() * 26);
            else if (f == 1)
                str[i] = (char) ('a' + Math.random() * 26);
            else
                str[i] = (char) ('0' + Math.random() * 10);
            i++;
        }
        String random_str = new String(str);
        return random_str;
    }

    /**
     * 核对私钥接口测试
     * @param PriKey  私钥
     * @param Pwd    密码
     */
    public void CheckPrikey(String PriKey ,String Pwd){
          Map<String,Object>map=new HashMap<>();
          map.put("PriKey",PriKey);
          map.put("Pwd",Pwd);
          log.info(PostTest.sendPostToJson(SDKADD+"/utxo/validatekey",map));
    }

    /**
     * 单签测试用
     * 初始化-发行token至归集地址
     * @return  返回发行token的tokenType币种类型
     * @throws Exception  由于使用了线程的休眠需要抛出异常
     */
    public  String SoloInit()throws Exception{
        String tokenType ="cx-"+Random(6);
        log.info("\n发行Token\n");
        String response = TestissueToken(tokenType);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第一次签名\n");
        Thread.sleep(1000*3);
        String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
        Thread.sleep(1000*3);
        JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
        String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第二次签名\n");
        String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
        JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
        String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
        log.info("\n第三次签名\n");
        TestSign(signHash2, PRIKEY3);
        Thread.sleep(1000*6);
        log.info("\n查询归集账号的余额\n");
        TestQuery(tokenType);
        return  tokenType;
    }

    @Test
    /**
     * 回收token测试
     */
    public void TestRecycle(){
        String multiAddr=USER1_2;
        String priKey=PRIKEY3;
        String tokenType=RECYCLETYPE;
        String amount="1";
        Map<String ,Object>map=new HashMap<>();
        map.put("MultiAddr",multiAddr);
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        log.info(PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycle",map));


    }
    @Test
    /**
     * 查询回收账户余额
     */
    public void TestQueryZero(){
        String tokenType=RECYCLETYPE;
        String param;
        Map<String ,Object>map=new HashMap<>();
        map.put("tokentype",tokenType);
        param= GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/utxo/balance/zero"+"?"+param));

    }
    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }

}
