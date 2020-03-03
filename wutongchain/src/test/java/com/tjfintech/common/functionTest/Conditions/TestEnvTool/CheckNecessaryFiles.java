package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class CheckNecessaryFiles {

   @Test
    public void test(){
       //检查节点必要文件 目前baseOK.toml
       String filelist = shExeAndReturn(PEER1IP,"ls " + PeerPATH + "conf/");
       assertEquals(true,filelist.contains("baseOK.toml"));

       filelist = shExeAndReturn(PEER2IP,"ls " + PeerPATH + "conf/");
       assertEquals(true,filelist.contains("baseOK.toml"));

       filelist = shExeAndReturn(PEER3IP,"ls " + PeerPATH + "conf/");
       assertEquals(true,filelist.contains("baseOK.toml"));

       filelist = shExeAndReturn(PEER4IP,"ls " + PeerPATH + "conf/");
       assertEquals(true,filelist.contains("baseOK.toml"));

       //检查sdk必要配置文件 configMysql.toml
       filelist = shExeAndReturn(getIPFromStr(SDKADD),"ls " + SDKPATH + "conf/");
       assertEquals(true,filelist.contains("configMysql.toml"));

       //检查主机shell脚本文件
       filelist = shExeAndReturn(PEER1IP,"ls " + destShellScriptDir);
       assertEquals(true,filelist.contains("GetConfig.sh"));
       assertEquals(true,filelist.contains("SetConfig.sh"));

      filelist = shExeAndReturn(PEER2IP,"ls " + destShellScriptDir);
      assertEquals(true,filelist.contains("GetConfig.sh"));
      assertEquals(true,filelist.contains("SetConfig.sh"));

      filelist = shExeAndReturn(PEER4IP,"ls " + destShellScriptDir);
      assertEquals(true,filelist.contains("GetConfig.sh"));
      assertEquals(true,filelist.contains("SetConfig.sh"));

      filelist = shExeAndReturn(PEER3IP,"ls " + destShellScriptDir);
      assertEquals(true,filelist.contains("GetConfig.sh"));
      assertEquals(true,filelist.contains("SetConfig.sh"));
      assertEquals(true,filelist.contains("startWithParam.sh"));
    }


}
