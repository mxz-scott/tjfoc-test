package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
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
    SetSubLedger setSubLedger = new SetSubLedger();

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
         * |合约安装|30|
         * |合约销毁|31|
         * |新建交易|32|
         */
        //Docker类交易 Type 2 SubType 30 31 32
        //创建合约
        dockerFileName="simple.go";
        log.info("创建合约"+ct.name);
        String response7 = ct.installTest();
        String txHash7 = JSONObject.fromObject(response7).getJSONObject("Data").get("Figure").toString();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,ContractInstallSleep);

        //确认所有节点均同步
        long nowTimeSync = (new Date()).getTime();
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER2IP + ":" + PEER2RPCPort,30*1000);
        mgToolCmd.mgCheckHeightOrSleep(
                PEER1IP + ":" + PEER1RPCPort,PEER4IP + ":" + PEER4RPCPort,30*1000);
        log.info("等待节点同步合约时间 " + ((new Date()).getTime() - nowTimeSync));

        //发送合约交易initMobile
        log.info("发送合约交易initMobile");
        String response81 = ct.initMobileTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        String txHash81 = JSONObject.fromObject(response81).getJSONObject("Data").get("Figure").toString();

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash81)).getString("State"));
        //发送合约交易changeCount 20190813补充测试
        log.info("发送合约交易querymobile");
        String response82 = ct.changeMobileCountTest("50","Mobile2");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        sleepAndSaveInfo(worldStateUpdTime,"等待worldstate更新");

        String txHash82 = JSONObject.fromObject(response82).getJSONObject("Data").get("Figure").toString();
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash82)).getString("State"));
        //检查交易详情中的scargs内容是否能够被base64解码成传入的参数
        checkContractArgs(txHash82,"SCArgs","changeMobileCount","50","Mobile2");

        //发送合约交易querymobile
        log.info("发送合约交易querymobile");
        String response8 = ct.queryMobileTest("Mobile1");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String txHash8 = JSONObject.fromObject(response8).getJSONObject("Data").get("Figure").toString();

        //销毁合约
        log.info("销毁合约"+ct.name);
        String response9 = ct.destroyTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").get("Figure").toString();

        //检查合约创建交易信息
        checkTXDetailHeaderMsg(txHash7,versionStore,typeDocker,subTypeCreateDocker);
        //Install chaincode [041801_2.0] success!  Install chaincode [2019103155687_sub20191031951_2.1] success!
        String checkMsg = "";
        if(subLedger.isEmpty()) checkMsg ="Install chaincode [" + ct.name + "_" + ct.version + "] success!";
        else checkMsg = "Install chaincode [" + ct.name + "_" + subLedger.toLowerCase() + "_" + ct.version + "] success!";
        assertEquals(checkMsg,JSONObject.fromObject(
                store.GetTxDetail(txHash7)).getJSONObject("Data").getJSONObject("Contract").getString("Message"));
//        assertEquals("Install chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
//                store.GetTransaction(txHash7)).getJSONObject("Data").getString("message"));


        //检查合约交易信息initMobile
        checkTXDetailHeaderMsg(txHash81,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash81,"initMobile","scDocker","200","1","Transaction excute success!");

        //querymobile
        checkTXDetailHeaderMsg(txHash8,versionStore,typeDocker,subTypeDockerTx);
        checkContractTx(txHash8,"queryMobile","scDocker","200","1",
                "Transaction excute success!");
//        assertThat(JSONObject.fromObject(store.GetTransaction(txHash8)).getJSONObject("Data").getJSONObject("contractResult").getString("payload"), containsString("Apple"));
        assertThat(JSONObject.fromObject(store.GetTxDetail(txHash8)).getJSONObject("Data").getJSONObject("Contract").getJSONObject("ContractResult").getString("Payload"), containsString("Apple"));

        //检查合约销毁交易信息
        checkTXDetailHeaderMsg(txHash9,versionStore,typeDocker,subTypeDeleteDocker);
        //Delete chaincode [041801_2.0] success!
        String msg = "";
        if(subLedger.isEmpty()) msg = "Delete chaincode ["+ct.name+"_"+ct.version+"] success!";
        else msg = "Delete chaincode [" + ct.name+ "_" + subLedger.toLowerCase() + "_" + ct.version + "] success!";
        assertEquals(msg,JSONObject.fromObject(
                store.GetTxDetail(txHash9)).getJSONObject("Data").getJSONObject("Contract").getString("Message"));
//        assertEquals("Delete chaincode ["+ct.name+"_"+ct.version+"] success!",JSONObject.fromObject(
//                store.GetTransaction(txHash9)).getJSONObject("Data").getString("message"));
    }


    public JSONObject checkTXDetailHeaderMsg(String hash, String version, String type, String subType)throws Exception{
        log.info("hash:"+hash);
        JSONObject objectDetail = JSONObject.fromObject(store.GetTxDetail(hash));
        JSONObject jsonObject = objectDetail.getJSONObject("Data").getJSONObject("Header");
        assertEquals(version,jsonObject.getString("Version"));
        assertEquals(type,jsonObject.getString("Type"));
        assertEquals(subType,jsonObject.getString("SubType"));
        assertEquals(hash,jsonObject.getString("TransactionHash"));

        return objectDetail;
    }

    public void checkContractArgs(String hash,String key,String...checkStr)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("Contract");
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
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("Contract");
        String sections=new String(utilsClass.decryptBASE64(jsonObject.getString("Src")));
        log.info(sections);
        assertEquals(ct.name,JSONObject.fromObject(sections).getString("Name"));//检查合约名称
        assertEquals(ct.version,JSONObject.fromObject(sections).getString("Version"));//检查合约Version

        String scArgs= new String(utilsClass.decryptBASE64(jsonObject.getJSONArray("SCArgs").getString(0)));
        log.info(jsonObject.getJSONArray("SCArgs").getString(0));
        assertThat(scArgs, containsString(method));
        assertEquals(code,jsonObject.getString("Code"));//检查合约交易结果code
        assertEquals(Msg,jsonObject.getString("Message"));//检查合约交易结果message

    }
}
