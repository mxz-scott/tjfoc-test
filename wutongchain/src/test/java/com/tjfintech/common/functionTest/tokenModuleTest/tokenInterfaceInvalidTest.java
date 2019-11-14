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

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class tokenInterfaceInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();

    String AddrNotInDB = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
//    String errParamCode = "\"state\": 400";
    String errParamMsg = "client parameter error";

    @BeforeClass
    public static void init()throws Exception
    {
        if(tokenMultiAddr1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
        }
    }


     @Test
    public void issueInterfaceTest()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        String stokenType11 = "ng11Token"+ UtilsClass.Random(3);
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = "18446744073709";
         String comments = "issue invalid test";

        //使用未注册的发行地址进行发行
         String issueResp = "";
         issueResp = tokenModule.tokenIssue(tokenAccount3,collAddr,stokenType11,issAmount,comments);

         String stokenType12 = "ng12Token"+ UtilsClass.Random(3);
         //归集地址未注册
         issueResp = tokenModule.tokenIssue(issueAddr,tokenAccount3,stokenType12,issAmount,comments);


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);
        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

         log.info("test issueAddr parameter...............");
         String stokenType13 = "ng13Token"+ UtilsClass.Random(3);
         //发行地址设置为空
         issueResp = tokenModule.tokenIssue("",collAddr,stokenType13,issAmount,comments);
         //当前panic 无信息返回
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType14 = "ng14Token"+ UtilsClass.Random(3);
         //发行地址非法-原归集地址的一部分
         issueResp = tokenModule.tokenIssue(issueAddr.substring(10),collAddr,stokenType14,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType15 = "ng15Token"+ UtilsClass.Random(3);
         //发行地址非法-原归集地址*2
         issueResp = tokenModule.tokenIssue(issueAddr + issueAddr,collAddr,stokenType15,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType16 = "ng16Token"+ UtilsClass.Random(3);
         //发行地址非法-"456"
         //当前panic 无信息返回
         issueResp = tokenModule.tokenIssue("456",collAddr,stokenType16,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType17 = "ng17Token"+ UtilsClass.Random(3);
         //发行地址非法-超长地址-"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
         //当前panic
         issueResp = tokenModule.tokenIssue("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",collAddr,stokenType17,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType18 = "ng18Token"+ UtilsClass.Random(3);
         //发行地址不在数据库中
         issueResp = tokenModule.tokenIssue(AddrNotInDB,collAddr,stokenType18,issAmount,comments);
         assertEquals(true, issueResp.contains("addr doesn't exist!"));


//         String stokenType19 = "ng19Token"+ UtilsClass.Random(3);
//         //发行地址首尾存在空格
//         issueResp = tokenModule.tokenIssue(" " + issueAddr + " ",collAddr,stokenType19,issAmount,comments);
//         assertEquals(true, issueResp.contains("addr doesn't exist!"));


         log.info("test collAddr parameter...............");
         String stokenType21 = "ng21Token"+ UtilsClass.Random(3);
         //归集地址设置为空
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"",stokenType21,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType22 = "ng22Token"+ UtilsClass.Random(3);
        //归集地址非法-原归集地址的一部分
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr.substring(10),stokenType22,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType23 = "ng23Token"+ UtilsClass.Random(3);
        //归集地址非法-原归集地址*2
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr + collAddr,stokenType23,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType24 = "ng24Token"+ UtilsClass.Random(3);
         //归集地址非法- "123"
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"123",stokenType24,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType25 = "ng25Token"+ UtilsClass.Random(3);
         //归集地址非法-超长- "123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,"123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",stokenType25,issAmount,comments);
         assertEquals(true, issueResp.contains("invalid address"));

         String stokenType26 = "ng26Token"+ UtilsClass.Random(3);
         //归集地址非法-非数据库中的地址 未注册
         //sdk无报错 返回交易hash 链上日志报错
         issueResp = tokenModule.tokenIssue(issueAddr,AddrNotInDB,stokenType26,issAmount,comments);


        log.info("test tokenType parameter...............");
         //tokenType为空
         String stokenType31 = "";
         //sdk无报错 空tokenType可以发行成功
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType31,issAmount,comments);
         assertEquals(true, issueResp.contains(errParamMsg));

         //tokenType为空格
         String stokenType32 = " ";
         //sdk无报错 空格tokenType可以发行成功
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType32,issAmount,comments);
         assertEquals(true, issueResp.contains(errParamMsg));

         //tokenType为超长字符
         String stokenType33 = UtilsClass.Random(65);
         //归集地址非法-非数据库中的地址 未注册
         //当前可以发行成功
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType33,issAmount,comments);


         //tokenType为64位字符 预期可以发行成功
         String stokenTypeOK34 = UtilsClass.Random(64);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK34,issAmount,comments);

         //tokenType为64位字符 预期可以发行成功
         String stokenTypeOK35 = UtilsClass.Random(63);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK35,issAmount,comments);


         //tokenType包含中文 预期可以发行成功
         String stokenTypeOK36 = "中文{@#ok" + UtilsClass.Random(4);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK36,issAmount,comments);

         //tokenType包含中文 预期可以发行成功
         String stokenType37 = " ngToken37" + UtilsClass.Random(4) + " ";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType37,issAmount,comments);


         log.info("test issAmount parameter...............");
         //数量为空
         //当前可以发行 数值为0
         String stokenType41 = "ng41Token" + UtilsClass.Random(6);
         issAmount = "";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType41,issAmount,comments);
         assertEquals(true, issueResp.contains(errParamMsg));

         //数量为带负号的字串
         String stokenType42 = "ng42Token" + UtilsClass.Random(6);
         issAmount = "-100";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType42,issAmount,comments);
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

         //数量为字母的字串
         String stokenType43 = "ng43Token" + UtilsClass.Random(6);
         issAmount = "ab";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType43,issAmount,comments);
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为0
         //当前可以发行 数值为0
         String stokenType44 = "ng44Token" + UtilsClass.Random(6);
         issAmount = "0";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType44,issAmount,comments);
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

         //数量字符超长125位
         //当前可以发行 数值为0
         String stokenType45 = "ng45Token" + UtilsClass.Random(6);
         issAmount = "10000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType44,issAmount,comments);
         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));


         log.info("test comments parameter...............");

         issAmount = "1235";
         //comments为空
         String stokenTypeOK51 = "51okToken" + UtilsClass.Random(6);
         comments = "";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK51,issAmount,comments);

         //comments字符超长257位
         String stokenType52 = "ng52Token" + UtilsClass.Random(6);
         comments = "a100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789012345627";
         log.info("comments length " + comments.length());
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType52,issAmount,comments);

         //comments字符超长300位
         String stokenType53 = "ng53Token" + UtilsClass.Random(6);
         comments = UtilsClass.Random(300);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenType53,issAmount,comments);


         //comments字符256位
         String stokenTypeOK54 = "54okToken" + UtilsClass.Random(6);
         comments = UtilsClass.Random(256);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK54,issAmount,comments);
//         assertEquals(true, issueResp.contains("Amount must be greater than 0 and less than 18446744073709"));

         //comments字符255位
         String stokenTypeOK55 = "55okToken" + UtilsClass.Random(6);
         comments = UtilsClass.Random(255);
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK55,issAmount,comments);

         //comments为空格
         String stokenTypeOK56 = "56okToken" + UtilsClass.Random(6);
         comments = " ";
         issueResp = tokenModule.tokenIssue(issueAddr,collAddr,stokenTypeOK56,issAmount,comments);


         sleepAndSaveInfo(6000,"issue waiting......");
//
         String queryBalance = tokenModule.tokenGetBalance(collAddr,"");
         assertEquals(false, queryBalance.contains(stokenType11));
         assertEquals(false, queryBalance.contains(stokenType12));
         assertEquals(false, queryBalance.contains(stokenType13));
         assertEquals(false, queryBalance.contains(stokenType14));
         assertEquals(false, queryBalance.contains(stokenType15));
         assertEquals(false, queryBalance.contains(stokenType16));
         assertEquals(false, queryBalance.contains(stokenType17));
         assertEquals(false, queryBalance.contains(stokenType18));

         assertEquals(false, queryBalance.contains(stokenType21));
         assertEquals(false, queryBalance.contains(stokenType22));
         assertEquals(false, queryBalance.contains(stokenType23));
         assertEquals(false, queryBalance.contains(stokenType24));
         assertEquals(false, queryBalance.contains(stokenType25));
         assertEquals(false, queryBalance.contains(stokenType26));

//         assertEquals(false, queryBalance.contains(stokenType31));//""无法测试 sdk端给出响应信息
         assertEquals(false, queryBalance.contains(stokenType32));
//         assertEquals(false, queryBalance.contains(stokenType33));//当前长度限制与文档不符

         assertEquals(false, queryBalance.contains(stokenType41));
         assertEquals(false, queryBalance.contains(stokenType42));
         assertEquals(false, queryBalance.contains(stokenType43));
         assertEquals(false, queryBalance.contains(stokenType44));
         assertEquals(false, queryBalance.contains(stokenType45));


//         assertEquals(false, queryBalance.contains(stokenType52));
//         assertEquals(false, queryBalance.contains(stokenType53));


         assertEquals(true, queryBalance.contains(stokenTypeOK34));
         assertEquals(true, queryBalance.contains(stokenTypeOK35));
         assertEquals(true, queryBalance.contains(stokenTypeOK36));

         assertEquals(true, queryBalance.contains(stokenTypeOK51));
         assertEquals(true, queryBalance.contains(stokenTypeOK54));
         assertEquals(true, queryBalance.contains(stokenTypeOK55));

         assertEquals(true, queryBalance.contains(stokenType37.trim()));

         assertEquals(true, queryBalance.contains(stokenTypeOK54));
    }


    @Test
    public void transferInterfaceTest()throws Exception{
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
        double trfAmount1 = 100;

        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000,"register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,issAmount,comments);
        sleepAndSaveInfo(3000,"issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        comments = from + "向" + to + " 转账token：" + transferToken + " 数量：" + transferAmount;
        String transferResp = "";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);


        log.info("test from parameter...............");
        //from地址为空
        transferResp = tokenModule.tokenTransfer("",to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains(errParamMsg));

        //from地址非法-456
        transferResp = tokenModule.tokenTransfer("456",to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址非法-from地址的一部分
        transferResp = tokenModule.tokenTransfer(from.substring(10),to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址非法-from*2
        transferResp = tokenModule.tokenTransfer(from + from,to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址不在地址数据库中
        transferResp = tokenModule.tokenTransfer(AddrNotInDB,to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("addr doesn't exist!"));


        //from地址使用*
        transferResp = tokenModule.tokenTransfer("*",to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //from地址使用#
        transferResp = tokenModule.tokenTransfer("*",to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));


        log.info("test to parameter...............");
        //to地址为空
        transferResp = tokenModule.tokenTransfer(from,"",transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains(errParamMsg));

        //to地址非法-456
        transferResp = tokenModule.tokenTransfer(from,"456",transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址非法-from地址的一部分
        transferResp = tokenModule.tokenTransfer(from,to.substring(10),transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址非法-to*2
        transferResp = tokenModule.tokenTransfer(from,to + to,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("invalid address"));

        //to地址不在地址数据库中  目前可以转账成功 可以转入 无法转出
        transferResp = tokenModule.tokenTransfer(from,AddrNotInDB,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        //to地址为自己 即自己转给自己
        transferResp = tokenModule.tokenTransfer(from,from,transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("can't transfer it to yourself"));


        log.info("test tokenType parameter...............");
        //tokenType为空
        transferResp = tokenModule.tokenTransfer(from,to,"",transferAmount,comments);
        assertEquals(true,transferResp.contains(errParamMsg));

        //tokenType为空格
        transferResp = tokenModule.tokenTransfer(from,to," ",transferAmount,comments);
        assertEquals(true,transferResp.contains(errParamMsg));

        //tokenType为*
        transferResp = tokenModule.tokenTransfer(from,to,"*",transferAmount,comments);
        assertEquals(true,transferResp.contains("Insufficient Balance"));

        //tokenType为#
        transferResp = tokenModule.tokenTransfer(from,to,"#",transferAmount,comments);
        assertEquals(true,transferResp.contains("Insufficient Balance"));

        //tokenType不存在
        transferResp = tokenModule.tokenTransfer(from,to,transferToken + transferToken,transferAmount,comments);
        assertEquals(true,transferResp.contains("Insufficient Balance"));

        //tokenType为已存在的tokenType的一部分，但是是不存在的tokenType
        transferResp = tokenModule.tokenTransfer(from,to,transferToken.substring(3),transferAmount,comments);
        assertEquals(true,transferResp.contains("Insufficient Balance"));


        sleepAndSaveInfo(3000,"transfer waiting......");


        log.info("test amount parameter...............");
        //数量为空
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"",comments);
        assertEquals(true, transferResp.contains(errParamMsg));

        //数量为带负号的字串
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"-100",comments);
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为字母的字串
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"an",comments);
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为0
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"0",comments);
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量字符超长125位
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,"10000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890",comments);
        assertEquals(true, transferResp.contains("Amount must be greater than 0 and less than 18446744073709"));


        log.info("test comments parameter...............");
        //comments 不做测试重点，后期考虑移除
        transferAmount = "100";
        //comments为空
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,"");
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        sleepAndSaveInfo(3000,"transfer waiting......");

        //comments字符超长257位
        comments = "a100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890100000000000000123456789012345627";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        sleepAndSaveInfo(3000,"transfer waiting......");

        //comments字符256位
        comments = UtilsClass.Random(256);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        sleepAndSaveInfo(3000,"transfer waiting......");

        //comments字符255位
        comments = UtilsClass.Random(255);
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        sleepAndSaveInfo(3000,"transfer waiting......");

        //comments为空格
        comments = " ";
        transferResp = tokenModule.tokenTransfer(from,to,transferToken,transferAmount,comments);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));
        sleepAndSaveInfo(3000,"transfer waiting......");


        log.info("query from address balance ......");
        queryBalance = tokenModule.tokenGetBalance(from,transferToken);
        assertEquals("4400.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(transferToken));
        log.info("query to address balance ......");
        queryBalance = tokenModule.tokenGetBalance(to,transferToken);
        assertEquals("600", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(transferToken));

    }

    @Test
    public void destoryInterfaceTest()throws Exception {
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount = "";

        //单签地址发行token 5000.999999
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken = "tokenSo-" + UtilsClass.Random(8);
        issAmount = "5000.999999";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000, "register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        String issueToken2 = "tokenSo-" + UtilsClass.Random(8);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken2, issAmount, comments);
        sleepAndSaveInfo(3000, "issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));

        comments = "回收token";
        String destoryResp = "";
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"100",comments);

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        log.info("test from parameter...............");
        //回收地址为空
        destoryResp = tokenModule.tokenDestory("",issueToken,"100",comments);
        assertEquals(true,destoryResp.contains(errParamMsg));

        //回收地址非法-456
        destoryResp = tokenModule.tokenDestory("456",issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址非法-collAddr地址的一部分
        destoryResp = tokenModule.tokenDestory(collAddr.substring(10),issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址非法-collAddr*2
        destoryResp = tokenModule.tokenDestory(collAddr + collAddr,issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址不在地址数据库中
        destoryResp = tokenModule.tokenDestory(AddrNotInDB,issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("addr doesn't exist!"));

        //回收地址使用*
        destoryResp = tokenModule.tokenDestory("*",issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址使用#
        destoryResp = tokenModule.tokenDestory("#",issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("invalid address"));

        //回收地址为无token账户 填写tokentype和amount
        String getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");
        destoryResp = tokenModule.tokenDestory(tokenAccount3,issueToken,"100",comments);
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        //回收地址为无token账户 填写tokentype不填写amount
        destoryResp = tokenModule.tokenDestory(tokenAccount3,issueToken,"",comments);
        assertEquals("200",JSONObject.fromObject(destoryResp).getString("state"));
        sleepAndSaveInfo(3000,"destory waiting......");
        getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");

        //回收地址为无token账户 不填写tokentype填写amount
        destoryResp = tokenModule.tokenDestory(tokenAccount3,"","100",comments);
        assertEquals(true,destoryResp.contains(errParamMsg));

        //回收地址为无token账户 不填写tokentype和amount
        destoryResp = tokenModule.tokenDestory(tokenAccount3,"","",comments);
        assertEquals(true,destoryResp.contains(errParamMsg));

        getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,"");

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");



        log.info("test tokentype parameter...............");

        //tokenType不填写
        destoryResp = tokenModule.tokenDestory(collAddr,"","100",comments);
        assertEquals(true,destoryResp.contains(errParamMsg));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        //tokenType非该账户的tokenType
        destoryResp = tokenModule.tokenDestory(collAddr,"tokenSo-0ak6f9fx","100",comments);
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        //tokenType的一部分
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken.substring(3),"100",comments);
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        //tokenType为“#”
        destoryResp = tokenModule.tokenDestory(collAddr,"#","100",comments);
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        //tokenType为“#”
        destoryResp = tokenModule.tokenDestory(collAddr,"*","100",comments);
        assertEquals(true,destoryResp.contains("Insufficient Balance"));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");


        log.info("test amount parameter...............");
        //数量字段变更为必输字段
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"",comments);
        assertEquals(true,destoryResp.contains(errParamMsg));

        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("4900.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        tokenModule.tokenGetDestroyBalance("");

        //数量为带负号的字串
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"-100",comments);
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为字母的字串
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"ojil",comments);
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量为0
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"0",comments);
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

        //数量字符超长125位
        destoryResp = tokenModule.tokenDestory(collAddr,issueToken,"10000000000000012345678901000000000000001234567890100000000000000123456789010000000000000012345678901000000000000001234567890",comments);
        assertEquals(true, destoryResp.contains("Amount must be greater than 0 and less than 18446744073709"));

    }

    @Test
    public void queryBalanceInterfaceTest()throws Exception {

        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount = "";

        //单签地址发行token 5000.999999
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issueToken = "tokenSo-" + UtilsClass.Random(8);
        issAmount = "5000.999999";


        //添加发行地址和归集地址
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        sleepAndSaveInfo(3000, "register issue and coll address waiting......");

        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + issAmount;
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken, issAmount, comments);
        String issueToken2 = "tokenSo-" + UtilsClass.Random(8);
        tokenModule.tokenIssue(issueAddr, collAddr, issueToken2, issAmount, comments);
        sleepAndSaveInfo(3000, "issue waiting......");

        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));


        log.info("test address parameter...............");
        //查询地址为空
        queryBalance = tokenModule.tokenGetBalance("",issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址非法-456
        queryBalance = tokenModule.tokenGetBalance("456",issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址非法-collAddr地址的一部分
        queryBalance = tokenModule.tokenGetBalance(collAddr.substring(10),issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址非法-collAddr*2
        queryBalance = tokenModule.tokenGetBalance(collAddr + collAddr,issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址不在地址数据库中
        queryBalance = tokenModule.tokenGetBalance(AddrNotInDB,issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址使用*
        queryBalance = tokenModule.tokenGetBalance("*",issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址使用#
        queryBalance = tokenModule.tokenGetBalance("#",issueToken);
        assertEquals(true,queryBalance.contains("\"data\":{}"));

        //查询地址为无token账户 填写tokentype和amount
        String getOtherBalance = tokenModule.tokenGetBalance(tokenAccount3,issueToken);
        assertEquals("200",JSONObject.fromObject(getOtherBalance).getString("state"));
        assertEquals(true,queryBalance.contains("\"data\":{}"));


        log.info("test tokenType parameter...............");
        //tokenType为空
        queryBalance = tokenModule.tokenGetBalance(collAddr,"");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(true,queryBalance.contains(issueToken));
        assertEquals(true,queryBalance.contains(issueToken2));

        //tokenType为*
        queryBalance = tokenModule.tokenGetBalance(collAddr,"*");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

        //tokenType为#
        queryBalance = tokenModule.tokenGetBalance(collAddr,"#");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

        //tokenType不存在
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken+"11111111");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

        //tokenType为已存在的tokenType的一部分，但是是不存在的tokenType
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken.substring(3));
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals(false,queryBalance.contains(issueToken));

    }

    @Test
    public void createAccountInterfaceTest()throws Exception{
        String entityID = "";
        String entityName = "";
        String groupID = "";
        String comments = "";
        Map<String, Object> mapTag = new HashMap<>();

        //id为空
        String createResp = tokenModule.tokenCreateAccount("","test","","",mapTag);
        assertEquals(true,createResp.contains("ID and namecan't be empty"));

        //id界限长度上限
        entityID = UtilsClass.Random(64);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",mapTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //id为已存在id的一部分
        createResp = tokenModule.tokenCreateAccount(entityID.substring(0,10),"test","","",mapTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //id为已存在id name也是已存在的
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",mapTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("create account failed: [entityID [" + entityID + "] exist!"));

        //id为已存在id name不存在
        createResp = tokenModule.tokenCreateAccount(entityID,"test"+ UtilsClass.Random(6),"","",mapTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("create account failed: [entityID [" + entityID + "] exist!"));

        //id长度超过上限
        entityID = UtilsClass.Random(65);
        createResp = tokenModule.tokenCreateAccount(entityID,"test","","",mapTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));


        //name为空
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),"","","",mapTag);
        assertEquals(true,createResp.contains("ID and namecan't be empty"));

        //name长度上限64位
        entityName = UtilsClass.Random(64);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",mapTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));

        //name长度超过上限64位
        entityName = UtilsClass.Random(65);
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),entityName,"","",mapTag);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));

        //name已存在 id不存在
        createResp = tokenModule.tokenCreateAccount(UtilsClass.Random(6),"test","","",mapTag);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));


    }
}