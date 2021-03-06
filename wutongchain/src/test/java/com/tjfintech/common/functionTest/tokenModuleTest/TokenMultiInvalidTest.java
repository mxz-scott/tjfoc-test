package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenMultiInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenMultiAddr1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        log.info("发行两种token100.123个");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,"100");
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,"100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("100",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
    }


    @Test
    public void TC37_InvalidMultiAddr() throws Exception {
        String AddrNotInDB = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
        Map<String, Object> addresses = new HashMap<>();
        String name = "test";
        int minSignatures = 2;
        String groupID = "testid";//0324移除groupID
        String comments = "create multi address";
        ArrayList<String> listTag = new ArrayList<>();//0324移除tag
        //addresses 三个 包含一个不存在的地址或者未托管的地址
        addresses.clear();
        addresses.put("1",tokenAccount1);
        addresses.put("2",tokenAccount2);
        addresses.put("3",AddrNotInDB);
        String createResp = tokenModule.tokenCreateMultiAddr(addresses,name,minSignatures,groupID,comments,listTag);
        assertEquals(true,createResp.contains("address["+AddrNotInDB+"] not exist!;error:sql: no rows in result set"));
//        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

//        String temp23Addr = JSONObject.fromObject(createResp).getString("data");
//        String AddIssueAddr = tokenModule.tokenAddMintAddr(temp23Addr);
//        String AddCollAddr = tokenModule.tokenAddCollAddr(temp23Addr);
//
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
//                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//
//        String IssueResp = tokenModule.tokenIssue(temp23Addr,temp23Addr,"test","12345","包含未托管地址的多签地址");
//        assertEquals(true,IssueResp.contains("addr doesn't exist!"));
//        assertEquals(true,IssueResp.contains(AddrNotInDB));
//        assertEquals("400",JSONObject.fromObject(IssueResp).getString("state"));
    }

    /**
     * Tc37 归集地址向两个多签地址转账异常测试
     * @throws Exception
     */
    @Test
    public void TC37_transferMultiInvalid() throws Exception {
        String transferData = "归集地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list7);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO关系，两笔交易之间需要休眠

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));


        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));

        log.info("回收Token");
        
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, "100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, "100");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));

    }


    /**
     * Tc38归集地址向单签、多签地址转账异常测试
     * @throws Exception
     */

    @Test
    public  void TC38_transferSoloMultiInvalid() throws  Exception{

        String transferData = "归集地址向单签与多签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list7);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO关系，两笔交易之间需要休眠

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));


        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount1, "");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType,"100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType2,"100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
    }
    /**
     * Tc39 归集地址向两个单签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC39_transferSoloInvalid()throws  Exception{  log.info("查询归集地址中两种token余额");
        String transferData = "归集地址向两个单签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list7);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO关系，两笔交易之间需要休眠

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));

        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount2, tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));


        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, "100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, "100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
    }

    /**
     * Tc238 多签地址向两个单签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC238_MultiToSoloInvalid()throws  Exception{

        String transferDataInit = "归集地址向" + "tokenMultiAddr5" + "转账100个" + tokenType + "归集地址向" + "tokenMultiAddr5" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType, "100");
        List<Map> listInit2 = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType2, "100", listInit);
        log.info(transferDataInit);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, listInit2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        String transferData = "多签地址向两个单签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list7);

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount2, tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));


        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType, "100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType2, "100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr5,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));

    }

    /**
     * Tc239 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC239_MultiToMulitInvalid()throws  Exception{

        String transferDataInit = "归集地址向" + "tokenMultiAddr5" + "转账100个" + tokenType + "归集地址向" + "tokenMultiAddr5" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType, "100");
        List<Map> listInit2 = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType2, "100", listInit);
        log.info(transferDataInit);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, listInit2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        String transferData = "多签地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list7);

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType, "100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType2, "100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr5,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));

    }

    /**
     * Tc240 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC240_MultiToSoloMulitInvalid()throws  Exception{
        String transferDataInit = "归集地址向" + "tokenMultiAddr5" + "转账100个" + tokenType + "归集地址向" + "tokenMultiAddr5" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType, "100");
        List<Map> listInit2 = utilsClass.tokenConstructToken(tokenMultiAddr5, tokenType2, "100", listInit);
        log.info(transferDataInit);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, listInit2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        String transferData = "多签地址向单签和多签地址转账异常测试";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list3);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list4);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list5);
        String transferInfo6 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list6);
        String transferInfo7 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr5, list7);

        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo2).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo3).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo4).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo5).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo6).getString("data"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo7).getString("data"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount1, "");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));

        log.info("回收Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType, "100");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr5, tokenType2, "100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr5,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));

    }


    //重复发行同一个tokentype
    @Test
    public void issueExistToken()throws Exception{
        String issueResp = tokenModule.tokenIssue(tokenAccount1,tokenAccount2,tokenType,"100","重复发行");
//        assertEquals(true,issueResp.contains("tokentype has been used"));
        //20200727 代码修改为链上验证
        sleepAndSaveInfo(SLEEPTIME);
        String checkOnchain = tokenModule.tokenGetTxDetail(commonFunc.getTxHash(issueResp,utilsClass.tokenApiGetTxHashType));
        assertEquals("400",JSONObject.fromObject(checkOnchain).getString("state"));

    }


    //回收超出余额
    @Test
    public void destoryExtBalance()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 500.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));


        //执行回收
        String desAddr = collAddr;
        double desAmount = 510.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destroyResp).getString("data"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(false,queryBalance.contains(desToken));

    }

    //一笔待回收的交易都没有的话
    @Test
    public void destoryByTokenNoDesToken()throws Exception{

        String issueToken = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr2,"500");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(tokenMultiAddr2,issueToken);
        assertEquals("500", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行一次回收by tokentype
        String destroyResp = commonFunc.tokenModule_DestoryTokenByTokenType(issueToken);
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        String destroyResp2 = commonFunc.tokenModule_DestoryTokenByTokenType(issueToken);
        assertEquals(true, destroyResp2.contains("invalid tokenType"));

    }
    //其中一部分账户已无token余额回收，其他账户存在token余额
    @Test
    public void destoryByTokenPartDesToken()throws Exception{
        String issueToken = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,"500");
        String issueToken2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,"500");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("500", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals("500", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));

        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,issueToken2,"100");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,issueToken,"200",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,issueToken,"100",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,issueToken2,"200",list3);

        //转账
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        String query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("100", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));

        //回收
        List<Map> listD= utilsClass.tokenConstructToken(tokenAccount3,issueToken,"100");
        List<Map> listD2 = utilsClass.tokenConstructToken(tokenMultiAddr2,issueToken,"90",listD);
        String destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(listD2);
        assertEquals("200",JSONObject.fromObject(destoryInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("10", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));


        //再执行一次回收 tokenMultiAddr2 issueToken超出余额 当前余额为10  tokenAccount3余额充足
        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(listD2);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));

        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("10", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));

        //回收tokenAccount3余额 100
        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(listD);
        assertEquals("200",JSONObject.fromObject(destoryInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals(false,query1.contains(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("10", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));

        //回收中存在无余额账户 以及超出余额账户
        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(listD2);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals(false,query1.contains(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("10", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));
        //回收中存在无余额账户 及充足账户
        List<Map> listD3 = utilsClass.tokenConstructToken(tokenMultiAddr2,issueToken,"5",listD);

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(listD3);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));
        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals(false,query1.contains(issueToken));
        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("10", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));

        //执行一次回收by tokentype
        String destroyResp = commonFunc.tokenModule_DestoryTokenByTokenType(issueToken);
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        query1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        query2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals(false,query1.contains(issueToken));
        assertEquals(false,query2.contains(issueToken));

        assertEquals("100", JSONObject.fromObject(query1).getJSONObject("data").getString(issueToken2));
        assertEquals("200", JSONObject.fromObject(query2).getJSONObject("data").getString(issueToken2));

    }

    //tokenType大小写敏感性检查
    @Test
    public void testMatchCaseQueryBalance()throws Exception{

        //查询余额账户地址大小写敏感性检查  当前不敏感
        log.info("查询余额账户地址大小写敏感性检查  当前不敏感");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr1.toLowerCase(),tokenType);
        assertEquals("invalid address",JSONObject.fromObject(query).getString("data"));
//        assertEquals(true,
//                JSONObject.fromObject(query).getString("data").contains(tokenType));

        query = tokenModule.tokenGetBalance(tokenMultiAddr1.toUpperCase(),tokenType);
        assertEquals("invalid address",JSONObject.fromObject(query).getString("data"));
//        assertEquals(true,
//                JSONObject.fromObject(query).getString("data").contains(tokenType));


        log.info("查询余额tokentype敏感检查");
        //查询余额tokentype敏感检查
        query = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType.toUpperCase());
        assertEquals(false,
                JSONObject.fromObject(query).getString("data").contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType.toLowerCase());
        assertEquals(false,
                JSONObject.fromObject(query).getString("data").contains(tokenType.toLowerCase()));

    }

    @Test
    public void testMatchCaseTransfer()throws Exception{

        //转账检查大小写敏感
        //检查小写tokentype转账
        log.info("转账检查大小写敏感");
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType.toLowerCase(),"10");
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));

        //检查小写tokentype转账
        list = utilsClass.tokenConstructToken(tokenAccount3,tokenType.toUpperCase(),"10");
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));
    }

    @Test
    public void testMatchCaseDestroy()throws Exception{
        List<Map> list;
        //回收检查大小写敏感
        log.info("回收检查大小写敏感");
        list = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType.toLowerCase(),"10");
        String desResp = commonFunc.tokenModule_DestoryTokenByList2(list);
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));


        list = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType.toUpperCase(),"10");
        desResp = commonFunc.tokenModule_DestoryTokenByList2(list);
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));


        desResp = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType.toLowerCase());
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("invalid tokenType",JSONObject.fromObject(desResp).getString("data").trim());

        desResp = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType.toUpperCase());
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("invalid tokenType",JSONObject.fromObject(desResp).getString("data").trim());
    }


    @Test
    public void testMatchCaseFreezeRecover()throws Exception{
        //冻结检查大小写
        log.info("冻结检查大小写");
        String freezeResp = tokenModule.tokenFreezeToken(tokenType.toLowerCase());
        assertEquals("500",JSONObject.fromObject(freezeResp).getString("state"));
        assertEquals(true,freezeResp.contains("rpc error: code = Unknown desc = token["+tokenType.toLowerCase()+"] not exist!"));

//        String hash1 = JSONObject.fromObject(freezeResp).getString("data");
//
//        freezeResp = tokenModule.tokenFreezeToken(tokenType.toUpperCase());
//        assertEquals("200",JSONObject.fromObject(freezeResp).getString("state"));
//        String hash2 = JSONObject.fromObject(freezeResp).getString("data");
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
//                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
//
//        assertEquals("400",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash1)).getString("state"));
//        assertEquals("400",JSONObject.fromObject(tokenModule.tokenGetTxDetail(hash2)).getString("state"));

        //确认tokenType未被冻结
        String transferResp = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType,"10");
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals("10",JSONObject.fromObject(
                tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType)).getJSONObject("data").getString(tokenType));


        String recoverResp = tokenModule.tokenRecoverToken(tokenType);//开发修改回链上验证 此处不做校验 20200722
//        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been frozen!"));

        //冻结tokentype测试解除
        String freezeBeforeRecover = tokenModule.tokenFreezeToken(tokenType);
        assertEquals("200",JSONObject.fromObject(freezeBeforeRecover).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        recoverResp = tokenModule.tokenRecoverToken(tokenType.toUpperCase());
//        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been frozen!"));
        assertEquals("500",JSONObject.fromObject(recoverResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("not exist"));
        assertEquals(true,JSONObject.fromObject(recoverResp).getString("message").contains("not exist"));


        recoverResp = tokenModule.tokenRecoverToken(tokenType.toLowerCase());
//        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been frozen!"));

        assertEquals("500",JSONObject.fromObject(recoverResp).getString("state"));
//        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("not exist"));
        assertEquals(true,JSONObject.fromObject(recoverResp).getString("message").contains("not exist"));

        //确认tokenType被冻结未被恢复 冻结token无法转账
        transferResp = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr3,tokenType,"10");
        //20200903修改为链上报错
//        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals("toketype(" + tokenType + ") has been frozen!",JSONObject.fromObject(transferResp).getString("data"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        assertEquals(false,tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType).contains(tokenType));

        String recoverR2 = tokenModule.tokenRecoverToken(tokenType);
        assertEquals("200",JSONObject.fromObject(recoverR2).getString("state"));
    }
}
