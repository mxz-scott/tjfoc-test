package com.tjfintech.common.functionTest;

import com.tjfintech.common.MultiSign;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class MulitTestInvalid2 {
    MultiSign multiSign = new MultiSign();
    UtilsClass utilsClass = new UtilsClass();

    /**
     * TC6 M参数非法时创建多签地址
     */
    @Test
    public void TC6_createAddInvalid1() {
        //int M = 0;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(0, map);
        String response2 = multiSign.genMultiAddress(4, map);
        assertThat(response, containsString("400"));
        assertThat(response, containsString("M can't be 0"));
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("Parameter 'm' must be less than or equal the number of address"));


    }

    /**
     * Tc08公钥参数异常创建多签地址测试
     */
    @Test
    public void TC8_createAddInvalid2() {
        int M = 1;
        Map<String, Object> map = new HashMap<>();
        map.put("1", "123");
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        map.put("1", "@#$%");
        String response2 = multiSign.genMultiAddress(M, map);
        map.put("1", PUBKEY2);
        String response3 = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("400"));
        assertThat(response2, containsString("400"));
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("duplicate pubkey"));

    }

    /**
     * TC12变更多签公钥顺序，多签地址不变
     */
    @Test
    public void TC12_createAdd() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
        map.put("1", PUBKEY2);
        map.put("2", PUBKEY1);
        String response2 = multiSign.genMultiAddress(M, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));

    }


    /**
     * TC281创建多签地址
     */
    @Test
    public void TC281_testGenMultiAddress() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
        map.remove("3");
        String response2 = multiSign.genMultiAddress(1, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("Data").getString("Address"), equalTo(MULITADD4));
    }

    /**
     * Tc272核对公私钥接口
     */
    @Test
    public void TC272_testCheckPriKey() {
        String response = multiSign.CheckPriKey(PRIKEY6, PWD6);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("This password match for the private key"));

    }

    /**
     * TC282 发行Token异常测试
     * token类型长度
     * 入参金额的大小、精度
     * 未在CA中配置3/3地址/删除配置的3/3地址
     */
    @Test
    public void TC282_issueTokenInvalid() {
        String tokenType = "CX-" + UtilsClass.Random(4);
        String amount = "90";
        log.info(MULITADD2 + "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
        //String response = multiSign.issueToken(MULITADD2, tokenType+"123456789000000000000000000", amount, data);
        String response2 = multiSign.issueToken(MULITADD2, tokenType, "900000000000000000000000000000000000", data);
        String response4 = multiSign.issueToken(MULITADD1, tokenType, amount, data);
        // String response5 = multiSign.issueToken("0123", tokenType, amount, data);
        // assertThat(response, containsString("400"));
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("tokenaddress verify failed"));
        //     assertThat(response5, containsString("400"));

    }

    /**
     * TC283签名发行token交易异常测试
     * 入参缺a输入的密码
     * 入参传错的a的密码
     * 参数传非法私钥
     * 入参传非abc的私钥 d
     * 入参传错误的缺待签名的交易
     * 同一私钥重复签名的交易
     */
    @Test
    public void TC283_signIssueInvalid() {
        String tokenType = "CX-" + UtilsClass.Random(4);
        String amount = "1000";
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(MULITADD3, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        String response2 = multiSign.Sign(Tx1, PRIKEY6);//不带密码
        String response3 = multiSign.Sign(Tx1, PRIKEY6, PWD7);//密码错误
        String response4 = multiSign.Sign(Tx1, "112");  //非法密钥
        String response5 = multiSign.Sign(Tx1, PRIKEY3);   //无关密钥
        String response6 = multiSign.Sign("123", PRIKEY1);//Tx非法
        String response7 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
        String response8 = multiSign.Sign(Tx2, PRIKEY1);//重复密钥
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("Incorrect private key or password"));
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("Incorrect private key or password"));
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("Private Key must be base64 string"));
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("Multiaddr is not matching for the prikey"));
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("Invalid parameter -- Tx"));
        assertThat(response7, containsString("200"));
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("Private key signed already"));
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * TC21-23多签地址回收异常测试
     */
    @Test
    public void TC21_23recycleInvalid() {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo, containsString("200"));
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, "abc", "1");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "0");
        String recycleInfo3 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "9000000000000000");
        String recycleInfo4 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "-10");
        String recycleInfo5 = multiSign.Recycle(IMPPUTIONADD, "123", tokenType, "1");
        String recycleInfo6 = multiSign.Recycle(IMPPUTIONADD, PRIKEY3, tokenType, "1");
        String recycleInfo7 = multiSign.Recycle(IMPPUTIONADD, "0", tokenType, "1");
        String recycleInfo8 = multiSign.Recycle("0", PRIKEY4, tokenType, "1");
        String recycleInfo9 = multiSign.Recycle(MULITADD3, PRIKEY4, tokenType, "1");
        assertThat(recycleInfo, containsString("400"));
        assertThat(recycleInfo, containsString("insufficient balance"));
        assertThat(recycleInfo2, containsString("400"));
        assertThat(recycleInfo2, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(recycleInfo3, containsString("400"));
        assertThat(recycleInfo3, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(recycleInfo4, containsString("400"));
        assertThat(recycleInfo4, containsString("Token amount must be a valid number and less than 900000000"));
        assertThat(recycleInfo5, containsString("400"));
        assertThat(recycleInfo5, containsString("Private key must be base64 string"));
        assertThat(recycleInfo6, containsString("400"));
        assertThat(recycleInfo6, containsString("Multiaddr is not matching for the prikey"));
        assertThat(recycleInfo7, containsString("400"));
        assertThat(recycleInfo8, containsString("400"));
        assertThat(recycleInfo8, containsString("Invalid multiple address"));
        assertThat(recycleInfo9, containsString("400"));
        assertThat(recycleInfo9, containsString("Multiaddr is not matching for the prikey"));
    }

    /**
     * TC13 14 20 多签查询异常测试
     * 私钥 地址 tokenType
     */
    @Test
    public void TC13_20balanceInvalid() {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.Balance(IMPPUTIONADD, "0", tokenType);
        String queryInfo1 = multiSign.Balance(IMPPUTIONADD, PRIKEY3, tokenType);
        String queryInfo2 = multiSign.Balance(IMPPUTIONADD, "1234abc", tokenType);
        String queryInfo3 = multiSign.Balance("0", PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance("Soirv9ikykFYbBLMExy4zUTUa", PRIKEY4, tokenType);
        String queryInfo5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, "0");
        String queryInfo6 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, "abc123e");
        assertThat(queryInfo, containsString("400"));
        assertThat(queryInfo, containsString("Private key must be base64 string"));
        assertThat(queryInfo1, containsString("400"));
        assertThat(queryInfo1, containsString("Multiaddr is not matching for the prikey"));
        assertThat(queryInfo2, containsString("400"));
        assertThat(queryInfo2, containsString("Private key must be base64 string"));
        assertThat(queryInfo3, containsString("400"));
        assertThat(queryInfo4, containsString("400"));
        assertThat(queryInfo4, containsString("Invalid multiple address"));
        assertThat(queryInfo5, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(queryInfo6, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * 1/2多签地址向归集地址转账异常测试
     * 归集地址先向ADD1转入1个token
     * ADD1再向归集地址转入0.5个token
     *
     * @throws Exception
     */
    @Test
    public void TC284_transferToImppution() throws Exception {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total").equals("0"), false);

        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType, "1");

        String transferInfo0 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list0);//1 归集地址向单签地址转账
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo0, containsString("200"));
        String queryInfo1 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(transferInfo0, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total").equals("0"), false);

        List<Map> list1 = utilsClass.constructToken(IMPPUTIONADD, tokenType, "0.5");
        String transferInfo1 = multiSign.Transfer(PRIKEY1, "cx-test", MULITADD4, list1);//多签地址向归集地址转账
        assertThat(transferInfo1, containsString("200"));

    }

}
