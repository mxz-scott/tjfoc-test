package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class ReplaceCNENforGD {

    static BufferedReader BR = null;
    Map  mapCNEN = new HashMap();
   @Test
    public void test()throws Exception{
       readFileToMap();
//       Trvs(new File("D:\\GoWorks\\src\\github.com\\tjfoc\\tjfoc-test\\wutongchain\\src\\test\\java\\com\\tjfintech\\common\\functionTest\\guDengTestV2"));
       Trvs(new File("D:\\guDengTestV2"));
    }

    public void readFileToMap()throws Exception{
        BufferedReader brMap  = new BufferedReader(new FileReader("CNEN.txt"));
        int iCount = 0;
        String line = "";
        while((line = brMap.readLine())!=null){

            String[] arr = line.split(",");
            iCount ++;
            log.info(iCount + "  line  " + line);
            if(arr[0].contains("(")) {
                mapCNEN.put(arr[0].replaceAll("\\(","\\\\(").replaceAll("\\)","\\\\)"),arr[1]);
                log.info("en brackets " + arr[0] );
            }
//            log.info( line + "  " + iCount + " arr size " + arr.length + " 0 " + arr[0] + " 1 " + arr[1]);
            else mapCNEN.put(arr[0],arr[1]);
        }
        log.info("map数据" + mapCNEN.size());
    }

    public void Trvs(File f) throws Exception {
        File[] childs = f.listFiles();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i].isFile()) {
                log.info("Process file " + childs[i].getPath());
                String fulPathFile = childs[i].getPath();

                String fullPathDir = fulPathFile.substring(0,fulPathFile.lastIndexOf("\\"))+ "/temp/";
                String fileName = fulPathFile.substring(fulPathFile.lastIndexOf("\\") + 1);
                File destFile = new File(fullPathDir + fileName);

                FileOperation fo = new FileOperation();
                String cont = fo.read(fulPathFile);

                Iterator iter = mapCNEN.keySet().iterator();
                while (iter.hasNext()) {
                    Object key = iter.next();
//                    log.info("test " + key.toString() + " replace " + mapCNEN.get(key).toString());
//                    fo.replace(childs[i].getPath(), key.toString(), mapCNEN.get(key).toString());
                    cont = cont.replaceAll("\"" + key.toString() + "\"", "\"" + mapCNEN.get(key).toString() + "\"");
                }

                //更新目的文件
                fo.write(cont, destFile);
                Thread.sleep(2000);


            }
//            else Trvs(childs[i]);
        }
    }
}
