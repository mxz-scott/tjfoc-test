package com.tjfintech.common.Interface;

public interface Kms {
    String createKey(String keySpec, String password);
    String createKey(String keySpec, String password, String pubFormat);
    String createKey_sm9(String keySpec, String password, String userId);
    String createKey_abe(String keySpec, String password, String attribute);
    String getKey(String keySpec, String pubFormat);
    String importKey(String importToken, String encryptedKeyMaterial);
    String getPublicKey(String keyId, String pubFormat);
    String genRandom(int size);
    String eccEncrypt(String keyId, String plainText);
    String eccDecrypt(String keyId, String password, String cipherText);
    String eccSign(String keyId, String password, String Digest);
    String eccVerify(String keyId, String Digest, String value);
    String encrypt(String keyId, String password, String plainText);
    String decrypt(String keyId, String password, String cipherText2);
    String changePwd(String keyId, String oldPwd, String newPwd);
    String buildinfo();
    String apihealth();
    String ibcEncrypt(String userId, String plainText);
    String abeEncrypt(String policy, String planText);


}
