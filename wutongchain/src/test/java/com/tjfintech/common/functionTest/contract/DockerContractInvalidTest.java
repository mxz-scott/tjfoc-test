package com.tjfintech.common.functionTest.contract;

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class DockerContractInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    public String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    public String version="2.1";
    public String category="docker";


    //调用不存在的方法 调用不存在的合约
    @Test
    public void testContractErrInvoke() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.setPermission999();


        String response=null;
        log.info(name);
        dockerFileName="simple_err1.go"; //无event方法
        //检查合约创建
        response=installTest();
        assertThat(response,containsString("200"));
        assertThat(response,containsString("success"));
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");

        //安装后恢复dockerFileName为默认好的simple.go
        dockerFileName="simple.go";
        sleepAndSaveInfo(ContractInstallSleep);
        String response1=store.GetTxDetail(hash);
        assertThat(response1,containsString("200"));
        assertThat(response1,containsString("success"));


        //检查合约交易接口
        response=eventTest();
        assertThat(response,containsString("200"));

        sleepAndSaveInfo(36000); //合约timeout时间
        String hash3 = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        log.info(name);
        response=store.GetTxDetail(hash3);
        assertThat(response,containsString("Invalid method"));
//        assertThat(response,containsString("failed to find transaction"));

        //销毁合约
        response=destroyTest();
        assertThat(response,containsString("200"));
        sleepAndSaveInfo(3000);

        //调用不存在的合约
        name="noexisting";
        response=queryMobileTest("Mobile1");
//        assertThat(response,containsString("error Category or empty Category!!"));
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

        //TC1797 TC1799
        category="wvm"; //category设置合法的但不匹配的合约类型wvm  此部分未调试
        response=installTest();
        assertThat(response,containsString("prikey cannot be empty !"));

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



    public String installTest() throws Exception {
        //String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String filePath =resourcePath + dockerFileName;
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
