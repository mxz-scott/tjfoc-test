package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.Iterator;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain_DockerContract {
    TestBuilder testBuilder = TestBuilder.getInstance();

    private static String subLedgerA = "Le_g.5";
    private static String subLedgerB = "Leg6";

    DockerContractTest dockerContractTest = new DockerContractTest();
    Store store =testBuilder.getStore();
    Contract contract = testBuilder.getContract();
    //MgToolCmd mgToolCmd = new MgToolCmd();
    BeforeCondition beforeCondition = new BeforeCondition();

    String changeCount1 = "123";
    String changeCount2 = "456";

    @BeforeClass
    public static void clearData() throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.setPermission999();

        mapledgerDockerName.clear(); //执行前清空列表
        sleepAndSaveInfo(SLEEPTIME);

        MgToolCmd mgToolCmd = new MgToolCmd();
        String resp = mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, ""); //获取子链的信息

        if (!resp.contains("\"name\": \"" + subLedgerA + "\"")) {//如果子链中不包含subLedgerA就新建一条子链
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerA, " -t sm3",
                    " -w subA", " -c raft", ids);
            Thread.sleep(SLEEPTIME * 2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerA + "\""), true);
        }

        if (!resp.contains("\"name\": \"" + subLedgerB + "\"")) {//如果子链中不包含subLedgerB就新建一条子链
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerB, " -t sm3",
                    " -w subA", " -c raft", ids);
            Thread.sleep(SLEEPTIME * 2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerB + "\""), true);
        }
    }

//该class中的测试项目前存在bug 20191030 会测试失败
    @Test
    public void TC2126_testIsolationMMSS_SameName()throws Exception{
        changeCount1 = "2126123";
        changeCount2 = "2126456";
        String dockerName1 = "tc2126" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = "";

        //主链创建合约 执行合约交易 合约交易中包含创建新mobile信息 修改Mobile1 数量为changeCount1
        String testResp2 = dockerCreateExeTx(dockerName1,true,true,true, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp2.contains("Mobile8")); //新添加的mobile型号信息


        subLedger = subLedgerA;
        //子链创建合约 执行合约交易 合约交易中不会创建新mobile信息 修改Mobile1 数量为changeCount2
        String testResp1 = dockerCreateExeTx(dockerName1,true,true,false, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp1.contains("Mobile8")); //新添加的mobile型号信息

        assertNotEquals(testResp2,testResp1);

        subLedger = "";
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = subLedgerA;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2124_testIsolationSSMM_SameName()throws Exception{
        changeCount1 = "2124123";
        changeCount2 = "2124456";
        String dockerName1 = "tc2124" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = subLedgerA;

        //子链创建合约 执行合约交易 合约交易中包含创建新mobile信息 修改Mobile1 数量为changeCount1
        String testResp2 = dockerCreateExeTx(dockerName1,true,true,true, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp2.contains("Mobile8")); //新添加的mobile型号信息


        subLedger = "";
        //主链创建合约 执行合约交易 合约交易中不会创建新mobile信息 修改Mobile1 数量为changeCount2
        String testResp1 = dockerCreateExeTx(dockerName1,true,true,false, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp1.contains("Mobile8")); //新添加的mobile型号信息

        assertNotEquals(testResp2,testResp1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = "";
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2118_testIsolationMMSS_DiffName()throws Exception{
        changeCount1 = "2118123";
        changeCount2 = "2118456";
        String dockerName1 = "tc2118a" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = "";

        //主链创建合约 执行合约交易 合约交易中包含创建新mobile信息 修改Mobile1 数量为changeCount1
        String testResp2 = dockerCreateExeTx(dockerName1,true,true,true, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = subLedgerA;
        String dockerName2 = "tc2118b" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);

        //子链创建合约 执行合约交易 合约交易中不会创建新mobile信息 修改Mobile1 数量为changeCount2
        String testResp1 = dockerCreateExeTx(dockerName2,true,true,false, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp1.contains("Mobile8")); //新添加的mobile型号信息

        assertNotEquals(testResp2,testResp1);

        subLedger = "";
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName2;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }



    @Test
    public void TC2120_testIsolationSSMM_DiffName()throws Exception{
        changeCount1 = "2120123";
        changeCount2 = "2120456";
        String dockerName1 = "tc2120a" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = subLedgerA;

        //子链创建合约 执行合约交易 合约交易中包含创建新mobile信息 修改Mobile1 数量为changeCount1
        String testResp2 = dockerCreateExeTx(dockerName1,true,true,true, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = "";
        String dockerName2 = "tc2120b" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        //主链创建合约 执行合约交易 合约交易中不会创建新mobile信息 修改Mobile1 数量为changeCount2
        String testResp1 = dockerCreateExeTx(dockerName2,true,true,false, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp1.contains("Mobile8")); //新添加的mobile型号信息

        assertNotEquals(testResp2,testResp1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = "";
        dockerContractTest.name = dockerName2;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2119_testIsolationMSMS_DiffName()throws Exception{
        changeCount1 = "2119123";
        changeCount2 = "2119456";

        String dockerName1 = "tc2119a" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = "";
        //主链只创建合约 不执行合约交易
        String testResp2 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp2);

        subLedger = subLedgerA;
        String dockerName2 = "tc2119b" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        //子链只创建合约 不执行合约交易
        String testResp1 = dockerCreateExeTx(dockerName2,true,false,false,"");
        log.info(testResp1);


        subLedger = "";
        //主链不创建合约 执行合约交易 合约交易不创建新mobile信息 修改Mobile1 数量为changeCount1
        testResp2 = dockerCreateExeTx(dockerName1,false,true,false, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = subLedgerA;
        //子链不创建合约 执行合约交易 合约交易中创建新mobile信息 修改Mobile1 数量为changeCount2
        testResp1 = dockerCreateExeTx(dockerName2,false,true,true, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp1.contains("Mobile8")); //新添加的mobile型号信息


        assertNotEquals(testResp2,testResp1);

        subLedger = "";
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName2;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2125_testIsolationMSMS_SameName()throws Exception{
        changeCount1 = "2125123";
        changeCount2 = "2125456";
        String dockerName1 = "tc2125" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = "";

        //主链只创建合约 不执行合约交易
        String testResp2 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp2);

        subLedger = subLedgerA;
        //子链只创建合约 不执行合约交易
        String testResp1 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp1);


        subLedger = "";
        //主链不创建合约 执行合约交易 合约交易不创建新mobile信息 修改Mobile1 数量为changeCount1
        testResp2 = dockerCreateExeTx(dockerName1,false,true,false, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = subLedgerA;
        //子链不创建合约 执行合约交易 合约交易中创建新mobile信息 修改Mobile1 数量为changeCount2
        testResp1 = dockerCreateExeTx(dockerName1,false,true,true, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp1.contains("Mobile8")); //新添加的mobile型号信息


        assertNotEquals(testResp2,testResp1);

        subLedger = "";
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName1;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2123_testIsolationSMSM_SameName()throws Exception{
        changeCount1 = "2123123";
        changeCount2 = "2123456";
        String dockerName1 = "tc2123" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = subLedgerA;

        //子链只创建合约 不执行合约交易
        String testResp2 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp2);

        subLedger = "";
        //主链只创建合约 不执行合约交易
        String testResp1 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp1);


        subLedger = subLedgerA;
        //子链不创建合约 执行合约交易 合约交易不创建新mobile信息 修改Mobile1 数量为changeCount1
        testResp2 = dockerCreateExeTx(dockerName1,false,true,false, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = "";
        //主链不创建合约 执行合约交易 合约交易中创建新mobile信息 修改Mobile1 changeCount2
        testResp1 = dockerCreateExeTx(dockerName1,false,true,true, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp1.contains("Mobile8")); //新添加的mobile型号信息


        assertNotEquals(testResp2,testResp1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = "";
        dockerContractTest.name = dockerName1;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    @Test
    public void TC2121_testIsolationSMSM_DiffName()throws Exception{
        changeCount1 = "2121123";
        changeCount2 = "2121456";
        String dockerName1 = "tc2121a" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);
        subLedger = subLedgerA;

        //子链只创建合约 不执行合约交易
        String testResp2 = dockerCreateExeTx(dockerName1,true,false,false,"");
        log.info(testResp2);

        subLedger = "";
        String dockerName2 = "tc2121b" + sdf.format(dt).substring(4) + RandomUtils.nextInt(1000);

        //主链只创建合约 不执行合约交易
        String testResp1 = dockerCreateExeTx(dockerName2,true,false,false,"");
        log.info(testResp1);


        subLedger = subLedgerA;
        //子链不创建合约 执行合约交易 合约交易不创建新mobile信息 修改Mobile1 数量为changeCount1
        testResp2 = dockerCreateExeTx(dockerName1,false,true,false, changeCount1);
        log.info(testResp2);
        assertNotEquals("{}",testResp2);
        assertEquals(true,testResp2.contains(changeCount1));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(false,testResp2.contains("Mobile8")); //新添加的mobile型号信息

        subLedger = "";
        //主链不创建合约 执行合约交易 合约交易中创建新mobile信息 修改Mobile1 changeCount2
        testResp1 = dockerCreateExeTx(dockerName2,false,true,true, changeCount2);
        log.info(testResp1);
        assertNotEquals("{}",testResp1);
        assertEquals(true,testResp1.contains(changeCount2));//新修改的mobile1数量 确保当前没有mobile信息中没有相同数量存在
        assertEquals(true,testResp1.contains("Mobile8")); //新添加的mobile型号信息


        assertNotEquals(testResp2,testResp1);

        subLedger = subLedgerA;
        dockerContractTest.name = dockerName1;
        String response1 = dockerContractTest.getAllMobileTest();
        assertThat(response1, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        response1 = store.GetTxDetail(hash1);
        String mainMsg1 = JSONObject.fromObject(response1).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp2,mainMsg1);

        subLedger = "";
        dockerContractTest.name = dockerName2;
        String response2 = dockerContractTest.getAllMobileTest();
        assertThat(response2, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME);
        String hash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");
        response2 = store.GetTxDetail(hash2);
        String mainMsg2 = JSONObject.fromObject(response2).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertEquals(testResp1,mainMsg2);

    }

    //合约white.go sales.go测试跨合约调用 主链调用子链合约 即主链sales 调子链white
    //当前允许调用 bug1002293
    //@Test
    public void TC2106_testCrossContractTxNewSalesMS()throws Exception{
        String Ledger = "";
        //sales.go 调用whitelist.go中的接口
        String crossLedger = "";
        SetSubLedger setSubLedger = new SetSubLedger();
        setSubLedger.createSubledger();
        Ledger = subLedger;
        crossLedger = subLedger;

        String response=null;
        dockerContractTest.category="docker";
        String name1="sn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String name2="wn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        assertEquals(name1.equals(name2),false);

        //安装第一个合约 销售
        subLedger = "";
        dockerContractTest.name=name1;
        dockerFileName="\\file1\\sales.go";
        log.info("docker file 1: "+name1);
        response=dockerContractTest.installTest();
        assertThat(response,containsString("200"));
        mapledgerDockerName.put(subLedger,dockerContractTest.name);

        //安装第二个合约 白名单

        subLedger = Ledger;
        dockerContractTest.name=name2;
        dockerFileName="\\file2\\whitelist.go";
        log.info("docker file 2: "+name2);
        response=dockerContractTest.installTest();
        assertThat(response,containsString("200"));
        mapledgerDockerName.put(subLedger,dockerContractTest.name);

        sleepAndSaveInfo(ContractInstallSleep);
//        sleepAndSaveInfo(30 * 1000);

        //跨合约调用
        subLedger = "";
        log.info("主链跨合约调用子链合约");
        dockerContractTest.name=name1;
        response=dockerContractTest.addSalesInfoNew("Company01",123456,name2,crossLedger);
        assertThat(response,containsString("200"));
        sleepAndSaveInfo(SLEEPTIME*2);
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash3);
        String contractResult = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertThat(contractResult,containsString("not found "));
    }


    //合约white.go sales.go测试跨合约调用 主链调用子链合约 即子链sales 调主链white
    //当前允许调用 bug1002293
    //@Test
    public void TC2108_testCrossContractTxNewSalesMS()throws Exception{
        String Ledger = "";
        //sales.go 调用whitelist.go中的接口
        String crossLedger = "main";
        SetSubLedger setSubLedger = new SetSubLedger();
        setSubLedger.createSubledger();
        Ledger = subLedger;

        String response=null;
        dockerContractTest.category="docker";
        String name1="sn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String name2="wn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        assertEquals(name1.equals(name2),false);

        //安装第一个合约 销售
        dockerContractTest.name=name1;
        dockerFileName="\\file1\\sales.go";
        log.info("docker file 1: "+name1);
        response=dockerContractTest.installTest();
        assertThat(response,containsString("200"));
        mapledgerDockerName.put(subLedger,dockerContractTest.name);

        //安装第二个合约 白名单
        subLedger = "";

        dockerContractTest.name=name2;
        dockerFileName="\\file2\\whitelist.go";
        log.info("docker file 2: "+name2);
        response=dockerContractTest.installTest();
        assertThat(response,containsString("200"));
        mapledgerDockerName.put(subLedger,dockerContractTest.name);

        sleepAndSaveInfo(ContractInstallSleep);

        //验证子链跨合约调用主链
        subLedger = Ledger;
        log.info("子链跨合约调用主链合约");
        dockerContractTest.name=name1;
        response=dockerContractTest.addSalesInfoNew("Company01",123456,name2,crossLedger);
        assertThat(response,containsString("200"));
        sleepAndSaveInfo(SLEEPTIME*2);
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash3);
        String contractResult = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertThat(contractResult,containsString("not found "));
    }



    public String dockerCreateExeTx(String dockerName,boolean bCreate,boolean bInvoke,boolean bCreateMobile,String changeCount)throws Exception{
        log.info(subLedger + " will do:"+ "合约 " + dockerName +" 创建:" + bCreate + " 执行合约交易:" + bInvoke +
                " 执行创建新mobile信息交易:" + bCreateMobile + " 修改Mobile1 数量为:" + changeCount);
        assertEquals(true,bCreate || bInvoke); //确认会执行创建或者执行合约交易
        String response = null;
        dockerFileName = "simple.go";
        dockerContractTest.name = dockerName;
        log.info("create docker " + dockerContractTest.name);
        String retMsg = "";

        //检查是否执行合约创建
        if(bCreate) {
            response = dockerContractTest.installTest();
            mapledgerDockerName.put(subLedger,dockerContractTest.name);//添加到map中以方便后面删除
            assertThat(response, containsString("200"));
            assertThat(response, containsString("success"));
            String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

            sleepAndSaveInfo(ContractInstallSleep);
            String response1 = store.GetTxDetail(hash);
            assertThat(response1, containsString("200"));
            assertThat(response1, containsString("success"));
            retMsg = "create docker" + dockerContractTest.name;
        }

        //检查是否执行合约交易
        if(bInvoke) {
            //检查合约交易接口
            response = dockerContractTest.initMobileTest();
            assertThat(response, containsString("200"));

            sleepAndSaveInfo(SLEEPTIME);
            String hash11 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
            assertThat(store.GetTxDetail(hash11), containsString("200"));


            if (bCreateMobile) {
                response = dockerContractTest.createMobileTest();
                assertThat(response, containsString("200"));
                sleepAndSaveInfo(SLEEPTIME);
                String response4 = contract.SearchByKey("Mobile8", dockerContractTest.name);//SDK发送按key查询请求
                assertThat(response4, containsString("200"));
                assertThat(response4, containsString("xiaomi"));
            }


            response = dockerContractTest.changeMobileCountTest(changeCount, "Mobile1");
            assertThat(response, containsString("200"));
            sleepAndSaveInfo(SLEEPTIME);
            String response6 = contract.SearchByKey("Mobile1", dockerContractTest.name);//SDK发送按key查询请求
            assertThat(response6, containsString("\\\"count\\\":" + changeCount));

            response = dockerContractTest.getAllMobileTest();
            assertThat(response, containsString("200"));
            sleepAndSaveInfo(SLEEPTIME);
            String hash1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
            response = store.GetTxDetail(hash1);
            retMsg = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        }

        return retMsg;
    }

    @Test
    public void crossDockerTestMS()throws Exception{
        //主链上执行跨合约调用
        subLedger = "";
        dockerContractTest.TC2105_testCrossContractTxNewSales();

        //子链上执行跨合约调用
        SetSubLedger setSubLedger = new SetSubLedger();
        setSubLedger.createSubledger();
        dockerContractTest.TC2105_testCrossContractTxNewSales();
    }


    @AfterClass
    public static void destoryDocker()throws Exception{
        DockerContractTest dockerCtrTest = new DockerContractTest();
        Iterator iter = mapledgerDockerName.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            subLedger = key.toString();
            dockerCtrTest.name = mapledgerDockerName.get(key);

            dockerCtrTest.destroyTest();
            sleepAndSaveInfo(SLEEPTIME,subLedger + "destory " + dockerCtrTest.name + " waiting......");
        }
    }
}
