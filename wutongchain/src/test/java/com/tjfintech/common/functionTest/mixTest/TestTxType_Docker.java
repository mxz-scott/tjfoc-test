package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetAppChain;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestTxType_Docker {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    DockerContractTest ct =new DockerContractTest();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    MgToolCmd mgToolCmd = new MgToolCmd();


    String versionStore="0";


    String typeDocker="2";
    String subTypeCreateDocker="30";
    String subTypeDockerTx="32";
    String subTypeDeleteDocker="31";

    String typeWVM="3";
    String subTypeCreateWVM="40";
    String subTypeWVMTx="42";
    String subTypeDeleteWVM="41";

    String typeAdmin="20";
    String subTypeAddColl="200";
    String subTypeDelColl="201";
    String subTypeAddIssue="202";
    String subTypeDelIssue="203";
    String subTypeFreezeToken="204";
    String subTypeRecoverToken="205";

    String typeSystem = "4";
    String subTypePerm = "3";
    String subTypeAddPeer = "0";
    String subTypeQuitPeer = "1";
    String subTypeAddLedger = "4";
    String subTypeFreezeLedger = "5";
    String subTypeRecoverLedger = "6";
    String subTypeDestroyLedger = "7";

    String zeroAddr="0000000000000000";
    SetAppChain setSubLedger = new SetAppChain();

   @Before
    public void beforeConfig() throws Exception {
       initSetting();
    }

    public void initSetting()throws Exception{
        log.info("current Ledger:" + subLedger);
        if(subLedger != "")   setSubLedger.createSubledger();
        BeforeCondition bf = new BeforeCondition();
        bf.setPermission999();
    }



    @Test
    public void checkDockerTx()throws Exception{
        /**|Docker|2|
         * |????????????|30|
         * |????????????|31|
         * |????????????|32|
         */
        //Docker????????? Type 2 SubType 30 31 32
        //????????????
        dockerFileName="simple.go";
        log.info("????????????"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("data").get("figure").toString();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,ContractInstallSleep);

        //???????????????????????????
        long nowTimeSync = (new Date()).getTime();
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER2IP + ":" + PEER2RPCPort,30*1000);
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER4IP + ":" + PEER4RPCPort,30*1000);
        log.info("?????????????????????????????? " + ((new Date()).getTime() - nowTimeSync));

        //??????????????????initMobile
        log.info("??????????????????initMobile");
        String response81 = ct.initMobileTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"??????worldstate??????");

        String txHash81 = JSONObject.fromObject(response81).getJSONObject("data").get("figure").toString();

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash81)).getString("state"));
        //??????????????????changeCount 20190813????????????
        log.info("??????????????????querymobile");
        String response82 = ct.changeMobileCountTest("50","Mobile2");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"??????worldstate??????");

        String txHash82 = JSONObject.fromObject(response82).getJSONObject("data").get("figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash82)).getString("state"));
        //????????????????????????scargs?????????????????????base64????????????????????????
        checkContractArgs(txHash82,"SCArgs","changeMobileCount","50","Mobile2");

        //??????????????????querymobile
        log.info("??????????????????querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("data").get("figure").toString();

        //????????????
        log.info("????????????"+ct.name);
        String response9 = ct.destroyTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").get("figure").toString();

        //??????????????????????????????
        checkTXDetailHeaderMsg(txHash7,versionStore,typeDocker,subTypeCreateDocker);
        //Install chaincode [041801_2.0] success!  Install chaincode [2019103155687_sub20191031951_2.1] success!
        String checkMsg = "";
        if(subLedger.isEmpty()) checkMsg ="Install chaincode [" + ct.name + "_" + ct.version + "] success!";
        else checkMsg = "Install chaincode [" + ct.name + "_" + subLedger.toLowerCase() + "_" + ct.version + "] success!";
        assertEquals(checkMsg,JSONObject.fromObject(
                store.GetTxDetail(txHash7)).getJSONObject("data").getJSONObject("contract").getString("message"));
//        assertEquals("Install chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
//                store.GetTransaction(txHash7)).getJSONObject("data").getString("message"));


        //????????????????????????initMobile
        checkTXDetailHeaderMsg(txHash81,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash81,"initMobile","scDocker","200","1","Transaction excute success!");

        //querymobile
        checkTXDetailHeaderMsg(txHash8,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash8,"queryMobile","scDocker","200","1",
                "Transaction excute success!");
//        assertThat(JSONObject.fromObject(store.GetTransaction(txHash8)).getJSONObject("data").getJSONObject("contractResult").getString("payload"), containsString("Apple"));
        assertThat(JSONObject.fromObject(store.GetTxDetail(txHash8)).getJSONObject("data").getJSONObject("contract").getJSONObject("contractResult").getString("payload"), containsString("Apple"));

        //??????????????????????????????
        checkTXDetailHeaderMsg(txHash9,versionStore,typeDocker,subTypeDeleteDocker);
        //Delete chaincode [041801_2.0] success!
        String msg = "";
        if(subLedger.isEmpty()) msg = "Delete chaincode ["+ct.name+"_"+ct.version+"] success!";
        else msg = "Delete chaincode [" + ct.name+ "_" + subLedger.toLowerCase() + "_" + ct.version + "] success!";
        assertEquals(msg,JSONObject.fromObject(
                store.GetTxDetail(txHash9)).getJSONObject("data").getJSONObject("contract").getString("message"));
//        assertEquals("Delete chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
//                store.GetTransaction(txHash9)).getJSONObject("data").getString("message"));
    }


    public JSONObject checkTXDetailHeaderMsg(String hash, String version, String type, String subType)throws Exception{
        log.info("hash:"+hash);
        JSONObject objectDetail = JSONObject.fromObject(store.GetTxDetail(hash));
        JSONObject jsonObject = objectDetail.getJSONObject("data").getJSONObject("header");
        assertEquals(version,jsonObject.getString("version"));
        assertEquals(type,jsonObject.getString("type"));
        assertEquals(subType,jsonObject.getString("subType"));
        assertEquals(hash,jsonObject.getString("transactionHash"));

        return objectDetail;
    }

    public void checkContractArgs(String hash,String key,String...checkStr)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("contract");
        int i = 0;
        for(String chkStr: checkStr) {
            String scdecodeArgs = new String(utilsClass.decryptBASE64(jsonObject.getJSONArray(key).getString(i)));
            log.info("org string: "+jsonObject.getJSONArray(key).getString(i));
            log.info("base64 decode string: "+ scdecodeArgs);
            log.info("Check String :"+chkStr);
            i++;
            assertEquals(chkStr, scdecodeArgs);
        }
    }

    public void checkContractTx(String hash,String method,String cttype,String ctResultStatus,String code,String Msg)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("contract");
        String sections=new String(utilsClass.decryptBASE64(jsonObject.getString("src")));
        log.info(sections);
        assertEquals(ct.name,JSONObject.fromObject(sections).getString("name"));//??????????????????
        assertEquals(ct.version,JSONObject.fromObject(sections).getString("version"));//????????????Version

        String scArgs= new String(utilsClass.decryptBASE64(jsonObject.getJSONArray("SCArgs").getString(0)));
        log.info(jsonObject.getJSONArray("SCArgs").getString(0));
        assertThat(scArgs, containsString(method));
        assertEquals(code,jsonObject.getString("code"));//????????????????????????code
        assertEquals(Msg,jsonObject.getString("message"));//????????????????????????message

    }
}
