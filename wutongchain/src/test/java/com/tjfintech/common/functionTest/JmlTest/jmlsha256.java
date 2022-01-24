package com.tjfintech.common.functionTest.JmlTest;

import org.apache.commons.codec.digest.DigestUtils;
import java.io.File;
import java.io.FileInputStream;

public class jmlsha256 {
    public static void main(String[] args) throws Exception {
        File file = new File("D:\\微信图片_20220105165806.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        String hex = DigestUtils.sha256Hex(fileInputStream);
        System.out.println(hex);
    }
}
