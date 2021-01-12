package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.WinExeOperation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.resourcePath;

@Slf4j
public class SetEventClientStop {

   @Test
    public void test()throws Exception{
       WinExeOperation exeOperation = new WinExeOperation();
       exeOperation.killProc("main.exe");
    }

}
