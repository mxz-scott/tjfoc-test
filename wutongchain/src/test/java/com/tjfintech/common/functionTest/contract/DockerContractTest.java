package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.*;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class DockerContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    MgToolCmd mgToolCmd = new MgToolCmd();

    public String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    public String version="2.1";
    public String category="docker";

    @BeforeClass
    public static void beforeCondition()throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.setPermission999();
    }

    //simple.go合约测试
    @Test
    public void testContract() throws Exception{

        String response = null;
        dockerFileName="simple.go";
        log.info(name);
        //检查合约创建
        response=installTest();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,ContractInstallSleep);

        //确认所有节点均同步
        long nowTimeSync = (new Date()).getTime();
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER2IP + ":" + PEER2RPCPort,30*1000);
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER4IP + ":" + PEER4RPCPort,30*1000);
        log.info("等待节点同步合约时间 " + ((new Date()).getTime() - nowTimeSync));


        String response1=store.GetTxDetail(hash);
        assertThat(response1,containsString("200"));
        assertThat(response1,containsString("success"));


        //检查合约交易接口
        response=initMobileTest();
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");//暂时添加 需要确认是否是这个问题

        String hash11 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(store.GetTxDetail(hash11),containsString("200"));

        String response2 = contract.SearchByKey("Mobile0",name);//SDK发送按key查询请求
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("HUAWEI"));

        String response3 = contract.SearchByPrefix("Mo",name);//SDK发送按prefix查询请求
        assertThat(response3,containsString("200"));
        assertThat(response3,containsString("Mobile1"));

        response=createMobileTest();
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        String response4 = contract.SearchByKey("Mobile8",name);//SDK发送按key查询请求
        assertThat(response4,containsString("200"));
        assertThat(response4,containsString("xiaomi"));


        response=deleteMobileTest("Mobile8");
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String response5 = contract.SearchByPrefix("Mo",name);//SDK发送按prefix查询请求
        assertThat(response5,containsString("200"));
        assertThat(response5,containsString("Mobile1"));
        assertEquals(response5.contains("Mobile8"),false);

        response=changeMobileCountTest("50","Mobile1");
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String response6 = contract.SearchByKey("Mobile1",name);//SDK发送按key查询请求
        assertThat(response6,containsString("\\\"count\\\":50"));

        response=getAllMobileTest();
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        response=store.GetTxDetail(hash1);
        assertThat(response,containsString("Mobile1"));
        assertThat(response,containsString("Mobile2"));
        assertThat(response,containsString("Mobile3"));


        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash2 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        response=store.GetTxDetail(hash2);
        assertThat(response,containsString("iphoneXS"));

        response=eventTest();
        assertThat(response,containsString("200"));

        //测试2.0.1兼容CreateNewTransaction接口 默认category为docker
//        invokeUpdate("initMobile");

        //销毁合约
        response=destroyTest();
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info(name);
        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        log.info(name);
        response=store.GetTxDetail(hash3);
        assertThat(response,containsString("failed to find transaction"));

    }


    //合约white.go sales.go测试跨合约调用
    @Test
    public void TC2105_testCrossContractTxNewSales()throws Exception{
        //subLedger = "";
        //sales.go 调用whitelist.go中的接口
        String crossLedger = "";
        if(subLedger.isEmpty()) crossLedger = "main";
        else crossLedger = subLedger;

        String response=null;
        category="docker";
        String name1="sn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        String name2="wn" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        assertEquals(name1.equals(name2),false);

        //安装第一个合约 销售
        name=name1;
        dockerFileName="\\file1\\sales.go";
        log.info("docker file 1: "+name1);
        response=installTest();
        assertThat(response,containsString("200"));

        //安装第二个合约 白名单
        name=name2;
        dockerFileName="\\file2\\whitelist.go";
        log.info("docker file 2: "+name2);
        response=installTest();
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,ContractInstallSleep);
//        sleepAndSaveInfo(20 * 1000,"等待合约同步");
        //确认所有节点均同步
        long nowTimeSync = (new Date()).getTime();
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER2IP + ":" + PEER2RPCPort,30*1000);
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER4IP + ":" + PEER4RPCPort,30*1000);
        log.info("等待节点同步合约时间 " + ((new Date()).getTime() - nowTimeSync));

        //跨合约调用
        log.info("正常跨合约调用");
        name=name1;
        response=addSalesInfoNew("Company01",123456,name2,crossLedger);
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash3);
        String contractResult = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertThat(contractResult,containsString("success"));

        //重复添加 则显示已存在信息
        log.info("跨合约调用接口重复添加信息");
        response=addSalesInfoNew("Company01",123456,name2,crossLedger);
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash4 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash4);
        String contractResult1 = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Message");
        assertThat(contractResult1,containsString("this data is exist"));

        //调用不存在的合约
        log.info("跨不存在的合约调用接口");
        response=addSalesInfoNew("Company02",2356,"tt",crossLedger);
        assertThat(response,containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String hash5 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash5);
        String contractResult2 = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload");
        assertThat(contractResult2,containsString("does not exist"));

        name=name1;
        destroyTest();

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        name=name2;
        destroyTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    public String installTest() throws Exception {
        //String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String filePath =resourcePath + dockerFileName;
        String file=utilsClass.readInput(filePath).toString();
        String data = utilsClass.encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.Install(name,version,category,data);
        return response;
    }

    public String initMobileTest() throws Exception {
        String method ="initMobile";
        return invokeNew(method);
    }

    public String createMobileTest() throws Exception {
        String method = "createMobile";
        String brand = "xiaomi";
        String model = "Mix2S";
        String price = "4000.00";
        String count = "black";
        String color = "123";
        String mobileID = "Mobile8";
        return invokeNew(method, brand, model, price, color, count, mobileID);
    }

    public String deleteMobileTest(String arg) throws Exception{
        String method ="deleteMobile";
        //String arg="Mobile5";
        return invokeNew(method,arg);
    }

    public String changeMobileCountTest(String count,String mobile) throws Exception{
        String method="changeMobileCount";
//        String arg="55";
//        String arg2="Mobile1";
        return invokeNew(method,count,mobile);
    }

    public String getAllMobileTest() throws Exception{
        String method="getAllMobile";
        return invokeNew(method);
    }

    public String eventTest() throws Exception {
        String method = "event";
        return invokeNew(method);
    }

    public String queryMobileTest(String mobile) throws Exception {
        String method = "queryMobile";
        //String arg = "Mobile8";
        return invokeNew(method, mobile);

    }

    public String destroyTest() throws Exception {
        String response = contract.Destroy(name, version,category);
        return response;

    }


    public String addSalesInfoNew(String compID,int sales,String anoDockerName,String ledgerName) throws Exception {
        String method = "addSalesInfo";
        Map<String, Object> map = new HashMap<>();
        map.put("CompanyID", compID);
        map.put("Sales", sales);
        JSONObject json = JSONObject.fromObject(map);
        String a ="\""+json.toString()+"\"";
        return invokeNew(method,a,"",version,anoDockerName,ledgerName);
    }

    public String addSalesInfo(String compID,int sales,String anoDockerName) throws Exception {
        String method = "addSalesInfo";
        Map<String, Object> map = new HashMap<>();
        map.put("CompanyID", compID);
        map.put("Sales", sales);
        JSONObject json = JSONObject.fromObject(map);
        String a ="\""+json.toString()+"\"";
        return invokeNew(method,a,"",version,anoDockerName);
    }

    public String invokeNew(String method, String... arg) throws Exception {
        List<String> args = new LinkedList<>();
        for (int i = 0; i < arg.length; i++) {
            args.add(arg[i]);
        }
        String response = contract.Invoke(name, version, category,method, args);
        return response;
    }

//    public void invokeUpdate(String method, String... arg) throws Exception {
//        List<String> args = new LinkedList<>();
//        for (int i = 0; i < arg.length; i++) {
//            args.add(arg[i]);
//        }
//        String response = contract.CreateNewTransaction(name, version, method, args);
//        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
//        sleepAndSaveInfo(SLEEPTIME);
//        String result = store.GetTxDetail(hash);
//        assertThat(result, containsString("200"));
//        assertThat(result, containsString("success"));
//
//    }

}
