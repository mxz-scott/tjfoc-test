package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.tjfintech.common.utils.UtilsClass.MULITADD1;

@Slf4j
public class CalCodeLines {

    static BufferedReader BR = null;
    static long Count = 0;
   @Test
    public void test()throws IOException{
       //计算functionTest目录下代码行数
       Trvs(new File(System.getProperty("user.dir") + "\\src\\test\\java\\com\\tjfintech\\common\\functionTest"));
       System.out.print(Count);
    }


    public void Trvs(File f) throws IOException {
        File[] childs = f.listFiles();
        for (int i = 0; i < childs.length; i++) {
            if (childs[i].isFile()) {
                BR = new BufferedReader(new FileReader(childs[i]));
                while (BR.readLine() != null) Count += 1;
            }
            else Trvs(childs[i]);
        }
    }
}
