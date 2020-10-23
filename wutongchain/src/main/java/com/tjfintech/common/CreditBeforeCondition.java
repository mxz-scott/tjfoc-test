package com.tjfintech.common;


import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassCredit.*;
import static org.junit.Assert.assertEquals;


@Slf4j

public class CreditBeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    UtilsClassCredit utilsClassCredit = new UtilsClassCredit();
    CommonFunc commonFunc = new CommonFunc();
    Contract contract = testBuilder.getContract();


    //赋值权限999 区分是否主子链
    public void setPermission999() throws Exception {

        String toolPath = "cd " + ToolPATH + ";";
        String exeCmd = "./" + ToolTPName + " permission ";

        SDKID = utilsClass.getSDKID();
        String ledger = "";
        ledger = (subLedger != "") ? " -z " + subLedger : "";
        String preCmd = toolPath + exeCmd + "-p " + PEER1RPCPort + " -s SDK " + ledger + " -d " + SDKID + " -m ";
        String getPerm = toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + " -d " + SDKID + ledger;


        //如果没有权限 则设置权限  修改为设置 兼容版本升级时 权限列表变更需要重新赋权限的问题
//        if(!shExeAndReturn(PEER1IP,getPerm).contains(fullPerm)){
        assertEquals(true, shExeAndReturn(PEER1IP, preCmd + "999").contains("success"));
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(true, shExeAndReturn(PEER1IP, getPerm).contains(fullPerm));
//        }
    }

    public void clearDataSetPerm999() throws Exception {
        utilsClass.delDataBase();//清空sdk当前使用数据库数据
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB, resetPeerBase);
        //重启SDK
        utilsClass.setAndRestartSDK();

        setPermission999();
    }

    public String contractInstallTest(String wvmfile, String Prikey) throws Exception {

        String category = "wvm";
        if (wvmfile == "") {
            return contract.InstallWVM("", category, Prikey);
        }
        String file = utilsClass.readInput(wvmfile).toString().trim();
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        log.info("base64 data: " + data);
        String response = contract.InstallWVM(data, category, Prikey);
        return response;
    }

    @Test
    /**
     * 征信项目安装合约
     */
    public void installZXContract() throws Exception {

        String creditfilePath = testDataPath + "credit/";
        String identity = "identity.wlang";
        String authorization = "authorization.wlang";
        String creditdata = "creditdata.wlang";
        String viewhistory = "viewhistory.wlang";
        String category = "WVM";

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        //安装3个公共合约和1个机构合约
        //安装authorization.wlang
        String filePath = creditfilePath + authorization;
        String response = contractInstallTest(filePath, "");
        authContractName = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装viewhistory.wlang
        filePath = creditfilePath + viewhistory;
        response = contractInstallTest(filePath, "");
        viewContractName = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装identity.wlang
        filePath = creditfilePath + identity;
        response = contractInstallTest(filePath, "");
        identityContractName = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装creditdata.wlang
        filePath = creditfilePath + creditdata;
        response = contractInstallTest(filePath, "");
        creditContractName = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        log.info("authContractName :" + authContractName);
        log.info("viewContractName :" + viewContractName);
        log.info("identityContractName :" + identityContractName);
        log.info("creditContractName :" + creditContractName);

    }
}


