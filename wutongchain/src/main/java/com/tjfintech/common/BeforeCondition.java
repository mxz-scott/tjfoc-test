package com.tjfintech.common;


import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.tjfintech.common.utils.UtilsClass.subLedger;


@Slf4j
//该类实现权限赋值999；更新证书类型，根据新的证书类型生成多签地址；注册发行地址和归集地址
public class BeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    Contract contract = testBuilder.getContract();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GoSmartToken st = new GoSmartToken();

    //赋值权限999 区分是否主子链
    public void setPermission999() throws Exception {

        String toolPath = "cd " + ToolPATH + ";";
        String exeCmd = "./" + ToolTPName + " permission ";

        SDKID = utilsClass.getSDKID();
        String ledger = "";
        ledger = (subLedger != "") ? " -c " + subLedger : "";
        String preCmd = toolPath + exeCmd + "-p " + PEER1RPCPort + " -s SDK " + ledger + " -d " + SDKID + " -m ";
        String getPerm = toolPath + "./" + ToolTPName + " getpermission -p " + PEER1RPCPort + " -d " + SDKID + ledger;


        //如果没有权限 则设置权限  修改为设置 兼容版本升级时 权限列表变更需要重新赋权限的问题
//        if(!shExeAndReturn(PEER1IP,getPerm).contains(fullPerm)){
        assertEquals(true, shExeAndReturn(PEER1IP, preCmd + "999").contains("success"));
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals(true, shExeAndReturn(PEER1IP, getPerm).contains(fullPerm));
//        }
    }

    //3.1版本sdk不再另外配置数据库
    public void clearDataSetPerm999() throws Exception {
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB, resetPeerBase);
        //重启SDK
        utilsClass.setAndRestartSDK();

        setPermission999();
    }


    /**
     * 赋予权限和读取公私钥对
     */
    //@Test
    public void givePermission() throws Exception {
        setPermission999();
        updatePubPriKey();//从文件中根据配置certPath读取指定类型的公私钥对
    }

    /**
     * 赋予权限和读取公私钥对。
     */
    @Test
    public void SetPermAndInit() throws Exception {
//        setPermission999();
        SDKADD = rSDKADD;
        updatePubPriKey();//从文件中根据配置certPath读取指定类型的公私钥对
//        collAddressTest();//添加归集地址发行地址

        SDKADD = TOKENADD;
//        createTokenAccount();
//        tokenAddIssueCollAddr();
    }

    @AfterClass
    public static void setURLSDK() {
        SDKADD = rSDKADD;
    }


    /**
     * 添加发行地址和归集地址
     */
    public void collAddressTest() throws Exception {
        createAddresses(); //生成多签地址
        String response = multiSign.addCollAddrs(IMPPUTIONADD, MULITADD3, MULITADD4, MULITADD5, MULITADD7,
                ADDRESS1, ADDRESS2, ADDRESS4);
        //3.0.1版本修改为重复添加时sdk会返回已存在的报错
        assertThat(response, anyOf(containsString("\"state\":200"), containsString("exist")));

        //2.0.1版本需要添加发行地址后才可以发行
        String response11 = multiSign.addIssueAddrs(IMPPUTIONADD, MULITADD3, MULITADD4, MULITADD5, MULITADD7,
                ADDRESS1, ADDRESS2, ADDRESS4);
        assertThat(response11, anyOf(containsString("\"state\":200"), containsString("exist")));

    }

    /**
     * token api添加发行地址和归集地址
     */
    public void tokenAddIssueCollAddr() throws Exception {
        String response11 = tokenModule.tokenAddMintAddr(tokenAccount1);
        String response12 = tokenModule.tokenAddMintAddr(tokenAccount2);
        String response13 = tokenModule.tokenAddMintAddr(tokenAccount3);
        String response14 = tokenModule.tokenAddMintAddr(tokenAccount4);
        String response15 = tokenModule.tokenAddMintAddr(tokenAccount5);
        String response21 = tokenModule.tokenAddMintAddr(tokenMultiAddr1);
        String response22 = tokenModule.tokenAddMintAddr(tokenMultiAddr2);
        String response23 = tokenModule.tokenAddMintAddr(tokenMultiAddr3);
        String response24 = tokenModule.tokenAddMintAddr(tokenMultiAddr4);

        String response31 = tokenModule.tokenAddCollAddr(tokenAccount1);
        String response32 = tokenModule.tokenAddCollAddr(tokenAccount2);
        String response33 = tokenModule.tokenAddCollAddr(tokenAccount3);
        String response34 = tokenModule.tokenAddCollAddr(tokenAccount4);
        String response35 = tokenModule.tokenAddCollAddr(tokenAccount5);
        String response41 = tokenModule.tokenAddCollAddr(tokenMultiAddr1);
        String response42 = tokenModule.tokenAddCollAddr(tokenMultiAddr2);
        String response43 = tokenModule.tokenAddCollAddr(tokenMultiAddr3);
        String response44 = tokenModule.tokenAddCollAddr(tokenMultiAddr4);

        assertThat(response11, containsString("200"));
        assertThat(response12, containsString("200"));
        assertThat(response13, containsString("200"));
        assertThat(response14, containsString("200"));
        assertThat(response15, containsString("200"));
        assertThat(response21, containsString("200"));
        assertThat(response22, containsString("200"));
        assertThat(response23, containsString("200"));
        assertThat(response24, containsString("200"));

        assertThat(response31, containsString("200"));
        assertThat(response32, containsString("200"));
        assertThat(response33, containsString("200"));
        assertThat(response34, containsString("200"));
        assertThat(response35, containsString("200"));
        assertThat(response41, containsString("200"));
        assertThat(response42, containsString("200"));
        assertThat(response43, containsString("200"));
        assertThat(response44, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME, "add issue and collect addr waiting......");

    }


    /**
     * 测试用例T284的前提条件。发行对应token
     */

    public void T284_BeforeCondition(String tokenType) {
        //String tokenType = "cx-chenxu"+certPath;
        String amount = "1000";
        //String amount = "1000";
        log.info(IMPPUTIONADD + "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY4);
    }

    /**
     * 创建公私钥对
     */
    public void updatePubPriKey() throws Exception {
        if (certPath == "") {
            log.info("using default setting key pairs in UtilsClass");
            return;
        }
        PRIKEY1 = utilsClass.getKeyPairsFromFile(certPath + "/keys1/key.pem");
        PRIKEY2 = utilsClass.getKeyPairsFromFile(certPath + "/keys2/key.pem");
        PRIKEY3 = utilsClass.getKeyPairsFromFile(certPath + "/keys3/key.pem");
        PRIKEY4 = utilsClass.getKeyPairsFromFile(certPath + "/keys4/key.pem");
        PRIKEY5 = utilsClass.getKeyPairsFromFile(certPath + "/keys5/key.pem");
        PRIKEY6 = utilsClass.getKeyPairsFromFile(certPath + "/keys6/key.pem");
        PRIKEY7 = utilsClass.getKeyPairsFromFile(certPath + "/keys7/key.pem");

        PUBKEY1 = utilsClass.getKeyPairsFromFile(certPath + "/keys1/pubkey.pem");
        PUBKEY2 = utilsClass.getKeyPairsFromFile(certPath + "/keys2/pubkey.pem");
        PUBKEY3 = utilsClass.getKeyPairsFromFile(certPath + "/keys3/pubkey.pem");
        PUBKEY4 = utilsClass.getKeyPairsFromFile(certPath + "/keys4/pubkey.pem");
        PUBKEY5 = utilsClass.getKeyPairsFromFile(certPath + "/keys5/pubkey.pem");
        PUBKEY6 = utilsClass.getKeyPairsFromFile(certPath + "/keys6/pubkey.pem");
        PUBKEY7 = utilsClass.getKeyPairsFromFile(certPath + "/keys7/pubkey.pem");


        log.info("PRIKEY1 :" + PRIKEY1);
        log.info("PUBKEY1 :" + PUBKEY1);

        log.info("PRIKEY2 :" + PRIKEY4);
        log.info("PUBKEY2 :" + PUBKEY4);

        log.info("PRIKEY6 :" + PRIKEY6);
        log.info("PUBKEY6 :" + PUBKEY6);

    }

    /**
     * 创建多签地址 保存在数据库中
     * 当数据库被清，库中没多签地址信息时候调用。
     */
    public void createAddresses() throws Exception {

        ADDRESS1 = JSONObject.fromObject(soloSign.genAddress(PUBKEY1)).getJSONObject("data").getString("address");
        ADDRESS2 = JSONObject.fromObject(soloSign.genAddress(PUBKEY2)).getJSONObject("data").getString("address");
        ADDRESS3 = JSONObject.fromObject(soloSign.genAddress(PUBKEY3)).getJSONObject("data").getString("address");
        ADDRESS4 = JSONObject.fromObject(soloSign.genAddress(PUBKEY4)).getJSONObject("data").getString("address");
        ADDRESS5 = JSONObject.fromObject(soloSign.genAddress(PUBKEY5)).getJSONObject("data").getString("address");
        ADDRESS6 = JSONObject.fromObject(soloSign.genAddress(PUBKEY6)).getJSONObject("data").getString("address");
        ADDRESS7 = JSONObject.fromObject(soloSign.genAddress(PUBKEY7)).getJSONObject("data").getString("address");

        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        MULITADD1 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//123
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        MULITADD2 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//126
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        map.put("3", PUBKEY7);
        MULITADD3 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//167
        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        MULITADD4 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//12
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY3);
        MULITADD5 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//13
        map = new HashMap<>();
        map.put("1", PUBKEY3);
        map.put("2", PUBKEY4);
        MULITADD6 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//34
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        MULITADD7 = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//16
        map = new HashMap<>();
        map.put("1", PUBKEY4);
        map.put("2", PUBKEY5);
        IMPPUTIONADD = JSONObject.fromObject(multiSign.genMultiAddress(M, map)).getJSONObject("data").getString("address");//45
    }

    /**
     * 安装SmartToken账户合约
     */
    public void installSmartAccountContract(String abfileName) throws Exception {

        String constFileName = "account_simple.wlang";
        String contractFileName = "account_simple.wlang";
        //如果smartAccoutCtHash为空或者contractFileName不为constFileName 即"wvm\\account_simple.wlang" 时会重新安装
        if (smartAccoutContractAddress.equals("") || (!contractFileName.equals(constFileName))) {
            //安装
            String filePath = testDataPath + "wvm/" + abfileName;
            log.info("filepath " + filePath);
            String file = utilsClass.readInput(filePath).toString().trim();
            String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
            String response = contract.InstallWVM(data, "wvm", "");
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(response, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            smartAccoutContractAddress = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        }
    }

    /**
     * 创建多签地址 保存在数据库中
     * 当数据库被清，库中没多签地址信息时候调用。
     */
    public void createSTAddresses() throws Exception {

        int M = 1;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        log.info("ADDRESS1");
        ADDRESS1 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY2);
        log.info("ADDRESS2");
        ADDRESS2 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY3);
        log.info("ADDRESS3");
        ADDRESS3 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY4);
        log.info("ADDRESS4");
        ADDRESS4 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY5);
        log.info("ADDRESS5");
        ADDRESS5 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY6);
        log.info("ADDRESS6");
        ADDRESS6 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        map = new HashMap<>();
        map.put("1", PUBKEY7);
        log.info("ADDRESS7");
        ADDRESS7 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");

        M = 3;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        log.info("MULITADD2");
        MULITADD2 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//126

        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        log.info("MULITADD4");
        MULITADD4 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//12
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        log.info("MULITADD7");
        MULITADD7 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//16

//        map = new HashMap<>();
//        map.put("1", PUBKEY1);
//        map.put("2", PUBKEY2);
//        map.put("3", PUBKEY3);
//        MULITADD1 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//123
//        map = new HashMap<>();
//        map.put("1", PUBKEY1);
//        map.put("2", PUBKEY6);
//        map.put("3", PUBKEY7);
//        MULITADD3 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//167
//        map = new HashMap<>();
//        map.put("1", PUBKEY1);
//        map.put("2", PUBKEY3);
//        MULITADD5 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//13
//        map = new HashMap<>();
//        map.put("1", PUBKEY3);
//        map.put("2", PUBKEY4);
//        MULITADD6 = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//34
//        map = new HashMap<>();
//        map.put("1", PUBKEY4);
//        map.put("2", PUBKEY5);
//        IMPPUTIONADD = JSONObject.fromObject(st.SmartGenarateAddress(M, map)).getString("data");//45
    }

    /**
     * 创建token 模块使用的单签以及多签地址
     *
     * @throws Exception
     */
    public void createTokenAccount() throws Exception {
        ArrayList<String> listTag = new ArrayList<>();


        listTag.add("test");
        listTag.add("test02");
        listTag.add("test03");

        tokenAccount1 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId01, userId01, "", "", listTag)).getString("data");
        tokenAccount2 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId02, userId02, "", "", listTag)).getString("data");
        tokenAccount3 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId03, userId03, "", "", listTag)).getString("data");
        tokenAccount4 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId04, userId04, "", "", listTag)).getString("data");
        tokenAccount5 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId05, userId05, "", "", listTag)).getString("data");
        tokenAccount6 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId06, userId06, "", "", listTag)).getString("data");
        tokenAccount7 = JSONObject.fromObject(
                tokenModule.tokenCreateAccount(userId07, userId07, "", "", listTag)).getString("data");


        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", tokenAccount1);
        map.put("2", tokenAccount2);
        map.put("3", tokenAccount3);

        tokenMultiAddr1 = JSONObject.fromObject(
                tokenModule.tokenCreateMultiAddr(map, "multiaddr1", M, "", "", listTag)).getString("data");

        M = 1;
        map = new HashMap<>();
        map.put("1", tokenAccount1);
        map.put("2", tokenAccount2);
        tokenMultiAddr2 = JSONObject.fromObject(
                tokenModule.tokenCreateMultiAddr(map, "multiaddr2", M, "", "", listTag)).getString("data");

        map.clear();
        map.put("1", tokenAccount1);
        map.put("2", tokenAccount2);
        map.put("3", tokenAccount3);
        tokenMultiAddr3 = JSONObject.fromObject(
                tokenModule.tokenCreateMultiAddr(map, "multiaddr3", M, "", "", listTag)).getString("data");

        map.clear();
        map.put("1", tokenAccount2);
        map.put("2", tokenAccount3);
        tokenMultiAddr4 = JSONObject.fromObject(
                tokenModule.tokenCreateMultiAddr(map, "multiaddr4", M, "", "", listTag)).getString("data");

        map.clear();
        map.put("1", tokenAccount3);
        map.put("2", tokenAccount4);
        tokenMultiAddr5 = JSONObject.fromObject(
                tokenModule.tokenCreateMultiAddr(map, "multiaddr5", M, "", "", listTag)).getString("data");
    }


}
