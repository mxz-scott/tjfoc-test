package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenSoloTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
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
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        issueAmount1 = "10000.12345678912345";
        issueAmount2 = "20000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "10000.1234567891";
            actualAmount2 = "20000.8765432123";
        }else {
            actualAmount1 = "10000.123456";
            actualAmount2 = "20000.876543";
        }

        log.info("发行两种token");
        tokenType = IssueToken(tokenAccount1,tokenAccount1,issueAmount1);
        tokenType2 = IssueToken(tokenAccount1,tokenAccount1,issueAmount2);


        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        String response2 = tokenModule.tokenGetBalance( tokenAccount1, tokenType2);

        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString(actualAmount1));
        assertThat(tokenType+"查询余额不正确",response2, containsString(actualAmount2));
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

        tokenType = IssueToken(tokenAccount1,tokenAccount1,actualAmount1);
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        log.info("查询归集地址中token余额");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
    }

    /**
     * 单签发行检查发行地址注册、未注册时的发行结果
     * @throws Exception
     */
    @Test
    public void    TC1279_checkSoloIssueAddr()throws Exception {
        //Thread.sleep(8000);
        //先前已经注册发行和归集地址tokenAccount1，确认发行无问题
        tokenType = IssueToken(tokenAccount1,tokenAccount1,"1009");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1009",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        log.info("删除发行地址，保留归集地址");
        //删除发行地址，保留归集地址
        String response3=tokenModule.tokenDelMintAddr(tokenAccount1);
        assertThat(response3, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        tokenType = IssueToken(tokenAccount1,tokenAccount1,"1009");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String response2 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(false,response2.contains(tokenType));

        //删除发行地址和归集地址
        log.info("删除发行地址和归集地址");
        String response4=tokenModule.tokenDelCollAddr(tokenAccount1);
        assertThat(response4, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        tokenType = IssueToken(tokenAccount1,tokenAccount1,"1009");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String response41 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        assertEquals(false,response41.contains(tokenType));


        //重新添加发行地址，保留删除归集地址
        String response51=tokenModule.tokenAddMintAddr(tokenAccount1);
        assertThat(response51, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        tokenType = IssueToken(tokenAccount1,tokenAccount1,"1009");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String response52 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response52).getString("state"));
        assertEquals(false,response52.contains(tokenType));

        //重新添加归集地址
        String response6=tokenModule.tokenAddCollAddr(tokenAccount1);
        assertThat(response6, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        tokenType = IssueToken(tokenAccount1,tokenAccount1,"2356");
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String response7 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("2356",JSONObject.fromObject(response7).getJSONObject("data").getString(tokenType));
    }

    /**
     * Tc024单签正常流程:
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        //"归集地址向" + tokenAccount2 + "转账100.25个" + tokenType+",并向"+tokenAccount3 +"转账";
        String transferInfo= TransferToken(tokenAccount1,tokenAccount2,tokenType,"100.25");
        assertThat(transferInfo, containsString("200"));
        String transferInfo2= TransferToken(tokenAccount1,tokenAccount3,tokenType2,"200.555");
        assertThat(transferInfo2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");


        log.info("查询"+ tokenAccount2 + "跟" +tokenAccount3+"余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
        assertEquals("100.25",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200.555",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        
        log.info(tokenAccount2 + " --> " + tokenAccount3 + "转账: " + tokenType);
        transferInfo = TransferToken(tokenAccount2,tokenAccount3,tokenType,"30");
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        log.info(tokenAccount3 + " --> " + tokenAccount2 + "转账: " + tokenType2);
        transferInfo = TransferToken(tokenAccount3,tokenAccount2,tokenType2,"80");
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String query = tokenModule.tokenGetBalance(tokenAccount1, "");
        query = tokenModule.tokenGetBalance(tokenAccount2, "");
        query = tokenModule.tokenGetBalance(tokenAccount3, "");


        String Info2 = DestoryToken(tokenAccount2, tokenType, "70.25");
        String Info3 = DestoryToken(tokenAccount2, tokenType2, "80");
        String Info4 = DestoryToken(tokenAccount3, tokenType, "30");
        String Info5 = DestoryToken(tokenAccount3, tokenType2, "120.555");

        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String queryBalanceInfo2 = tokenModule.tokenGetBalance(tokenAccount2, "");
        String queryBalanceInfo3 = tokenModule.tokenGetBalance(tokenAccount3, "");

        assertEquals(false,queryBalanceInfo2.contains(tokenType));
        assertEquals(false,queryBalanceInfo2.contains(tokenType2));
        assertEquals(false,queryBalanceInfo3.contains(tokenType));
        assertEquals(false,queryBalanceInfo3.contains(tokenType2));

    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC024_PrecisionTest() throws Exception {

        String transferInfo1 = TransferToken(tokenAccount1,tokenAccount2,tokenType,issueAmount1);
        String transferInfo2 = TransferToken(tokenAccount1,tokenAccount3,tokenType2,issueAmount2);
        assertEquals("200",JSONObject.fromObject(transferInfo1).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String amount1, amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "10000.1234567891";
            amount2 = "20000.8765432123";
        }else {
            amount1 = "10000.123456";
            amount2 = "20000.876543";
        }

        log.info("查询帐号3跟帐号2余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));


        String Info3 = DestoryToken(tokenAccount2,tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        String Info4 = DestoryToken(tokenAccount3, tokenType2, issueAmount2);
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        String queryInfo11 = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
        String queryInfo12 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
        String queryInfo5 = tokenModule.tokenGetDestroyBalance(tokenType);
        String queryInfo6 = tokenModule.tokenGetDestroyBalance(tokenType2);

        assertEquals(false,queryInfo11.contains(tokenType));
        assertEquals(false,queryInfo12.contains(tokenType2));
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo6).getJSONObject("data").getString(tokenType2));


    }


//    /**
//     * Tc024锁定后转账:
//     *
//     */
//    @Test
//    public void TC024_TransferAfterFrozen() throws Exception {
//
//        //20190411增加锁定步骤后进行转账
//        log.info("锁定待转账Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenAccount1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//
//        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
//        log.info(transferData);
//        List<Map> listModel1 = soloSign.constructToken(tokenAccount3,tokenType,"100.25");
//        log.info(tokenAccount3);
//        List<Map> list1=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel1);
//        String transferInfo= TransferToken(list1,tokenAccount1, transferData);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        assertThat(transferInfo, containsString("200"));
//        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
//        String queryInfo = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
//        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
//        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType), containsString("0"));
//        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType), containsString("0"));
//
//
//        log.info("解除锁定待转账Token: "+tokenType);
//        String resp1=multiSign.recoverFrozenToken(tokenAccount1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//
//        transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
//        log.info(transferData);
//        List<Map> listModel = soloSign.constructToken(tokenAccount3,tokenType,"100.25");
//        log.info(tokenAccount3);
//        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
//        transferInfo= TransferToken(list,tokenAccount1, transferData);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        assertThat(transferInfo, containsString("200"));
//        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
//        queryInfo = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
//        queryInfo2 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("100.25"));
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("200.555"));
//
//    }

    /**
     * Tc040单签转单签异常测试:
     *
     */
    @Test
    public void TC040_SoloProgress() throws Exception {
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";;
        String transferInfo = TransferToken(tokenAccount1,tokenAccount2,tokenType,"3000");
        String transferInfo2 = TransferToken(tokenAccount1,tokenAccount2,tokenType2,"3000");
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        //tokenAccount2 向 tokenAccount3转账4000 tokenType
        String recycleInfo2 = TransferToken(tokenAccount2, tokenAccount3,tokenType,"4000");
        assertThat(recycleInfo2, containsString("Insufficient Balance"));

        //tokenAccount2 向 tokenAccount3转账4000 tokenType2
        String recycleInfo3 = TransferToken(tokenAccount2, tokenAccount3,tokenType2,"4000");
        assertThat(recycleInfo3, containsString("Insufficient Balance"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String Info = DestoryToken(tokenAccount2, tokenType, "3000");
        String Info3 = DestoryToken(tokenAccount2, tokenType2, "3000");
        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String queryInfo11 = tokenModule.tokenGetBalance( tokenAccount3, tokenType);
        String queryInfo12 = tokenModule.tokenGetBalance( tokenAccount3, tokenType2);
        String queryInfo5 = tokenModule.tokenGetDestroyBalance("");

        assertEquals(false,queryInfo11.contains(tokenType));
        assertEquals(false,queryInfo12.contains(tokenType2));
        assertEquals("3000",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
        assertEquals("3000",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType2));
    }
    /**
     * Tc041单签转单签+多签异常测试:锁定解锁后执行回收
     *
     */
    @Test
    public void TC041_SoloProgress() throws Exception {
        //"归集地址向" + tokenAccount3 + "转账3000个" + tokenType+",并向"+tokenMultiAddr3+"转账";
        String transferInfo = TransferToken(tokenAccount1, tokenAccount3,tokenType,"3000");
        String transferInfo2= TransferToken(tokenAccount1, tokenMultiAddr3,tokenType2,"3000");
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String transferInfo3 = TransferToken(tokenAccount3, tokenAccount2,tokenType,"7000");
        String transferInfo4 = TransferToken(tokenMultiAddr3, tokenMultiAddr2,tokenType2,"7000");
        assertThat(transferInfo3, containsString("Insufficient Balance"));
        assertThat(transferInfo4, containsString("Insufficient Balance"));

        //转账余额不足后余额范围内转账
        String transferInfo5 = TransferToken(tokenAccount3, tokenAccount2,tokenType,"1000");
        String transferInfo6 = TransferToken(tokenMultiAddr3, tokenMultiAddr2,tokenType2,"1000");
        assertEquals("200",JSONObject.fromObject(transferInfo5).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo6).getString("state"));


        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

//        //20190411增加锁定解锁操作步骤后进行回收
//        log.info("锁定待回收Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenAccount1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        log.info("解除锁定待回收Token: "+tokenType);
//        String resp1=multiSign.recoverFrozenToken(tokenAccount1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("开始回收....");
        String Info = DestoryToken(tokenAccount3, tokenType, "2000");
        String Info1 = DestoryToken(tokenMultiAddr3, tokenType2, "2000");
        String Info2 = DestoryToken(tokenAccount1, tokenType, "7000.123456789");
        String Info3 = DestoryToken(tokenAccount1, tokenType2, "17000.87654321");
        String Info4 = DestoryToken(tokenAccount2, tokenType, "1000");
        String Info5 = DestoryToken(tokenMultiAddr2, tokenType2, "1000");

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info1).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info5).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        log.info("开始查询余额....");
        String response1 = tokenModule.tokenGetBalance(tokenAccount1, "");
        String response2 = tokenModule.tokenGetBalance(tokenAccount2,  "");
        String response3 = tokenModule.tokenGetBalance( tokenMultiAddr2, "");
        String response4 = tokenModule.tokenGetBalance( tokenMultiAddr3, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));

        assertEquals(false,response1.contains(tokenType));
        assertEquals(false,response1.contains(tokenType2));
        assertEquals(false,response2.contains(tokenType));
        assertEquals(false,response2.contains(tokenType2));
        assertEquals(false,response3.contains(tokenType));
        assertEquals(false,response3.contains(tokenType2));
        assertEquals(false,response4.contains(tokenType));
        assertEquals(false,response4.contains(tokenType2));

    }

//    /**
//     * Tc042单签转单签+多签测试:回收前锁定token
//     *
//     */
//    @Test
//    public void TC042_SoloProgress() throws Exception {
//        String transferData = "归集地址向" + "PUBKEY3" + "转账3000个" + "tokenType"+",并向"+"PUBKEY4"+"转账tokenType2";
//        log.info(transferData);
//
//        List<Map> list=soloSign.constructToken(tokenAccount3,tokenType,"3000");
//        List<Map> list1=soloSign.constructToken(tokenAccount3,tokenType2,"3000",list);
//        String transferInfo= TransferToken(list1,tokenAccount1,transferData);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//
//        List<Map> list2 = soloSign.constructToken(tokenAccount3,tokenType,"200");
//        List<Map>list3= soloSign.constructToken(MULITADD5,tokenType,"70",list2);
//        String recycleInfo2 = TransferToken(list3, tokenAccount2, "李四向小六转账4000 TT001, 70 TT001");
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        List<Map> list4 = soloSign.constructToken(tokenAccount3,tokenType,"400");
//        List<Map>list5= soloSign.constructToken(MULITADD5,tokenType2,"401",list4);
//        String recycleInfo3 = TransferToken(list5, tokenAccount2, "李四向小六转账4000 TT001, 4001 TT002");
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//
//        //20190411增加锁定操作步骤后进行回收
//        log.info("锁定待回收Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenAccount1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//
//        log.info("开始回收....");
//        String Info = DestoryToken( tokenAccount2, tokenType, "2330");
//        String Info1 = DestoryToken( tokenAccount2, tokenType2, "2599");
//        String Info2 = DestoryToken( PRIKEY4, tokenType, "600");
//        String Info3 = DestoryToken(MULITADD5, tokenAccount1, tokenType, "70");
//        String Info4 = DestoryToken(MULITADD5, tokenAccount2, tokenType2, "401");
//        String Info5 = DestoryToken(tokenAccount1, tokenType, "7000.123456789");
//        String Info6 = DestoryToken(tokenAccount1, tokenType2, "17000.87654321");
//
//        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info1).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info5).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info6).getString("state"));
//
//
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
//        log.info("开始查询余额....");
//        String response1 = tokenModule.tokenGetBalance(IMPPUTIONADD, PRIKEY4, tokenType);
//        String response2 = tokenModule.tokenGetBalance(IMPPUTIONADD, PRIKEY4, tokenType2);
//        String response3 = tokenModule.tokenGetBalance(MULITADD5, tokenAccount1, tokenType);
//        String response4 = tokenModule.tokenGetBalance(MULITADD5, tokenAccount1, tokenType2);
//        String response5 = tokenModule.tokenGetBalance( tokenAccount2, tokenType);
//        String response6 = tokenModule.tokenGetBalance( tokenAccount2, tokenType2);
//        String response7 = tokenModule.tokenGetBalance( PRIKEY4, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
//        assertEquals("200",JSONObject.fromObject(response7).getString("state"));
//
//        assertEquals(JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response3).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response4).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response5).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response6).getJSONObject("data").getString(tokenType).equals("0"),true);
//        assertEquals(JSONObject.fromObject(response7).getJSONObject("data").getString(tokenType).equals("0"),true);
//    }

    /**
     * Tc244单签接口双花测试:
     *
     */
    @Test
    public void TC0244_SoloProgress() throws Exception {
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        String transferInfo1= TransferToken(tokenAccount1, tokenAccount3,tokenType2,"3000");
        String transferInfo2= TransferToken(tokenAccount1, tokenAccount2,tokenType2,"3000");
        assertEquals("200",JSONObject.fromObject(transferInfo1).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String response = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals("17000.876543",JSONObject.fromObject(response).getJSONObject("data").getString(tokenType2));
        assertEquals("10000.123456",JSONObject.fromObject(response).getJSONObject("data").getString(tokenType));


        String transferInfo3= TransferToken(tokenAccount1, tokenAccount3,tokenType,"2000");
        String transferInfo4= TransferToken(tokenAccount1, tokenAccount2,tokenType,"2000");
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo4).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        String response2 = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals("17000.876543",JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
        assertEquals("8000.123456",JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType));

    }

    //验证无法转账给自己
    @Test
    public void TransferToSelf()throws Exception{
        String transferInfo= TransferToken(tokenAccount1, tokenAccount1,tokenType,"100.25");
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
        String response = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        assertEquals(actualAmount1,JSONObject.fromObject(response).getJSONObject("data").getString(tokenType));
    }


    @Test
    public void singleAccountDoubleSpend_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;



        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = from + "向" + to + " 转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        comments = from + "向" + to + " 转账token：" + issueToken + " 数量：" + transferAmount;
        transferResp = tokenModule.tokenTransfer(from,to,issueToken,transferAmount,comments);


        sleepAndSaveInfo(3000,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,"");
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
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(3000,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void singleAccountDoubleSpend_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount2;
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

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = from + "向" + to + " 转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        comments = from + "向" + to + " 转账token：" + issueToken + " 数量：" + transferAmount;
        transferResp = tokenModule.tokenTransfer(from,to,issueToken,transferAmount,comments);


        sleepAndSaveInfo(3000,"transfer waiting......");

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
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(3000,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }


    @Test
    public void singleAccount_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;



        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = from + "向" + to + " 转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);

        sleepAndSaveInfo(3000,"transfer waiting......");

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        comments = from + "向" + to + " 转账token：" + issueToken + " 数量：" + transferAmount;
        transferResp = tokenModule.tokenTransfer(from,to,issueToken,transferAmount,comments);


        sleepAndSaveInfo(3000,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1- trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(3000,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void singleAccount_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount2;
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

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");


        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = from + "向" + to + " 转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        sleepAndSaveInfo(3000,"transfer waiting......");

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        comments = from + "向" + to + " 转账token：" + issueToken + " 数量：" + transferAmount;
        transferResp = tokenModule.tokenTransfer(from,to,issueToken,transferAmount,comments);


        sleepAndSaveInfo(3000,"transfer waiting......");

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
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(3000,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

//-----------------------------------------------------------------------------------------------------------

    public  String IssueToken(String issueAddr,String collAddr,String amount){
        String issueToken = "tokenSo-"+ UtilsClass.Random(8);
        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + amount;
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String TransferToken(String from,String to, String tokenType,String amount){
        String comments = from + "向" + to + " 转账token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        return tokenModule.tokenTransfer(from,to,tokenType,amount,comments);
    }

    public  String DestoryToken(String addr,String tokenType,String amount){
        String comments = addr + "销毁token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        return tokenModule.tokenDestory(addr,tokenType,amount,comments);
    }
}
