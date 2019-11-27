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

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenMultiInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
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
    public void issueMax_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = "18446744073709";

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }


    //同时发行
    @Test
    public void issueTwo()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenAccount2;
        collAddr = tokenMultiAddr1;
        issueToken = stokenType;
        issAmount = "1844674";
        String issueToken2 = "tokenS2o-"+ UtilsClass.Random(8);
        String issAmount2 = "18022.1";

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);
        tokenModule.tokenAddMintAddr(collAddr);
        tokenModule.tokenAddCollAddr(issueAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        issueToken2 = commonFunc.tokenModule_IssueToken(collAddr,issueAddr,issAmount2);


        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken2);
        assertEquals(false,queryBalance.contains(issueToken2));


        queryBalance = tokenModule.tokenGetBalance(issueAddr,"");
        assertEquals(issAmount2, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

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

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));


        //执行回收
        String desAddr = collAddr;
        double desAmount = 510.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(false,queryBalance.contains(desToken));

    }
}
