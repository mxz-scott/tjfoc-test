package com.tjfintech.common.functionTest;

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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.encryptBASE64;
import static com.tjfintech.common.utils.UtilsClass.readInput;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");

    String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    String version="2.0";

    public void testContract() throws Exception{
        installTest();
        initMobileTest();
        String response1 = contract.SearchByKey("Mobile0",name);//SDK发送按key查询请求
        String response2 = contract.SearchByPrefix("Mo",name);//SDK发送按prefix查询请求

        createMobileTest();
        Thread.sleep(4000);
        String response3 = contract.SearchByKey("Mobile8",name);//SDK发送按key查询请求

        deleteMobileTest("Mobile8");
        changeMobileCountTest("50","Mobile1");
        String response4 = contract.SearchByKey("Mobile1",name);//SDK发送按key查询请求
        assertThat(response4,containsString("Count:50"));

        getAllMobileTest();
        queryMobileTest("Mobile1");

        eventTest();
        destroyTest();
    }


    public void installTest() throws Exception {
        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.Install(name,version,data);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response,containsString("success"));
        Thread.sleep(SLEEPTIME*15);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }

    public void initMobileTest() throws Exception {
        String method ="initMobile";
        invoke1(method);
    }

    public void createMobileTest() throws Exception {
        String method = "createMobile";
        String brand = "xiaomi";
        String model = "Mix2S";
        String price = "4000.00";
        String count = "black";
        String color = "123";
        String mobileID = "Mobile8";
        invoke1(method, brand, model, price, color, count, mobileID);
    }

    public void deleteMobileTest(String arg) throws Exception{
        String method ="deleteMobile";
        //String arg="Mobile5";
        invoke1(method,arg);
    }

    public void changeMobileCountTest(String count,String mobile) throws Exception{
        String method="changeMobileCount";
//        String arg="55";
//        String arg2="Mobile1";
        invoke1(method,count,mobile);
    }

    public void getAllMobileTest() throws Exception{
        String method="getAllMobile";
        invoke1(method);
    }

    public void eventTest() throws Exception {
        String method = "event";
        invoke1(method);
    }

    public void queryMobileTest(String mobile) throws Exception {
        String method = "queryMobile";
        //String arg = "Mobile8";
        invoke1(method, mobile);

    }

    public void destroyTest() throws Exception {
        String response = contract.Destroy(name, version);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response, containsString("success"));
        Thread.sleep(SLEEPTIME);
        String response2 = store.GetTransaction(hash);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));

    }

    public void invoke1(String method, String... arg) throws Exception {
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

    /**
     * 安装合约
     * @throws Exception
     */
    @Test
    public void TC001_installTest() throws Exception {
        String name="chenxu";//+ Random(5);
        String version="1.0";
        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.Install(name,version,data);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response,containsString("success"));
        Thread.sleep(SLEEPTIME*10);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }
    /**
     * 调用前需初始化
     * @throws Exception
     */
    @Test
    public void TC002_initTest() throws Exception {
        String name="chenxu";
        String version="1.0";
        String method="init";
        List<String>args=new LinkedList<String>();
        args.add("a");
        args.add("200");
        args.add("b");
        args.add("233");
        String response = contract.CreateNewTransaction(name, version, method, args);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }


    /**
     * 合约自定义初始化
     * @throws Exception
     */
    @Test
    public void TC003_initMobileTest() throws Exception {
        String method ="initMobile";
        invoke(method);
    }

    /**
     * 创建新手机信息
     * 需要6个参数 商标 型号 价格 数量 颜色 ID
     *
     * @throws Exception
     */
    @Test
    public void TC004_createMobileTest() throws Exception {
        String method = "createMobile";
        String brand = "xiaomi";
        String model = "Mix2S";
        String price = "4000.00";
        String count = "black";
        String color = "123";
        String mobileID = "Mobile8";
        invoke(method, brand, model, price, color, count, mobileID);
    }

    /**
     * 删除指定手机信息。只删除世界状况中信息。链上信息仍存在
     * @throws Exception
     */
    @Test
    public void TC005_deleteMobileTest() throws Exception{
        String method ="deleteMobile";
        String arg="Mobile5";
        invoke(method,arg);
    }

    /**
     * 修改指定ID的手机数量信息
     * @throws Exception
     */
    @Test
    public void TC006_changeMobileCountTest() throws Exception{
        String method="changeMobileCount";
        String arg="55";
        String arg2="Mobile1";
        invoke(method,arg,arg2);
    }

    /**
     * 遍历所有手机信息
     * @throws Exception
     */
    @Test
    public void TC007_getAllMobileTest() throws Exception{
        String method="getAllMobile";

        invoke(method);
    }

    /**
     * 发送事务至KAFKA
     *
     * @throws Exception
     */
    @Test
    public void TC008_eventTest() throws Exception {
        String method = "event";
        invoke(method);
    }

    /**
     * 查询指定ID的手机信息
     * @throws Exception
     */
    @Test
    public void TC009_queryMobileTest() throws Exception {
        String method = "queryMobile";
        String arg = "Mobile8";
        invoke(method, arg);

    }


    /**
     * 销毁合约
     *
     * @throws Exception
     */
    @Test
    public void TC011_destroyTest() throws Exception {
        String name = "chenxu";
        String version = "1.0";
        String response = contract.Destroy(name, version);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response, containsString("success"));
        Thread.sleep(SLEEPTIME);
        String response2 = store.GetTransaction(hash);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));

    }

    /**
     * 合约Invoke调用方法
     * @param method  方法名
     * @param arg     参数，可多个
     * @throws Exception
     */

    public void invoke(String method, String... arg) throws Exception {
        String name = "chenxu";
        String version = "1.0";
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
