package com.tjfintech.common;

import com.alibaba.fastjson.parser.JSONLexer;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClassKMS.KMSADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;
import static com.tjfintech.common.utils.UtilsClassKMS.keySpecSm2;
import static com.tjfintech.common.utils.UtilsClassKMS.plainText;

@Slf4j
public class GoKms implements Kms {

    /***
     * 生成对称算法密钥
     * @param keySpec
     * @param password
     * @return
     */
    public String createKey(String keySpec, String password) {
        Map<String, Object> map = new HashMap<>();
        map.put("keySpec", keySpec);
        map.put("password", password);
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(KMSADD + "/v1/createKey", map);
        return result;
    }

    /***
     * 生成对称算法密钥
     * @param keySpec
     * @param password
     * @return
     */
    public String createKey(String keySpec, String password, String pubFormat) {
        Map<String, Object> map = new HashMap<>();
        map.put("keySpec", keySpec);
        map.put("password", password);
        map.put("pubFormat", pubFormat);
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(KMSADD + "/v1/createKey", map);
        log.info(result);
        return result;
    }


    /***
     * 获取密钥导入参数
     * @param keySpec
     * @param pubFormat
     * @return
     */
    public String getKey(String keySpec, String pubFormat) {
        Map<String, Object> map = new HashMap<>();
        map.put("keySpec", keySpecSm2);
        map.put("pubFormat", pubFormat);

        String result = PostTest.postMethod(KMSADD + "/v1/getKeyImportParams", map);
        log.info(result);
        return result;
    }

    /***
     * 导入公钥
     * @param importToken
     * @param encryptedKeyMaterial
     * @return
     */
    public String importKey(String importToken, String encryptedKeyMaterial) {
        Map<String, Object> map = new HashMap<>();
        map.put("importToken", importToken);
        map.put("encryptedKeyMaterial", encryptedKeyMaterial);

        String result = PostTest.postMethod(KMSADD + "/v1/importKey", map);
        log.info(result);
        return result;
    }

    /***
     * 获取公钥
     * @param keyId
     * @param pubFormat
     * @return
     */
    public String getPublicKey(String keyId, String pubFormat) {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("pubFormat", pubFormat);

        String result = PostTest.postMethod(KMSADD + "/v1/getPublicKey", map);
        log.info(result);
        return result;
    }

    /***
     * 获取随机数
     * @param size
     * @return
     */
    public String genRandom(int size) {
        Map<String, Object> map = new HashMap<>();
        map.put("size", size);

        String result = PostTest.postMethod(KMSADD + "/v1/genRandom", map);
        log.info(result);
        return result;
    }

    /***
     * 非对称算法密钥加密
     * @param keyId
     * @param plainText
     * @return
     */
    public String eccEncrypt(String keyId, String plainText) {
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("plainText", plainText);

        String result = PostTest.postMethod(KMSADD + "/v1/eccEncrypt", map);
        log.info(result);
        return result;
    }

    /***
     * 非对称算法密钥解密
     * @param keyId
     * @param password
     * @param cipherText
     * @return
     */
    public String eccDecrypt(String keyId, String password, String cipherText) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("password", password);
        map.put("cipherText", cipherText);

        String result = PostTest.postMethod(KMSADD + "/v1/eccDecrypt", map);
        log.info(result);
        return result;
    }

    /***
     * 非对称算法密钥签名
     * @param keyId
     * @param password
     * @param Digest
     * @return
     */
    public String eccSign(String keyId, String password, String Digest) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("password", password);
        map.put("Digest", Digest);

        String result = PostTest.postMethod(KMSADD + "/v1/eccSign", map);
        log.info(result);
        return result;
    }

    /***
     * 非对称算法密钥验签
     * @param keyId
     * @param Digest
     * @param value
     * @return
     */
    public String eccVerify(String keyId, String Digest, String value) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("Digest", Digest);
        map.put("value", value);

        String result = PostTest.postMethod(KMSADD + "/v1/eccVerify", map);
        log.info(result);
        return result;
    }

    /***
     * 对称算法密钥加密
     * @param keyId
     * @param password
     * @param plainText
     * @return
     */
    public String encrypt(String keyId, String password, String plainText) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("password", password);
        map.put("plainText", plainText);

        String result = PostTest.postMethod(KMSADD + "/v1/encrypt", map);
        log.info(result);
        return result;
    }

    /***
     * 对称算法密钥解密
     * @param keyId
     * @param password
     * @param cipherText
     * @return
     */
    public String decrypt(String keyId, String password, String cipherText) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("password", password);
        map.put("cipherText", cipherText);

        String result = PostTest.postMethod(KMSADD + "/v1/decrypt", map);
        log.info(result);
        return result;
    }

    /***
     * 修改密钥PIN码
     * @param keyId3
     * @param oldPwd
     * @param newPwd
     * @return
     */
    public String changePwd(String keyId, String oldPwd, String newPwd) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyId", keyId);
        map.put("oldPwd", oldPwd);
        map.put("newPwd", newPwd);

        String result = PostTest.postMethod(KMSADD + "/v1/changePwd", map);
        log.info(result);
        return result;
    }

}

