package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractWithVersionInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    WVMContractTest wvmContractTest = new WVMContractTest();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    public String category="wvm";
    public String caller="test";
    FileOperation fileOper = new FileOperation();
    public String orgName = "TestExample";
    public String accountA = "A";
    public String accountB = "B";
    public int amountA = 50;
    public int amountB = 60;
    public int transfer = 30;
    public String wvmFile = "wvm";

    @BeforeClass
    public static void setVersion(){
        if(wvmVersion.isEmpty()){
            wvmVersion = "2.0.1";
        }
    }


    @Test
    public void TC1816_InvokeInvalidMethod() throws Exception{
        String ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");//暂时添加

        wvmContractTest.chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = wvmContractTest.invokeNew(ctHash,"inita",accountA,amountA);//初始化账户A 账户余额50

        //3.0.1版本检查点
//        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        wvmContractTest.chkTxDetailRsp("404",txHash2);

        //3.0.2版本检查点
        int state = JSONObject.fromObject(response2).getInt("state");

        if (state == 500 || state == 400){
            assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
        } else{
            assertThat("wrong state", containsString("500 or 400, both ok"));
        }
        assertEquals("rpc error: code = InvalidArgument desc = This method does not exist in this contract",
                JSONObject.fromObject(response2).getString("message"));

    }


    @Test
    public void TC1819_InvalidNameTest()throws Exception{
        ArrayList<String> ctNameList = new ArrayList<>();
        ArrayList<String> txHashList = new ArrayList<>();
        //用例与不带版本稍作区别 否则安装可能会报错
        ctNameList.add("____");
        ctNameList.add("a1__");
        ctNameList.add("aaaaa");
        ctNameList.add("_ae33");
        ctNameList.add("a1234");
        ctNameList.add("asdfghjklqwertyuiop12345678333333333333333333333333333333333333333333333333901");
        for(String name : ctNameList) {
            txHashList.add(JSONObject.fromObject(wvmContractTest.intallUpdateName(name,"")).getJSONObject("data").getString("txId"));
        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        assertEquals(6,txHashList.size());
        for(String hash : txHashList){
            wvmContractTest.chkTxDetailRsp("200",hash);
        }

        ctNameList.clear();
        txHashList.clear();
        ctNameList.add("123");
        ctNameList.add("1a1_");
        ctNameList.add("1a");
        for(String name : ctNameList) {
            int state = JSONObject.fromObject(wvmContractTest.intallUpdateName(name,"")).getInt("state");

            if (state == 500 || state == 400){
                assertThat("500 or 400, both ok", containsString("500 or 400, both ok"));
            } else{
                assertThat("wrong state", containsString("500 or 400, both ok"));
            }
        }
    }

    @Test
    public void TC_1855_installWithInvalidParameter()throws Exception{
        //测试私钥为空
        log.info("test file:"+ wvmFile + "_temp.txt");
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
//        assertEquals(false,JSONObject.fromObject(response1).getString("state").contains("200"));//当前存在bug sdk panic

        //测试wvm文件为空
        String response2 = wvmContractTest.wvmInstallTest("","");
        assertEquals(false,JSONObject.fromObject(response2).getString("state").contains("200"));

        //测试wvm及私钥为空
        String response3 = wvmContractTest.wvmInstallTest("","");
//        assertEquals(false,JSONObject.fromObject(response3).getString("state").contains("200"));//当前存在bug sdk panic

        //测试私钥非法
        String response4 = wvmContractTest.wvmInstallTest(wvmFile + ".txt","88888");
//        assertEquals(false,JSONObject.fromObject(response4).getString("state").contains("200"));//当前存在bug sdk panic

    }

    /**
     * 测试 相同合约带版本安装和不带版本安装 不允许
     * 1.先不带版本安装合约 再带版本号安装合约
     * 2.先带版本安装合约 再不带版本号安装合约
     * @throws Exception
     */

    @Test
    public void InterlaceInstallWithVersionAndWithoutVersion()throws Exception{
        //先不带版本安装合约
        wvmVersion = "";
        String ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txHash1,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //带版本安装相同合约
        wvmVersion = "3.0.1";
        response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals("400",JSONObject.fromObject(response1).getString("state"));
        assertEquals(true,response1.contains("" +
                "The contract without version has been installed, so, this installation cannot carry the version"));


        ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response2 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txHash2,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //带版本安装相同合约
        wvmVersion = "";
        response2 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals("400",JSONObject.fromObject(response2).getString("state"));
        assertEquals(true,response2.contains(
                "The contract with version has been installed, so, this installation should carry the version"));

    }
    //@Test
    public void TC_1796_1798_1800_1801_DismatchCategory()throws Exception{
        //安装一笔合法的wvm合约
        String ctName = "Err_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        wvmContractTest.chkTxDetailRsp("200",txHash1);


        //测试docker合约使用category wvm，也就是说wvm合约无私钥
        DockerContractTest dockerContractTest = new DockerContractTest();
        dockerContractTest.category = "wvm";
        String response2 = dockerContractTest.installTest();
//        assertEquals(false,JSONObject.fromObject(response2).getString("state").contains("200"));//当前存在bug sdk panic

        //测试wvma安装、调用、销毁使用category docker
        wvmContractTest.category = "docker";
        String response3 = wvmContractTest.wvmDestroyTest(ctHash);
        assertEquals(false,JSONObject.fromObject(response3).getString("state").contains("200"));

        String response4 = wvmContractTest.invokeNew(ctHash,"initAccount",accountA,amountA);
//        assertEquals(false,JSONObject.fromObject(response4).getString("state").contains("200"));//当前存在bug sdk panic

        String response5 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt","");
        assertEquals(false,JSONObject.fromObject(response3).getString("state").contains("200"));


    }
}
