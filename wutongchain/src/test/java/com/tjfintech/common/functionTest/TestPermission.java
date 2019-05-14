package com.tjfintech.common.functionTest;

import com.sun.deploy.util.StringUtils;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.USERNAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class TestPermission {
    public static final String ToolIP="10.1.3.240";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();

    APermfuncSys pFun1 =new APermfuncSys();
    APermfuncDocker pFunCt =new APermfuncDocker();
    APermfuncUTXO pFunUTXO =new APermfuncUTXO();

    String glbBlockHash ="xP4fUESPobYXi1+ROFjlc32XLAu4GGUg2FfC5ygPpqU=";
    String glbTxHash="wsjmIJhZaXRS5mNGFMC4xiL529XoUpow3bu5N/JWNB8=";
    String glbPriTxHash="wsjmIJhZaXRS5mNGFMC4xiL529XoUpow3bu5N/JWNB8=";
    String glbCtName="t888";
    String okCode="200";
    String okMsg="success";

    String errCode="404";
    String errMsg="does not found Permission";
    String def="Def:111";
    //"+def+Sys0+Store0+Docker0+Mg0+UTXO0+"
    String Sys0="Sys:000000000";
    String Store0="Store:00";
    String Docker0="Docker:00000";
    String Mg0="Mg:000000";
    String UTXO0="UTXO:0000000000";


    String glbMultiToken3="";//MULITADD3的全局预设发行token
    String glbMultiToken4="";//MULITADD4的全局预设发行token
    String glbSoloToken="";//ADDRESS1的全局预设发行token


    String toolPath="cd "+PTPATH+"toolkit;";
    String exeCmd="./toolkit permission ";
    String peerIP="9300";
    String sdkID= UtilsClass.getSDKID();
    //String sdkID="144166a82d85a96d79388e987a456ba70db683d7105505c38d768829c702eba6717a447c5e858165faefdaa847b3558a4b72db87fd379ac5154ad8fc4f3e13d2";
    //String sdkID="7d8c8eb266a6a445cde55e086c2ee63e577e3ff8ba5724ff2090a2a691384cbf87a881bc690695836c3e99424756bf3a3726bc0ae6c66795e51d351e6de7c0db";
    String preCmd=toolPath+exeCmd+"-p "+peerIP+" -d "+sdkID+" -m ";
    ArrayList<String> dockerList =new ArrayList();

    boolean bExe=false;


    @Before
    public void beforeTest() throws Exception {

        if(certPath!=""&& bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();

            bExe=true;
        }

        if(bExe==false) {

            String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));
            Shell shellSDK = new Shell(sdkIP, USERNAME, PASSWD);
            shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
            Thread.sleep(2000);
            shellSDK.execute("sh " + PTPATH + "sdk/start.sh");
            Thread.sleep(3000);
        }

        String mValue="999";
        String cmd=preCmd+mValue;

        shellCmd(ToolIP,cmd);//添加所有权限

        pFun1.Data="GlobalStore:"+UtilsClass.Random(4);
        pFun1.createStore();  //SDK发送基础存证交易请求
        glbTxHash=pFun1.txHash; //此txHash是根据CreateStore执行后返回的txHash

        String Data = "cxTest-" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreateStorePwd(Data,map);
        log.info(response1);
        JSONObject jsonObject=JSONObject.fromObject(response1);
        glbPriTxHash = jsonObject.getJSONObject("Data").get("Figure").toString();


        pFun1.getBlockByHeight(1); //SDK发送通过区块高度获取区块信息请求
        //pFun1.blockHash是根据pFun1.getBlockByHeight提取的区块哈希
        glbBlockHash=pFun1.blockHash;
        pFun1.getBlockByBlockHash(glbBlockHash); //SDK发送通过区块哈希获取区块信息请求


        pFunUTXO.soloGenAddr(PUBKEY1); //SDK发送UTXO生成单签地址交易请求
        pFunUTXO.multiGenAddr(3,PUBKEY1,PUBKEY6,PUBKEY7); //SDK发送UTXO生成3/3多签地址交易请求
        pFunUTXO.multiGenAddr(1,PUBKEY1,PUBKEY2);     //SDK发送UTXO生成1/2多签地址交易请求

        pFunUTXO.addCollAddr(PRIKEY1,ADDRESS1); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(2000);
        pFunUTXO.addCollAddr(PRIKEY1,MULITADD3); //SDK发送UTXO添加归集地址-3/3带密码多签地址请求
        Thread.sleep(2000);
        pFunUTXO.addCollAddr(PRIKEY1,MULITADD4); //SDK发送UTXO添加归集地址-1/2不带密码多签地址请求

        glbSoloToken="Glb1So"+UtilsClass.Random(4);
        glbMultiToken4="Glb1Mu4"+UtilsClass.Random(4);

        pFunUTXO.issAmount="300";

        pFunUTXO.soloIssueToken(PRIKEY1,glbSoloToken,ADDRESS1); //SDK发送UTXO - Token单签发行请求
        Thread.sleep(6000);

        //多签MULITADD3发行给其他多签地址MULITADD4
        pFunUTXO.multiIssueToken(MULITADD3,glbMultiToken4,MULITADD4);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求

        Thread.sleep(6000);

        //多签MULITADD3发行给自己MULITADD3
        glbMultiToken3="GlbMu3"+UtilsClass.Random(4);
        pFunUTXO.multiIssueToken(MULITADD3,glbMultiToken3,MULITADD3);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求

        Thread.sleep(5000);

        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(multiSign.Balance(PRIKEY1,glbSoloToken)).getJSONObject("Data").getString("Total"));
        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(multiSign.Balance(MULITADD4,PRIKEY1,glbMultiToken4)).getJSONObject("Data").getString("Total"));
        assertEquals(pFunUTXO.issAmount,JSONObject.fromObject(multiSign.Balance(MULITADD3,PRIKEY1,glbMultiToken3)).getJSONObject("Data").getString("Total"));

        bExe=true;
    }

    @Test
    public void CheckAll()throws Exception{
        //String mValue="1,2,3,4,5,6,7,8,9,10,21,211,212,22,221,222,223,224,23,231,232,233,234,235,236,24,25,251,252";
        checkAllInterface("0",def+Sys0+Store0+Docker0+Mg0+UTXO0);

        Thread.sleep(3000);
        checkAllInterface("999",def+"Sys:111111111Store:11Docker:11111Mg:111111UTXO:1111111111");
        Thread.sleep(3000);
    }

    @Test
    public void chkSys1by1()throws Exception{
        String[] mArray={"5","6","7","9","10","11","300"};

        checkAllInterface("1",def+"Sys:100000000"+Store0+Docker0+Mg0+UTXO0);

        checkAllInterface("2",def+"Sys:010000000"+Store0+Docker0+Mg0+UTXO0);

        checkAllInterface("3",def+"Sys:001100000"+Store0+Docker0+Mg0+UTXO0);

        checkAllInterface("4",def+"Sys:000011110"+Store0+Docker0+Mg0+UTXO0);

        checkAllInterface("8",def+"Sys:000000001"+Store0+Docker0+Mg0+UTXO0);

        for(int i=0;i<mArray.length;i++)
        {
            checkAllInterface(mArray[i],def+Sys0+Store0+Docker0+Mg0+UTXO0);
        }

    }

@Test
    public void ChkStore1by1() throws Exception{
        checkAllInterface("21",def+Sys0+Store0+Docker0+Mg0+UTXO0);
        checkAllInterface("211",def+Sys0+"Store:10"+Docker0+Mg0+UTXO0);
        checkAllInterface("212",def+Sys0+"Store:01"+Docker0+Mg0+UTXO0);
        checkAllInterface("211,212",def+Sys0+"Store:11"+Docker0+Mg0+UTXO0);
    }

    @Test
    public void ChkDocker1by1() throws Exception{
        String mValue="999";
        String cmd=preCmd+mValue;

        shellCmd(ToolIP,cmd);//添加所有权限

        pFunCt.name="0220"+ RandomUtils.nextInt(100000);
        glbCtName=pFunCt.name;
        pFunCt.version="2.0";
        pFunCt.installContract();
        Thread.sleep(ContractInstallSleep);
        log.info("Contract install sleep time(ms): "+ContractInstallSleep);
        dockerList.add(pFunCt.name);
        log.info("docker list size: "+dockerList.size());

        pFunCt.initMobileTest();

//        String[] mArr={"22","221","222","223","224","221,222,223,224"};
//        for(int i=0;i<mArr.length;i++)
//        {
        checkAllInterface("22",def+Sys0+Store0+Docker0+Mg0+UTXO0);
//        }
        //合约安装权限
        checkAllInterface("221",def+Sys0+Store0+"Docker:10000"+Mg0+UTXO0);

        //合约交易权限
        checkAllInterface("223",def+Sys0+Store0+"Docker:01000"+Mg0+UTXO0);

        //合约销毁权限
        checkAllInterface("222",def+Sys0+Store0+"Docker:00100"+Mg0+UTXO0);

        //合约搜索
        checkAllInterface("224",def+Sys0+Store0+"Docker:00011"+Mg0+UTXO0);

        //合约安装及销毁权限
        checkAllInterface("221,223",def+Sys0+Store0+"Docker:11000"+Mg0+UTXO0);


        for (String str : dockerList) {
            log.info("Destroy Docker:" + str);
            pFunCt.name=str;
            pFunCt.destroyContract();
        }
        Thread.sleep(10000);
    }

    public void check233Interface(String chk) throws Exception{
        //233为对账接口为此处检查接口
        //shellCmd(ToolIP,preCmd + "233");
        assertEquals(pFunUTXO.getTotal(0,0,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getSDKBalance(MULITADD3,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getChainBalance(MULITADD3,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getUTXODetail(MULITADD3,glbMultiToken3),chk);
        assertEquals(pFunUTXO.getTokenState(glbMultiToken3),chk);
        assertEquals(pFunUTXO.getTotalByDay(0,0),chk);
    }

//    @Test
//    //public void check233Interface(String chk) throws Exception{
//    public void check233Interface() throws Exception{
//        //233为对账接口为此处检查接口
//        String chk="0";
//        shellCmd(ToolIP,preCmd + "232");
//        Thread.sleep(5000);
//        assertEquals(pFunUTXO.getTotal(0,0,glbMultiToken3),chk);
//        assertEquals(pFunUTXO.getSDKBalance(MULITADD3,glbMultiToken3),chk);
//        assertEquals(pFunUTXO.getChainBalance(MULITADD3,glbMultiToken3),chk);
//        assertEquals(pFunUTXO.getUTXODetail(MULITADD3,glbMultiToken3),chk);
//        assertEquals(pFunUTXO.getTokenState(glbMultiToken3),chk);
//        assertEquals(pFunUTXO.getTotalByDay(0,0),chk);
//    }

    @Test
    public void chkUTXO1by1()throws Exception{

//        String[] mArr={"23","231","232","233","234","235","236","231,232,233,234,235,236"};
//        for(int i=0;i<mArr.length;i++)
//        {
        checkAllInterface("23",def+Sys0+Store0+Docker0+Mg0+UTXO0);
//        }

        //发行权限
        checkAllInterface("231",def+Sys0+Store0+Docker0+Mg0+"UTXO:0000111000");

        //转账权限
        checkAllInterface("232",def+Sys0+Store0+Docker0+Mg0+"UTXO:1100000000");
        //233同时管控多个接口，此为补充测试所有接口开放关闭一致
        check233Interface("0");

        //233为对账接口为此处检查接口  冻结权限--》变更为255
        checkAllInterface("233",def+Sys0+Store0+Docker0+Mg0+"UTXO:0001000000");
        //233同时管控多个接口，此为补充测试所有接口开放关闭一致
        check233Interface("1");
        //解除冻结权限--》变更为256
        //checkAllInterface("234",def+Sys0+Store0+Docker0+Mg0+"UTXO:00001000000");

        //回收权限 当前无效 需要根据SDK修改适配

        checkAllInterface("235",def+Sys0+Store0+Docker0+Mg0+"UTXO:0010000000");

        //余额查询权限
        checkAllInterface("236",def+Sys0+Store0+Docker0+Mg0+"UTXO:0000000111");

        //所有UTXO权限
        checkAllInterface("231,232,233,235,236",def+Sys0+Store0+Docker0+Mg0+"UTXO:1111111111");

    }

    @Test
    public void chkMg1by1()throws Exception{
        checkAllInterface("25",def+Sys0+Store0+Docker0+Mg0+UTXO0);
        //添加归集地址权限
        checkAllInterface("251",def+Sys0+Store0+Docker0+"Mg:100000"+UTXO0);
        //注销归集地址权限
        checkAllInterface("252",def+Sys0+Store0+Docker0+"Mg:010000"+UTXO0);

        //添加发行地址权限
        checkAllInterface("253",def+Sys0+Store0+Docker0+"Mg:001000"+UTXO0);
        //注销发行地址权限
        checkAllInterface("254",def+Sys0+Store0+Docker0+"Mg:000100"+UTXO0);

        //冻结token权限
        checkAllInterface("255",def+Sys0+Store0+Docker0+"Mg:000010"+UTXO0);
        //解除冻结token权限
        checkAllInterface("256",def+Sys0+Store0+Docker0+"Mg:000001"+UTXO0);
        //添加及注销归集地址权限
        checkAllInterface("251,252,253,254,255,256",def+Sys0+Store0+Docker0+"Mg:111111"+UTXO0);

    }
//@Test
    public String defaultSup()throws Exception{
        //201904确认不再测试validatekey接口，且
        String permStr="";
        //log.info("*************************************************************");

        permStr=permStr+pFunUTXO.soloGenAddr(PUBKEY1); //默认开启，无权限控制
        permStr=permStr+pFunUTXO.multiGenAddr(1,PUBKEY1,PUBKEY2); //默认开启，无权限控制
        //permStr=permStr+pFunUTXO.validateKey(PRIKEY6,PWD6); //默认开启，无权限控制
        permStr=permStr+pFun1.getPeerList();  //默认开启，无权限控制
        //log.info(permStr);
        return permStr; //should be a string with a length of 3
    }

    public String sysPermCheck()throws Exception{
        String permStr="";
        permStr=permStr+pFun1.getHeight();  //SDK发送查看链当前高度请求
        permStr=permStr+pFun1.getTransationBlock(glbTxHash); //SDK发送获取交易所在区块高度请求

        permStr=permStr+pFun1.getBlockByHeight(1); //SDK发送通过区块高度获取区块信息请求
        permStr=permStr+pFun1.getBlockByBlockHash(glbBlockHash); //SDK发送通过区块哈希获取区块信息请求

        permStr=permStr+pFun1.getStore(glbTxHash); //SDK发送查看基础存证交易请求
        permStr=permStr+pFun1.getStorePost(glbPriTxHash); //SDK发送查看隐私存证交易请求
        permStr=permStr+pFun1.getTransaction(glbTxHash); //SDK发送查看交易请求
        permStr=permStr+pFun1.getTransactionIndex(glbTxHash); //SDK发送查看交易索引请求

        permStr=permStr+pFun1.txSearch("Global");//复杂查询
        return permStr; //should be a string with a length of 9
    }

    public String dockerPermCheck()throws Exception{
        String permStr="";
        pFunCt.name="0215"+ RandomUtils.nextInt(100000);
        pFunCt.version="2.0";
        permStr=permStr+pFunCt.installContract();//SDK发送合约安装交易请求
        log.info("docker permission:"+permStr);
        //perStr 若为"1"则表示存在合约安装权限，则等待合约安装
        if(permStr.contains("1")) {
            Thread.sleep(ContractInstallSleep);
            log.info("Contract install sleep time(ms): "+ContractInstallSleep);
            dockerList.add(pFunCt.name);
        }
        permStr=permStr+pFunCt.initMobileTest();//SDK发送合约交易请求
        permStr=permStr+pFunCt.destroyContract();//SDK发送合约删除交易请求
        log.info(glbCtName);
        permStr=permStr+pFunCt.searchByKey("Mobile0",glbCtName);//SDK发送按key查询请求
        permStr=permStr+pFunCt.searchByPrefix("Mo",glbCtName);//SDK发送按prefix查询请求
        return permStr; //should be a string with a length of 5
    }

    public String utxoPermCheck()throws Exception{
        String permStr="";
        permStr=permStr+pFunUTXO.soloTransfer(PRIKEY1,ADDRESS2,glbSoloToken);//SDK发送UTXO - Token单签转移请求，响应消息为节点无此权限
        permStr=permStr+pFunUTXO.multiTransfer(MULITADD4,PRIKEY1,"",ADDRESS2,glbMultiToken4);//SDK发送UTXO - Token多签转移请求，响应消息为节点无此权限
        permStr=permStr+pFunUTXO.recycle(MULITADD4,PRIKEY1,"",glbMultiToken4,"10");//SDK发送回收请求，响应消息为节点无此权限

        permStr=permStr+pFunUTXO.getTotal(0,0,glbSoloToken);
//        permStr=permStr+pFunUTXO.utxoFreeze(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 冻结Token Type请求，响应消息为节点无此权限
//        Thread.sleep(4000);
//        permStr=permStr+pFunUTXO.utxoRecoverToken(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 解除Token冻结请求，响应消息为节点无此权限


        /**
         * SDK发送UTXO - Token发行请求，响应消息为节点无此权限
         */
        pFunUTXO.issueSolTokenType="PermSo"+UtilsClass.Random(4);
        pFunUTXO.issueMulTokenType="PermMu"+UtilsClass.Random(4);
        permStr=permStr+pFunUTXO.soloIssueToken(PRIKEY1,pFunUTXO.issueSolTokenType,ADDRESS1); //SDK发送UTXO - Token单签发行请求，响应消息为节点无此权限
        //3/3多签发行时，会再最后一步sign提示无权限
        permStr=permStr+pFunUTXO.multiIssueToken(MULITADD3,pFunUTXO.issueMulTokenType,MULITADD3);//SDK发送UTXO - Token3/3多签发行给自己请求
        pFunUTXO.multiSign(PRIKEY1,"");//SDK发送UTXO - Token3/3多签第一次签名请求
        pFunUTXO.multiSign(PRIKEY6,PWD6);//SDK发送UTXO - Token3/3多签第二次签名请求
        permStr=permStr+pFunUTXO.multiSign(PRIKEY7,PWD7);//SDK发送UTXO - Token3/3多签第三次签名请求 完成签名即发行，会提示无权限

        //1/2多签发行带私钥，此同单签发行提示无权限
//        pFunUTXO.multiIssueToken(MULITADD4,pFunUTXO.issueMulTokenType,MULITADD4);//SDK发送UTXO - Token1/2多签发行给自己请求
//        permStr=permStr+pFunUTXO.multiSign(PRIKEY1,"");

        permStr=permStr+pFunUTXO.getZeroAddrBalance(""); //查询零地址地址
        permStr=permStr+pFunUTXO.soloBalance(PRIKEY1,""); //默认开启，无权限控制
        permStr=permStr+pFunUTXO.multiPostBalance(MULITADD4,PRIKEY1,""); //默认开启，无权限控制

        return permStr; //should be a string with a length of 10
    }


    public String collManageCheck()throws Exception {
        String permStr="";
        permStr=permStr+pFunUTXO.addCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr+pFunUTXO.delCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求

        permStr=permStr+pFunUTXO.addIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr+pFunUTXO.delIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求

        permStr=permStr+pFunUTXO.utxoFreeze(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 冻结Token Type请求，响应消息为节点无此权限
        Thread.sleep(4000);
        permStr=permStr+pFunUTXO.utxoRecoverToken(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 解除Token冻结请求，响应消息为节点无此权限

        return permStr;//should be a string with a length of 6
    }

    public String storePermCheck()throws Exception{
        String permStr="";
        pFun1.Data="PermStore:"+UtilsClass.Random(4);
        permStr=permStr+pFun1.createStore();  //SDK发送基础存证交易请求，响应消息为节点无此权限
        permStr=permStr+pFun1.createStorePwd(); //SDK发送基础存证交易请求，响应消息为节点无此权限
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
        log.info("sleep time(ms): "+6000);
    }


    public void checkAllInterface(String right,String chkStr)throws Exception{

        shellCmd(ToolIP,preCmd + right);
//        Thread.sleep(10000);
//        log.info("sleep time(ms): "+10000);

        //权限更新后查询检查生效与否
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        //String cmd1="cd zll;ls";//替换为权限命令
        shell1.execute(toolPath+"./toolkit getpermission -p 9300");
        ArrayList<String> stdout = shell1.getStandardOutput();
        String resp = StringUtils.join(stdout,"\n");
        log.info(resp);
        if(right=="999")
        {
            assertEquals(resp.contains("[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 224 231 232 233 235 236 251 252 253 254 255 256]"),true);
        }else
            assertEquals(resp.contains("["+right.replace(","," ")+"]"),true);

        if(right.equals("0"))
        {
            Thread.sleep(3000);
        }


        String permList="";
        permList=permList+"Def:";
        //默认开启接口检查
        permList=permList+defaultSup(); //must be def+"" a length of 4

        permList=permList+"Sys:";
        //系统类交易检查
        permList=permList+sysPermCheck(); //Eg. "Sys:111111111" a length of 9

        permList=permList+"Store:";
        //存证类交易检查
        permList=permList+storePermCheck();//Eg. "Store:11" a length of 2

         //SDK发送Fate交易请求，响应消息为节点无此权限
        permList=permList+"Docker:";
        //合约交易类检查
        permList=permList+dockerPermCheck();//Eg. "Docker:11111" a length of 5

        permList=permList+"Mg:";
        //管理类接口
        permList=permList+collManageCheck();//Eg. "Mg:111111" a length of 6

        permList=permList+"UTXO:";
        //UTXO类交易权限检查
        permList=permList+utxoPermCheck();//Eg. "UTXO:1111111111" a length of 10
        //return permList; //Eg. def+"Sys:111111111Store:11Docker:11111Mg:111111UTXO:1111111111" a length of 33
        log.info("Right:"+ right +" with Check Str:"+permList);
        assertThat(permList, containsString(chkStr));


    }

    @Test
    public void pConfTest()throws Exception{
        String peerIP240="10.1.3.240:9300";
        String peerIP246="10.1.3.246:9300";
        String nonIP247="10.1.3.168:9300";

        String cmd1=toolPath+exeCmd+"-p "+peerIP240.split(":")[1]+" -d "+sdkID+" -m ";
        String cmd2=toolPath+exeCmd+"-p "+peerIP246.split(":")[1]+" -d "+sdkID+" -m ";
        String cmd3=toolPath+exeCmd+"-p "+nonIP247.split(":")[1]+" -d "+sdkID+" -m ";

        //依次向系统中不同的节点发送权限更新命令，以后一个权限更新为主
        log.info("向节点240更新权限列表 1");
        shellCmd(peerIP240.split(":")[0],cmd1+"1");
        log.info("向节点246更新权限列表 211");
        shellCmd(peerIP246.split(":")[0],cmd2+"211");
        Thread.sleep(6000);
        assertThat(pFun1.getHeight(), containsString("0"));
        assertThat(pFun1.createStore(), containsString("1"));

        //权限设置-p依次为两个区块系统中的两个节点，sdk最终权限以同系统设置为准
        shellCmd(peerIP240.split(":")[0],cmd1+"1");
        shellCmd(nonIP247.split(":")[0],cmd3+"211");
        assertThat(pFun1.getHeight(), containsString("1"));
        assertThat(pFun1.createStore(), containsString("0"));

        //权限设置通知的节点与SDK配置的节点不一致，权限设置有效
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        shellCmd(sdkIP,"cd "+PTPATH+"sdk/conf;cp config1.toml config.toml");//配置文件中仅配置246作为发送节点
        shellCmd(sdkIP,"ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellCmd(sdkIP,"sh "+PTPATH+"sdk/start.sh");
        //shellCmd("cd "+PTPATH+"sdk1;nohub httpservice &");
        Thread.sleep(5000);
        shellCmd(peerIP246.split(":")[0],cmd2+"211");//向节点246发送权限变更通知
        shellCmd(sdkIP,"cd "+PTPATH+"sdk/conf;cp config2.toml config.toml");//恢复配置文件中的节点配置
        assertThat(pFun1.createStore(), containsString("1"));


    }
    @After
    public void resetPermission() throws Exception{
        BeforeCondition bf=new BeforeCondition();
        bf.initTest();
    }
}
