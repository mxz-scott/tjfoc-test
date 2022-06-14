package com.tjfintech.common.stableTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.Random;

import static com.tjfintech.common.utils.UtilsClass.tempWVMDir;

public class WvmIpcrTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Kms kms = testBuilder.getKms();
    Contract contract = testBuilder.getContract();
    WVMContractTest wvm = new WVMContractTest();
    WVMContractTest wvmContractTest = new WVMContractTest();
    FileOperation fileOper = new FileOperation();
    UtilsClass utilsClass = new UtilsClass();


    /**
     * 循环安装灵鲸合约，升级链做回归使用
     * @throws Exception
     */
    @Test
    public void TC_001_account_nft() throws Exception{

        for (int i = 0; i < 100 ; i++) {
            String response1 = wvm.IpcrwvmInstallTest("account_nft.wlang", "wvm");
            String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
            utilsClass.IpAccountAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
            System.out.println(i);
        }
    }

    @Test
    public void TC_002_LJT_nft() throws Exception{
        for (int i = 0; i < 100 ; i++) {
            String response1 = wvm.IpcrwvmInstallTest("LJT.wlang", "wvm");
            String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
            utilsClass.IpAccountAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");
            System.out.println(i);
        }
    }

    String file = "Y29udHJhY3QgQWNjb3VudCB7CgoKICAgIHB1YmxpYyBzdHJpbmcgY2FsbEV2ZW50KHN0cmluZyB0b3BpYyxzdHJpbmcgbXNnKXsKICAgICAgICBldmVudCh0b3BpYyxtc2cpCiAgICAgICAgcmV0dXJuICJzdWNjZXNzIgogICAgfQoKfQ==";
//    @Test
    public void TC_003_wvmtest() throws Exception{

        String response1 = contract.InstallWVM(file, "wvm","");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        for (int i =1;i>0;i++) {
            String a = String.valueOf(i);

            String response2 = wvmContractTest.invokeNew("fd2f3a30c67c5d182ee67bbeade3e487a167cef3ffef18cdfa67098d1c8acdba","callEvent",a,a);
            System.out.println(response2);
            System.out.println(a);
            Thread.sleep(2);
        }
    }
}