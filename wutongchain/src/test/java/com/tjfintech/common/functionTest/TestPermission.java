package com.tjfintech.common.functionTest;

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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class TestPermission {
    public static final String ToolIP="10.1.3.240";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    TestBuilder testBuilder=TestBuilder.getInstance();
    //SoloSign soloSign=testBuilder.getSoloSign();
    MultiSign multiSign=testBuilder.getMultiSign();
    Store store =testBuilder.getStore();

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

    String glbMultiToken3="";//MULITADD3的全局预设发行token
    String glbMultiToken4="";//MULITADD4的全局预设发行token
    String glbSoloToken="";//ADDRESS1的全局预设发行token


    String toolPath="cd /root/zll/permission/toolkit;";
    String exeCmd="./toolkit permission ";
    String peerIP="10.1.3.240:9300";
    String sdkID="29dd9b8931e7a82b5c4067b0c80a1d53eba100bb3625f580558b509f01132ada60c5fe45fed42a9699c686e3cdabcb22a3441583d230fd9fd0e1db4928f81cd4";
    //String sdkID="144166a82d85a96d79388e987a456ba70db683d7105505c38d768829c702eba6717a447c5e858165faefdaa847b3558a4b72db87fd379ac5154ad8fc4f3e13d2";
    //String sdkID="7d8c8eb266a6a445cde55e086c2ee63e577e3ff8ba5724ff2090a2a691384cbf87a881bc690695836c3e99424756bf3a3726bc0ae6c66795e51d351e6de7c0db";
    String preCmd=toolPath+exeCmd+"-p "+peerIP+" -d "+sdkID+" -m ";


    @Before
    public void beforeTest() throws Exception {
        //String mValue="1,2,3,4,5,6,7,8,9,10,21,211,212,22,221,222,223,224,23,231,232,233,234,235,236,24,25,251,252";
        String mValue="999";
        String cmd=preCmd+mValue;

        shellCmd(cmd);//添加所有权限
//
//        pFunCt.name="0215"+ RandomUtils.nextInt(100000);
//        glbCtName=pFunCt.name;
//        pFunCt.version="2.0";
//        pFunCt.installContract();
//        Thread.sleep(40000);
//        pFunCt.initMobileTest();

        pFun1.Data="GlobalStore:"+UtilsClass.Random(4);
        pFun1.createStore();  //SDK发送基础存证交易请求
        glbTxHash=pFun1.txHash; //此txHash是根据CreateStore执行后返回的txHash

        String Data = "cxTest-" + UtilsClass.Random(7);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response1= store.CreateStorePwd(Data,map);
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

        pFunUTXO.issAmount="200";

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

        pFunUTXO.soloBalance(PRIKEY1,glbSoloToken);
        pFunUTXO.multiPostBalance(MULITADD4,PRIKEY1,glbMultiToken4);
        pFunUTXO.multiPostBalance(MULITADD3,PRIKEY1,glbMultiToken3);
    }

    @Test
    public void CheckAll()throws Exception{
        //String mValue="1,2,3,4,5,6,7,8,9,10,21,211,212,22,221,222,223,224,23,231,232,233,234,235,236,24,25,251,252";
        String mValue="999";

        checkAllInterface(mValue,"Def:1111Sys:111111111Store:11Docker:11111Mg:1111UTXO:11111111111");
        Thread.sleep(3000);
        checkAllInterface("0","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");

        Thread.sleep(3000);
    }

    @Test
    public void chkSys1by1()throws Exception{
        //shellCmd("cd /root/zll/permission/toolkit;./toolkit permission -p 10.1.3.246:9300 -d 144166a82d85a96d79388e987a456ba70db683d7105505c38d768829c702eba6717a447c5e858165faefdaa847b3558a4b72db87fd379ac5154ad8fc4f3e13d2  -m 1");//添加所有权限
        String[] mArray={"5","6","7","9","10","11","300"};

        checkAllInterface("1","Def:1111Sys:100000000Store:00Docker:00000Mg:0000UTXO:00000000000");

        checkAllInterface("2","Def:1111Sys:010000000Store:00Docker:00000Mg:0000UTXO:00000000000");

        checkAllInterface("3","Def:1111Sys:001100000Store:00Docker:00000Mg:0000UTXO:00000000000");

        checkAllInterface("4","Def:1111Sys:000011110Store:00Docker:00000Mg:0000UTXO:00000000000");

        checkAllInterface("8","Def:1111Sys:000000001Store:00Docker:00000Mg:0000UTXO:00000000000");

        for(int i=0;i<mArray.length;i++)
        {
            checkAllInterface(mArray[i],"Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");
        }

    }

@Test
    public void ChkStore1by1() throws Exception{
        checkAllInterface("21","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");
        checkAllInterface("211","Def:1111Sys:000000000Store:10Docker:00000Mg:0000UTXO:00000000000");
        checkAllInterface("212","Def:1111Sys:000000000Store:01Docker:00000Mg:0000UTXO:00000000000");
        checkAllInterface("211,212","Def:1111Sys:000000000Store:11Docker:00000Mg:0000UTXO:00000000000");
    }

    @Test
    public void ChkDocker1by1() throws Exception{

        //String mValue="1,2,3,4,5,6,7,8,9,10,21,211,212,22,221,222,223,224,23,231,232,233,234,235,236,24,25,251,252";
        String mValue="999";
        String cmd=preCmd+mValue;

        shellCmd(cmd);//添加所有权限

        pFunCt.name="0220"+ RandomUtils.nextInt(100000);
        glbCtName=pFunCt.name;
        pFunCt.version="2.0";
        pFunCt.installContract();
        Thread.sleep(40000);
        pFunCt.initMobileTest();

//        String[] mArr={"22","221","222","223","224","221,222,223,224"};
//        for(int i=0;i<mArr.length;i++)
//        {
        checkAllInterface("22","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");
//        }
        //合约安装权限
        checkAllInterface("221","Def:1111Sys:000000000Store:00Docker:10000Mg:0000UTXO:00000000000");

        //合约交易权限
        checkAllInterface("223","Def:1111Sys:000000000Store:00Docker:01000Mg:0000UTXO:00000000000");

        //合约销毁权限
        checkAllInterface("222","Def:1111Sys:000000000Store:00Docker:00100Mg:0000UTXO:00000000000");

        //合约搜索
        checkAllInterface("224","Def:1111Sys:000000000Store:00Docker:00011Mg:0000UTXO:00000000000");

        //合约安装及销毁权限
        checkAllInterface("221,223","Def:1111Sys:000000000Store:00Docker:11000Mg:0000UTXO:00000000000");
        Thread.sleep(10000);
    }

    @Test
    public void chkUTXO1by1()throws Exception{

//        String[] mArr={"23","231","232","233","234","235","236","231,232,233,234,235,236"};
//        for(int i=0;i<mArr.length;i++)
//        {
        checkAllInterface("23","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");
//        }

        //发行权限
        checkAllInterface("231","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000111000");

        //转账权限 需要根据SDK修改适配
        checkAllInterface("232","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:11000000000");

        //冻结权限
        checkAllInterface("233","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00010000000");

        //解除冻结权限
        checkAllInterface("234","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00001000000");

        //回收权限 当前无效 需要根据SDK修改适配

        checkAllInterface("235","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00100000000");

        //余额查询权限
        checkAllInterface("236","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000111");

        //所有UTXO权限
        checkAllInterface("231,232,233,234,235,236","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:11111111111");

    }

    @Test
    public void chkMg1by1()throws Exception{
        checkAllInterface("25","Def:1111Sys:000000000Store:00Docker:00000Mg:0000UTXO:00000000000");
        //添加归集地址权限
        checkAllInterface("251","Def:1111Sys:000000000Store:00Docker:00000Mg:1000UTXO:00000000000");
        //注销归集地址权限
        checkAllInterface("252","Def:1111Sys:000000000Store:00Docker:00000Mg:0100UTXO:00000000000");

        //添加发行地址权限
        checkAllInterface("253","Def:1111Sys:000000000Store:00Docker:00000Mg:0010UTXO:00000000000");
        //注销发行地址权限
        checkAllInterface("254","Def:1111Sys:000000000Store:00Docker:00000Mg:0001UTXO:00000000000");

        //添加及注销归集地址权限
        checkAllInterface("251,252,253,254","Def:1111Sys:000000000Store:00Docker:00000Mg:1111UTXO:00000000000");

    }
//@Test
    public String defaultSup()throws Exception{
        String permStr="";
        //log.info("*************************************************************");

        permStr=permStr+pFunUTXO.soloGenAddr(PUBKEY1); //默认开启，无权限控制
        permStr=permStr+pFunUTXO.multiGenAddr(1,PUBKEY1,PUBKEY2); //默认开启，无权限控制
        permStr=permStr+pFunUTXO.validateKey(PRIKEY6,PWD6); //默认开启，无权限控制
        permStr=permStr+pFun1.getPeerList();  //默认开启，无权限控制
        //log.info(permStr);
        return permStr; //should be a string with a length of 4
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
        //perStr 若为"1"则表示存在合约安装权限，则等待合约安装
        if(permStr=="1") {
            Thread.sleep(40000);
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

        permStr=permStr+pFunUTXO.utxoFreeze(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 冻结Token Type请求，响应消息为节点无此权限
        Thread.sleep(4000);
        permStr=permStr+pFunUTXO.utxoRecoverToken(PRIKEY1,pFunUTXO.issueSolTokenType);//SDK发送UTXO - 解除Token冻结请求，响应消息为节点无此权限


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

        return permStr; //should be a string with a length of 11
    }


    public String collManageCheck()throws Exception {
        String permStr="";
        permStr=permStr+pFunUTXO.addCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr+pFunUTXO.delCollAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求

        permStr=permStr+pFunUTXO.addIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO添加归集地址-单签地址请求
        Thread.sleep(3000);

        permStr=permStr+pFunUTXO.delIssAddr(PRIKEY2,ADDRESS2); //SDK发送UTXO注销归集地址请求
        return permStr;//should be a string with a length of 2
    }

    public String storePermCheck()throws Exception{
        String permStr="";
        pFun1.Data="PermStore:"+UtilsClass.Random(4);
        permStr=permStr+pFun1.createStore();  //SDK发送基础存证交易请求，响应消息为节点无此权限
        permStr=permStr+pFun1.createStorePwd(); //SDK发送基础存证交易请求，响应消息为节点无此权限
        return permStr;//should be a string with a length of 2
    }

    public void shellCmd(String tempCmd) throws  Exception{
        //开启shell远程下指令开启
        Shell shell1=new Shell(ToolIP,USERNAME,PASSWORD);
        //String cmd1="cd zll;ls";//替换为权限命令
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        for (String str : stdout) {
            log.info(str);
            assertEquals(str.contains("失败"), false);
            //assertEquals(str.contains("FuncUpdatePeerPermission success:  true"),true);
        }
        Thread.sleep(8000);
    }


    public void checkAllInterface(String right,String chkStr)throws Exception{
        shellCmd(preCmd + right);
        Thread.sleep(3000);
        //shellCmd("cd /root/zll/permission/toolkit;./toolkit getpermission -p 10.1.3.246:9300");//添加所有权限
        String permList="";
        permList=permList+"Def:";
        //默认开启接口检查
        permList=permList+defaultSup(); //must be "Def:1111" a length of 4

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
        permList=permList+collManageCheck();//Eg. "Mg:1111" a length of 4

        permList=permList+"UTXO:";
        //UTXO类交易权限检查
        permList=permList+utxoPermCheck();//Eg. "UTXO:11111111111" a length of 11
        //return permList; //Eg. "Def:1111Sys:111111111Store:11Docker:11111Mg:1111UTXO:11111111111" a length of 33
        log.info("Right:"+ right +" with Check Str:"+permList);
        assertThat(permList, containsString(chkStr));


    }

    @Test
    public void pConfTest()throws Exception{
        String peerIp1="10.1.3.240:9300";
        String peerIP2="10.1.3.246:9300";
        String nonIp="10.1.3.247:9300";

        String cmd1=toolPath+exeCmd+"-p "+peerIp1+" -d "+sdkID+" -m ";
        String cmd2=toolPath+exeCmd+"-p "+peerIP2+" -d "+sdkID+" -m ";
        String cmd3=toolPath+exeCmd+"-p "+nonIp+" -d "+sdkID+" -m ";

        //依次向系统中不同的节点发送权限更新命令，以后一个权限更新为主
        shellCmd(cmd1+"1");
        shellCmd(cmd2+"211");
        assertThat(pFun1.getHeight(), containsString("0"));
        assertThat(pFun1.createStore(), containsString("1"));

        //权限设置-p依次为两个区块系统中的两个节点，sdk最终权限以同系统设置为准
        shellCmd(cmd1+"1");
        shellCmd(cmd3+"211");
        assertThat(pFun1.getHeight(), containsString("1"));
        assertThat(pFun1.createStore(), containsString("0"));

        //权限设置通知的节点与SDK配置的节点不一致，权限设置有效
        shellCmd("cd /root/zll/permission/sdk1/conf;cp config1.toml config.toml");//配置文件中仅配置246作为发送节点
        shellCmd("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellCmd("sh /root/zll/permission/sdk1/start.sh");
        //shellCmd("cd /root/zll/permission/sdk1;nohub httpservice &");
        Thread.sleep(5000);
        shellCmd(cmd2+"211");//向节点246发送权限变更通知
        shellCmd("cd /root/zll/permission/sdk1/conf;cp config2.toml config.toml");//恢复配置文件中的节点配置
        assertThat(pFun1.createStore(), containsString("1"));


//        //权限设置通知的节点与SDK配置的节点不一致，权限设置有效
//        shellCmd("cd /zll/chain2.0/sdk/conf;cp config1.toml config.toml");//配置文件中仅配置240作为发送节点
//        shellCmd("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
//        shellCmd("sh /zll/chain2.0/sdk/start.sh");
//
//        Thread.sleep(5000);
//        shellCmd(cmd2+"211");//向节点246发送权限变更通知
//        shellCmd("cd /zll/chain2.0/sdk/conf;cp config3.toml config.toml");//恢复配置文件中的节点配置
//        assertThat(pFun1.createStore(), containsString("1"));

    }
}
