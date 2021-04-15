package com.tjfintech.common.functionTest.sendMessage;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.WinExeOperation;
import com.tjfintech.common.stableTest.StableAutoTest;
import com.tjfintech.common.utils.UtilsClass;
import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class CallBack {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    HashMap<String, Object> mapSendMsg = new HashMap<>();
    List<Map> receiverListEmptyPubkey = utilsClass.constructReceiver("lucy002李", "");
    List<Map> receiverListEmptyId = utilsClass.constructReceiver("", PUBKEY1);
    List<Map> receiverListInvalidId = utilsClass.constructReceiver("123", "");
    List<Map> receiverListInvalidPubkey = utilsClass.constructReceiver("lucy002李", "123");
    List<Map> receiverList = utilsClass.constructReceiver("lucy002李", PUBKEY2);
    List<Map> receiverLists = utilsClass.constructReceiver("lucy001李", PUBKEY1, receiverList);
    List<Map> receiverListsInconformity = utilsClass.constructReceiver("lucy001李", PUBKEY1, receiverListEmptyPubkey);
    List<Map> receiverDecryptList = utilsClass.constructReceiver("lucy002李", PUBKEY3);

    List<Map> receiverList2 = utilsClass.constructReceiver("lucy003李", PUBKEY3);

    String jsondata = "{\"state\":400,\"message\":\"error\",\"data\":\"check\"}";
    String filedata = "";
    //    String msgdatafile = resourcePath + "SendMsgTestFiles\\callBackData.txt";
    String msgdatafile = System.getProperty("user.dir") + "\\callBackData.txt";
//    String msgdatafile = testDataPath + "SendMsgTestFiles\\callBackData.txt";


    @BeforeClass
    public static void init() throws Exception {
        //启动main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        if (winExeOperation.findProcess("main.exe"))
            winExeOperation.killProc("main.exe");
        if (winExeOperation.findProcess("mainSuccess.exe"))
            winExeOperation.killProc("mainSuccess.exe");
        if (winExeOperation.findProcess("mainError.exe"))
            winExeOperation.killProc("mainError.exe");

        if (!winExeOperation.findProcess("main.exe"))
            winExeOperation.startProc(testDataPath + "SendMsgTestFiles\\main.exe");
        sleepAndSaveInfo(5000, "wait.......");

        if (StringUtils.isEmpty(PUBKEY1)) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
        }

        if (tokenMultiAddr1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }

        UtilsClass utilsClass = new UtilsClass();
        CommonFunc commonFunc = new CommonFunc();
        commonFunc.setSDKApiCallbackDecrypt(utilsClass.getIPFromStr(TOKENADD), PRIKEY3);
//        commonFunc.setSDKApiCallbackLLocalId(utilsClass.getIPFromStr(TOKENADD), "lucy002李");
        commonFunc.setSDKApiCallbackLLocalIds(utilsClass.getIPFromStr(TOKENADD), "[\"lucy002李\",\"lucy003李\"]");
        commonFunc.setSDKApiOneLedger(utilsClass.getIPFromStr(TOKENADD), subLedger, "[\"http://10.1.4.19:9300/callback\"]");
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD), killSDKCmd, startTokenApiCmd + " -i true"); //重启sdk api
        sleepAndSaveInfo(SLEEPTIME, "等待SDK重启");

    }

    public static void clearMsgDateForFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String updateMsgDate(String fileName) throws Exception {

        sleepAndSaveInfo(5000, "update file.......");
        String filedata = utilsClass.readInput(fileName).toString();
        return filedata;
    }


    @Test
    public void callBackTest() throws Exception {
        //启动main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        if (winExeOperation.findProcess("main.exe"))
            winExeOperation.killProc("main.exe");
        if (winExeOperation.findProcess("mainSuccess.exe"))
            winExeOperation.killProc("mainSuccess.exe");
        if (winExeOperation.findProcess("mainError.exe"))
            winExeOperation.killProc("mainError.exe");

        if (!winExeOperation.findProcess("mainSuccess.exe"))
            winExeOperation.startProc(testDataPath + "SendMsgTestFiles\\mainSuccess.exe");
        sleepAndSaveInfo(5000, "wait.......");

        String issueAddr = tokenMultiAddr1;
        String collAddr = tokenMultiAddr1;
        String issAmount = "5000.999999";
        String comments = "issue send msg test";
        //发消息存证,接口返回参数增加exthash，消息内容reftx为发行交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        String tokentype1 = "token1" + utilsClass.Random(6);
        String issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype1, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertThat(queryBalance, allOf(containsString(tokentype1), containsString(tokentype1), containsString(tokentype1)));
        winExeOperation.killProc("mainSuccess.exe");

        if (!winExeOperation.findProcess("mainError.exe"))
            winExeOperation.startProc(testDataPath + "SendMsgTestFiles\\mainError.exe");
        sleepAndSaveInfo(5000, "wait.......");
        String tokentype2 = "token2" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype2, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("500", JSONObject.fromObject(queryBalance).getString("state"));
        winExeOperation.killProc("mainError.exe");
        if (!winExeOperation.findProcess("main.exe"))
            winExeOperation.startProc(testDataPath + "SendMsgTestFiles\\main.exe");
        sleepAndSaveInfo(5000, "wait.......");

    }

    @Test
    public void sendMsgInterfaceTest() throws Exception {

        //msgcode为空值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgcode为空值");
        mapSendMsg.put("reftx", "reftx001测试");
        String sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("check the validity of interface parameters, including length and character limits"));


        //不传参msgcode
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "不传参msgcode");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("check the validity of interface parameters, including length and character limits"));


        //msgdata为空值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("check the validity of interface parameters, including length and character limits"));


        //不传参msgdata
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("check the validity of interface parameters, including length and character limits"));


        //msgcode、sender、msgdata、reftx均为json格式数据
        clearMsgDateForFile(msgdatafile);
        log.info(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("sender", jsondata);
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", jsondata);
        mapSendMsg.put("reftx", jsondata);
        clearMsgDateForFile(msgdatafile);
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        String sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(sendMsgResphash));
        assertEquals(false, filedata.contains(jsondata));


        //仅必输字段传值，其余字段均为空值,不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("sender", "");
        mapSendMsg.put("receivers", null);
        mapSendMsg.put("msgdata", jsondata);
        mapSendMsg.put("reftx", "");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(sendMsgResphash));


        //仅传参必输字段msgcode、msgdata，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", jsondata);
        mapSendMsg.put("msgdata", jsondata);
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(sendMsgResphash));


        //receivers.pubkey为空，msgdata为明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListEmptyPubkey);
        mapSendMsg.put("msgdata", "receivers.pubkey为空，msgdata为明文");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(sendMsgResphash));
        assertEquals(true, filedata.contains("receivers.pubkey为空，msgdata为明文"));
        assertEquals(true, filedata.contains("reftx001测试"));
        assertEquals(true, filedata.contains(subLedger));


        //receivers.id为空，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试密文");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListEmptyId);
        mapSendMsg.put("msgdata", "receivers.id为空，不调回调接口");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(sendMsgResphash));


        //receivers.id为未配置的接收ID，不调回调接口
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试密文");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListInvalidId);
        mapSendMsg.put("msgdata", "receivers.id为未配置的接收ID，不调回调接口");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(sendMsgResphash));


        //receivers.pubkey为非法值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListInvalidPubkey);
        mapSendMsg.put("msgdata", "receivers.pubkey为非法值");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("公钥[123]不合法!"));


        //receivers.id/pubkey合法正确，msgdata为密文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList);
        mapSendMsg.put("msgdata", "msgdata为密文");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains("msgdata为密文"));
        assertEquals(true, filedata.contains(sendMsgResphash));
        assertEquals(true, filedata.contains("reftx001测试"));
        assertEquals(true, filedata.contains(subLedger));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "多个receivers时，一个公钥传值一个未传值");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        assertEquals("400", JSONObject.fromObject(sendMsgResp).getString("state"));
        assertEquals(true, sendMsgResp.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(sendMsgResphash));
        assertEquals(true, filedata.contains(subLedger));

        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        sendMsgResp = tokenModule.tokenSendMsg(mapSendMsg);
        sendMsgResphash = JSONObject.fromObject(sendMsgResp).getString("data");
        assertEquals("200", JSONObject.fromObject(sendMsgResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(sendMsgResphash));
        assertEquals(true, filedata.contains(subLedger));

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
        mapSendMsg.put("msgdata", "msgcode为空值，不发消息存证");
        mapSendMsg.put("reftx", "reftx001测试");

        String createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false, createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains("reftx001测试"));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "不传参msgcode，不发消息存证");
        mapSendMsg.put("reftx", "reftx001测试");

        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(createResp).getString("state"));
        assertEquals(false, createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains("reftx001测试"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据");
        mapSendMsg.put("reftx", "reftx001测试");

        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true, createResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));
        assertEquals(true, filedata.contains("reftx001测试"));
        assertEquals(false, filedata.contains("发消息存证,接口返回参数增加exthash，消息内容reftx为请求所填数据，TxHash为exthash数据"));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(createResp).getString("state"));
        assertEquals(true, createResp.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(createResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains("reftx001测试"));
        assertEquals(true, filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));
        assertEquals(true, filedata.contains(subLedger));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        createResp = tokenModule.tokenCreateAccount
                (utilsClass.Random(6), utilsClass.Random(6), "", "", listTag, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(createResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains("reftx001测试"));
        assertEquals(true, filedata.contains(JSONObject.fromObject(createResp).getString("exthash")));
        assertEquals(true, filedata.contains(subLedger));

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

        String tokentype1 = "token1" + utilsClass.Random(6);
        String issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype1, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(false, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        String tokentype2 = "token2" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype2, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(false, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为发行交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        String tokentype3 = "token3" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype3, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertThat(queryBalance, allOf(containsString(tokentype1), containsString(tokentype2), containsString(tokentype3)));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        String tokentype4 = "token4" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype4, issAmount, comments, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype4, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(subLedger));


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

        String tokentype5 = "token5"+utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(address,address,tokentype5,issAmount,comments,mapSendMsg);
        assertEquals("400",JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(false,issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false,filedata.contains("同步接口失败，不发消息存证"));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        String tokentype6 = "token6" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype6, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(subLedger));

    }

    @Test
    public void transferSendMsgTest() throws Exception {

        String fromAddr = tokenMultiAddr1;
        String comments = "transfer send msg test";
        String token = commonFunc.tokenModule_IssueToken(fromAddr, fromAddr, "5000.999999");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, token, "100");
        List<Map> lists = utilsClass.tokenConstructToken(tokenAccount2, token, "100", list);

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgcode为空值，不发消息存证");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");

        String transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(false, transferResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(transferResp).getString("data")));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(false, transferResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(transferResp).getString("data")));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为转账交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(fromAddr, token);
        assertEquals(true, queryBalance.contains("4200.999999"));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferResp).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(fromAddr, token);
        assertEquals(true, queryBalance.contains("4000.999999"));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        transferResp = tokenModule.tokenTransfer(fromAddr, comments, lists, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("所有的接收者的公钥是否传入应该保持一致!"));

    }

    @Test
    public void transferByUTXOSendMsgTest() throws Exception {

        String comments = "transfer by utxo send msg test";
        //T1>T1发行1000.123456的token1，M1>M2发行1000.876543的token2
        String tokenType = utilsClass.Random(6);
        String tokenType2 = UtilsClass.Random(6);
        String tokenType3 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, tokenType, "1000.123456", "发行token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1, tokenMultiAddr2, tokenType2, "1000.876543", "发行token2/1000.876543");
        String response3 = tokenModule.tokenIssue(tokenAccount2, tokenAccount2, tokenType3, "1000.876543", "发行token2/1000.876543");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");
        String hash3 = JSONObject.fromObject(response3).getString("data");

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgcode为空值，不发消息存证");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");
        //T1>>M1\M2\M3分别转账200的token1
        List<Map> list = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr2, list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr3, list2);
        String transferInfo1 = tokenModule.tokenTransferUTXOMsg(tokenAccount1, comments, list3, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo1).getString("state"));
        assertEquals(false, transferInfo1.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(transferInfo1).getString("data")));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");

        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");
        //M2>>T1\M1\M3分别转账200的token2
        list.clear();
        list2.clear();
        list3.clear();
        list = utilsClass.tokenConstructUTXO(hash2, 0, "200", tokenAccount1);
        list2 = utilsClass.tokenConstructUTXO(hash2, 0, "200", tokenMultiAddr1, list);
        list3 = utilsClass.tokenConstructUTXO(hash2, 0, "200", tokenMultiAddr3, list2);
        String transferInfo2 = tokenModule.tokenTransferUTXOMsg(tokenMultiAddr2, comments, list3, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo1).getString("state"));
        assertEquals(false, transferInfo1.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(transferInfo2).getString("data")));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String transferInfoHash2 = JSONObject.fromObject(transferInfo2).getString("data");


        //发消息存证,接口返回参数增加exthash，消息内容reftx为转账交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        //T1>>\M3分别转账200的token1
        list.clear();
        list = utilsClass.tokenConstructUTXO(transferInfoHash1, 3, "200", tokenMultiAddr3);
        String transferInfo3 = tokenModule.tokenTransferUTXOMsg(tokenAccount1, comments, list, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo3).getString("state"));
        assertEquals(true, transferInfo3.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo3).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo3).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //M2>>\M3分别转账200的token2
        list.clear();
        list = utilsClass.tokenConstructUTXO(transferInfoHash2, 3, "200", tokenMultiAddr3);
        String transferInfo4 = tokenModule.tokenTransferUTXOMsg(tokenMultiAddr2, comments, list, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo4).getString("state"));
        assertEquals(true, transferInfo4.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo4).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo4).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));
        String transferInfoHash4 = JSONObject.fromObject(transferInfo4).getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(tokenMultiAddr3, "");
        assertEquals("400", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("400", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType2));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //T2>>\T1分别转账200的token3
        list.clear();
        list = utilsClass.tokenConstructUTXO(hash3, 0, "200", tokenAccount1);
        String transferInfo5 = tokenModule.tokenTransferUTXOMsg(tokenAccount2, comments, list, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo5).getString("state"));
        assertEquals(true, transferInfo5.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo5).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo5).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1, tokenType3);
        assertEquals("200", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType3));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        list.clear();
        list = utilsClass.tokenConstructUTXO(transferInfoHash4, 0, "200", tokenMultiAddr2);
        String transferInfo6 = tokenModule.tokenTransferUTXOMsg(tokenMultiAddr3, comments, list, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(transferInfo6).getString("state"));
        assertEquals(true, transferInfo6.contains("所有的接收者的公钥是否传入应该保持一致!"));

    }

    @Test
    public void transferToZeroAccountSendMsgTest() throws Exception {

        String comments = "transfer to zeroaccount for destroy send msg test";
        String DBZeroAccout = "osEoy933LkHyyBcgjE7vCivfsX";
        String actualAmount1 = "1000.123456";
        String actualAmount2 = "1000.876543";
        log.info("使用转账接口 通过地址列表方式 冻结其中一个token 回收到零地址账户");
        //T1>M2发行1000.123456的token1，M1>M2发行1000.876543的token2
        String tokenType = utilsClass.Random(6);
        String tokenType2 = UtilsClass.Random(6);
        String tokenType3 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1, tokenMultiAddr2, tokenType, actualAmount1, "发行token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1, tokenMultiAddr2, tokenType2, actualAmount2, "发行token2/1000.876543");
        String response3 = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, tokenType3, "1000", "发行token3");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");
        String hash3 = JSONObject.fromObject(response3).getString("data");
        tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        //发消息存证,接口返回参数增加exthash，消息内容reftx为转账交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        //T1通过utxo转账200的token1到0地址做回收交易
        List<Map> list = utilsClass.tokenConstructUTXO(hash1, 0, "100", DBZeroAccout);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash2, 0, "900.876543", DBZeroAccout, list);
        String transferInfo = tokenModule.tokenTransferUTXOMsg(tokenMultiAddr2, comments, list2, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(true, transferInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //M2通过utxo转账200的token1到0地址做回收交易
        list.clear();
        list2.clear();
        list = utilsClass.tokenConstructToken(DBZeroAccout, tokenType, "900.123456");
        list2 = utilsClass.tokenConstructToken(DBZeroAccout, tokenType2, "100", list);
        transferInfo = tokenModule.tokenTransfer(tokenMultiAddr2, comments, list2, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(true, transferInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        log.info("交易上链后查询转出账户是否仍有token");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals(false, query.contains(tokenType));
        assertEquals(false, query.contains(tokenType2));

        log.info("交易上链后检查回收账户地址余额，使用两种方式");
        String queryDBZeroAcc = tokenModule.tokenGetBalance(DBZeroAccout, "");
        String queryZeroBalance = tokenModule.tokenGetDestroyBalance();

        assertEquals(actualAmount1, JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2, JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType2));

        assertEquals(actualAmount1, JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2, JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType2));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //T1通过utxo转账1000的token3到0地址做回收交易
        list.clear();
        list = utilsClass.tokenConstructUTXO(hash3, 0, "1000", DBZeroAccout);
        transferInfo = tokenModule.tokenTransferUTXOMsg(tokenAccount1, comments, list, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(true, transferInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(transferInfo).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        log.info("交易上链后查询转出账户是否仍有token");
        query = tokenModule.tokenGetBalance(tokenAccount1, "");
        assertEquals(false, query.contains(tokenType3));

        log.info("交易上链后检查回收账户地址余额，使用两种方式");
        queryDBZeroAcc = tokenModule.tokenGetBalance(DBZeroAccout, "");
        queryZeroBalance = tokenModule.tokenGetDestroyBalance();

        assertEquals("1000", JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType3));
        assertEquals("1000", JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType3));

    }

    @Test
    public void destorySendMsgTest() throws Exception {
        String token = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        commonFunc.tokenModule_TransferToken(tokenAccount1, tokenAccount2, token, "100");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, token, "100");
        List<Map> lists = utilsClass.tokenConstructToken(tokenAccount2, token, "10", list);
        String comments = "destory by list send msg test";


        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");

        String destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(false, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(false, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为回收交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1, token);
        assertEquals(true, queryBalance.contains("4500.999999"));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByList(lists, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1, token);
        assertEquals(true, queryBalance.contains("4400.999999"));


    }

    @Test
    public void destoryByUTXOSendMsgTest() throws Exception {

        //T1>T1发行1000.123456的token1，M1>M2发行1000.876543的token2
        String tokenType = utilsClass.Random(6);
        String tokenType2 = UtilsClass.Random(6);
        String tokenType3 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, tokenType, "1000.123456", "发行token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1, tokenMultiAddr2, tokenType2, "1000.876543", "发行token2/1000.876543");
        String response3 = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, tokenType3, "1000", "发行token3/1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");
        String hash3 = JSONObject.fromObject(response3).getString("data");
        String comments = "destory by utxo send msg test";

        //T1>>M1\M2\M3分别转账200的token1
        List<Map> list = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr2, list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1, 0, "200", tokenMultiAddr3, list2);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list3);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");
        //M1通过utxo回收100的token1
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1, 0, "100");
        String destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(false, destroyInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destroyInfo).getString("data")));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");
        //M2通过utxo回收100的token1
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1, 1, "100");
        destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(false, destroyInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destroyInfo).getString("data")));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        //M3通过utxo回收100的token1,交易失败
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1, 2, "100");
        destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(true, destroyInfo.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为回收交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        //T1通过utxo回收100的token1
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1, 3, "100");
        destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(true, destroyInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //M2通过utxo回收100的token2
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(hash2, 0, "100");
        destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(true, destroyInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        //余额查询
        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1, tokenType);
        String queryBalance1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr2, tokenType);
        String queryBalance3 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        String queryBalance4 = tokenModule.tokenGetBalance(tokenMultiAddr2, tokenType2);

        assertEquals("300.123456", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance1).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType));
        assertEquals("200", JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType));
        assertEquals("900.876543", JSONObject.fromObject(queryBalance4).getJSONObject("data").getString(tokenType2));


        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");
        //M2通过utxo回收100的token2
        list.clear();
        list = utilsClass.tokenConstrucDestroytUTXO(hash3, 0, "100");
        destroyInfo = tokenModule.tokenDestoryByList(null, list, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destroyInfo).getString("state"));
        assertEquals(true, destroyInfo.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destroyInfo).getString("data")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        //余额查询
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1, tokenType3);
        assertEquals("900", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType3));

    }

    @Test
    public void destoryByTokenSendMsgTest() throws Exception {

        String token20 = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        String token21 = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        String token22 = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        String token23 = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        String token24 = commonFunc.tokenModule_IssueToken(tokenAccount1, tokenAccount1, "5000.999999");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String comments = "destory by token send msg test";

        //msgcode为空值，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "msgcode为空值，不发消息存证");

        String destoryResp = tokenModule.tokenDestoryByTokenType(token20, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(false, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));


        //不传参msgcode，不发消息存证
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "不传参msgcode，不发消息存证");

        destoryResp = tokenModule.tokenDestoryByTokenType(token21, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(false, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(destoryResp).getString("data")));


        //多个receivers时，一个公钥传值一个未传值
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverListsInconformity);
        mapSendMsg.put("msgdata", "msgdata001测试密文");
        mapSendMsg.put("reftx", "reftx001测试");
        destoryResp = tokenModule.tokenDestoryByTokenType(token22, comments, mapSendMsg);
        assertEquals("400", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("所有的接收者的公钥是否传入应该保持一致!"));


        //发消息存证,接口返回参数增加exthash，消息内容reftx为回收交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByTokenType(token22, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("hash")));
        assertEquals(false, filedata.contains("msgdata001测试"));


        //配置文件配置私钥，请求传输对应公钥，消息发送明文
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverDecryptList);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByTokenType(token23, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("hash")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String destroyBalance = tokenModule.tokenGetDestroyBalance();
        assertThat(destroyBalance, allOf(containsString(token20), containsString(token21), containsString(token22), containsString(token23)));

        //配置文件localID支持配置数组，支持向多个ID发送消息
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverList2);
        mapSendMsg.put("msgdata", "配置文件配置私钥，请求传输对应公钥，消息发送明文");
        mapSendMsg.put("reftx", "reftx001测试");

        destoryResp = tokenModule.tokenDestoryByTokenType(token24, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(destoryResp).getString("state"));
        assertEquals(true, destoryResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(destoryResp).getJSONObject("data").getString("hash")));
        assertEquals(true, filedata.contains("配置文件配置私钥，请求传输对应公钥，消息发送明文"));
        assertEquals(true, filedata.contains(subLedger));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        destroyBalance = tokenModule.tokenGetDestroyBalance();
        assertThat(destroyBalance, allOf(containsString(token24)));

    }

    @Test
    public void ledgersSendMsgTest() throws Exception {

        StableAutoTest stableAutoTest = new StableAutoTest();
        String ledger = stableAutoTest.getLedgerIDs()[0];

        //正确配置该应用链消息回调，消息发送成功,API数据同步成功
        String issueAddr = tokenMultiAddr1;
        String collAddr = tokenMultiAddr1;
        String issAmount = "5000.999999";
        String comments = "issue send msg test";
        //发消息存证,接口返回参数增加exthash，消息内容reftx为发行交易哈希data数据，TxHash为exthash数据
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        String tokentype = "token" + utilsClass.Random(6);
        String issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(true, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        String queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("200", JSONObject.fromObject(queryBalance).getString("state"));
        assertThat(queryBalance, allOf(containsString(tokentype), containsString(tokentype), containsString(tokentype)));

        //删除该应用链消息回调配置，消息发送成功,API数据同步失败
        commonFunc.setSDKApiOneLedger(utilsClass.getIPFromStr(TOKENADD), "", "");
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD), killSDKCmd, startTokenApiCmd); //重启sdk api
        sleepAndSaveInfo(SLEEPTIME, "等待SDK重启");
        clearMsgDateForFile(msgdatafile);
        mapSendMsg.clear();
        mapSendMsg.put("msgcode", "msgcode001测试");
        mapSendMsg.put("sender", "sender001测试");
        mapSendMsg.put("receivers", receiverLists);
        mapSendMsg.put("msgdata", "msgdata001测试");
        mapSendMsg.put("reftx", "reftx001测试");
        tokentype = "token" + utilsClass.Random(6);
        issueeResp = tokenModule.tokenIssue(issueAddr, collAddr, tokentype, issAmount, comments, mapSendMsg);
        assertEquals("200", JSONObject.fromObject(issueeResp).getString("state"));
        assertEquals(true, issueeResp.contains("exthash"));
        filedata = updateMsgDate(msgdatafile);
        assertEquals(false, filedata.contains(JSONObject.fromObject(issueeResp).getString("exthash")));
        assertEquals(false, filedata.contains(JSONObject.fromObject(issueeResp).getString("data")));
        assertEquals(false, filedata.contains("msgdata001测试"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);
        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("500", JSONObject.fromObject(queryBalance).getString("state"));

        //正常重启API，不同步上一笔异常数据
        commonFunc.setSDKApiOneLedger(utilsClass.getIPFromStr(TOKENADD), subLedger, "[\"http://10.1.4.19:9300/callback\"]");
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD), killSDKCmd, startTokenApiCmd); //重启sdk api
        sleepAndSaveInfo(SLEEPTIME, "等待SDK重启");
        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("500", JSONObject.fromObject(queryBalance).getString("state"));

        //使用./wtsdk API -i true 重启，同步上一笔异常数据
        commonFunc.setSDKApiOneLedger(utilsClass.getIPFromStr(TOKENADD), subLedger, "[\"http://10.1.4.19:9300/callback\"]");
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD), killSDKCmd, startTokenApiCmd + " -i true"); //重启sdk api
        sleepAndSaveInfo(SLEEPTIME, "等待SDK重启");
        queryBalance = tokenModule.tokenGetBalance(collAddr, "");
        assertEquals("200", JSONObject.fromObject(queryBalance).getString("state"));
        assertThat(queryBalance, allOf(containsString(tokentype), containsString(tokentype), containsString(tokentype)));

    }


    //    @AfterClass
    public static void KillMainExe() throws Exception {
        //测试结束关闭main.exe 事件通知及回调信息接收客户端
        WinExeOperation winExeOperation = new WinExeOperation();
        winExeOperation.killProc("main.exe");
    }
}



