package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class BeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();

    /**
     * 创建归集地址
     * 第一个参数为私钥。后续...参数为地址
     */
    @Test
    public  void collAddressTest(){
        String response= multiSign.collAddress(PRIKEY1,IMPPUTIONADD);
        String response2= multiSign.collAddress(PRIKEY1,MULITADD3);
        String response3= multiSign.collAddress(PRIKEY1,ADDRESS1);
        String response4=multiSign.collAddress(PRIKEY2,ADDRESS2);
        assertThat(response4,containsString("200"));
    }
    /**
     * 创建多签地址
     * 当数据库被清，库中没多签地址信息时候调用。
     */
    @Test
    public void TC12_createAdd() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY6);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY6);
        map.put("3", PUBKEY7);
        multiSign.genMultiAddress(M, map);
        M = 1;
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY3);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY3);
        map.put("2", PUBKEY4);
        multiSign.genMultiAddress(M, map);
        map = new HashMap<>();
        map.put("1", PUBKEY4);
        map.put("2", PUBKEY5);
        multiSign.genMultiAddress(1, map);
    }

    /**
     * 测试用例T284的前提条件。发行对应token
     */

    public  void  T284_BeforeCondition(){
        String tokenType = "cx-chenxu";
        String amount="1000";
        //String amount = "1000";
        log.info(IMPPUTIONADD+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY4);
    }
}
