package com.tjfintech.common.functionTest.JmlTest;

import com.tjfintech.common.utils.UtilsClassScf;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jcajce.provider.symmetric.ChaCha;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

public class jmlsha256 {
    public static void main(String[] args) throws Exception {
        File file = new File("D:\\微信图片_20220105165806.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        String hex = DigestUtils.sha256Hex(fileInputStream);
        System.out.println(hex);

//        UtilsClassScf scf = new UtilsClassScf();
//        String str_encode = "ogj/G2aRHGcXQj8/BQ9k5Ph/o6Ha+uF9z/KvLugSrrg=";
//        System.out.println(scf.str2HexStr(str_encode));
        }
}
