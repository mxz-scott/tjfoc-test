package com.tjfintech.common.functionTest.sendMessage;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.WinExeOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * @Author: Lilu
 * @Date: 2020/6/30 10:54
 * @Description:
 **/
@Slf4j
public class CallBackSyncTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();


    HashMap<String, Object> mapSendMsg = new HashMap<>();
    List<Map> receiverListEmptyPubkey = utilsClass.constructReceiver("lucy002李", "");
    List<Map> receiverListEmptyId = utilsClass.constructReceiver("",PUBKEY1);
    List<Map> receiverListInvalidId = utilsClass.constructReceiver("123", "");
    List<Map> receiverListInvalidPubkey = utilsClass.constructReceiver("lucy002李", "123");
    List<Map> receiverList = utilsClass.constructReceiver("lucy002李",PUBKEY1);
    List<Map> receiverLists = utilsClass.constructReceiver("lucy001李", "", receiverListEmptyPubkey);
    String jsondata = "{\"state\":400,\"message\":\"error\",\"data\":\"check\"}";
    String filedata = "";
//    String msgdatafile = resourcePath + "SendMsgTestFiles\\callBackData.txt";
    String msgdatafile = System.getProperty("user.dir") + "\\callBackData.txt";



    @BeforeClass
    public static void initBefore() throws Exception {
        //启动main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        if(!winExeOperation.findProcess("main.exe"))
            winExeOperation.startProc(testDataPath + "SendMsgTestFiles\\main.exe");

        syncFlag = true;
        if (StringUtils.isEmpty(PUBKEY1)) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
        }

        if(tokenMultiAddr1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
//        syncTimeout =1;
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
        log.info("file data: \n" + filedata);
        return filedata;
    }


    @Test
    public void sendMsgInterfaceTest() throws Exception {

        //msgcode、sender、msgdata、reftx均为json格式数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("sender", jsondata);
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", jsondata);
        mapSendMsg.put("reftx", jsondata);
        clearMsgDateForFile(msgdatafile);
        String sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        String sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(sendMsgResphash));


        //仅必输字段传值，其余字段均为空值,不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        receiverLists.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("sender", "");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", jsondata);
        mapSendMsg.put("reftx", "");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains(sendMsgResphash));


        //仅传参必输字段msgcode、msgdata，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("msgdata", jsondata);
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains(sendMsgResphash));


        //receivers.pubkey为空，msgdata为明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListEmptyPubkey);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(sendMsgResphash));
        assertEquals(true,filedata.contains("msgdata001测试"));


        //receivers.id为空，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试密文");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListEmptyId);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains(sendMsgResphash));


        //receivers.id为未配置的接收ID，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试密文");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListInvalidId);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains(sendMsgResphash));


        //receivers.pubkey为非法值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListInvalidPubkey);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true,sendMsgResp.contains("公钥[123]不合法!"));


        //receivers.id/pubkey合法正确，msgdata为密文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("msgdata001测试密文"));
        assertEquals(true,filedata.contains(sendMsgResphash));

    }


    @Test
    public void createAccountSendMsgTest() throws Exception {

        String entityID = "";
        String entityName = "";
        String groupID = "";
        String comments = "";
        ArrayList<String> listTag = new ArrayList<>();

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");

        String createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6),utilsClass.Random(6),"","",listTag,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("msgcode为空值，不发消息存证"));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6),utilsClass.Random(6),"","",listTag,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("不传参msgcode，不发消息存证"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6),utilsClass.Random(6),"","",listTag,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));

    }

    @Test
    public void issueSendMsgTest() throws Exception {

        String issueAddr = tokenMultiAddr1;
        String collAddr = tokenMultiAddr1;
        String issAmount = "5000.999999";
        String comments = "issue send msg test";

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");

        String tokentype1 = "token1"+utilsClass.Random(6);
        String createResp = tokenModule.tokenIssue(issueAddr,collAddr,tokentype1,issAmount,comments,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("msgcode为空值，不发消息存证"));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        String tokentype2 = "token2"+utilsClass.Random(6);
        createResp = tokenModule.tokenIssue(issueAddr,collAddr,tokentype2,issAmount,comments,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("不传参msgcode，不发消息存证"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        String tokentype3 = "token3" + utilsClass.Random(6);
        createResp = tokenModule.tokenIssue(issueAddr,collAddr,tokentype3,issAmount,comments,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));

        String queryBalance = tokenModule.tokenGetBalance(collAddr,"");
        assertThat(queryBalance,allOf(containsString(tokentype1),containsString(tokentype2),containsString(tokentype3)));

        //同步接口失败，不发消息存证
        ArrayList<String> listTag = new ArrayList<>();
        String response = tokenModule.tokenCreateAccount
                (utilsClass.Random(6),utilsClass.Random(6),"","",listTag);
        String address = JSONObject.fromObject(response).getString("data");
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "同步接口失败，不发消息存证");

        String tokentype4 = "token4"+utilsClass.Random(6);
        createResp = tokenModule.tokenIssue(address,address,tokentype4,issAmount,comments,mapSendMsg);
        assertEquals("400",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("同步接口失败，不发消息存证"));

    }


    @Test
    public void transferSendMsgTest() throws Exception {

        String fromAddr = tokenMultiAddr1;
        String comments = "transfer send msg test";
        String token = commonFunc.tokenModule_IssueToken(fromAddr,fromAddr,"5000.999999");

        sleepAndSaveInfo(7000);//等待发行成功

        List<Map>list = utilsClass.tokenConstructToken(tokenAccount1,token,"100");
        List<Map>lists = utilsClass.tokenConstructToken(tokenAccount2,token,"100",list);




        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        String createResp = tokenModule.tokenTransfer(fromAddr,comments,lists,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));

        String queryBalance = tokenModule.tokenGetBalance(fromAddr,token);
        assertEquals(true,queryBalance.contains("4800.999999"));


    }

    @Test
    public void destorySendMsgTest() throws Exception {
        String token = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"5000.999999");

        sleepAndSaveInfo(7000); //等待发行成功

        commonFunc.tokenModule_TransferToken(tokenAccount1,tokenAccount2,token,"100");

        sleepAndSaveInfo(2000,"等待数据库同步");
//
        List<Map>list = utilsClass.tokenConstructToken(tokenAccount1,token,"100");
        List<Map>lists = utilsClass.tokenConstructToken(tokenAccount2,token,"10",list);
        String comments = "destory by list send msg test";



        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        String createResp = tokenModule.tokenDestoryByList(lists,comments,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));

        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1,token);
        assertEquals(true,queryBalance.contains("4800.999999"));

    }


    @Test
    public void destoryByTokenSendMsgTest() throws Exception {

        String token22 = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"5000.999999");

        String comments = "destory by token send msg test";

        sleepAndSaveInfo(7000);//等待发行成功



        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        String createResp = tokenModule.tokenDestoryByTokenType(token22,comments,mapSendMsg);
        assertEquals("200",JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true,createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(createResp).getJSONObject("data").getString("exthash")));

        String destroyBalance = tokenModule.tokenGetDestroyBalance();
        assertThat(destroyBalance,containsString(token22));

    }

    @Test
    public void DestoryUseTrfInterface()throws Exception{
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("sender", jsondata);
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", jsondata);
        mapSendMsg.put("reftx", jsondata);

        commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"5000.999999");
        assertEquals("200",JSONObject.fromObject(globalResponse).getString("state"));
        String txHash = JSONObject.fromObject(globalResponse).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        List<Map>list = utilsClass.tokenConstructUTXO(txHash,0,"10","osEoy933LkHyyBcgjE7vCivfsX");
        String response2 = tokenModule.tokenTransferUTXOMsg(tokenAccount1,"",list,mapSendMsg);
        assertEquals(true,response2.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true,filedata.contains(JSONObject.fromObject(response2).getString("exthash")));
    }


//    @AfterClass
    public static void KillMainExe()throws Exception{
        //测试结束关闭main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        winExeOperation.killProc("main.exe");
    }
}



