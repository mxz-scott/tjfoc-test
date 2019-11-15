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
public class TokenSoloInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
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


     @Test
    public void issueMax_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = "18446744073709";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

    }

    @Test
    public void issueExtMax_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = "18446744073709.1";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(false, queryBalance.contains(issueToken));

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
        collAddr = tokenAccount1;
        issueToken = stokenType;
        issAmount = "1844674";
        String issueToken2 = "tokenS2o-"+ UtilsClass.Random(8);
        String issAmount2 = "18022.1";

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);
        tokenModule.tokenAddMintAddr(collAddr);
        tokenModule.tokenAddCollAddr(issueAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        tokenModule.tokenIssue(collAddr,issueAddr,issueToken2,issAmount2,"");


        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken2);
        assertEquals(false,queryBalance.contains(issueToken2));


        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken2);
        assertEquals(issAmount2, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

    }


    //转账回收超出余额
    @Test
    public void tranferExtBalance()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType = "tokenSo-"+ UtilsClass.Random(8);
        double sAmount = 500.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount2;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 689.333;
        double trfAmount2 = 100.333;



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
        assertEquals(get6(sAmount-trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //执行回收超出余额
        String desAddr = collAddr;
        double desAmount = 510.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        comments = "回收" + desAddr + " token：" + desToken + " 数量：" + desAmountStr;
        String destroyResp = tokenModule.tokenDestory(desAddr,desToken,desAmountStr,comments);

        sleepAndSaveInfo(3000,"destroy waiting......");

        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(get6(sAmount-trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(false,queryBalance.contains(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance("");
        assertEquals(false,queryBalance.contains(desToken));

    }
}
