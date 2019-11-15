package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
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
public class TokenAccurateTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();

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
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        double amount = 5869.8989284222222;
        issAmount = String.valueOf(amount);

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(get6(amount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void issueAccurate5_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        double amount = 586699.89892;
        issAmount = String.valueOf(amount);

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

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
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        double amount = 85696.36;
        issAmount = String.valueOf(amount);

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

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
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        double amount = 856965636;
        issAmount = "856965636";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void transferAccurateInt_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = "5000";

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100;
        double trfAmount2 = 689;



        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

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


        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf("4900"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf("100"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.6985474;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf("100"), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(get6(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void transferAccurate1_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 5000;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = "5000";

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.3;
        double trfAmount2 = 689.2;



        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(SLEEPTIME,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

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
        double desAmount = 500.69856;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

}
