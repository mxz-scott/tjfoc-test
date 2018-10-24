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
    @Test
    public void  T284_BeforeCondition(){
        String tokenType = "cx-8oVNI";
        String amount="1000";
        //String amount = "1000";
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(MULITADD3, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY6,PWD6);
        String Tx2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第二次签名");
        String response3 = multiSign.Sign(Tx2, PRIKEY7,PWD7);
        String Tx3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
        log.info("第三次签名");
        String response4 = multiSign.Sign(Tx3, PRIKEY1);
        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertThat(response4, containsString("200"));
    }
}
