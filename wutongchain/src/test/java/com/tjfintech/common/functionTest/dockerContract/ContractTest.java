package com.tjfintech.common.functionTest.dockerContract;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
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
public class ContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");

    public String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    public String version="2.0";
    public String category="docker";

    @Test
    public void testContract() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.collAddressTest();


        String response=null;
        log.info(name);
        //检查合约创建
        response=installTest();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        Thread.sleep(ContractInstallSleep);
        String response1=store.GetTransaction(hash);
        Thread.sleep(5000);
        assertThat(response1,containsString("200"));
        assertThat(response1,containsString("success"));


        //检查合约交易接口
        response=initMobileTest();
        assertThat(response,containsString("200"));
        Thread.sleep(SLEEPTIME);
        String response2 = contract.SearchByKey("Mobile0",name);//SDK发送按key查询请求
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("HUAWEI"));

        String response3 = contract.SearchByPrefix("Mo",name);//SDK发送按prefix查询请求
        assertThat(response3,containsString("200"));
        assertThat(response3,containsString("Mobile1"));

        response=createMobileTest();
        assertThat(response,containsString("200"));
        Thread.sleep(7000);
        String response4 = contract.SearchByKey("Mobile8",name);//SDK发送按key查询请求
        assertThat(response4,containsString("200"));
        assertThat(response4,containsString("xiaomi"));


        response=deleteMobileTest("Mobile8");
        assertThat(response,containsString("200"));
        Thread.sleep(5000);
        String response5 = contract.SearchByPrefix("Mo",name);//SDK发送按prefix查询请求
        assertThat(response5,containsString("200"));
        assertThat(response5,containsString("Mobile1"));
        assertEquals(response5.contains("Mobile8"),false);

        response=changeMobileCountTest("50","Mobile1");
        assertThat(response,containsString("200"));
        Thread.sleep(5000);
        String response6 = contract.SearchByKey("Mobile1",name);//SDK发送按key查询请求
        assertThat(response6,containsString("\\\"count\\\":50"));

        response=getAllMobileTest();
        assertThat(response,containsString("200"));
        Thread.sleep(6000);
        String hash1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        response=store.GetTransaction(hash1);
        assertThat(response,containsString("Mobile1"));
        assertThat(response,containsString("Mobile2"));
        assertThat(response,containsString("Mobile3"));


        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("200"));
        Thread.sleep(5000);
        String hash2 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        response=store.GetTransaction(hash2);
        assertThat(response,containsString("iphoneXS"));

        response=eventTest();
        assertThat(response,containsString("200"));

        //测试2.0.1兼容CreateNewTransaction接口 默认category为docker
        invokeUpdate("initMobile");

        //销毁合约
        response=destroyTest();
        assertThat(response,containsString("200"));
        Thread.sleep(6000);
        log.info(name);
        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("200"));
        Thread.sleep(6000);
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        log.info(name);
        response=store.GetTransaction(hash3);
        assertThat(response,containsString("failed to find transaction"));


    }

    @Test
    public void testContractErrInvoke() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.collAddressTest();


        String response=null;
        log.info(name);
        dockerFileName="simple_err1.go";
        //检查合约创建
        response=installTest();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        //安装后恢复dockerFileName为默认好的simple.go
        dockerFileName="simple.go";
        Thread.sleep(ContractInstallSleep);
        String response1=store.GetTransaction(hash);
        assertThat(response1,containsString("200"));
        assertThat(response1,containsString("success"));


        //检查合约交易接口
        response=eventTest();
        assertThat(response,containsString("200"));

        Thread.sleep(36000); //合约timeout时间
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        log.info(name);
        response=store.GetTransaction(hash3);
        assertThat(response,containsString("failed to find transaction"));

        //销毁合约
        response=destroyTest();
        assertThat(response,containsString("200"));
        Thread.sleep(3000);
    }

    @Test
    public void InvalidTestCtr()throws Exception{

        String response=null;
        category="";
        response=installTest();
        assertThat(response,containsString("error Category or empty Category!!"));

        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("error Category or empty Category!!"));

        response=destroyTest();
        assertThat(response,containsString("error Category or empty Category!!"));

        category="222";
        response=installTest();
        assertThat(response,containsString("error Category or empty Category!!"));

        response=queryMobileTest("Mobile1");
        assertThat(response,containsString("error Category or empty Category!!"));

        response=destroyTest();
        assertThat(response,containsString("error Category or empty Category!!"));

        category="docker";
        name="A12"+RandomUtils.nextInt(100000);
        response=installTest();
        assertThat(response,containsString("Invalid contract name"));

        name=sdf.format(dt)+ RandomUtils.nextInt(100000);
        version=".0";
        response=installTest();
        assertThat(response,containsString("Invalid contract version [.0]"));

        version="";
        response=installTest();
        assertThat(response,containsString("Invalid contract version []"));
        version="2.0";
    }

    @Test
    public void testCrossContractTx()throws Exception{
        //sales.go 调用whitelist.go中的接口
        String response=null;
        category="docker";
        String name1=sdf.format(dt)+ RandomUtils.nextInt(100000);
        String name2=sdf.format(dt)+ RandomUtils.nextInt(100000);
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

        Thread.sleep(ContractInstallSleep);
        Thread.sleep(15 * 1000);

        //跨合约调用
        log.info("正常跨合约调用");
        name=name1;
        response=addSalesInfo("Company01",123456,name2);
        assertThat(response,containsString("200"));
        Thread.sleep(SLEEPTIME*2);
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash3);
        String contractResult = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("contractResult").getString("payload");
        assertThat(contractResult,containsString("success"));

        //重复添加 则显示已存在信息
        log.info("跨合约调用接口重复添加信息");
        response=addSalesInfo("Company01",123456,name2);
        assertThat(response,containsString("200"));
        Thread.sleep(SLEEPTIME);
        String hash4 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash4);
        String contractResult1 = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("contractResult").getString("message");
        assertThat(contractResult1,containsString("this data is exist"));

        //调用不存在的合约
        log.info("跨不存在的合约调用接口");
        response=addSalesInfo("Company02",2356,"tt");
        assertThat(response,containsString("200"));
        Thread.sleep(SLEEPTIME);
        String hash5 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        response=store.GetTxDetail(hash5);
        String contractResult2 = JSONObject.fromObject(response).getJSONObject("Data").getJSONObject("Contract").getJSONObject("contractResult").getString("payload");
        assertThat(contractResult2,containsString("does not exist"));

        name=name1;
        destroyTest();
        Thread.sleep(SLEEPTIME);
        name=name2;
        destroyTest();
        Thread.sleep(SLEEPTIME);
    }



    public String installTest() throws Exception {
        //String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String filePath = System.getProperty("user.dir") + "/src/main/resources/"+dockerFileName;
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
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

    public void invokeUpdate(String method, String... arg) throws Exception {
        List<String> args = new LinkedList<>();
        for (int i = 0; i < arg.length; i++) {
            args.add(arg[i]);
        }
        String response = contract.CreateNewTransaction(name, version, method, args);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String result = store.GetTransaction(hash);
        assertThat(result, containsString("200"));
        assertThat(result, containsString("success"));

    }

}
