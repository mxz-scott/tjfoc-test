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

@Slf4j
public class MgToolCmd {
    UtilsClass utilsClass = new UtilsClass();
    public String toolExePath  = "cd " + ToolPATH + ";./" + ToolTPName;
    public String licExePath  = "cd " + ToolPATH + ";./license";
    
    public String setPeerPerm(String netPeerIP,String sdkID,String permStr,String...ShowName)throws Exception{
        String rpcPort = netPeerIP.split(":")[1];//9300
        String queryIP = netPeerIP.split(":")[0];//10.1.3.240

        if(subLedger!="")  rpcPort = rpcPort + " -z " + subLedger;
        if(ShowName.length != 0) rpcPort = rpcPort + " -s "+ShowName[0];

        String cmd = toolExePath  + " permission -p " + rpcPort + " -d "+ sdkID + " -m " + permStr;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerPerm(String netPeerIP,String sdkID)throws Exception{
        String rpcPort = netPeerIP.split(":")[1];//9300
        String queryIP = netPeerIP.split(":")[0];//10.1.3.240

        if(subLedger != "")  rpcPort = rpcPort + " -z "+subLedger;
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
        String queryRpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String[] temp = joinPeerIP.substring(joinPeerIP.lastIndexOf("/") + 1).split("\\.");
        log.info("peer ip ***************  " + joinPeerIP.substring(joinPeerIP.lastIndexOf("/")));
        String peerID = getPeerId(joinPeerIP.substring(joinPeerIP.lastIndexOf("/") + 1),USERNAME,PASSWD);
        String peerName = "peer" + temp[3];
        String peerIPlan = joinPeerIP + joinTcpPort;
        String peerIPwan = joinPeerIP + joinTcpPort;

        //20200320 确认移除rpc port传入
//        String cmd = toolExePath + " " + addPeerType + " -p " + queryRpcPort + " -n " + peerID + " -s " + peerName
//                + " -l " + peerIPlan + " -w " + peerIPwan + " -r " + joinRpcPort;
        String cmd = toolExePath + " " + addPeerType + " -p " + queryRpcPort + " -n " + peerID + " -s " + peerName
                + " -l " + peerIPlan + " -w " + peerIPwan;
        return shExeAndReturn(queryIP,cmd);
    }


    public String quitPeer(String queryIPPort,String peerIP)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        String peerID = getPeerId(peerIP,USERNAME,PASSWD);

        String cmd = toolExePath + " quit -p " + rpcPort + " -n " + peerID;
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryPeerUnconfirmedTx(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " ntx -p " + rpcPort + " -i";
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryBlockHeight(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        if(subLedger!="")  rpcPort = rpcPort + " -z " + subLedger;
        String cmd = toolExePath + " height -p " + rpcPort;
        String resp = shExeAndReturn(queryIP,cmd);
        //管理工具加入执行时间打印，高度值需要处理后返回
        return resp.substring(resp.lastIndexOf("Height:")+7).trim();
    }

    public String queryBlockByHeight(String queryIPPort,String height)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240
        if(subLedger!="")  rpcPort = rpcPort + " -z " + subLedger;
        String cmd = toolExePath + " query -p "+ rpcPort + " -v " + height;

        return shExeAndReturn(queryIP,cmd);
    }

    public String sendNewTx(String queryIPPort,String txNo,String txType)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String tempTxNo = txNo.isEmpty()?"":" -n " + txNo;
        String tempTxType = txType.isEmpty() ? "" : " -t " + txType;
        String temp = tempTxNo + tempTxType;
        if(subLedger!="")  rpcPort = rpcPort + " -z " + subLedger;
        String cmd = toolExePath + " newtx -p "+ rpcPort + temp;
        return shExeAndReturn(queryIP,cmd);
    }

    public String checkPeerHealth(String queryIPPort)throws Exception {

        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " health -p " + rpcPort;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerSimpleInfo(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " peer -p " + rpcPort;
        return shExeAndReturn(queryIP,cmd);
    }

    public String getPeerDetails(String queryIPPort)throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " peer -p " + rpcPort;//20191105 管理工具不再对外提供 因此去掉simple info的功能
        return shExeAndReturn(queryIP,cmd);
    }

    public String queryMemberList(String queryIPPort) throws Exception{
        String rpcPort = queryIPPort.split(":")[1];//9300
        String queryIP = queryIPPort.split(":")[0];//10.1.3.240

        String cmd = toolExePath + " mem -p "+rpcPort;
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

//
//    public String createSubChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
//                                 String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
//        String mainCmd =" addledger ";
//        String cmd=toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam +
//                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
//        String resp = shExeAndReturn(shellIP,cmd);
//
//        if(resp.contains("transaction success")) {
//
//            chkLedgerExist(shellIP,rpcPort,chainNameParam.replaceAll("-z",""),SLEEPTIME*2);
//
//            subLedger = chainNameParam.trim().split(" ")[1];
//            log.info("**************  set permission 999 for " + subLedger);
//            String resp1 = setPeerPerm(PEER1IP + ":" + PEER1RPCPort, utilsClass.getSDKID(),"999");
//            subLedger = "";
//        }
//        return resp;
//    }
//
//    public String createSubChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
//                                       String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception{
//        String mainCmd =" addledger ";
//        String cmd= toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam +
//                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam;
//
//        return shExeAndReturn(shellIP,cmd);
//    }
//
//    /*
//     * 获取子链信息 包括所有子链及单个子链
//     * */
//    public String getSubChain(String shellIP,String rpcPort,String chainNameParam){
//        String mainCmd =" getledger ";
//        String cmd = toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam;
//        return shExeAndReturn(shellIP,cmd);
//    }
//
//    /*
//     * 冻结子链
//     * */
//    public String freezeSubChain(String shellIP,String rpcPort,String chainNameParam){
//        String mainCmd =" freezeledger ";
//        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
//        return shExeAndReturn(shellIP,cmd);
//    }
//
//    /*
//     * 恢复子链
//     * */
//    public String recoverSubChain(String shellIP,String rpcPort,String chainNameParam){
//        String mainCmd = " recoverledger ";
//        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
//        return shExeAndReturn(shellIP,cmd);
//    }
//
//    /*
//     * 销毁子链
//     * */
//    public String destroySubChain(String shellIP,String rpcPort,String chainNameParam){
//        String mainCmd =" destoryledger ";
//        String cmd = toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam;
//        return shExeAndReturn(shellIP,cmd);
//    }

    public Boolean mgCheckHeightOrSleep(String queryIPPortRefer,String queryIPPortTest,long... sleeptime)throws Exception{

        long internal = 0;
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("开始时间 " + nowTime);
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
                sleepAndSaveInfo(stepTime,"等待查询高度是否一致时间");
        }
        //计算查询时间
        log.info("当前时间 " + (new Date()).getTime());
        internal = (new Date()).getTime() - nowTime;

        log.info("检查高度一致 " + bOK + " 等待时间 " + internal);
        return  bOK;
    }

    public String chkLedgerExist(String shellIP,String rpcPort,String ledgerName,long sleeptime)throws Exception{
        Date dtTest = new Date();
        long nowTime = dtTest.getTime();
        log.info("开始时间 " + nowTime);

        Boolean bOK = false;
        //查询子链是否存在
        while((new Date()).getTime() - nowTime < sleeptime && bOK == false){
            //管理工具查询子链是否存在
            log.info("开始时间 " + (new Date()).getTime());
            String cmd = "cd " + ToolPATH + ";./" + ToolTPName + " ledger get -p "+ rpcPort ;//+" -z " + ledgerName.toLowerCase().trim();
            String resp = shExeAndReturn(shellIP,cmd);

//            if(resp.contains(ledgerName.toLowerCase().trim()) && (!resp.contains("not exist"))) bOK = true;
            if(resp.contains(ledgerName.toLowerCase().trim())) bOK = true;

            sleepAndSaveInfo(1000,"等待再次查询子链是否存在");
        }
        log.info("============================= 管理工具查询子链存在 " + bOK + " 等待时间 " +
                ledgerName + " " + ((new Date()).getTime() - nowTime));

        sleepAndSaveInfo(3000,"============================= 等待SDK同步数据");
        if(bOK) return "exist";
        else return "not exist";
    }


    public String createAppChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                 String firstBlockInfoParam,String consensusParam,String peeridsParam,String outPeerList)throws Exception{
        String mainCmd =" ledger create ";
        String cmd=toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam +
                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam + outPeerList;
        String resp = shExeAndReturn(shellIP,cmd);

        if(resp.contains("transaction success")) {
            subLedger = resp.substring(resp.lastIndexOf("")+1).trim();
            resp = chkLedgerExist(shellIP,rpcPort,chainNameParam.replaceAll("-z",""),SLEEPTIME*2);
            if(!resp.contains("not exist")) {
                log.info("**************  set permission 999 for " + subLedger);
                String resp1 = setPeerPerm(PEER1IP + ":" + PEER1RPCPort, utilsClass.getSDKID(), "999");
                subLedger = "";
            }
        }
        return resp;
    }

    public String createAppChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                       String firstBlockInfoParam,String consensusParam,String peeridsParam,String outPeerList)throws Exception{
        String mainCmd =" ledger create ";
        String cmd= toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam +
                hashTypeParam + firstBlockInfoParam + consensusParam + peeridsParam + outPeerList;

        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * 获取子链信息 包括所有子链及单个子链
     * */
    public String getAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger get ";
        String cmd = toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * 冻结子链
     * */
    public String freezeAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger freeze ";
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * 恢复子链
     * */
    public String recoverAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd = " ledger recover ";
        String cmd = toolExePath + mainCmd + " -p " + rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }

    /*
     * 销毁子链
     * */
    public String destroyAppChain(String shellIP,String rpcPort,String chainNameParam){
        String mainCmd =" ledger destory ";
        String cmd = toolExePath + mainCmd + " -p "+ rpcPort + chainNameParam;
        return shExeAndReturn(shellIP,cmd);
    }
}
