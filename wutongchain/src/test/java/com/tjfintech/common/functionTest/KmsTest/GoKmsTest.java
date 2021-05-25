package com.tjfintech.common.functionTest.KmsTest;

import com.alibaba.fastjson.JSON;
import com.gmsm.utils.GmUtils;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.TjParseEncryptionKey;
import com.tjfintech.common.utils.UtilsClassKMS;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import sun.security.util.Password;

import java.util.Base64;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassKMS.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class GoKmsTest {
    public final static int SHORTSLEEPTIME = 3 * 1000;
    TestBuilder testBuilder = TestBuilder.getInstance();
    Kms kms = testBuilder.getKms();

    public int KMS_SLEEP_TIME = 1000;

    /***
     * 生成对称算法密钥
     * 对称密钥加密
     * 对称密钥解密
     */
    @Test
    public void Test001_createKey() {
        //生成对称密钥算法
        String response = kms.createKey(keySpecSm4, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
       //获取对称密钥加密参数
        String keyIdsm4 = UtilsClassKMS.getKeyIdSm4(response);

//        keyIdsm4 = "bv4b3b9pgflv43mqhsug";

                //对称密钥加密
        String response1 = kms.encrypt(keyIdsm4, password, plainText);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取对称密钥解密参数
        String cipherText = UtilsClassKMS.getCipherText(response1);
        String response2 = kms.decrypt(keyIdsm4, password, cipherText);
        //验证加解密参数
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        assertThat(response2, containsString(plainText));
    }

    /***
     * 获取导入密钥参数
     * 导入密钥
     * 生成非对称算法密钥ID
     * 获取非对称密钥加密ID公钥
     */
    @Test
    public void Test002_getKey() throws  Exception{
        //生成非对称密钥导入参数 sm2
        String response = kms.getKey(keySpecSm2, pubFormat);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);
        //导入公钥
        String response2 = kms.getPublicKey(keyIdsm2, pubFormat);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
    }


    @Test
    public void Test003_genRandom() {
        JSONObject data = new JSONObject();

        data.put(16, size);

        String Data = data.toString();
        log.info(Data);
        String response = kms.genRandom(size);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        assertThat(response, containsString("random"));
    }



    @Test
    public void Test004_eccEncrypt() throws Exception{
        //获取非对称密钥导入参数
        String response = kms.getKey(keySpecSm2, pubFormat);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        //获取导入密钥参数

        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);

        String response2 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        String response3 = kms.eccDecrypt(keyIdsm2, password, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

    @Test
    public void Test005_eccSign() throws Exception{
        //获取非对称密钥导入参数
        String response = kms.getKey(keySpecSm2, pubFormat);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        //获取导入密钥参数
        String importToken = UtilsClassKMS.getImportToken(response);
        String encryptPublicKey = UtilsClassKMS.getEncryptPublicKey(response);
        //将获取的密钥参数加密转码
        System.out.println(encryptPublicKey.substring(2));
        byte[] pub=Base64.getDecoder().decode(encryptPublicKey);
        TjParseEncryptionKey tjParseEncryptionKey=new TjParseEncryptionKey();
        byte[] pubkey=tjParseEncryptionKey.readPublicKey(pub);
        GmUtils gmUtils=new GmUtils();
        byte[] key="{\"PrivateKey\":\"LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ2gxR3A3eG1lNjdwTlRPbUEKLzlPVkxHcmRueG44cUUySHhHdlBTRFlhODl5Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSSEpFd2h5Y2xMTjNJbQp1aFpPR2tadWZpVFBuS0YrRGRNUnBKSW1ITkNtZURrL1N1bndPY2F5ZjhDdGs0ZGJxSDhOMCtjYXh6WE12TTVtCmFoSitIYkthCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K\",\"Password\":\"123\",\"PriFormat\":\"sm2_pem\"}".getBytes();
        byte[] cipher=gmUtils.sm2Encrypt(pubkey,key);
        byte[] ciphertext=Base64.getEncoder().encode(cipher);
        System.out.println("密文："+new String(ciphertext));
        //导入密钥
        String response1 = kms.importKey(importToken, new String(ciphertext));
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        //获取导入公钥参数
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response1);

        String response2 = kms.eccSign(keyIdsm2, password, Digest);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String value = UtilsClassKMS.getValue(response2);

        String response3 = kms.eccVerify(keyIdsm2, Digest, value);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString("true"));
        //验签异常测试
        String response4 = kms.eccVerify(keyIdsm2, Digesterror, value);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("false"));

    }


    @Test
    public void Test006_eccEncrypt() throws Exception{

        String response = kms.createKey(keySpecSm2, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response);

//        String keyIdsm2 = "bv7f759pgflpa6oine6g";

        String response2 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        String response3 = kms.eccDecrypt(keyIdsm2, password, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

    @Test
    public void Test007_eccSign() throws Exception{

        String response = kms.createKey(keySpecSm2, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String keyIdsm2 = UtilsClassKMS.getKeySm2(response);

//        String keyIdsm2 = "bv7f759pgflpa6oineeg";

        String response2 = kms.eccSign(keyIdsm2, password, Digest);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String value = UtilsClassKMS.getValue(response2);

        String response3 = kms.eccVerify(keyIdsm2, Digest, value);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString("true"));
        //验签异常测试
        String response4 = kms.eccVerify(keyIdsm2, Digesterror, value);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("false"));

    }

    @Test
    public void Test008_changePwd() throws Exception {
        //创建sm4账号
        String response = kms.createKey(keySpecSm4, password);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String keyIdsm4 = UtilsClassKMS.getKeyIdSm4(response);

        //修改密码
        String response1 = kms.changePwd(keyIdsm4, password, newPwd);
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //新密码加密
        String response2 = kms.encrypt(keyIdsm4, newPwd,plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        //新密码解密
        String cipherText = UtilsClassKMS.getCipherText(response2);
        String response3 = kms.decrypt(keyIdsm4, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

        //旧密码加密
        String response4 = kms.encrypt(keyIdsm4, password,plainText);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("pin码与密钥不匹配"));

        //旧密码解密
        String response5 = kms.decrypt(keyIdsm4, password,cipherText);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("pin码与密钥不匹配"));
    }

    @Test
    public void Test009_changePwd() {
        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));

        String keyIdsm2 = UtilsClassKMS.getKeySm2(response);

        String response1 = kms.changePwd(keyIdsm2, password, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));
        //新密码加密，验证修改后的密码可用
        String response2 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        //新密码解密
        String cipherText = UtilsClassKMS.getCipherText(response2);
        String response3 = kms.eccDecrypt(keyIdsm2, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));
        //旧密码加密,验证失败
        String response4 = kms.eccEncrypt(keyIdsm2, plainText);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("data"));

        String response5 = kms.eccDecrypt(keyIdsm2, password,cipherText);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("pin码与密钥不匹配"));

    }

    @Test
    public void Test010_eccChangePWDandEncrypt() throws Exception {

        String response = kms.createKey(keySpecSm2, password);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        String keyId = UtilsClassKMS.getKeySm2(response);

        //修改密码
        String response1 = kms.changePwd(keyId, password, oldPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));
        //再改一次密码
        response1 = kms.changePwd(keyId, oldPwd, newPwd);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("data"));
        assertThat(response1, containsString("ok"));

        //加密
        String response2 = kms.eccEncrypt(keyId, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        //解密
        String response3 = kms.eccDecrypt(keyId, newPwd, cipherText);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }

    @Test
    public void Test011_buildinfo() {
        String response = kms.buildinfo();

        assertEquals(JSONObject.fromObject(response).getString("status").equals("200"),true);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getString("Build time").equals(""),false);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getString("Git commit").equals(""),false);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getString("Go version").equals(""),false);
        assertEquals(JSONObject.fromObject(response).getJSONObject("data").getString("Version").equals(""),false);

    }

    @Test
    public void Test012_apihealth() {
        String response = kms.apihealth();

        assertThat(response, containsString("200"));
        assertThat(response, containsString("OK"));
    }
//sm9创建密钥-加密-解密 -成功
    @Test
    public void Test013_ibcEncrypt() throws Exception {

        String userId = "18896808089";
        String response = kms.createKey_sm9(keySpecSm9, password, userId);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        Map key = JSON.parseObject(response, Map.class);
        Map key1 = JSON.parseObject(key.get("data").toString(), Map.class);
        String keyId = key1.get("keyId").toString();
        //System.out.println(keyId);
        //加密
        String response2 = kms.ibcEncrypt(userId, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        //解密
        String response3 = kms.decrypt(keyId,password, cipherText );
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }
    //sm9创建密钥-加密-解密 -失败
    @Test
    public void Test014_ibcEncrypt() throws Exception {

        String userId = "test2";
        String response = kms.createKey_sm9(keySpecSm9, password, userId);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        Map key = JSON.parseObject(response, Map.class);
        Map key1 = JSON.parseObject(key.get("data").toString(), Map.class);
        String keyId = key1.get("keyId").toString();
        //System.out.println(keyId);
        //加密
        String response2 = kms.ibcEncrypt("test3", plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        //解密
        String response3 = kms.decrypt(keyId,password, cipherText );
        assertEquals("400", JSONObject.fromObject(response3).getString("status"));
        assertEquals(true, response3.contains("decrypt error:Invalid pkcs7 padding (unpadding \\u003e BlockSize || unpadding == 0"));


        //加密（验证pin码不匹配）
        response2 = kms.ibcEncrypt("test2", plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));


        //解密(解密失败)
        response3 = kms.decrypt(keyId,"000", cipherText );
        assertEquals("400", JSONObject.fromObject(response3).getString("status"));
        assertEquals(true, response3.contains("pin码与密钥不匹配,或请稍后再试"));


    }

    //abe创建密钥-加密-解密(3个属性3分之二覆盖策略 2/3且满足2of2)
    @Test
    public void Test015_abeEncrypt() throws Exception {

        String attribute = "u1:foo v2:bar w3:baf";
        String response = kms.createKey_abe(keySpecabe, password, attribute);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        Map key = JSON.parseObject(response, Map.class);
        Map key1 = JSON.parseObject(key.get("data").toString(), Map.class);
        String keyId = key1.get("keyId").toString();
        //System.out.println(keyId);
        //加密
        String policy = "u1:foo v2:fim v2:bar 2of3 w3:baf 2of2";
        String response2 = kms.abeEncrypt(policy, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));

        String cipherText = UtilsClassKMS.getCipherText(response2);

        //解密
        String response3 = kms.decrypt(keyId,password, cipherText );
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("data"));
        assertThat(response3, containsString(plainText));

    }
    //abe创建密钥-加密-解密(5个属性,5分之3覆盖策略，1/5解密失败。2/5解密失败.3/5且不满足2of2 解密失败。)
    @Test
    public void Test016_abeEncrypt() throws Exception {

        String attribute = "u1:foo v2:bar u2:456 u3:ooeec u4:cwdf w3:bade";
        String response = kms.createKey_abe(keySpecabe, password, attribute);

        assertThat(response, containsString("200"));
        assertThat(response, containsString("data"));
        Map key = JSON.parseObject(response, Map.class);
        Map key1 = JSON.parseObject(key.get("data").toString(), Map.class);
        String keyId = key1.get("keyId").toString();
        //System.out.println(keyId);
        //加密

        //加密 1/5
        String policy1 = "u1:foo v2:ba u2:456 v2:fim u4:ctesc 3of5 w3:bade 2of2";
        String response2 = kms.abeEncrypt(policy1, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        String cipherText = UtilsClassKMS.getCipherText(response2);
        //解密
        String response3 = kms.decrypt(keyId,password, cipherText );
        assertEquals("400", JSONObject.fromObject(response3).getString("status"));
        assertEquals(true, response3.contains("cannot decrypt, attributes in key do not satisfy policy"));

        //加密 2/5
        String policy2 = "u1:foo v2:ba u2:456 v2:fim u4:ctesc 3of5 w3:bade 2of2";
        response2 = kms.abeEncrypt(policy2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        cipherText = UtilsClassKMS.getCipherText(response2);
        //解密
        response3 = kms.decrypt(keyId,password, cipherText );
        assertEquals("400", JSONObject.fromObject(response3).getString("status"));
        assertEquals(true, response3.contains("cannot decrypt, attributes in key do not satisfy policy"));

        //加密3/5但不满足2of2
        String policy3 = "u1:foo v2:bar u2:456 v2:fim u4:ctesc 3of5 w3:bad 2of2";
        response2 = kms.abeEncrypt(policy2, plainText);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("data"));
        cipherText = UtilsClassKMS.getCipherText(response2);
        //解密
        response3 = kms.decrypt(keyId,password, cipherText );
        assertEquals("400", JSONObject.fromObject(response3).getString("status"));
        assertEquals(true, response3.contains("cannot decrypt, attributes in key do not satisfy policy"));

    }

}

