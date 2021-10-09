package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSDKPerm999;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.FileOperation.setSDKConfigByShell;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    public String category="wvm";
    public String caller=""; // 这个字段旧版本中不能为空
    FileOperation fileOper = new FileOperation();
    public String orgName = "TestExample";
    public String accountA = "A";
    public String accountB = "B";
    public int amountA = 50;
    public int amountB = 60;
    public int transfer = 30;
    public String wvmFile = "wvm";
    long onChainTime = 400;

    @BeforeClass
    public static void setVersion(){
        if(wvmVersion.isEmpty()){
            wvmVersion = "";
        }
        UtilsClass.syncFlag = true;
    }

    @Test
    public void TC001_testContract() throws Exception{

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        String ctName="A_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        fileOper.replace(tempWVMDir + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        long start = System.currentTimeMillis();
        String response1 = wvmInstallTest(wvmFile +"_temp.txt","");
        long end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
        assertThat(response1,containsString("200"));
        assertThat(response1,containsString("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //调用合约内的交易
        start = System.currentTimeMillis();
        String response2 = invokeNew(ctHash,"initAccount",accountA,amountA);//初始化账户A 账户余额50
        end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //销毁wvm合约
        start = System.currentTimeMillis();
        String response9 = wvmDestroyTest(ctHash);
        end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        assertThat(response9,containsString("200"));
        assertThat(response9,containsString("success"));

    }



    @After
    public void resetParam()throws Exception{
        wvmFile = "wvm";
        orgName = "TestExample";
    }


    public String wvmInstallTest(String wvmfile,String Prikey) throws Exception {
        if(wvmfile == "") return contract.InstallWVM("",category,Prikey);

        String filePath = testDataPath + "wvm/" + wvmfile;
        log.info("filepath "+ filePath);
        String file = utilsClass.readInput(filePath).toString().trim();
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        log.info("base64 data: " + data);
        String response=contract.InstallWVM(data,category,Prikey);
        return response;
    }


    public String wvmDestroyTest(String cthash) throws Exception {
        String response = contract.DestroyWVM(cthash,category);
        return response;

    }


    public String invokeNew(String cthash, String method, Object... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.Invoke(cthash, wvmVersion, category,method,caller, args);
        return response;
    }

    public String query(String cthash, String method, String... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.QueryWVM(cthash, wvmVersion, category,method,caller,args);
        return response;
    }

}
