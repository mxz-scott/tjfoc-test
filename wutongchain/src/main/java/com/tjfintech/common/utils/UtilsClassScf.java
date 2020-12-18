package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.math.BigInteger;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;
import static org.junit.Assert.assertThat;

@Slf4j
public class UtilsClassScf {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Scf scf = testBuilder.getScf();

    public static String platformKeyID = "buushlte655bj3jflqog";//平台ID
    public static String platformPubkey = "";//平台公钥
    public static String platformPubkeyPem = "";//平台公钥PEM格式
    public static String platformPIN = "123";

    public static String coreCompanyKeyID = "buushlte655bj3jflqv0";//核心企业ID
    public static String coreCompanyPubkey = "";//核心企业公钥
    public static String coreCompanyPubkeyPem = "";//核心企业公钥PEM格式
//    public static String coreCompanyAddress = "SnsEEcAsq9364NW1y72XNKfXE3tJvTacyxX838Cwr8VRdiscmky";//核心企业地址
    public static String coreCompanyAddress = "4RXgvexgSJ3fvhKiFqKRYQzuJycjxijzr8oeijyzvhkgwAXDgm";//核心企业地址

    public static String supplyAddress1 = "SnMn7eXperY2Vp6MMexUW5sdVC1PKEQo7grXP2SBypee8irugZg";//供应商地址1
    public static String supplyAddress2 = "Sn5ANYdXD8ZK1ioghfoZ2LfFa82QTXvDWGiZiaxCxMFz6ZjxMPi";//供应商地址2
    public static String supplyAddress3 = "SoAx16qvTbobNnZyQEWhSXuDubKeDryBfneQ3neThrwrY6CYHfY";//供应商地址3

    public static String supplyID1 = "buushlte655bj3jflsd0";//供应商ID1
    public static String supplyID2 = "buushlte655bj3jflslg";//供应商ID2
    public static String supplyID3 = "buushlte655bj3jflsrg";//供应商ID3

    public static String companyID1 = "001";//资金方ID01
    public static String companyID2 = "002";//资金方ID02
    public static String companyID3 = "003";//资金方ID03

    public static String PIN = "123456";

    public static String AccountAddress = "99773fdc7a8442e91ccf0d75f1af588c1ae6f9990d86768df900f9e84e2f5298";
    public static String PlatformAddress = "827dd1501d88f543d45aa07c074144cee788562a8dee16244be3730696e62502";//平台合约
    public static String QFJGAddress = "7af8cb6b2faaac695bda1b56b8ba52eeeeafb812dc180d16c9ca93acaa8c7541";
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

    /**
     * 转账list
     * @param amount
     * @param subType
     * @param list
     * @return
     */
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

    /**
     * 兑付list
     * @param address
     * @param keyID
     * @param subtype
     * @param amount
     * @param list
     * @return
     */
    public static List<Map> paying(String address, String keyID, String subtype, String amount, List<Map> list) {
        List<Map> accounts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            accounts.add(list.get(i));
        }
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("address", address);
        amountMap.put("keyID", keyID);
        amountMap.put("subtype", subtype);
        amountMap.put("amount", amount);

        accounts.add(amountMap);
        return accounts;
    }
    /**
     * base64转16进制
     */
    public static String strToHex(String s) {
        byte[] decoded = Base64.getDecoder().decode(s);
        return String.format("%040x", new BigInteger(1, decoded));
    }
}

