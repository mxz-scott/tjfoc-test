package com.tjfintech.common.functionTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
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

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest33 {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
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
        Thread.sleep(SLEEPTIME/2);

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
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
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
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));


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
        Thread.sleep(SLEEPTIME/2);

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
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
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
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));


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
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));


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
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));

        String queryZero1 =multiSign.QueryZero(tokenType);
        String queryZero2 =multiSign.QueryZero(tokenType2);
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryZero1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount2),JSONObject.fromObject(queryZero2).getJSONObject("Data").getString("Total"));
    }

}
