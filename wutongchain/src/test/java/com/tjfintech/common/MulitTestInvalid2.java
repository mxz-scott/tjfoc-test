package com.tjfintech.common;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
public class MulitTestInvalid2 {
    MultiSign multiSign = new MultiSign();

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
        map.put("1",PUBKEY2);
        String response3 = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("400"));
        assertThat(response2, containsString("400"));
        assertThat(response3, containsString("400"));
        assertThat(response3,containsString("duplicate pubkey"));

    }

    /**
     * TC12变更多签公钥顺序，多签地址不变
     */
    @Test
    public void TC12_createAdd(){
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
        map.put("1",PUBKEY2);
        map.put("2",PUBKEY1);
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
    public void TC272_testCheckPriKey() throws Exception {
        String response = multiSign.CheckPriKey(PRIKEY6, PWD6);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("This password match for the private key"));

    }
}
