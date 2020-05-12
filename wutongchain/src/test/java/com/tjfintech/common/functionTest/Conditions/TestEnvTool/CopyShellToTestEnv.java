package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import com.tjfintech.common.CommonFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class CopyShellToTestEnv {

   @Test
   public void test(){
       //如果是已经获取过shell脚本则可以不用执行uploadFile()操作 因此此项默认关闭
       CommonFunc commonFunc = new CommonFunc();
       commonFunc.uploadFile();
       
    }


}
