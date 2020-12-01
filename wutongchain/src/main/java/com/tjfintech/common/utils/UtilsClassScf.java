package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
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

    public static String platformKeyID = "bugeu8te655f4d6m4qkg";//平台ID
    public static String platformPubkey = "-----BEGIN PUBLIC KEY-----\n" +
            "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEGHCtDx1FOkopYU75YMpHxaYi7S4l\n" +
            "+jZfFhICvr7HNTzm2QfHoq6bbNOrKMcs4eUJ5pV4E6MQInqDg8L3FUvUnQ==\n" +
            "-----END PUBLIC KEY-----";//平台公钥
    public static String platformPubkeyPem = "";//平台公钥PEM格式
    public static String platformPIN = "123";

    public static String coreCompanyKeyID = "bugeu8te655f4d6m4qjg";//核心企业ID
    public static String coreCompanyPubkey = "";//核心企业公钥
    public static String coreCompanyPubkeyPem = "-----BEGIN PUBLIC KEY-----\\nMFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEc6kNe5as+na1DnvT/0Yo3+NhgtdT\\n7G7ciDoSuViStheLajrn1jyIhancka9TDZS8egH43aA9UnKU/DwdzPfWfQ==\\n-----END PUBLIC KEY-----\\n";//核心企业公钥PEM格式
    public static String coreCompanyAddress = "SngPcZJ21VL1b2aNnYK1Fn64Xsb9hVvXM4tLKVHnYwiwJopmxqB";//核心企业地址

    public static String supplyAddress1 = "SnbzpS7hndcXv1aHTDJsRVQezPTj2daL64WrELEf63ZyK1CSVL1";//供应商地址1
    public static String supplyAddress2 = "SoD5sJYbuigjUTc2RdkpVyhodsrA8rhEzkBQGhesdwCHDx4nLrz";//供应商地址2
    public static String supplyAddress3 = "SnqTiKrYMgqTu5oXNmckN3MrFaVVpoLfCNj4EyxNdKBAnYhwcco";//供应商地址3

    public static String supplyID1 = "bugeu8te655f4d6m4ql0";//供应商ID1
    public static String supplyID2 = "bugeu8te655f4d6m4qlg";//供应商ID2
    public static String supplyID3 = "bugeu8te655f4d6m4qm0";//供应商ID3

    public static String companyID1 = "001";//资金方ID01
    public static String companyID2 = "002";//资金方ID02
    public static String companyID3 = "003";//资金方ID03

    public static String PIN = "123456";

    public static String AccountAddress = "f85afef224e3b5c3e7eefbc3f21ecf904d84c4dbfb0e9857756415e7a28501fe";
    public static String PlatformAddress = "827dd1501d88f543d45aa07c074144cee788562a8dee16244be3730696e62502";//平台合约
    public static String QFJGAddress = "328d117c874ad576fac9290f8f6eec3e302f04856acbce0fa36a4763bcf84c50";
    public static String ZJFAddress = "79e3381736610d2914a2ee5ca88c279b4d31aa6af3f5d5ac392658677710bba7";
    public static String pubFormatSM2 = "sm2_pem";
    public static String comments = "BwAAAGZpbmFuY2UKAAAAdGVzdFNlbmRlcgENAAAAdGVzdFJlY2VpdmVycwAAAAAAAAAAAQAAADEAAAAADAAAAOa1i+ivlea2iOaBrw==";


    /**
     * base64解码
     */
    public String decodeBase64(String text) throws Exception {

        Base64.Decoder decoder = Base64.getDecoder();

        final byte[] textByte = text.getBytes("UTF-8");

        String result = new String(decoder.decode(textByte));

        return result;
    }

    /**
     * 生成消息
     */
    public String generateMessage() throws Exception {

        List<Map> receiversList = new ArrayList<>();
        Map<String, Object> receiversMap = new HashMap<>();
        receiversMap.put("id", "testReceivers");
        receiversMap.put("pubkey", "");
        receiversList.add(receiversMap);

        String Data = scf.SendMsg("finance", "testSender", receiversList, "1", "", "测试消息");

        return JSONObject.fromObject(Data).getString("data");
    }

    /**
     * 利用KMS随机数获取tokenType
     */

    public static String gettokenType(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String tokenType = mapType1.get("random").toString();
        System.out.println("a = " + tokenType);
        return tokenType;
    }

    public static List<Map> Assignment(String amount, String subType, List<Map> list) {
        List<Map> tokenList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            tokenList.add(list.get(i));
        }
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("amount", amount);
        amountMap.put("subType", subType);

        tokenList.add(amountMap);
        return tokenList;
    }
}

