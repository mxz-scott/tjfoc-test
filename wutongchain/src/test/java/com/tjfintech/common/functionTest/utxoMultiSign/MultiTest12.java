package com.tjfintech.common.functionTest.utxoMultiSign;


import com.google.gson.JsonObject;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
//import com.tjfintech.common.JavaStore;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest12 {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    MultiTest33 multiTest33 =new MultiTest33();
    private static String tokenType;
    private static String tokenType2;

    //@Test
    @Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();

            bReg=true;
        }
    }


    /***
     * 1/2多签(MULITADD7,带密码和不带密码组成的账户)发行给自己,发行时不带私钥，使用无密码私钥和有密码私钥签名，
     * 使用带密码私钥转账，
     * 使用带密码和不带密码私钥查询余额
     * 使用带密码私钥及不带密码私钥回收
     */
    @Test
    public  void Issue12AddrTest01()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(8);
        int amount = 555;
        String issData = MULITADD7 + "不带私钥发行" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(8);
        int amount2 = 666;
        String issData2 = MULITADD7 + "不带私钥发行" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行时不带私钥,签名使用带密码私钥
        String response11 = multiSign.issueToken(MULITADD7,"", tokenType, String.valueOf(amount),"","", issData);
        assertThat(response11, containsString("200"));
        String Tx1 = JSONObject.fromObject(response11).getJSONObject("Data").getString("Tx");
        log.info("发行使用PRIKEY6-有密码私钥签名");
        String response12 = multiSign.Sign(Tx1, PRIKEY6,PWD6);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response12, containsString("200"));
        assertEquals(MULITADD7,JSONObject.fromObject(response12).getJSONObject("Data").getString("CollectAddr"));


        log.info(issData2);
        //发行时不带私钥，签名使用带密码私钥签名
        String response21 = multiSign.issueToken(MULITADD7, "",tokenType2, String.valueOf(amount2),"","", issData2);
        assertThat(response21, containsString("200"));
        String Tx21 = JSONObject.fromObject(response21).getJSONObject("Data").getString("Tx");
        log.info("发行使用PRIKEY1-无密码私钥签名");
        String response22 = multiSign.Sign(Tx21, PRIKEY1);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response22, containsString("200"));
        assertEquals(MULITADD7,JSONObject.fromObject(response22).getJSONObject("Data").getString("CollectAddr"));

        Thread.sleep(SLEEPTIME/2);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType+","+tokenType2);
        String queryInfo= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行"+tokenType2+"后查询余额");
        String queryInfo2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥
        int tf1=10;
        String transferData = "MULITADD7向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY6,PWD6, transferData, MULITADD7,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        Thread.sleep(SLEEPTIME/2);
        //查询余额时使用带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD7,PRIKEY1,tokenType2);
        ;
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));

        log.info("回收1/2账户MULITADD7前锁定token");
        multiSign.freezeToken(PRIKEY1,tokenType);
        multiSign.freezeToken(PRIKEY1,tokenType2);
        Thread.sleep(SLEEPTIME/2);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD7, PRIKEY1, tokenType, String.valueOf(amount-tf1-tf1));
        String recycleInfo2 = multiSign.Recycle(MULITADD7, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2-tf1));
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, String.valueOf(tf1));
        String recycleInfo4 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, String.valueOf(tf1));
        String recycleInfo5 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, String.valueOf(tf1));

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        Thread.sleep(SLEEPTIME/2);
        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo6= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo7).getJSONObject("Data").getString("Total"));


        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));
    }

    /***
     * 1/2多签(MULITADD7,带密码和不带密码组成的账户)发行给自己,发行时带无和有密码私钥，无需后续签名，
     * 使用不带密码私钥转账，
     * 使用带密码和不带密码私钥查询余额
     * 使用带密码私钥及不带密码私钥回收
     */
    @Test
    public  void Issue12AddrTest02()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(8);
        int amount = 555;
        String issData = MULITADD7 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(8);
        int amount2 = 666;
        String issData2 = MULITADD7 + "带有密码私钥发行" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行时带无密码私钥,一步完成
        String response1 = multiSign.issueToken(MULITADD7, "",tokenType, String.valueOf(amount), PRIKEY1,"",issData);
        assertThat(response1, containsString("200"));
        assertEquals(MULITADD7,JSONObject.fromObject(response1).getJSONObject("Data").getString("CollectAddr"));

        //发行时带密码私钥,一步完成
        log.info(issData2);
        String response2 = multiSign.issueToken(MULITADD7,"",tokenType2, String.valueOf(amount2), PRIKEY6,PWD6,issData);
        assertThat(response2, containsString("200"));
        assertEquals(MULITADD7,JSONObject.fromObject(response2).getJSONObject("Data").getString("CollectAddr"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType+","+tokenType2);
        String queryInfo= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行"+tokenType2+"后查询余额");
        String queryInfo2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥
        int tf1=10;
        String transferData = "MULITADD7向ADDRESS1转账10个"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY6,PWD6, transferData, MULITADD7,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        Thread.sleep(SLEEPTIME);
        //查询余额时使用带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD7,PRIKEY1,tokenType2);
        ;
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));


        log.info("回收1/2账户MULITADD7前锁定Token");
        multiSign.freezeToken(PRIKEY1,tokenType);
        multiSign.freezeToken(PRIKEY1,tokenType2);
        Thread.sleep(SLEEPTIME/2);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD7, PRIKEY1, tokenType, String.valueOf(amount-tf1-tf1));
        String recycleInfo2 = multiSign.Recycle(MULITADD7, PRIKEY6, PWD6,tokenType2, String.valueOf(amount2-tf1));
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, String.valueOf(tf1));
        String recycleInfo4 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, String.valueOf(tf1));
        String recycleInfo5 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, String.valueOf(tf1));

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        Thread.sleep(SLEEPTIME);
        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo6= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo7).getJSONObject("Data").getString("Total"));


        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));


    }

    /***
     * 多签发行给其他3/3账户MULITADD3
     * 1/2多签(MULITADD7,带密码和不带密码组成的账户)发行给MULTIADD3,发行时不带私钥，使用无密码私钥和有密码私钥签名，
     * 使用带密码私钥转账，
     * 使用带密码和不带密码私钥查询余额
     * 使用带密码私钥及不带密码私钥回收
     */
    @Test
    public  void Issue12AddrTest03()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(8);
        int amount = 555;
        String issData = MULITADD7 + "不带私钥发行给MULITADD3" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(8);
        int amount2 = 666;
        String issData2 = MULITADD7 + "不带私钥发行给MULITADD3" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行时不带私钥,签名使用带密码私钥
        String response11 = multiSign.issueToken(MULITADD7,MULITADD3, tokenType, String.valueOf(amount),"","", issData);
        assertThat(response11, containsString("200"));
        String Tx1 = JSONObject.fromObject(response11).getJSONObject("Data").getString("Tx");
        log.info("发行使用PRIKEY6-有密码私钥签名");
        String response12 = multiSign.Sign(Tx1, PRIKEY6,PWD6);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response12, containsString("200"));
        assertEquals(MULITADD3,JSONObject.fromObject(response12).getJSONObject("Data").getString("CollectAddr"));


        log.info(issData2);
        //发行时不带私钥，签名使用带密码私钥签名
        String response21 = multiSign.issueToken(MULITADD7, MULITADD3,tokenType2, String.valueOf(amount2),"","", issData2);
        assertThat(response21, containsString("200"));
        String Tx21 = JSONObject.fromObject(response21).getJSONObject("Data").getString("Tx");
        log.info("发行使用PRIKEY1-无密码私钥签名");
        String response22 = multiSign.Sign(Tx21, PRIKEY1);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response22, containsString("200"));
        assertEquals(MULITADD3,JSONObject.fromObject(response22).getJSONObject("Data").getString("CollectAddr"));

        Thread.sleep(SLEEPTIME/2);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType+","+tokenType2);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行"+tokenType2+"后查询余额");
        String queryInfo2= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));


        log.info("发行后查询MULITADD7余额(不会有tokenType/tokenType2余额)");
        String queryInfo3= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String queryInfo4= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD3, PRIKEY1, tokenType, String.valueOf(amount));
        assertThat(recycleInfo, containsString("200"));
        String Tx11 = JSONObject.fromObject(recycleInfo).getJSONObject("Data").getString("Tx");
        //签名流程3
        String response14 = multiTest33.SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response14).getString("State"));

        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2));
        assertThat(recycleInfo2, containsString("200"));
        String Tx12 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程4
        String response15 = multiTest33.SignPro3(Tx12);
        assertThat(JSONObject.fromObject(response15).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response15).getString("State"));

        Thread.sleep(SLEEPTIME/2);
        log.info("查询余额判断回收成功与否");

        String queryInfo6= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);


        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo7).getJSONObject("Data").getString("Total"));


        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));
    }

    /***
     * 多签发行给其他3/3账户MULITADD3
     * 1/2多签(MULITADD7,带密码和不带密码组成的账户)发行给MULTIADD3,发行时带无和有密码私钥，无需后续签名
     * 使用带密码私钥转账，
     * 使用带密码和不带密码私钥查询余额
     * 使用带密码私钥及不带密码私钥回收
     */
    @Test
    public  void Issue12AddrTest04()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(8);
        int amount = 686;
        String issData = MULITADD7 + "不带私钥发行给MULITADD3" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(8);
        int amount2 = 757;
        String issData2 = MULITADD7 + "不带私钥发行给MULITADD3" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行时带无密码私钥PRIKEY1,不需后续签名
        String response11 = multiSign.issueToken(MULITADD7,MULITADD3, tokenType, String.valueOf(amount),PRIKEY1,"", issData);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response11, containsString("200"));
        assertEquals(MULITADD3,JSONObject.fromObject(response11).getJSONObject("Data").getString("CollectAddr"));


        log.info(issData2);
        //发行时带有密码私钥PRIKEY6,不需后续签名
        String response21 = multiSign.issueToken(MULITADD7, MULITADD3,tokenType2, String.valueOf(amount2),PRIKEY6,PWD6, issData2);
        assertThat(response21, containsString("200"));
        assertEquals(MULITADD3,JSONObject.fromObject(response21).getJSONObject("Data").getString("CollectAddr"));

        Thread.sleep(SLEEPTIME/2);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType+","+tokenType2);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行"+tokenType2+"后查询余额");
        String queryInfo2= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));


        log.info("发行后查询MULITADD7余额(不会有tokenType/tokenType2余额)");
        String queryInfo3= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String queryInfo4= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD3, PRIKEY1, tokenType, String.valueOf(amount));
        assertThat(recycleInfo, containsString("200"));
        String Tx11 = JSONObject.fromObject(recycleInfo).getJSONObject("Data").getString("Tx");
        //签名流程4
        String response14 = multiTest33.SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response14).getString("State"));

        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2));
        assertThat(recycleInfo2, containsString("200"));
        String Tx12 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程3
        String response15 = multiTest33.SignPro3(Tx12);
        assertThat(JSONObject.fromObject(response15).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response15).getString("State"));

        Thread.sleep(SLEEPTIME/2);
        log.info("查询余额判断回收成功与否");

        String queryInfo6= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);


        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo7).getJSONObject("Data").getString("Total"));


        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));
    }
    /***
     * 多签发行给其他1/2账户IMPPUTIONADD
     * 1/2多签(MULITADD7,带密码和不带密码组成的账户)发行给MULTIADD3,发行时带无和有密码私钥，无需后续签名
     * 使用带密码私钥转账，
     * 使用带密码和不带密码私钥查询余额
     * 使用带密码私钥及不带密码私钥回收
     */
    @Test
    public  void Issue12AddrTest05()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(8);
        int amount = 686;
        String issData = MULITADD7 + "不带私钥发行给IMPPUTIONADD" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(8);
        int amount2 = 757;
        String issData2 = MULITADD7 + "不带私钥发行给IMPPUTIONADD" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行时带无密码私钥PRIKEY1,不需后续签名
        String response11 = multiSign.issueToken(MULITADD7,IMPPUTIONADD, tokenType, String.valueOf(amount),PRIKEY1,"", issData);
        //检查发行SDK返回结果中的正常响应及归集地址信息
        assertThat(response11, containsString("200"));
        assertEquals(IMPPUTIONADD,JSONObject.fromObject(response11).getJSONObject("Data").getString("CollectAddr"));


        log.info(issData2);
        //发行时带有密码私钥PRIKEY6,不需后续签名
        String response21 = multiSign.issueToken(MULITADD7, IMPPUTIONADD,tokenType2, String.valueOf(amount2),PRIKEY6,PWD6, issData2);
        assertThat(response21, containsString("200"));
        assertEquals(IMPPUTIONADD,JSONObject.fromObject(response21).getJSONObject("Data").getString("CollectAddr"));

        Thread.sleep(SLEEPTIME/2);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType+","+tokenType2);
        String queryInfo= multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行"+tokenType2+"后查询余额");
        String queryInfo2= multiSign.Balance(IMPPUTIONADD,PRIKEY5,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));


        log.info("发行后查询MULITADD7余额(不会有tokenType/tokenType2余额)");
        String queryInfo3= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String queryInfo4= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));

        log.info("回收1/2账户IMPPUTIONADD前锁定Token");
        multiSign.freezeToken(PRIKEY1,tokenType);
        multiSign.freezeToken(PRIKEY1,tokenType2);
        Thread.sleep(SLEEPTIME/2);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, String.valueOf(amount));
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));

        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY5,tokenType2, String.valueOf(amount2));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));

        Thread.sleep(SLEEPTIME/2);
        log.info("查询余额判断回收成功与否");

        String queryInfo6= multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenType);
        String queryInfo7= multiSign.Balance(IMPPUTIONADD,PRIKEY5,tokenType2);


        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo7).getJSONObject("Data").getString("Total"));


        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));
    }

}
