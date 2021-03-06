package com.tjfintech.common.functionTest.PermissionTest;

import com.sun.deploy.util.StringUtils;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.IgnoreForBinding;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.functionTest.PermissionTest.APermfuncSysSetMg.subLedgerName;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.USERNAME;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class TestPermission {
    public static final String ToolIP=PEER1IP;
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    APermfuncSys pFun1 =new APermfuncSys();
    APermfuncDocker pFunCt =new APermfuncDocker();
    APermfuncUTXO pFunUTXO =new APermfuncUTXO();
    APermfuncWVM pFunWVM = new APermfuncWVM();
    APermfuncSysSetMg pFunSysSet = new APermfuncSysSetMg();
    MgToolCmd mgToolCmd = new MgToolCmd();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    WVMContractTest wvmContractTest = new WVMContractTest();
    FileOperation fileOperation = new FileOperation();
    public String orgName = "TestExample";
    public String accountA = "A";
    public int amountA = 50;
    public String wvmFile = "wvm";
    String glbWVMHash = "111";

    String glbBlockHash ="xP4fUESPobYXi1+ ROFjlc32XLAu4GGUg2FfC5ygPpqU=";
    String glbTxHash="wsjmIJhZaXRS5mNGFMC4xiL529XoUpow3bu5N/JWNB8=";
    String glbPriTxHash="wsjmIJhZaXRS5mNGFMC4xiL529XoUpow3bu5N/JWNB8=";
    String glbCtName="t888";

    String def="Def:111";
    //"+ def+ Sys0+ Store0+ Docker0+ WVM0+ Mg0+ UTXO0+ "
    String Sys0="Sys:00000000"; //移除tx/search接口测试标记
    String Store0="Store:00";
//    String Docker0 ="Docker:00000";
    String DockerNill ="";  //处于最小变动仍保留docker占位
    String WVM0="WVM:000";
    String Mg0="Mg:000000";
    String UTXO0="UTXO:0000000000";
//    String full = "Sys:11111111Store:11Docker:11111WVM:111Mg:111111UTXO:1111111111";
    String full = "Sys:11111111Store:11WVM:111Mg:111111UTXO:1111111111";

    String dynPeerPerm = "10,261,262";
    String subLedgerPerm = "10,281,282,283,284";

    String glbMultiToken3="";//MULITADD3的全局预设发行token
    String glbMultiToken4="";//MULITADD4的全局预设发行token
    String glbSoloToken="";//ADDRESS1的全局预设发行token


    String toolPath="cd " + ToolPATH + ";";
    String ledger = (subLedger!="")?" -c " + subLedger:"";
    String exeCmd="./" + ToolTPName + " permission " + ledger;

    String peerIP=PEER1RPCPort;
    String sdkID= utilsClass.getSDKID();
    String preCmd=toolPath + exeCmd + " -p " + peerIP + " -d " + sdkID + " -m ";
    ArrayList<String> dockerList =new ArrayList();

    boolean bExe=false;

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;

    @Before
    public void beforeTest() throws Exception {

        //将管理工具id权限设置为999
        shellExeCmd(PEER1IP,toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getToolID(PEER1IP) + " -m 999");

        if(bExe == false) {
            BeforeCondition bf = new BeforeCondition();
            bf.setPermission999();
            sleepAndSaveInfo(SLEEPTIME,"赋权限999后等待");
            bf.updatePubPriKey();
            bf.collAddressTest();
            bExe=true;
        }

        utilsClass.setAndRestartSDK();


        pFun1.Data="GlobalStore:" + UtilsClass.Random(4);
        pFun1.createStore();  //SDK发送基础存证交易请求
        glbTxHash = pFun1.txHash; //此txHash是根据CreateStore执行后返回的txHash

        String Data = "cxTest-" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreatePrivateStore(Data,map);
        log.info(response1);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        glbPriTxHash = jsonObject.getString("data");


        pFun1.getBlockByHeight(1); //SDK发送通过区块高度获取区块信息请求
        //pFun1.blockHash是根据pFun1.getBlockByHeight提取的区块哈希
        glbBlockHash=pFun1.blockHash;
        pFun1.getBlockByBlockHash(glbBlockHash); //SDK发送通过区块哈希获取区块信息请求


        pFunUTXO.soloGenAddr(PUBKEY1); //SDK发送UTXO生成单签地址交易请求
        pFunUTXO.multiGenAddr(3,PUBKEY1,PUBKEY6,PUBKEY7); //SDK发送UTXO生成3/3多签地址交易请求
        pFunUTXO.multiGenAddr(1,PUBKEY1,PUBKEY2);     //SDK发送UTXO生成1/2多签地址交易请求

        glbSoloToken="Glb1So" + UtilsClass.Random(4);
        glbMultiToken4="Glb1Mu4" + UtilsClass.Random(4);

        pFunUTXO.issAmount="300";

        pFunUTXO.soloIssueToken(PRIKEY1,glbSoloToken,ADDRESS1); //SDK发送UTXO - Token单签发行请求
        Thread.sleep(6000); log.info("sleep time/ms:" + 6000);

        //多签MULITADD3发行给其他多签地址MULITADD4
        pFunUTXO.multiIssueToken(MULITADD3,glbMultiToken4,MULITADD4);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求

        Thread.sleep(6000);

        //多签MULITADD3发行给自己MULITADD3
        glbMultiToken3="GlbMu3" + UtilsClass.Random(4);
        pFunUTXO.multiIssueToken(MULITADD3,glbMultiToken3,MULITADD3);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求

        Thread.sleep(SLEEPTIME);

        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(soloSign.BalanceByAddr(ADDRESS1,glbSoloToken)).getJSONObject("data").getString("total"));
        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(multiSign.BalanceByAddr(MULITADD4,glbMultiToken4)).getJSONObject("data").getString("total"));
        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(multiSign.BalanceByAddr(MULITADD3,glbMultiToken3)).getJSONObject("data").getString("total"));

        //预先安装WVM合约
        String ctName="ok_" + sdf.format(dt) + RandomUtils.nextInt(100000);
        fileOperation.replace(testDataPath + "wvm/" + wvmFile + ".txt", orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response4 = wvmContractTest.wvmInstallTest(wvmFile + "_temp.txt",PRIKEY1);
        glbWVMHash = JSONObject.fromObject(response4).getJSONObject("data").getString("name");

        bExe=true;
    }

    @Test
    public void CheckAll()throws Exception{
        //String mValue="1,2,3,4,5,6,7,8,9,10,21,211,212,22,221,222,223,224,23,231,232,233,234,235,236,24,25,251,252";
        checkAllInterface("0",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);

        Thread.sleep(3000);
        checkAllInterface("999",def + full);
        Thread.sleep(3000);
    }

    @Test
    public void chkSys1by1()throws Exception{
        String[] mArray={"5","6","7","9","10","11","300"};

        checkAllInterface("1",def + "Sys:10000000" + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("2",def + "Sys:01000000" + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("3",def + "Sys:00110000" + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("4",def + "Sys:00001111" + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        //mysql数据库不支持/tx/search接口 测试时需要使用mongodb 否则需要修改测试预期结果
        //20191217 后面可能是支持mysql较多 不再测试tx/search接口
//        checkAllInterface("8",def + "Sys:000000001" + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);

        for(int i=0;i<mArray.length;i ++ )
        {
            checkAllInterface(mArray[i],def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        }

    }

@Test
    public void ChkStore1by1() throws Exception{
        checkAllInterface("21",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("211",def + Sys0 + "Store:10" + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("212",def + Sys0 + "Store:01" + DockerNill + WVM0 + Mg0 + UTXO0);
        checkAllInterface("211,212",def + Sys0 + "Store:11" + DockerNill + WVM0 + Mg0 + UTXO0);
    }

    //分离docker合约相关测试

    @Test
    public void ChkWVM1by1() throws Exception{
        String mValue="999";
        String cmd = preCmd + mValue;

        shellCmd(ToolIP,cmd);//添加所有权限

        checkAllInterface("22",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);

        //合约安装权限
        checkAllInterface("226",def + Sys0 + Store0 + DockerNill + "WVM:100" + Mg0 + UTXO0);

        //合约交易权限
        checkAllInterface("227",def + Sys0 + Store0 + DockerNill + "WVM:001" + Mg0 + UTXO0);

        //合约销毁权限
        checkAllInterface("228",def + Sys0 + Store0 + DockerNill + "WVM:010" + Mg0 + UTXO0);

        //合约安装及合约交易
        checkAllInterface("226,227",def + Sys0 + Store0 + DockerNill + "WVM:101" + Mg0 + UTXO0);
    }

    public void check233Interface(String chk) throws Exception{
        //233为对账接口为此处检查接口
        //shellCmd(ToolIP,preCmd + "233");
        assertEquals(pFunUTXO.getTotal(0,0,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getSDKBalance(glbMultiToken3,MULITADD3),chk);
        assertEquals(pFunUTXO.getChainBalance(MULITADD3,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getUTXODetail(MULITADD3,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getTokenState(glbMultiToken3),chk);
        assertEquals(pFunUTXO.getTotalByDay(0,0),chk);
    }


    @Test
    public void chkUTXO1by1()throws Exception{

//        String[] mArr={"23","231","232","233","234","235","236","231,232,233,234,235,236"};
//        for(int i=0;i<mArr.length;i++ )
//        {
        checkAllInterface("23",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
//        }

        //发行权限
        checkAllInterface("231",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:0000111000");

        //转账权限
        checkAllInterface("232",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:1100000000");
        //233同时管控多个接口，此为补充测试所有接口开放关闭一致
        check233Interface("0");

        //233为对账接口为此处检查接口  冻结权限--》变更为255
        checkAllInterface("233",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:0001000000");
        //233同时管控多个接口，此为补充测试所有接口开放关闭一致
        Thread.sleep(SLEEPTIME);
        check233Interface("1");
        //解除冻结权限--》变更为256
        //checkAllInterface("234",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:00001000000");

        //回收权限 当前无效 需要根据SDK修改适配

        checkAllInterface("235",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:0010000000");

        //余额查询权限
        checkAllInterface("236",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:0000000111");

        //临时增加权限设置999 为恢复sdk 同步区块
        SetPermCertainRight("999");

        //所有UTXO权限
        checkAllInterface("231,232,233,235,236",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + "UTXO:1111111111");

    }

    @Test
    public void chkMg1by1()throws Exception{
        checkAllInterface("25",def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        //添加归集地址权限
        checkAllInterface("251",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:100000" + UTXO0);
        //注销归集地址权限
        checkAllInterface("252",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:010000" + UTXO0);

        //添加发行地址权限
        checkAllInterface("253",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:001000" + UTXO0);
        //注销发行地址权限
        checkAllInterface("254",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:000100" + UTXO0);

        //冻结token权限
        checkAllInterface("255",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:000010" + UTXO0);
        //解除冻结token权限
        checkAllInterface("256",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:000001" + UTXO0);
        //添加及注销归集地址权限
        checkAllInterface("251,252,253,254,255,256",def + Sys0 + Store0 + DockerNill + WVM0 + "Mg:111111" + UTXO0);

    }


    @Test
    public void chkSunledgerMg()throws Exception{

        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),subLedgerPerm);
        checkAllInterface(subLedgerPerm,def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);
        //执行子链相关操作
        assertEquals("1111",subLedgerCheck());


        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"1,2,3,10");
        //执行子链相关操作
        assertEquals("0000",subLedgerCheck());


        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,281");
        //执行子链相关操作
        assertEquals("1000",subLedgerCheck());

        subLedgerName = "permOl_"+sdf.format(dt).substring(4)+ RandomUtils.nextInt(1000);//尽量将子链名称构造复杂一些
        String response = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -c " + subLedgerName,
                " -t sm3", " -w first", " -c raft", ids);
        sleepAndSaveInfo(SLEEPTIME);


        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,282");
        //执行子链相关操作
        assertEquals("0100",subLedgerCheck());


        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,283");
        //执行子链相关操作
        assertEquals("0010",subLedgerCheck());


        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,284");
        //执行子链相关操作
        assertEquals("0001",subLedgerCheck());


        //设置为full权限
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"999");
        //执行子链相关操作
        assertEquals("1111",subLedgerCheck());
    }

    @Test
    public void chkPeerDynamicChange()throws Exception{
        //将管理工具id权限
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),dynPeerPerm);
        //执行节点变更相关操作
        assertEquals("111",peerChangeCheck());
        checkAllInterface(dynPeerPerm,def + Sys0 + Store0 + DockerNill + WVM0 + Mg0 + UTXO0);


        //将管理工具id权限 设置为支持节点修改 不支持退出
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,261");
        //执行节点变更相关操作
        assertEquals("110",peerChangeCheck());


        //将管理工具id权限 设置为不支持节点修改 支持退出
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"10,262");
        //执行节点变更相关操作
        assertEquals("001",peerChangeCheck());


        //设置无权限
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"1,2,3,10");
        //执行子链相关操作
        assertEquals("000",peerChangeCheck());


        //设置权限999
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"999");
        //执行节点变更相关操作
        assertEquals("111",peerChangeCheck());

    }


    public String subLedgerCheck()throws Exception{
        String permStr="";
        permStr = permStr + pFunSysSet.subLedgerCreate();
        sleepAndSaveInfo(SLEEPTIME);
        permStr = permStr + pFunSysSet.subLedgerFreeze(subLedgerName);
        permStr = permStr + pFunSysSet.subLedgerRecover(subLedgerName);
        permStr = permStr + pFunSysSet.subLedgerDestroy(subLedgerName);

        return permStr;
    }

    public String peerChangeCheck()throws Exception{
        String permStr="";
        permStr = permStr + pFunSysSet.addPeerJoin(PEER1IP + ":" + PEER1RPCPort,PEER3IP,PEER3TCPPort,PEER3RPCPort);
        permStr = permStr + pFunSysSet.addPeerObserver(PEER1IP + ":" + PEER1RPCPort,PEER3IP,PEER3TCPPort,PEER3RPCPort);

        sleepAndSaveInfo(SLEEPTIME);
        permStr = permStr + pFunSysSet.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER3IP);
        return permStr;
    }
//@Test
    public String defaultSup()throws Exception{
        //201904确认不再测试validatekey接口，且
        String permStr="";
        //log.info("*************************************************************");

        permStr=permStr + pFunUTXO.soloGenAddr(PUBKEY1); //默认开启，无权限控制
        permStr=permStr + pFunUTXO.multiGenAddr(1,PUBKEY1,PUBKEY2); //默认开启，无权限控制
        //permStr=permStr + pFunUTXO.validateKey(PRIKEY6,PWD6); //默认开启，无权限控制
        permStr=permStr + pFun1.getPeerList();  //默认开启，无权限控制
        //log.info(permStr);
        return permStr; //should be a string with a length of 3
    }

    public String sysPermCheck()throws Exception{
        String permStr="";
        permStr=permStr + pFun1.getHeight();  //SDK发送查看链当前高度请求
        permStr=permStr + pFun1.getTransationBlock(glbTxHash); //SDK发送获取交易所在区块高度请求

        permStr=permStr + pFun1.getBlockByHeight(1); //SDK发送通过区块高度获取区块信息请求

        permStr=permStr + pFun1.getBlockByBlockHash(glbBlockHash); //SDK发送通过区块哈希获取区块信息请求

        permStr=permStr + pFun1.getStore(glbTxHash); //SDK发送查看基础存证交易请求
        permStr=permStr + pFun1.getStorePost(glbPriTxHash); //SDK发送查看隐私存证交易请求
        permStr=permStr + pFun1.getTransaction(glbTxHash); //SDK发送查看交易请求
        permStr=permStr + pFun1.getTransactionIndex(glbTxHash); //SDK发送查看交易索引请求

//        permStr=permStr + pFun1.txSearch("Global");//复杂查询 20191217移除tx/search接口测试 for mysql数据库
        return permStr; //should be a string with a length of 9
    }


    public String wvmPermCheck()throws Exception{
        String permStr= "";
        log.info("wvm permission:" + permStr);

        //安装wvm合约
        String ctName = "perm_" + sdf.format(dt) + RandomUtils.nextInt(100000);
        fileOperation.replace(testDataPath + "wvm/" + wvmFile + ".txt", orgName, ctName);
        permStr = permStr + pFunWVM.wvmInstallTest(wvmFile + "_temp.txt",PRIKEY2);
        permStr = permStr + pFunWVM.invokeNew(glbWVMHash,"initAccount",accountA,amountA);
        permStr = permStr + pFunWVM.wvmDestroyTest(glbWVMHash);
        return permStr; //should be a string with a length of 3
    }

    public String utxoPermCheck()throws Exception{
        String permStr="";
        permStr=permStr + pFunUTXO.soloTransfer(PRIKEY1,ADDRESS2,glbSoloToken);//SDK发送UTXO - Token单签转移请求，响应消息为节点无此权限
        permStr=permStr + pFunUTXO.multiTransfer(MULITADD4,PRIKEY1,"",ADDRESS2,glbMultiToken4);//SDK发送UTXO - Token多签转移请求，响应消息为节点无此权限
        permStr=permStr + pFunUTXO.recycle(MULITADD4,PRIKEY1,"",glbMultiToken4,"10");//SDK发送回收请求，响应消息为节点无此权限

        permStr=permStr + pFunUTXO.getTotal(0,0,glbSoloToken);

        /**
         * SDK发送UTXO - Token发行请求，响应消息为节点无此权限
         */
        pFunUTXO.issueSolTokenType="PermSo" + UtilsClass.Random(4);
        pFunUTXO.issueMulTokenType="PermMu" + UtilsClass.Random(4);
        permStr=permStr + pFunUTXO.soloIssueToken(PRIKEY1,pFunUTXO.issueSolTokenType,ADDRESS1); //SDK发送UTXO - Token单签发行请求，响应消息为节点无此权限
        //3/3多签发行时，会再最后一步sign提示无权限
        permStr=permStr + pFunUTXO.multiIssueToken(MULITADD3,pFunUTXO.issueMulTokenType,MULITADD3);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        permStr=permStr + pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求 完成签名即发行，会提示无权限

        permStr=permStr + pFunUTXO.getZeroAddrBalance(""); //查询零地址地址
        permStr=permStr + pFunUTXO.soloBalance(ADDRESS1,"");
        permStr=permStr + pFunUTXO.multiPostBalance(MULITADD4,"");

        return permStr; //should be a string with a length of 10
    }


    public String collManageCheck()throws Exception {
        String permStr="";
        permStr=permStr + pFunUTXO.addCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr + pFunUTXO.delCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求

        permStr=permStr + pFunUTXO.addIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr + pFunUTXO.delIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求

        permStr=permStr + pFunUTXO.utxoFreeze(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 冻结Token Type请求，响应消息为节点无此权限
        Thread.sleep(4000);
        permStr=permStr + pFunUTXO.utxoRecoverToken(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 解除Token冻结请求，响应消息为节点无此权限

        return permStr;//should be a string with a length of 6
    }

    public String storePermCheck()throws Exception{
        String permStr="";
        pFun1.Data="PermStore:" + UtilsClass.Random(4);
        permStr=permStr + pFun1.createStore();  //SDK发送基础存证交易请求，响应消息为节点无此权限
        permStr=permStr + pFun1.createStorePwd(); //SDK发送基础存证交易请求，响应消息为节点无此权限
        return permStr;//should be a string with a length of 2
    }

    public void shellCmd(String shellIP,String tempCmd) throws  Exception{
        //开启shell远程下指令开启
        Shell shell1=new Shell(shellIP,USERNAME,PASSWORD);
        //String cmd1="cd zll;ls";//替换为权限命令
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        for (String str : stdout) {
            log.info(str);
            assertEquals(str.contains("失败"), false);
            //assertEquals(str.contains("FuncUpdatePeerPermission success:  true"),true);
        }
        Thread.sleep(6000);
        log.info("sleep time(ms): " + 6000);
    }


    public void checkAllInterface(String right,String chkStr)throws Exception{

        SetPermCertainRight(right);

        String permList="";
        permList=permList +  "Def:";
        //默认开启接口检查
        permList=permList +  defaultSup(); //must be def +  "" a length of 4

        permList=permList +  "Sys:";
        //系统类交易检查
        permList=permList +  sysPermCheck(); //Eg. "Sys:111111111" a length of 9

        permList=permList +  "Store:";
        //存证类交易检查
        permList=permList +  storePermCheck();//Eg. "Store:11" a length of 2

        //20200413 分离docker合约相关测试
//        permList=permList +  "Docker:";
//        //合约交易类检查
//        permList=permList +  dockerPermCheck();//Eg. "Docker:11111" a length of 5

        permList = permList +  "WVM:";
        //wvm合约交易类检查
        permList = permList +  wvmPermCheck();//Eg. "WVM:111" a length of 3

        permList=permList +  "Mg:";
        //管理类接口
        permList=permList +  collManageCheck();//Eg. "Mg:111111" a length of 6

        permList=permList +  "UTXO:";
        //UTXO类交易权限检查
        permList=permList +  utxoPermCheck();//Eg. "UTXO:1111111111" a length of 10
        //return permList; //Eg. def +  "Sys:111111111Store:11Docker:11111Mg:111111UTXO:1111111111" a length of 33
        log.info("Right:" +  right +  " with Check Str:" +  permList);
        assertThat(permList, containsString(chkStr));


    }

    public void SetPermCertainRight(String right)throws Exception{
        String tempResp = shExeAndReturn(ToolIP,preCmd + right);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(tempResp,"mg"),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //权限更新后查询检查生效与否
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        //String cmd1="cd zll;ls";//替换为权限命令
        shell1.execute(toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + ledger);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String resp = StringUtils.join(stdout,"\n");
        log.info(" +  +  +  +  +  +  + :" + resp);
        if(right=="999")
        {
            assertEquals(resp.contains(fullPerm),true);
//            assertThat(resp,anyOf(containsString(fullPerm),containsString(fullPerm2)));
        }else
            assertEquals(resp.contains("[" + right.replace(","," ") + "]"),true);

        if(right.equals("0"))
        {
            Thread.sleep(3000);
        }
        //确认获取权限无异常后 增加sleep时间 此时间最好大于sdk从链上拉取权限列表时间
        Thread.sleep(SLEEPTIME*3/2);
    }

   // @Test
    public void pConfTest()throws Exception{
        String peerIP240=PEER1IP +  ":" +  PEER1RPCPort;//"10.1.3.240:9300";
        String peerIP246=PEER2IP +  ":" +  PEER2RPCPort;//"10.1.3.246:9300";
        String nonIP247=PEER3IP +  ":" + PEER3RPCPort;//"10.1.3.168:9300";

        String cmd1=toolPath + exeCmd + "-p " + peerIP240.split(":")[1] + " -d " + sdkID + " -m ";
        String cmd2=toolPath + exeCmd + "-p " + peerIP246.split(":")[1] + " -d " + sdkID + " -m ";
        String cmd3=toolPath + exeCmd + "-p " + nonIP247.split(":")[1] + " -d " + sdkID + " -m ";

        //依次向系统中不同的节点发送权限更新命令，以后一个权限更新为主
        log.info("向节点240更新权限列表 1");
        shellCmd(peerIP240.split(":")[0],cmd1 + "1");
        log.info("向节点246更新权限列表 211");
        shellCmd(peerIP246.split(":")[0],cmd2 + "211");
        Thread.sleep(6000);
        assertThat(pFun1.getHeight(), containsString("0"));
        assertThat(pFun1.createStore(), containsString("1"));

        //权限设置-p依次为两个区块系统中的两个节点，sdk最终权限以同系统设置为准
        shellCmd(peerIP240.split(":")[0],cmd1 + "1");
        shellCmd(nonIP247.split(":")[0],cmd3 + "211");
        assertThat(pFun1.getHeight(), containsString("1"));
        assertThat(pFun1.createStore(), containsString("0"));

        //权限设置通知的节点与SDK配置的节点不一致，权限设置有效
        String sdkIP = utilsClass.getIPFromStr(SDKADD);
        commonFunc.setSDKOnePeer(sdkIP,PEER2IP + ":" + PEER2RPCPort,"true",peerTLSServerName);
        shellCmd(sdkIP,killSDKCmd);
        shellCmd(sdkIP,startSDKCmd);
        Thread.sleep(5000);
        shellCmd(peerIP246.split(":")[0],cmd2 + "211");//向节点246发送权限变更通知
        commonFunc.setSDKOnePeer(sdkIP,PEER1IP + ":" + PEER1RPCPort,"true",peerTLSServerName);
        commonFunc.addSDKPeerCluster(sdkIP,PEER2IP + ":" + PEER2RPCPort,"true",peerTLSServerName);
        assertThat(pFun1.createStore(), containsString("1"));


    }
    @After
    public void resetPermission() throws Exception{
        commonFunc.setPermAndCheckResp(PEER1IP,PEER1RPCPort,utilsClass.getToolID(PEER1IP),"999");
        BeforeCondition bf=new BeforeCondition();
        bf.setPermission999();

        //将管理工具id权限设置为999
        shellExeCmd(PEER1IP,toolPath + exeCmd + " -p " + peerIP + " -d " + utilsClass.getToolID(PEER1IP) + " -m 999");

    }
}
