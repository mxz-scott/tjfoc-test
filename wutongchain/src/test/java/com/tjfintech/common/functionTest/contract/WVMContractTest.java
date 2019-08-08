package com.tjfintech.common.functionTest.contract;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class WVMContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");

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
    public  static void setPermFull()throws Exception{
        BeforeCondition bf = new BeforeCondition();
        bf.collAddressTest(); //给主链或者子链赋权限999
    }

    @Test
    public void TC1774_1784_1786_testContract() throws Exception{

        String ctName=sdf.format(dt)+ RandomUtils.nextInt(100000);

        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile +"_temp.txt",PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        sleepAndSaveInfo(SLEEPTIME);
        //调用合约内的交易
        String response2 = invokeNew(ctHash,"init",accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");

        String response3 = invokeNew(ctHash,"init",accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME);

        String response4 = invokeNew(ctHash,"transfer",accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME);

        //查询余额invoke接口
        String response5 = invokeNew(ctHash,"getBalance",accountA);//获取账户A账户余额
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("Data").getString("Figure");

        String response6 = invokeNew(ctHash,"getBalance",accountB);//获取账户A账户余额
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("Data").getString("Figure");

        //查询余额query接口 交易不上链
        String response7 = invokeNew(ctHash,"getBalance",accountA);//获取转账后账户A账户余额
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Figure");
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("Data").getString("Result:"));

        String response8 = invokeNew(ctHash,"getBalance",accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("Data").getString("Result:"));

        //销毁wvm合约
        String response9 = wvmDestroyTest(ctHash);
        sleepAndSaveInfo(SLEEPTIME);
        String response10 = invokeNew(ctHash,"getBalance",accountB);//获取转账后账户B账户余额 报错
        assertThat(JSONObject.fromObject(response10).getString("Message"),containsString("no such file or directory")); //销毁后会提示找不到合约文件 500 error code

        chkTxDetailRsp("200",txHash1,txHash2,txHash3,txHash4,txHash5,txHash6,response9);
        chkTxDetailRsp("404",txHash7);
    }

    @Test
    public void TC1779_SamePriDiffCt() throws Exception{
        String ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);

        String ctHash1 = installInitTransfer(ctName,PRIKEY1,"init","transfer","getBalance");

        //再安装一个不同合约名 相同合约内容的合约
        ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);
        String ctHash2 =installInitTransfer(ctName,PRIKEY1,"init","transfer","getBalance");

        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
    }

    @Test
    public void TC1780_DiffPriDiffCt() throws Exception{
        String ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);

        String ctHash1 = installInitTransfer(ctName,PRIKEY1,"init","transfer","getBalance");

        //再安装一个不同合约名 相同合约内容的合约
        ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);
        wvmFile = "wvm2";
        orgName = "Test";
        String ctHash2 =installInitTransfer(ctName,PRIKEY2,"initB","transferB","getBalanceB");
        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
    }

    @Test
    public void TC1781_DiffPriSameCt() throws Exception{
        String ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);

        String ctHash1 = installInitTransfer(ctName,PRIKEY1,"init","transfer","getBalance");

        //使用不同私钥安装相同合约及内容的合约
        String ctHash2 =installInitTransfer(ctName,PRIKEY2,"init","transfer","getBalance");

        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
    }

    @Test
    public void TC1782_DiffPriSameCtDiffContent() throws Exception{
        String ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);

        String ctHash1 = installInitTransfer(ctName,PRIKEY1,"init","transfer","getBalance");

        //再安装一个不同合约名 相同合约内容的合约
        ctName = sdf.format(dt)+ RandomUtils.nextInt(100000);
        wvmFile = "wvm_updata";
        String ctHash2 =installInitTransfer(ctName,PRIKEY2,"initB","transferB","getBalanceB");
        assertEquals(false,ctHash1.equals(ctHash2)); //确认两个hash不相同
    }

    public String installInitTransfer(String ctName,String Prikey,String...method)throws Exception{
        //当前示例合约仅存在三个方法：init ->method[0] transfer->method[1],getBalance->method[2]
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvmInstallTest(wvmFile + "_temp.txt",Prikey);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        sleepAndSaveInfo(SLEEPTIME);
        chkTxDetailRsp("200",txHash1);
        //调用合约内的方法 init方法
        String response2 = invokeNew(ctHash,method[0],accountA,amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");

        String response3 = invokeNew(ctHash,method[0],accountB,amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME);
        //调用合约内的方法 transfer方法
        String response4 = invokeNew(ctHash,method[1],accountA,accountB,transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure");

        sleepAndSaveInfo(SLEEPTIME);

        //调用合约内的方法 getBalance方法查询余额query接口 交易不上链
        String response7 = invokeNew(ctHash,method[2],accountA);//获取转账后账户A账户余额
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Figure");
        assertEquals(Integer.toString(amountA-transfer),JSONObject.fromObject(response7).getJSONObject("Data").getString("Result:"));

        String response8 = invokeNew(ctHash,method[2],accountB);//获取转账后账户B账户余额
        assertEquals(Integer.toString(amountB+transfer),JSONObject.fromObject(response8).getJSONObject("Data").getString("Result:"));

        sleepAndSaveInfo(SLEEPTIME);
        chkTxDetailRsp("404",txHash7);

        return ctHash;
    }

    public void chkTxDetailRsp(String retCode,String...hashList){
        for(String hash : hashList) {
            log.info("Check Hash: " + hash);
            assertEquals(retCode,JSONObject.fromObject(store.GetTxDetail(hash)).getString("state"));
        }
    }

    @Test
    public void testContractErrInvoke() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.collAddressTest();

    }

    @Test
    public void InvalidTestCtr()throws Exception{

        String response=null;

    }

    @After
    public void resetParam()throws Exception{
        wvmFile = "wvm";
        orgName = "TestExample";
    }


    public String wvmInstallTest(String wvmfile,String Prikey) throws Exception {
        String filePath = resourcePath + wvmfile;
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.InstallWVM(data,Prikey);
        return response;
    }


    public String wvmDestroyTest(String cthash) throws Exception {
        String response = contract.DestroyWVM(cthash);
        return response;

    }


    public String invokeNew(String cthash, String method, Object... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.Invoke(cthash, version, category,method,caller, args);
        return response;
    }

    public String query(String cthash, String method, String... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.QueryWVM(cthash, version, category,method,caller,args);
        return response;
    }


}
