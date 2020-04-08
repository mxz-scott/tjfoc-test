package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenAccurateTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

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


     @Test
    public void issueAccurateMorethan6_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        double amount = 5869.8989284222222;
        issAmount = String.valueOf(amount);
        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(utilsClass.get6(amount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void issueAccurate5_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        double amount = 586699.89892;
        issAmount = String.valueOf(amount);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void issueAccurate2_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        double amount = 85696.36;
        issAmount = String.valueOf(amount);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void issueAccurateInt_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        double amount = 856965636;
        issAmount = "856965636";

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void transferAccurateInt_IssueSelf()throws Exception{

        String issueToken = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"5000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1,issueToken);
        assertEquals("5000", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));


        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr1,issueToken,"100");
        String transferResp = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1,issueToken);
        assertEquals(String.valueOf("4900"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(tokenMultiAddr1,issueToken);
        assertEquals(String.valueOf("100"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收

        String destroyResp = commonFunc.tokenModule_DestoryToken(tokenAccount1,issueToken,"500");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals("4400", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(String.valueOf("100"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals("500", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void transferAccurate1_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issAmount = "5000";

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.3;
        double trfAmount2 = 689.2;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        List<Map> list = utilsClass.tokenConstructToken(to,transferToken,transferAmount);
        String transferResp = commonFunc.tokenModule_TransferTokenList(from,list);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.69856;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

}
