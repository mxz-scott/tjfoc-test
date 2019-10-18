package com.tjfintech.common.utils;


import org.junit.Test;

import java.io.*;

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

    public void appendToFile(String data,String filename)throws Exception{
        FileWriter fw = null;
        try {
            //如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(filename);
            fw = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(data);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
