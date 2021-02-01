package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.math.BigInteger;
import java.util.*;


@Slf4j
public class UtilsClassScf {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Scf scf = testBuilder.getScf();

    public static String platformKeyID = "";//平台ID
    public static String platformPubkey = "";//平台公钥
    public static String platformPubkeyPem = "";//平台公钥PEM格式
    public static String platformPIN = "123";

    public static String coreCompanyKeyID = "";//核心企业ID
    public static String coreCompanyPubkeyPem = "";//核心企业公钥PEM格式
    public static String coreCompanyAddress = "";//核心企业地址

    public static String supplyAddress1 = "";//供应商地址1
    public static String supplyAddress2 = "";//供应商地址2
    public static String supplyAddress3 = "";//供应商地址3
    public static String supplyID1 = "";//供应商ID1
    public static String supplyID2 = "";//供应商ID2
    public static String supplyID3 = "";//供应商ID3

    public static String companyID1 = "001";//资金方ID01
    public static String companyID2 = "002";//资金方ID02

    public static String PIN = "123456";

    public static String AccountAddress = ""; //账户合约
    public static String PlatformAddress = ""; //平台合约
    public static String QFJGAddress = ""; //清分机构合约，需要赋予everyone权限
    public static String ZJFAddress = ""; //资金方合约，需要赋予everyone权限
    public static String pubFormatSM2 = "sm2_pem";
    public static String comments = "";


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

        String Data = scf.SendMsg("finance", "testSender",platformKeyID, receiversList, "1", "", "测试消息");

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

    /**
     * 消息加解密list
     * @param id
     * @param pubkey
     * @param list
     * @return
     */
    public static List<Map> Sendmsg(String id, String pubkey, List<Map> list) {
        List<Map> tokenList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            tokenList.add(list.get(i));
        }
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("id", id);
        amountMap.put("pubkey", pubkey);

        tokenList.add(amountMap);
        return tokenList;
    }

}

