package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
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

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest_33_12 {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
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

    //3/3 3次签名顺序1：不带密码--带密码--带密码
    public String signPro1(String tx)throws Exception{
        log.info("第一次使用不带密码的私钥PRIKEY1进行签名");
        String response1 = multiSign.Sign(tx, PRIKEY1);
        String Tx1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        log.info("第二次使用带密码111的私钥PRIKEY6进行签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY6,PWD6);
        String Tx13 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第三次使用带密码222的私钥PRIKEY7进行签名");
        String response3 = multiSign.Sign(Tx13, PRIKEY7,PWD7);
        return response3;
    }

    //3/3 3次签名顺序2：带密码--带密码--不带密码
    public String signPro2(String tx)throws Exception{
        log.info("第一次使用带密码222的私钥PRIKEY7进行签名");
        String response1 = multiSign.Sign(tx, PRIKEY7,PWD7);
        String Tx1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        log.info("第二次使用带密码111的私钥PRIKEY6进行签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY6,PWD6);
        String Tx13 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第三次使用不带密码的私钥PRIKEY1进行签名");
        String response3 = multiSign.Sign(Tx13, PRIKEY1);
        return response3;
    }

    //3/3 2次签名顺序1：不带密码PRIKEY1--带密码PRIKEY7
    public String SignPro3(String tx)throws Exception{
        log.info("第一次使用不带密码的私钥PRIKEY1进行签名");
        String response1 = multiSign.Sign(tx, PRIKEY1);
        String Tx1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        log.info("第二次使用带密码222的私钥PRIKEY7进行签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY7,PWD7);
        return response2;
    }

    //3/3 2次签名顺序1：带密码PRIKEY6--带密码PRIKEY7
    public String SignPro4(String tx)throws Exception{
        log.info("第一次使用带密码111的私钥PRIKEY6进行签名");
        String response1 = multiSign.Sign(tx, PRIKEY6,PWD6);
        String Tx1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        log.info("第二次使用带密码222的私钥PRIKEY7进行签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY7,PWD7);
        return response2;
    }


    //----------------以下为3/3多签账户发起token发行测试用例----------------//
    /***
     * 3/3多签(MULITADD3,1/6/7：无密码/有密码/有密码，发行申请不带私钥
     * 签名顺序1：不带密码--带密码--带密码
     * 签名顺序2：带密码--带密码--不带密码
     * 转账及签名：带密码--不带密码--带密码
     * 回收及签名：不带密码--带密码--带密码
     */
    @Test
    public  void Issue33AddrTest01()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 777;
        String issData = MULITADD3 + "不带私钥发行" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(9);
        int amount2 = 888;
        String issData2 = MULITADD3 + "不带私钥发行" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行申请不带私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueToken(MULITADD3, tokenType, String.valueOf(amount), issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = signPro1(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));




        log.info(issData2);
        //发行申请不带私钥，签名为：带密码->带密码->不带密码
        String response2 = multiSign.issueToken(MULITADD3, tokenType2, String.valueOf(amount2), issData2);
        assertThat(response2, containsString("200"));
        String Tx21 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        //签名流程2
        String response24 = signPro2(Tx21);
        assertThat(JSONObject.fromObject(response24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response24).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response24, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行后带密码私钥查询余额: "+tokenType2);
        String queryInfo2= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥,签名顺序3：不带密码--带密码
        int tf1=110;
        String transferData = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY6,PWD6, transferData, MULITADD3,list3);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");

        //签名流程3：不带密码+带密码
        String tfResponse24 = SignPro3(tfTx21);
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        Thread.sleep(SLEEPTIME);
        //查询余额时使用带密码私钥和不带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);
        ;
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));


        //锁定后回收3/3账户
        log.info("锁定token后回收3/3账户MULITADD3");
        multiSign.freezeToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD3, PRIKEY1, tokenType, String.valueOf(amount-tf1-tf1));
        String recTx21 = JSONObject.fromObject(recycleInfo).getJSONObject("Data").getString("Tx");
        //签名流程4：带密码+带密码
        String recResponse24 = SignPro4(recTx21);
        assertEquals("200",JSONObject.fromObject(recResponse24).getString("State"));
        assertThat(JSONObject.fromObject(recResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2-tf1));
        String recTx31 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程3：不带密码+带密码
        String recResponse = SignPro3(recTx31);
        assertEquals("200",JSONObject.fromObject(recResponse).getString("State"));
        assertThat(JSONObject.fromObject(recResponse).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

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
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo6= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType);

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
     * 3/3多签(MULITADD3,1/6/7：无密码/有密码/有密码，发行申请带无密码私钥和有密码私钥
     * 签名顺序4：带密码--带密码
     * 签名顺序3：不带密码--带密码
     * 转账及签名3：带密码--不带密码--带密码
     * 回收及签名4：不带密码--带密码--带密码
     */
    @Test
    public  void Issue33AddrTest02()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 777;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(9);
        int amount2 = 888;
        String issData2 = MULITADD3 + "带有密码私钥发行" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        log.info(issData2);
        //发行申请带有密码私钥，签名为：带密码->不带密码->带密码
        String response2 = multiSign.issueTokenCarryPri(MULITADD3,tokenType2,String.valueOf(amount2),PRIKEY6,PWD6,issData2);
        assertThat(response2, containsString("200"));
        String Tx21 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        //签名流程3
        String response24 = SignPro3(Tx21);
        assertThat(JSONObject.fromObject(response24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response24).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response24, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行后带密码私钥查询余额: "+tokenType2);
        String queryInfo2= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1=110;
        String transferData = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list3);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");

        //签名流程4：带密码+带密码
        String tfResponse24 = SignPro4(tfTx21);
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        Thread.sleep(SLEEPTIME);
        //查询余额时使用带密码私钥和不带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD3,PRIKEY7,PWD7,tokenType2);

        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));


        log.info("回收3/3账户MULITADD3前锁定token");
        multiSign.freezeToken(PRIKEY1,tokenType);
        multiSign.freezeToken(PRIKEY1,tokenType2);
        Thread.sleep(SLEEPTIME);


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD3, PRIKEY1, tokenType, String.valueOf(amount-tf1-tf1));
        String recTx21 = JSONObject.fromObject(recycleInfo).getJSONObject("Data").getString("Tx");
        //签名流程4：带密码+带密码
        String recResponse24 = SignPro4(recTx21);
        assertEquals("200",JSONObject.fromObject(recResponse24).getString("State"));
        assertThat(JSONObject.fromObject(recResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2-tf1));
        String recTx31 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程3：不带密码+带密码
        String recResponse = SignPro3(recTx31);
        assertEquals("200",JSONObject.fromObject(recResponse).getString("State"));
        assertThat(JSONObject.fromObject(recResponse).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

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
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo6= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        String queryInfo7= multiSign.Balance(MULITADD3,PRIKEY6,PWD6,tokenType);

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
     * 3/3多签(MULITADD3,1/6/7：无密码/有密码/有密码，发行给其他账户MULTIADD7申请不带私钥
     * 签名顺序1：不带密码--带密码--带密码
     * 签名顺序2：带密码--带密码--不带密码
     * 转账及签名：带密码--不带密码--带密码
     * 回收及签名：不带密码--带密码--带密码
     */
    @Test
    public  void Issue33AddrTest03()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 777;
        String issData = MULITADD3 + "不带私钥发行给MULTIADD7" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(9);
        int amount2 = 888;
        String issData2 = MULITADD3 + "不带私钥发行发行给MULTIADD7" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行申请不带私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueToken(MULITADD3,MULITADD7, tokenType, String.valueOf(amount),"","", issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = signPro1(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD7,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));




        log.info(issData2);
        //发行申请不带私钥，签名为：带密码->带密码->不带密码
        String response2 = multiSign.issueToken(MULITADD3,MULITADD7, tokenType2, String.valueOf(amount2),"","", issData2);
        assertThat(response2, containsString("200"));
        String Tx21 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        //签名流程2
        String response24 = signPro2(Tx21);
        assertThat(JSONObject.fromObject(response24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD7,JSONObject.fromObject(response24).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response24, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行后带密码私钥查询余额: "+tokenType2);
        String queryInfo2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥,签名顺序3：不带密码--带密码
        int tf1=110;
        String transferData = "MULITADD7向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY6,PWD6, transferData, MULITADD7,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        Thread.sleep(SLEEPTIME);
        //查询余额时使用带密码私钥和不带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        ;
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));


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

        Thread.sleep(SLEEPTIME);
        log.info("查询余额判断回收成功与否");
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
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
     * 3/3多签(MULITADD3,1/6/7发行给其他账户：无密码/有密码/有密码，发行申请带无密码私钥和有密码私钥
     * 签名顺序4：带密码--带密码
     * 签名顺序3：不带密码--带密码
     * 转账及签名3：带密码--不带密码--带密码
     * 回收及签名4：不带密码--带密码--带密码
     */
    @Test
    public  void Issue33AddrTest04()throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 777;
        String issData = MULITADD3 + "带无密码私钥发行给其他账户MULTIADD7" + tokenType + " token，数量为：" + amount;

        String tokenType2 = "CX-" + UtilsClass.Random(9);
        int amount2 = 888;
        String issData2 = MULITADD3 + "带有密码私钥发行给其他账户MULTIADD7" + tokenType2 + " token，数量为：" + amount2;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueToken(MULITADD3, MULITADD7,tokenType, String.valueOf(amount),PRIKEY1,"", issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD7,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        log.info(issData2);
        //发行申请带有密码私钥，签名为：带密码->不带密码->带密码
        String response2 = multiSign.issueToken(MULITADD3,MULITADD7,tokenType2,String.valueOf(amount2),PRIKEY6,PWD6,issData2);
        assertThat(response2, containsString("200"));
        String Tx21 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        //签名流程3
        String response24 = SignPro3(Tx21);
        assertThat(JSONObject.fromObject(response24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD7,JSONObject.fromObject(response24).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response24, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("发行后带密码私钥查询余额: "+tokenType2);
        String queryInfo2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1=110;
        String transferData = "MULITADD7向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,String.valueOf(tf1),list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf1),list2);
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData, MULITADD7,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        Thread.sleep(SLEEPTIME);
        //查询余额时使用带密码私钥和不带密码私钥
        log.info("查询余额判断转账是否成功");
        String queryInfoA1= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfoM1= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfoM2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfoCM1= multiSign.Balance(MULITADD7,PRIKEY1,tokenType);
        String queryInfoCM2= multiSign.Balance(MULITADD7,PRIKEY6,PWD6,tokenType2);

        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoA1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(queryInfoM2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf1),JSONObject.fromObject(queryInfoCM1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2-tf1),JSONObject.fromObject(queryInfoCM2).getJSONObject("Data").getString("Total"));


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD7, PRIKEY1,tokenType, String.valueOf(amount-tf1-tf1));
        String recycleInfo2 = multiSign.Recycle(MULITADD7, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2-tf1));
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
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
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


    @Test
    public void TC992_TranferFromOneInput1() throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 222;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=150;
        int tf2=160;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));



        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = SignPro4(tfTx21);
        //Thread.sleep(SLEEPTIME);
        //第二笔签名流程4：带密码+带密码
        String tfResponse25 = SignPro4(tfTx22);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("success",JSONObject.fromObject(tfResponse25).getString("Message"));

        Thread.sleep(SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));//因为花费同一笔input

        log.info("查询余额判断回收成功与否");
        String query1= soloSign.Balance(PRIKEY1,tokenType);
        String query2= soloSign.Balance(PRIKEY2,tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

    }

    //第一笔转账交易签名后有上链等待时间
    @Test
    public void TC992_TranferFromOneInput2() throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 222;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=150;
        int tf2=160;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));



        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = SignPro4(tfTx21);
        Thread.sleep(SLEEPTIME);
        String tfResponse25 = SignPro4(tfTx22);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


        //第二笔签名流程4：带密码+带密码 应该会提示fail
        //String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("mutli transfer failed!",JSONObject.fromObject(tfResponse25).getString("Message"));

        Thread.sleep(SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        //assertEquals("404",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));//因为花费同一笔input，第二笔交易不会上链

        log.info("查询余额判断回收成功与否");
        String query1= soloSign.Balance(PRIKEY1,tokenType);
        String query2= soloSign.Balance(PRIKEY2,tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));


    }


    //3/3多签转账两个并发-总转账超出余额范围
    //@Test
    public void TC991_TwoTranferFromDiffInput1() throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 222;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        //先向MULITADD4转一半的发行数量
        int tf = 111;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = SignPro4(tfTx);
        Thread.sleep(SLEEPTIME);


        //转账发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回80给MULITADD3

        int tfA = 80;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tf));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,transferData, MULITADD4,list02);
        Thread.sleep(SLEEPTIME);


        //转账发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=90;
        int tf2=100;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));



        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = SignPro4(tfTx21);
        //Thread.sleep(SLEEPTIME);
        String tfResponse25 = SignPro4(tfTx22);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


        assertEquals("success",JSONObject.fromObject(tfResponse25).getString("Message"));

        Thread.sleep(SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        //assertEquals("404",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));//因为花费同一笔input，第二笔交易不会上链

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }


    //3/3多签转账两个并发（转出账户至少有两个input）-总转账在余额范围内
    //@Test
    public void TC991_TwoTranferFromDiffInput2() throws Exception{
        String tokenType = "CX-" + UtilsClass.Random(9);
        int amount = 222;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));


        Thread.sleep(SLEEPTIME);

        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        //先向MULITADD4转一半的发行数量
        int tf = 111;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = SignPro4(tfTx);
        Thread.sleep(SLEEPTIME);


        //转账发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回80给MULITADD3

        int tfA = 80;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,transferData, MULITADD4,list02);
        Thread.sleep(SLEEPTIME);


        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=70;
        int tf2=80;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));



        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = SignPro4(tfTx21);
        //Thread.sleep(SLEEPTIME);
        String tfResponse25 = SignPro4(tfTx22);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("success",JSONObject.fromObject(tfResponse25).getString("Message"));

        Thread.sleep(SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }



    //----------------以下为1/2多签账户发起token发行测试用例----------------//

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
        String transferData = "MULITADD7向ADDRESS1转账"+tf1+"*"+tokenType+";向MULITADD4转账"+tf1+"*"+tokenType+","+tf1+"*"+tokenType2;
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

        log.info("回收1/2账户MULITADD7前锁定token");
        multiSign.freezeToken(PRIKEY1,tokenType);
        multiSign.freezeToken(PRIKEY1,tokenType2);
        Thread.sleep(SLEEPTIME);

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

        Thread.sleep(SLEEPTIME);
        log.info("查询余额判断回收成功与否");
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
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
        Thread.sleep(SLEEPTIME);

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
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
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

        Thread.sleep(SLEEPTIME);

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
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response14).getString("State"));

        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2));
        assertThat(recycleInfo2, containsString("200"));
        String Tx12 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程4
        String response15 = SignPro3(Tx12);
        assertThat(JSONObject.fromObject(response15).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response15).getString("State"));

        Thread.sleep(SLEEPTIME);
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

        Thread.sleep(SLEEPTIME);

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
        String response14 = SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response14).getString("State"));

        String recycleInfo2 = multiSign.Recycle(MULITADD3, PRIKEY6,PWD6,tokenType2, String.valueOf(amount2));
        assertThat(recycleInfo2, containsString("200"));
        String Tx12 = JSONObject.fromObject(recycleInfo2).getJSONObject("Data").getString("Tx");
        //签名流程3
        String response15 = SignPro3(Tx12);
        assertThat(JSONObject.fromObject(response15).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals("200",JSONObject.fromObject(response15).getString("State"));

        Thread.sleep(SLEEPTIME);
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

        Thread.sleep(SLEEPTIME);

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
        Thread.sleep(SLEEPTIME);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, String.valueOf(amount));
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));

        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY5,tokenType2, String.valueOf(amount2));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));

        Thread.sleep(SLEEPTIME);
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
