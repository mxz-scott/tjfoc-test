package com.tjfintech.common.functionTest.tokenModuleTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    Token tokenModule = testBuilder.getToken();


    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        SDKADD = TOKENADD;
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("多签发行两种token");
        //两次发行之前不可以有sleep时间
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount2);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        log.info("查询归集地址中两种token余额");

        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    @Test
    public void multiToSoloIssue() throws Exception {

        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("多签发行两种token");
        //两次发行之前不可以有sleep时间
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenAccount1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount2);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        log.info("查询归集地址中两种token余额");

        String response1 = tokenModule.tokenGetBalance(tokenAccount1, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    /**
     *  测试最大发行量
     *
     */
    @Test
    public void TC001_TestMaxValue()throws Exception {

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1844674407";
        }else {
            actualAmount1 = "18446744073709";
        }
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,actualAmount1);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        log.info("查询归集地址中token余额");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

    }




    /**
     * 多签发行检查发行地址注册、未注册时,归集地址注册、未注册时的发行结果
     * 发行给其他账户
     * @throws Exception
     */
    @Test
    public void TC1280_checkMultiIssueAddr()throws Exception {
        //先前已经注册发行及归集地址tokenMultiAddr1及tokenMultiAddr2,确认发行无异常
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        //删除发行地址，保留归集地址
        String response2 = tokenModule.tokenDelMintAddr(tokenMultiAddr3);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(false,response3.contains(tokenType));

        //删除发行地址和归集地址
        String response41=tokenModule.tokenDelCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response42 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response42).getString("state"));
        assertEquals(false,response42.contains(tokenType));

        //重新添加发行地址，归集地址已删除
        String response5=tokenModule.tokenAddMintAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response53 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response53).getString("state"));
        assertEquals(false,response53.contains(tokenType));

        //重新添加发行地址和归集地址
        String response6=tokenModule.tokenAddCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response62 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("data").getString(tokenType));
    }


    /**
     * 多签发行token。接收地址不为本身。指定其他多签地址
     * @throws Exception
     */
     @Test
     public void TC994_issueToOther()throws Exception {
         tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
     }




    /**
     * Tc03多签正常流程：转账 回收
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        // tokenMultiAddr1 + "向" + tokenMultiAddr3 + "转账10个" + tokenType;
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10");
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("查询"+ tokenMultiAddr1 +"和" + tokenMultiAddr3 + "余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //回收
        log.info(tokenMultiAddr1 +"和" + tokenMultiAddr3 + "回收" + tokenType);
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3,tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));
    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC03_PrecisionTest() throws Exception {
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,issueAmount1);
        //tokenMultiAddr1 + "向" + tokenMultiAddr3 + "转账"+ issueAmount1 + " * " + tokenType;
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "1000.1234567891";
        }else {
            amount1 = "1000.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //tokenMultiAddr1 +"和" + tokenMultiAddr3 + "回收" + tokenType);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));

        String queryInfo5 = tokenModule.tokenGetDestroyBalance();
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));

    }



    /**
     * Tc024冻结后转账 转冻结token和未冻结token
     * 冻结后回收
     * 解除冻结 转账 连续转账两次
     * 一转多
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //增加锁定步骤后进行转账
        log.info("锁定待转账Token: " + tokenType);
        String resp = tokenModule.tokenFreezeToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");


        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        log.info("冻结后转账，分别转冻结token和未冻结token");
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2); //同时转账锁定和不锁定的两种token
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(true,transferInfo.contains("toketype(" + tokenType + ") has been freezed!"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));


        log.info("冻结token后回收冻结token和未冻结token");
        String desInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType,"10");
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        String desInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType2,"10");
        assertEquals("200",JSONObject.fromObject(desInfo2).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));

        //查询回收账户冻结和未冻结token余额
        queryInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));


        log.info("解除锁定待转账Token: " + tokenType);
        String resp1 = tokenModule.tokenRecoverToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("查询归集地址中两种token余额");
        //当前余额 tokenType 990.123456  tokenType2 980.876543
        list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        list2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10",list);
        List<Map>list3 = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10",list2);
        List<Map>list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list3);
        List<Map>list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list4);//填写重复的转账内容
        List<Map>list6 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list5);

        //转出tokenType * 30 tokenType2 * 30
        transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list6);//不同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......"); //UTXO关系，两笔交易之间需要休眠

        //转出tokenType * 30 tokenType2 * 10
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list4);//不同币种
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");


        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "930.1234567891";
            amount2 = "950.8765432123";
        }else {
            amount1 = "930.123456";
            amount2 = "950.876543";
        }

        log.info("查询余额判断转账是否成功");
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4 = tokenModule.tokenGetBalance(tokenAccount1,"");

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));

        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));//查询tokenMultiAddr1 tokenType
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));//查询tokenMultiAddr1 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));//查询tokenMultiAddr2 tokenType
        assertEquals("30",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));//查询tokenMultiAddr2 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));//查询tokenMultiAddr3 tokenType
        assertEquals(false,queryInfo3.contains(tokenType2));//查询tokenMultiAddr3 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType));//tokenAccount1 tokenType
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType2));//tokenAccount1 tokenType2



        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "30");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "20");
        String recycleInfo6 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType2, "10");
        String recycleInfo7 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");


        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo7).getString("state"));


        log.info("查询余额判断回收成功与否");
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        queryInfo4 = tokenModule.tokenGetBalance(tokenAccount1,"");

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));

        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        log.info(tokenType2);
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));

    }

    /**
     *TC19归集地址向分别两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{

         log.info("查询归集地址中两种token余额");
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));

         List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
         List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list);
         List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10",list);

         String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
         assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

         sleepAndSaveInfo(SLEEPTIME,"transfer waiting......"); //UTXO关系，两笔交易之间需要休眠

         String transferInfo2= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

         sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

         log.info("查询余额判断转账是否成功");
         String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
         String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
         assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
         assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

         String amount1, amount2;

         if (UtilsClass.PRECISION == 10) {
             amount1 = "970.1234567891";
             amount2 = "990.8765432123";
         }else {
             amount1 = "970.123456";
             amount2 = "990.876543";
         }
         queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
         assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
         assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

         log.info("回收Token");
         String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
         String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
         String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "20");
         String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
         String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");
         sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

         assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

         log.info("查询余额判断回收成功与否");
         String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
         String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
         String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
         assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
         assertEquals(false,queryInfo3.contains(tokenType2));
         assertEquals(false,queryInfo3.contains(tokenType));
         assertEquals(false,queryInfo4.contains(tokenType2));
         assertEquals(false,queryInfo4.contains(tokenType));
         assertEquals(false,queryInfo5.contains(tokenType2));
         assertEquals(false,queryInfo5.contains(tokenType));


         String zeroInfo = tokenModule.tokenGetDestroyBalance();
         assertEquals(actualAmount1,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
         assertEquals(actualAmount2,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));

     }


    /**
     *TC31归集地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{

        List<Map>list=utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>list2=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        List<Map>list3=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list);

        //tokenMultiAddr1 +"向" + tokenAccount1 tokenMultiAddr1 转账tokenType tokenType2
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠


        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "970.1234567891";
            amount2 = "990.8765432123";
        }else {
            amount1 = "970.123456";
            amount2 = "990.876543";
        }
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "10");
        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }


    /**
     *TC32归集地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC32_transferSolo()throws  Exception{
        List<Map>list=utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>list2=utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list);
        List<Map>list3=utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);

        //tokenMultiAddr1 +"向" + tokenAccount1 tokenMultiAddr1 转账tokenType tokenType2
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠


        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "970.1234567891";
            amount2 = "990.8765432123";
        }else {
            amount1 = "970.123456";
            amount2 = "990.876543";
        }
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType2, "10");
        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     *TC35被转账多签地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC35_MultiToSoloMulti() throws Exception {

        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //归集地址向一个多签地址转两种token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("锁定待回收Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        log.info("查询余额判断回收成功与否");
        String queryInfo6= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     *TC33被转多签地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC33_MultiToMulti() throws Exception {

        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //归集地址向一个多签地址转两种token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("冻结待回收Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        log.info("解除冻结的Token: "+tokenType);
        String recoverInfo = tokenModule.tokenRecoverToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr4, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        log.info("查询余额判断回收成功与否");
        String queryInfo6= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     *TC34被转多签地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-锁定token-回收-查询
     * @throws Exception
     */
    @Test
    public void TC34_MultiToSolo() throws Exception {
        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //归集地址向一个多签地址转两种token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("冻结待回收Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        log.info("解除冻结的Token: "+tokenType);
        String recoverInfo = tokenModule.tokenRecoverToken(tokenType);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        log.info("查询余额判断回收成功与否");
        String queryInfo6= tokenModule.tokenGetBalance(tokenAccount2,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     * 多签账户转账给自己 无法转给自己
     * @throws Exception
     */
    @Test
    public void TransferToSelf() throws Exception {
        String transferInfo= commonFunc.tokenModule_TransferToken(tokenMultiAddr1, tokenMultiAddr1,tokenType,"10");
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
    }


    @Test
    public void multi33AccountDoubleSpend_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr2;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi33AccountDoubleSpend_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }


    @Test
    public void multi12AccountDoubleSpend_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr1;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi12AccountDoubleSpend_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


       issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi12AccountDoubleSpendSameAcc_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);

        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(sAmount - trfAmount1)),containsString(String.valueOf(sAmount - trfAmount2))));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(trfAmount1)),containsString(String.valueOf(trfAmount2))));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 50.123;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(sAmount - trfAmount1 - desAmount)),
                        containsString(String.valueOf(sAmount - trfAmount2 - desAmount))));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi33Account_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr2;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi33Account_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr2;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }


    @Test
    public void multi12Account_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr1;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    //1/2账户发行给3/3多签账户发行给
    //3/3账户转账给单签账户和1/2账户
    //单签账户转给其他单签账户
    //单签账户回收
    @Test
    public void multi12Account_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 1000.253;
        double trfAmount2 = 689.333;
        double trfAmount3 = 500.123456;


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        from = tokenAccount1;
        to = tokenAccount3;
        transferAmount = String.valueOf(trfAmount3);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);

        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        queryBalance = tokenModule.tokenGetBalance(from,desToken);
        assertEquals(get6(trfAmount1 - trfAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to,desToken);
        assertEquals(String.valueOf(trfAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        //执行回收
        desAddr = tokenAccount1;
        double desAmount2 = 100.123252;
        desToken = issueToken;
        desAmountStr = String.valueOf(desAmount2);
        destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);
        desAddr = tokenAccount3;
        double desAmount3 = 100.123252;
        desToken = issueToken;
        desAmountStr = String.valueOf(desAmount2);
        destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount + desAmount2 + desAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1,desToken);
        assertEquals(get6(trfAmount1 - trfAmount3 - desAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        queryBalance = tokenModule.tokenGetBalance(tokenAccount3,desToken);
        assertEquals(get6(trfAmount3 - desAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void transfer10Addr()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"10",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"10",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list4);
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list5);
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list6);
        List<Map> list8 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list7);
        List<Map> list9 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list8);
        List<Map> list10 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list9);
        List<Map> list11 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list10);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list11);
        assertEquals(true,transferInfo.contains("Transfer list cannot be more than 10"));

        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");


        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType).contains("950."));
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType2).contains("950."));


    }
    @Test
    public void destory10Addr()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"10",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"10",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list4);
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list5);
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list6);
        List<Map> list8 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list7);
        List<Map> list9 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list8);
        List<Map> list10 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list9);
        List<Map> list11 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list10);

        List<Map> list12 = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"10",list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");


        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType).contains("950."));
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType2).contains("950."));

        String destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list11);
        assertEquals(true,destoryInfo.contains("Transfer list cannot be more than 10"));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list10);
        assertEquals("200",JSONObject.fromObject(destoryInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String query2 = tokenModule.tokenGetDestroyBalance();
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType));
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType2));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list12);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));
    }

    @Test
    public void destoryByTokenTest()throws Exception{
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);

        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String amount1,amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "300.1234567891";
            amount2 = "300.8765432123";
        }else {
            amount1 = "300.123456";
            amount2 = "300.876543";
        }

        String desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));
//        assertEquals(true,desInfo.contains("\"address\":\""+tokenAccount3+"\"," + "\"amount\":\"400\""));
//        assertEquals(true,desInfo.contains("\"address\":\""+tokenMultiAddr2+"\"," + "\"amount\":\"300\""));
//        assertEquals(true,desInfo.contains("\"address\":\""+tokenMultiAddr1+"\"," + "\"amount\":\""+amount1+"\""));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));

        desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));



//        assertEquals(true,desInfo.contains("\"address\":\""+tokenAccount3+"\"," + "\"amount\":\"300\""));
//        assertEquals(true,desInfo.contains("\"address\":\""+tokenMultiAddr2+"\"," + "\"amount\":\"400\""));
//        assertEquals(true,desInfo.contains("\"address\":\""+tokenMultiAddr1+"\"," + "\"amount\":\""+amount2+"\""));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount2,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));

        String queryInfo1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");

        assertEquals(false,queryInfo1.contains(tokenType2));
        assertEquals(false,queryInfo1.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));


    }

    @Test
    public void destoryByList()throws Exception{
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);

        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(list4);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));
    }

    @AfterClass
    public static void resetAddr()throws Exception{
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();
    }
}
