package com.tjfintech.common.utils;

import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;
import static org.junit.Assert.assertThat;

@Slf4j
public class UtilsClassScf {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Scf scf = testBuilder.getScf();

    public static String platformKeyID = "";//平台ID
    public static String platformPubkey = "";//平台公钥
    public static String platformPubkeyPem= "";//平台公钥PEM格式

    public static String coreCompanyKeyID = "";//核心企业ID
    public static String coreCompanyPubkey = "";//核心企业公钥
    public static String coreCompanyPubkeyPem = "";//核心企业公钥PEM格式
    public static String coreCompanyAddress = "";//核心企业地址

    public static String  supplyAddress1 = "";//供应商地址1
    public static String  supplyAddress2 = "";//供应商地址2
    public static String  supplyAddress3 = "";//供应商地址3

    public static String PIN = "123456";

    public static String AccountAddress = "f8cf823ab29cc3eada50d37951da61bfc5f39be700ef64b57a1d35cfcee3f22c";
    public static String PlatformAddress = "69e82dd275f1be41a2179d83f52e3520a938fee7eb61a2c32329c98dd8812312";
    public static String QFJGAddress = "";
    public static String ZJFAddress = "";
    public static String pubFormatSM2 = "sm2_pem";

    /**
    * base64解码
    */
    public String decodeBase64(String text) throws  Exception {

        Base64.Decoder decoder = Base64.getDecoder();

        final byte[] textByte = text.getBytes("UTF-8");

        String result = new String(decoder.decode(textByte));

        return result;
    }

    /**
     * 生成消息
     */
    public String generateMessage() throws  Exception {

        List<Map> receiversList=new ArrayList<>();
        Map<String,Object> receiversMap = new HashMap<>();
        receiversMap.put("id","testReceivers");
        receiversMap.put("pubkey","");
        receiversList.add(receiversMap);

        String Data = scf.SendMsg("finance", "testSender", receiversList, "1", "", "测试消息");

        return JSONObject.fromObject(Data).getString("data");
    }

}

