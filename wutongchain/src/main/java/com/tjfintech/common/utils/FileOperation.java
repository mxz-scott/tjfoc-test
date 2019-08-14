package com.tjfintech.common.utils;


import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileOperation {

    //@Test
    public void replace(String fulPathFile,String srcStr,String destStr) throws Exception{
        String fileName = fulPathFile.substring(fulPathFile.lastIndexOf("/")+1,fulPathFile.lastIndexOf("."));
        File destFile = new File(fulPathFile.replace(fileName,fileName+"_temp"));
        String cont = read(fulPathFile);
        //System.out.println(cont);
        //对得到的内容进行处理
        cont = cont.replaceAll(srcStr, destStr);
        //System.out.println(cont);
        //更新源文件
        write(cont, destFile);
        Thread.sleep(2000);

    }

    public static String read(String path) {
        File file = new File(path);
        StringBuffer res = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                res.append(line + "\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public static boolean write(String cont, File dist) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dist));
            writer.write(cont);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}