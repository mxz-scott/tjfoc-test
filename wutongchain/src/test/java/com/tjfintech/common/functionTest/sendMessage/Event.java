package com.tjfintech.common.functionTest.sendMessage;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.*;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.WinExeOperation;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * @Author: Lilu
 * @Date: 2020/7/07 10:54
 * @Description:
 **/
@Slf4j
public class Event {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    Store store = testBuilder.getStore();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Contract contract = testBuilder.getContract();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    WVMContractTest wvmContractTest = new WVMContractTest();

    String jsondata = "{\"state\":400,\"message\":\"error\",\"data\":\"check\"}";
    String filedata = "";
    String msgdatafile = resourcePath + "SendMsgTestFiles\\eventData.txt";
//    String msgdatafile = System.getProperty("user.dir") + "\\eventData.txt";//自动化启动进程后文件的位置



    @BeforeClass
    public static void init() throws Exception {
        //启动main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        if(!winExeOperation.findProcess("main.exe"))
            winExeOperation.startProc(resourcePath + "SendMsgTestFiles\\main.exe");

        if(StringUtils.isEmpty(PUBKEY1)) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }

//        if(StringUtils.isEmpty(ADDRESS1)) {
//            BeforeCondition beforeCondition = new BeforeCondition();
//            beforeCondition.createAddresses();
//            beforeCondition.collAddressTest();
//        }
    }


    public static void clearMsgDateForFile(String fileName) {
        File file =new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter =new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  String updateMsgDate(String fileName) throws Exception {

        sleepAndSaveInfo(4000,"update file.......");
        String filedata = utilsClass.readInput(fileName).toString();
        log.info(filedata);
        return filedata;
    }


    @Test
    public void tokenEventTest() throws Exception {

        //普通存证交易
        clearMsgDateForFile(msgdatafile);
        String response = tokenModule.tokenCreateStore(jsondata);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //隐私存证交易
        clearMsgDateForFile(msgdatafile);
        Map<String,Object> map = new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);
        response = tokenModule.tokenCreatePrivateStore(jsondata,map);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //数据存证扩展交易
        clearMsgDateForFile(msgdatafile);
        HashMap<String, Object> mapSendMsg = new HashMap<>();
        List<Map> receiverList = utilsClass.constructReceiver("","");
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers",receiverList);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");
        response = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));

        //移除发行地址
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenDelMintAddr(tokenAccount1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //移除归集地址
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenDelCollAddr(tokenAccount1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //添加发行地址
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenAddMintAddr(tokenAccount1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //添加归集地址
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenAddCollAddr(tokenAccount1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));

        //发行token
        clearMsgDateForFile(msgdatafile);
        String token = utilsClass.Random(6);
        response = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,token,"1000","");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));

        //转让token
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenTransfer(tokenAccount1,tokenAccount2,token,"100","");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //回收token（按账户地址）
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenDestoryByList(tokenAccount2,token,"100","");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //冻结token
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenFreezeToken(token);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //解冻token
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenRecoverToken(token);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("data")),
                containsString("onchain"),containsString("txevent")));


        //回收token（按类型）
        clearMsgDateForFile(msgdatafile);
        response = tokenModule.tokenDestoryByTokenType(token,"");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("data").getString("hash")),
                containsString("onchain"),containsString("txevent")));


    }


//    @Test
//    public void SDKEventTest() throws Exception {
//
//        //普通存证交易
//        clearMsgDateForFile(msgdatafile);
//        String response = store.CreateStore(jsondata);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //隐私存证交易
//        clearMsgDateForFile(msgdatafile);
//        Map<String,Object>map = new HashMap<>();
//        map.put("pubkey1",PUBKEY1);
//        map.put("pubkey2",PUBKEY2);
//        response = store.CreatePrivateStore(jsondata,map);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
//
//
//
//        //移除发行地址
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.delissueaddress("",ADDRESS1);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //移除归集地址
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.delCollAddress("",ADDRESS1);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        clearMsgDateForFile(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //添加发行地址
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.addissueaddress("",ADDRESS1);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //添加归集地址
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.collAddress("",ADDRESS1);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//        //单签发行token
//        clearMsgDateForFile(msgdatafile);
//        String token = "SOLO"+utilsClass.Random(6);
//        response = soloSign.issueToken(PRIKEY1,token,"1000","单签发行token",ADDRESS1);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//        //单签转让token
//        clearMsgDateForFile(msgdatafile);
//        List<Map> list = soloSign.constructToken(ADDRESS2,token,"100");
//        response = soloSign.Transfer(list,PRIKEY1,"转让token");
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//        //回收token
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.Recycle(PRIKEY1,token,"100");
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //冻结token
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.freezeToken(PRIKEY1,token);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //解冻token
//        clearMsgDateForFile(msgdatafile);
//        response = multiSign.recoverFrozenToken(PRIKEY1,token);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getString("Data")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //多签发行token
//        clearMsgDateForFile(msgdatafile);
//        token = "MULTI"+Random(6);
//        response = multiSign.issueToken(MULITADD1,token,"1000","多签发行token");
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
//        String Tx2 = JSONObject.fromObject(multiSign.Sign(Tx1,PRIKEY1)).getJSONObject("Data").getString("Tx");
//        String Tx3 = JSONObject.fromObject(multiSign.Sign(Tx2,PRIKEY2)).getJSONObject("Data").getString("Tx");
//        response = multiSign.Sign(Tx3,PRIKEY3);
//        assertEquals("true",JSONObject.fromObject(response).getJSONObject("Data").getString("IsCompleted"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("TxId")),
//                containsString("onchain"),containsString("txevent")));
//
//
//        //多签转让token
//        clearMsgDateForFile(msgdatafile);
//        list.clear();
//        list = utilsClass.constructToken(MULITADD2,token,"100");
//        response = multiSign.Transfer(PRIKEY1,"多签转让token",MULITADD1,list);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        Tx2 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
//        Tx3 = JSONObject.fromObject(multiSign.Sign(Tx2,PRIKEY2)).getJSONObject("Data").getString("Tx");
//        response = multiSign.Sign(Tx3,PRIKEY3);
//        assertEquals("true",JSONObject.fromObject(response).getJSONObject("Data").getString("IsCompleted"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("TxId")),
//                containsString("onchain"),containsString("txevent")));
//
//
//    }



//    @Test
//    public void WVMContractEventTest() throws Exception {
//
//        String category = "wvm";
//        String prikey = PRIKEY1;
//        String wvmfilepath = resourcePath + "SendMsgTestFiles\\Identity.wlang";
//        String contractname = "";
//
//
//        //安装合约，当前版本会自动调init方法，会发一笔交易事件一笔合约事件
//        clearMsgDateForFile(msgdatafile);
//        String data = utilsClass.encryptBASE64(utilsClass.readInput(wvmfilepath).toString().getBytes());
//        String response = contract.InstallWVM(data,prikey,category);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
//        assertThat(filedata,allOf(containsString("init"),containsString("scevent")));
//        contractname = JSONObject.fromObject(response).getJSONObject("Data").getString("Name");
//
//
//        //调用合约，成功后发一笔交易事件一笔合约事件
//        clearMsgDateForFile(msgdatafile);
////        response = wvmContractTest.invokeNew(contractname,"AddUserInfo",
////                "\"Name\":\"lcuy\",\"ID\":\"12333\",\"Age\":\"111\"","3046022100c8494c97496c7a3030be82beddb873de3d66df87c377b971115c78b6c8ef9221022100e5819d553a77c4598b5a683de9dd065b5da419b085e56d12f5d34175a1e56d7f");
//        response = wvmContractTest.invokeNew(contractname,"GetUserInfo",
//                "123");
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
////        assertThat(filedata,allOf(containsString("You have no permission to add [userInfo]!"),
////                containsString("scevent"),containsString("Message")));
//        assertThat(filedata,allOf(containsString("scevent"),containsString("123")));
//
//        //销毁合约，成功后发送一笔交易事件
//        clearMsgDateForFile(msgdatafile);
//        response = contract.DestroyWVM(contractname,category);
//        assertEquals("200", JSONObject.fromObject(response).getString("State"));
//        filedata = updateMsgDate(msgdatafile);
//        assertThat(filedata,allOf(containsString(JSONObject.fromObject(response).getJSONObject("Data").getString("Figure")),
//                containsString("onchain"),containsString("txevent")));
//
//    }

//    @AfterClass
    public static void KillMainExe()throws Exception{
        //测试结束关闭main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        winExeOperation.killProc("main.exe");
    }
}



