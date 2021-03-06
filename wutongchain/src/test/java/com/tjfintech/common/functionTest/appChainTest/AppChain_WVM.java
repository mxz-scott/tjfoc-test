package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_WVM {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MgToolCmd mgToolCmd = new MgToolCmd();
    WVMContractTest wvmContractTest = new WVMContractTest();
    FileOperation fileOper = new FileOperation();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();


//    @BeforeClass
    public static void clearPeerDB()throws Exception{
        UtilsClass utilsClass = new UtilsClass();
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }

    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }


    @Test
    public void TC2005_DiffTestFlow_InDiffAppChain() throws Exception{
        String ctName = "A_" + sdf.format(dt) + RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmContractTest.wvmFile + ".txt", wvmContractTest.orgName, ctName);

        //globalAppId1上安装wvm合约
        subLedger = globalAppId1;
        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile + "_temp.txt","");
        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType20);
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);


        //调用合约内的交易
        String response2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType20);

        String response3 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60
        String txHash3 = commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType20);

        sleepAndSaveInfo(SLEEPTIME);

        String response4 = wvmContractTest.invokeNew(ctHash,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer);//A向B转30
        String txHash4 = commonFunc.getTxHash(response4,utilsClass.sdkGetTxHashType20);

        sleepAndSaveInfo(SLEEPTIME);

        //查询余额invoke接口
        String response5 = wvmContractTest.invokeNew(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        String txHash5 = commonFunc.getTxHash(response5,utilsClass.sdkGetTxHashType20);

        String response6 = wvmContractTest.invokeNew(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户A账户余额
        String txHash6 = commonFunc.getTxHash(response6,utilsClass.sdkGetTxHashType20);
        sleepAndSaveInfo(SLEEPTIME/2);
        //查询余额query接口 交易不上链
        String response7 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取转账后账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));


        //globalAppId2上安装合约
        subLedger = globalAppId2;
        //安装合约后会得到合约hash cName确定后 相同sdk合约名会是固定的 因使用sdk的私钥进行绑定创建
        String response11 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile +"_temp.txt","");
        String txHash12 = commonFunc.getTxHash(response11,utilsClass.sdkGetTxHashType20);
        String ctHash12 = JSONObject.fromObject(response11).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);

        //调用合约内的交易
        String response12 = wvmContractTest.invokeNew(ctHash12,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        sleepAndSaveInfo(SLEEPTIME);
        String response13 = wvmContractTest.invokeNew(ctHash12,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60

        sleepAndSaveInfo(SLEEPTIME);
        //查询余额query接口 交易不上链
        String response17 = wvmContractTest.query(ctHash12,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA),JSONObject.fromObject(response17).getJSONObject("data").getString("result"));

        String response18 = wvmContractTest.query(ctHash12,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB),JSONObject.fromObject(response18).getJSONObject("data").getString("result"));

        String response14 = wvmContractTest.invokeNew(ctHash12,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer/2);//A向B转30
        String txHash14 = commonFunc.getTxHash(response14,utilsClass.sdkGetTxHashType20);
        sleepAndSaveInfo(SLEEPTIME);
        //查询余额query接口 交易不上链
        String response27 = wvmContractTest.query(ctHash12,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer/2 ),JSONObject.fromObject(response27).getJSONObject("data").getString("result"));

        String response28 = wvmContractTest.query(ctHash12,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer/2),JSONObject.fromObject(response28).getJSONObject("data").getString("result"));


        //重新切换回globalAppId1查询数据
        subLedger = globalAppId1;
        //查询余额query接口 交易不上链
        String response37 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取转账后账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(response37).getJSONObject("data").getString("result"));

        String response38 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(response38).getJSONObject("data").getString("result"));
    }

    @Test
    public void TC1792_DiffTestFlow_InDiffAppChain() throws Exception{
        String ctName = "A_" + sdf.format(dt) + RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmContractTest.wvmFile + ".txt", wvmContractTest.orgName, ctName);

        //globalAppId1上安装wvm合约
        subLedger = globalAppId1;
        String resp1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile +"_temp.txt","");
        String txHash1 = JSONObject.fromObject(resp1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(resp1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);

        //调用合约内的交易
        String resp2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String resp3 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60

        sleepAndSaveInfo(SLEEPTIME);

        String resp4 = wvmContractTest.invokeNew(ctHash,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(resp4).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //globalAppId2链上安装合约
        subLedger = globalAppId2;
        String response1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile + "_temp.txt","");
        String txHash2 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType20);
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);

        //查询globalAppId1上A、B账户余额
        subLedger = globalAppId1;

        String resp17 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(resp17).getJSONObject("data").getString("result"));

        String resp18 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(resp18).getJSONObject("data").getString("result"));

        //查询globalAppId2链上A、B账户余额 应为0
        subLedger = globalAppId2;

        String response17 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals("0",JSONObject.fromObject(response17).getJSONObject("data").getString("result"));

        String response18 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals("0",JSONObject.fromObject(response18).getJSONObject("data").getString("result"));


        //globalAppId1销毁wvm合约
        subLedger = globalAppId1;
        String resp9 = wvmContractTest.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(resp9).getJSONObject("data").getString("txId");
        sleepAndSaveInfo(SLEEPTIME);
        String response10 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额 报错
        assertThat(JSONObject.fromObject(response10).getString("message"),containsString("This smart contract is destroyed")); //销毁后会提示找不到合约文件 500 error code

        //进行globalAppId2链相关交易
        subLedger = globalAppId2;
        //调用合约内的交易
        String response2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String response3 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60

        sleepAndSaveInfo(SLEEPTIME);
        String response7 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        String response4 = wvmContractTest.invokeNew(ctHash,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer);//A向B转30
        sleepAndSaveInfo(SLEEPTIME);

        response7 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        response8 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

    }

    @Test
    public void TC1792_DiffTestFlow_InDiffAppChain02() throws Exception{
        String ctName = "A_" + sdf.format(dt) + RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmContractTest.wvmFile + ".txt", wvmContractTest.orgName, ctName);

        //globalAppId1上安装wvm合约
        subLedger = globalAppId2;
        String resp1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile +"_temp.txt","");
        String txHash1 = JSONObject.fromObject(resp1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(resp1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);

        //调用合约内的交易
        String resp2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String resp3 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60

        sleepAndSaveInfo(SLEEPTIME);

        String resp4 = wvmContractTest.invokeNew(ctHash,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(resp4).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //globalAppId1链上安装合约
        subLedger = globalAppId1;
        String response1 = wvmContractTest.wvmInstallTest(wvmContractTest.wvmFile + "_temp.txt","");
        String txHash2 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType20);
        String ctHash2 = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        sleepAndSaveInfo(SLEEPTIME);

        //查询globalAppId2上A、B账户余额
        subLedger = globalAppId2;

        String resp17 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(resp17).getJSONObject("data").getString("result"));

        String resp18 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(resp18).getJSONObject("data").getString("result"));

        //查询globalAppId2链上A、B账户余额 应为0
        subLedger = globalAppId1;

        String response17 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals("0",JSONObject.fromObject(response17).getJSONObject("data").getString("result"));

        String response18 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals("0",JSONObject.fromObject(response18).getJSONObject("data").getString("result"));


        //globalAppId1销毁wvm合约
        subLedger = globalAppId2;
        String resp9 = wvmContractTest.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(resp9).getJSONObject("data").getString("txId");
        sleepAndSaveInfo(SLEEPTIME);
        String response10 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额 报错
        assertThat(JSONObject.fromObject(response10).getString("message"),containsString("This smart contract is destroyed")); //销毁后会提示找不到合约文件 500 error code

        //进行globalAppId2链相关交易
        subLedger = globalAppId1;
        //调用合约内的交易
        String response2 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountA,wvmContractTest.amountA);//初始化账户A 账户余额50
        String response3 = wvmContractTest.invokeNew(ctHash,"initAccount",wvmContractTest.accountB,wvmContractTest.amountB);//初始化账户B 账户余额60

        sleepAndSaveInfo(SLEEPTIME);
        String response7 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        String response8 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

        String response4 = wvmContractTest.invokeNew(ctHash,"transfer",wvmContractTest.accountA,wvmContractTest.accountB,wvmContractTest.transfer);//A向B转30
        sleepAndSaveInfo(SLEEPTIME);

        response7 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountA);//获取账户A账户余额
        assertEquals(Integer.toString(wvmContractTest.amountA - wvmContractTest.transfer),JSONObject.fromObject(response7).getJSONObject("data").getString("result"));

        response8 = wvmContractTest.query(ctHash,"BalanceTest",wvmContractTest.accountB);//获取账户B账户余额
        assertEquals(Integer.toString(wvmContractTest.amountB + wvmContractTest.transfer),JSONObject.fromObject(response8).getJSONObject("data").getString("result"));

    }

}
