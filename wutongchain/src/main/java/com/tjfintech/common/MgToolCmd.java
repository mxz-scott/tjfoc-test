package com.tjfintech.common;

import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;

//import com.tjfintech.common.Interface.ManageTool;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.subLedger;
import static org.junit.Assert.assertEquals;

@Slf4j
public class MgToolCmd {
    UtilsClass utilsClass = new UtilsClass();
    public String toolExePath  = "cd " + ToolPATH + ";./" + ToolTPName;
    public String licExePath  = "cd " + ToolPATH + ";./license";
    
    public String setPeerPerm(String queryIPPort,String sdkID,String permStr,String...ShowName)throws Exception{
        String rpcPort = queryIPPort;//netPeerIP.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        if(subLedger != "")  rpcPort = rpcPort + " -c " + subLedger;
        if(ShowName.length != 0) rpcPort = rpcPort + " -s "+ShowName[0];

        String cmd = toolExePath  + " permission -p " + rpcPort + " -d "+ sdkID + " -m " + permStr;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerPerm(String queryIPPort,String sdkID)throws Exception{
        String rpcPort = queryIPPort;//netPeerIP.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        if(subLedger != "")  rpcPort = rpcPort + " -c "+subLedger;
        if(sdkID != "") rpcPort = rpcPort + " -d " + sdkID;

        String cmd = toolExePath + " getpermission -p " + rpcPort + " -d " + sdkID;
        return shExeAndReturn(queryIP,cmd);
    }
    public String getID(String queryIP,String keyPath,String cryptType)throws Exception{
        String keySetting = keyPath.isEmpty() ? "" : " -p " + keyPath;
        String crySetting = cryptType.isEmpty() ? "" : " -k " + cryptType;

        String cmd = toolExePath + " getid" + keySetting + crySetting ;
        return shExeAndReturn(queryIP,cmd);
    }

    public String addPeer(String addPeerType,String queryIPPort,String joinPeerIP,String joinTcpPort,String joinRpcPort)throws Exception{
        String queryRpcPort = queryIPPort;//queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String[] temp = joinPeerIP.substring(joinPeerIP.lastIndexOf("/") + 1).split("\\.");
        log.info("peer ip ***************  " + joinPeerIP.substring(joinPeerIP.lastIndexOf("/")));
        String peerID = getPeerId(joinPeerIP.substring(joinPeerIP.lastIndexOf("/") + 1),USERNAME,PASSWD);
        String peerName = "peer" + temp[3];
        String peerIPlan = joinPeerIP + joinTcpPort;
        String peerIPwan = joinPeerIP + joinTcpPort;

        //20200320 ????????????rpc port??????
//        String cmd = toolExePath + " " + addPeerType + " -p " + queryRpcPort + " -n " + peerID + " -s " + peerName
//                + " -l " + peerIPlan + " -w " + peerIPwan + " -r " + joinRpcPort;
        String cmd = toolExePath + " " + addPeerType + " -p " + queryRpcPort + " -n " + peerID + " -s " + peerName
                + " -l " + peerIPlan + " -w " + peerIPwan + " -c " + subLedger;
        return shExeAndReturn(queryIP,cmd);
    }


    public String quitPeer(String queryIPPort,String quitIP)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        String peerID = getPeerId(quitIP,USERNAME,PASSWD);

        String cmd = toolExePath + " quit -p " + rpcPort + " -n " + peerID + " -c " + subLedger;
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryPeerUnconfirmedTx(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " ntx -p " + rpcPort + " -i";
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryBlockHeight(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        if(subLedger!="")  rpcPort = rpcPort + " -c " + subLedger;
        String cmd = toolExePath + " height -p " + rpcPort;
        String resp = shExeAndReturn(queryIP,cmd);
        //?????????????????????????????????????????????????????????????????????
        return resp.substring(resp.lastIndexOf("Height:")+7).trim();
    }
    public String queryBlockHeight(String shIP,String queryIPPort)throws Exception{
        if(subLedger != "")  queryIPPort = queryIPPort + " -c " + subLedger;
        String cmd = toolExePath + " height -p " + queryIPPort;
        String resp = shExeAndReturn(shIP,cmd);
        //?????????????????????????????????????????????????????????????????????
        return resp.substring(resp.lastIndexOf("Height:") + 7).trim();
    }

    public String queryBlockByHeight(String queryIPPort,String height)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        if(subLedger!="")  rpcPort = rpcPort + " -c " + subLedger;
        String cmd = toolExePath + " query -p "+ rpcPort + " -v " + height;

        return shExeAndReturn(queryIP,cmd);
    }

    public String sendNewTx(String queryIPPort,String txNo,String txType)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String tempTxNo = txNo.isEmpty()?"":" -n " + txNo;
        String tempTxType = txType.isEmpty() ? "" : " -t " + txType;
        String temp = tempTxNo + tempTxType;
        if(subLedger!="")  rpcPort = rpcPort + " -c " + subLedger;
        String cmd = toolExePath + " newtx -p "+ rpcPort + temp;
        return shExeAndReturn(queryIP,cmd);
    }

    public String checkPeerHealth(String queryIPPort)throws Exception {

        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " health -p " + rpcPort;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerSimpleInfo(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " peer -p " + rpcPort;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerDetails(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " peer -p " + rpcPort;//20191105 ?????????????????????????????? ????????????simple info?????????
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryMemberList(String queryIPPort) throws Exception{
        String rpcPort = queryIPPort;//.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " mem -p " + rpcPort;
        return shExeAndReturn(queryIP,cmd);
    }

    public String genLicence(String shellIP,String macAddr,String ipAddr,String validPeriod,String maxPeerNo,String version)throws Exception{
        String macSetting = macAddr.isEmpty() ? "" : " -m " + macAddr;
        String ipSetting = ipAddr.isEmpty() ? "" : " -p " + ipAddr;
        String validSetting = validPeriod.isEmpty() ? "" : " -d " + validPeriod;
        String NoSetting = maxPeerNo.isEmpty() ? "" : " -n " + maxPeerNo;
        String Version = version.isEmpty() ? "" : " -v " + version;

        String cmd = licExePath + " create" + macSetting + ipSetting + validSetting + NoSetting + Version;
        return shExeAndReturn(shellIP,cmd);
    }

    public String deLicence(String shellIP,String licPath)throws Exception{
        String licPayjSetting = licPath.isEmpty() ? "" : " -p " + licPath;
        String cmd = licExePath + " decode" + licPayjSetting;
        return shExeAndReturn(shellIP,cmd);
    }

    public Boolean mgCheckHeightOrSleep(String queryIPPortRefer,String queryIPPortTest,long... sleeptime)throws Exception{

        long internal = 0;
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("???????????? " + nowTime);
        Boolean bOK = false;
        long waitTime = sleeptime[0];
        long stepTime = 3000;
        if(sleeptime.length == 2){
            stepTime = sleeptime[1];
        }

        while((new Date()).getTime() - nowTime < waitTime && bOK == false){
            if(queryBlockHeight(queryIPPortRefer).equals(queryBlockHeight(queryIPPortTest)))
                bOK = true;
            else
                sleepAndSaveInfo(stepTime,"????????????????????????????????????");
        }
        //??????????????????
        log.info("???????????? " + (new Date()).getTime());
        internal = (new Date()).getTime() - nowTime;

        log.info("?????????????????? " + bOK + " ???????????? " + internal);
        return  bOK;
    }

    public String chkLedgerExist(String shellIP,String rpcPort,String ledgerName,long sleeptime)throws Exception{
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("???????????? " + nowTime);

        Boolean bOK = false;
        //????????????????????????
        while((new Date()).getTime() - nowTime < sleeptime && bOK == false){
            //????????????????????????????????????
            log.info("???????????? " + (new Date()).getTime());
            //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
            if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
            String cmd = "cd " + ToolPATH + ";./" + ToolTPName + " ledger get -p " + rpcPort ;//+" -c " + ledgerName.toLowerCase().trim();
            String resp = shExeAndReturn(shellIP,cmd);

//            if(resp.contains(ledgerName.toLowerCase().trim()) && (!resp.contains("not exist"))) bOK = true;
            if(resp.contains(ledgerName.trim())) bOK = true;
            sleepAndSaveInfo(1000,"????????????????????????????????????");
        }
        log.info("============================= ?????????????????????????????? " + bOK + " ???????????? " +
                ledgerName + " " + ((new Date()).getTime() - nowTime));

        sleepAndSaveInfo(2000,"============================= ??????SDK????????????");
        if(bOK) return "exist";
        else return "not exist";
    }


    public String createAppChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                 String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
        String ledgerName = chainNameParam.trim().split(" ")[1];
        String mainCmd =" ledger create ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd=toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam +
                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
        String resp = shExeAndReturn(shellIP,cmd);
        if(resp.contains("ledgerid")) {
            subLedger = resp.substring(resp.lastIndexOf(":") + 1).trim();
            resp = chkLedgerExist(shellIP,rpcPort,subLedger.replaceAll("-c",""),SLEEPTIME);
            if(!resp.contains("not exist")) {
                log.info("**************  set permission 999 for " + subLedger);
                resp = setPeerPerm(PEER1IP + ":" + PEER1RPCPort, utilsClass.getSDKID(), "999 -c " + subLedger);
//                assertEquals(true,resp.contains("send transaction success"));
            }
        }
        return resp;
    }

    public String createAppChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                       String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
        String mainCmd =" ledger create ";
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam +
                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
        String response = shExeAndReturn(shellIP,cmd);
        if(response.contains("ledgerid")) subLedger = response.substring(response.lastIndexOf(":") + 1).trim();;
        return response;
    }

    /*
     * ?????????????????? ?????????????????????????????????
     * */
    public String getAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger get ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd = toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam;

        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * ????????????
     * */
    public String freezeAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger freeze ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * ????????????
     * */
    public String recoverAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd = " ledger recover ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * ????????????
     * */
    public String destroyAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger destroy ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /**
     *
     * @param shellIP  ??????shell???????????????IP
     * @param rpcPort  -p ?????? ?????????????????????????????????
     * @param chainName ??????????????????
     * @param contractName  ???????????????/???????????????????????????????????????
     * @param contractVersion ????????????????????? ??????????????????????????? ?????????????????????
     * @param contractMethod ??????????????????/?????????????????????
     * @param permitStr  ????????????????????????/?????? ??????SDK???????????????sdk???id  ????????????????????????
     * @return
     */
    public String contractFuncPermit(String shellIP,String rpcPort,String chainName,
                                     String contractName,String contractVersion,String contractMethod,
                                     String permitStr){
        String mainCmd =" contract modifyfunc ";
        //??????IP:Port ????????????????????? ?????????shellIp?????????IP:Port??????
        if (!rpcPort.contains(":"))  rpcPort = shellIP + ":" + rpcPort;
        String versionParam = "";
        String chainNameParam = " -c " + chainName;
        String contractNameParam = " -m " + contractName;
        if(!contractVersion.isEmpty()) versionParam = " -v " + contractVersion;
        String contractMethodParam = " -f " + contractMethod;
        String permitStrParam = " -a " + "everyone";//??????
        if(!permitStr.isEmpty()) permitStrParam = " -a " + permitStr;
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam +
                contractNameParam + versionParam + contractMethodParam + permitStrParam;
        return shExeAndReturn(shellIP,cmd);
    }
}
