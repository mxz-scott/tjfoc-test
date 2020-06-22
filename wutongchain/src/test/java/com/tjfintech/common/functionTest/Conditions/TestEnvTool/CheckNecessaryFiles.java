package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class CheckNecessaryFiles {
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void test(){
       //检查节点必要文件 目前baseOK.toml 正常的节点配置文件备份
       String filelist = shExeAndReturn(PEER1IP,"ls " + PeerPATH + "conf/");
       log.info(PeerPATH + "conf/  contains file " + "baseOK.toml :" + filelist.contains("baseOK.toml") );

       filelist = shExeAndReturn(PEER2IP,"ls " + PeerPATH + "conf/");
       log.info(PeerPATH + "conf/  contains file " + "baseOK.toml :" + filelist.contains("baseOK.toml") );

       filelist = shExeAndReturn(PEER3IP,"ls " + PeerPATH + "conf/");
       log.info(PeerPATH + "conf/  contains file " + "baseOK.toml :" + filelist.contains("baseOK.toml") );

       filelist = shExeAndReturn(PEER4IP,"ls " + PeerPATH + "conf/");
       log.info(PeerPATH + "conf/  contains file " + "baseOK.toml :" + filelist.contains("baseOK.toml") );

       //检查sdk必要配置文件 configMysql.toml 正常的sdk配置文件备份
       filelist = shExeAndReturn(utilsClass.getIPFromStr(SDKADD),"ls " + SDKPATH + "conf/");
       log.info(SDKPATH + "conf/  contains file " + "configMysql.toml :" + filelist.contains("configMysql.toml") );

       //检查主机shell脚本文件
       filelist = shExeAndReturn(PEER1IP,"ls " + destShellScriptDir);
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "GetConfig.sh :" + filelist.contains("GetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "SetConfig.sh :" + filelist.contains("SetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "startWithParam.sh :" + filelist.contains("startWithParam.sh") );

      filelist = shExeAndReturn(PEER2IP,"ls " + destShellScriptDir);
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "GetConfig.sh :" + filelist.contains("GetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "SetConfig.sh :" + filelist.contains("SetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "startWithParam.sh :" + filelist.contains("startWithParam.sh") );

      filelist = shExeAndReturn(PEER4IP,"ls " + destShellScriptDir);
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "GetConfig.sh :" + filelist.contains("GetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "SetConfig.sh :" + filelist.contains("SetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "startWithParam.sh :" + filelist.contains("startWithParam.sh") );

      filelist = shExeAndReturn(PEER3IP,"ls " + destShellScriptDir);
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "GetConfig.sh :" + filelist.contains("GetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "SetConfig.sh :" + filelist.contains("SetConfig.sh") );
       log.info(PEER1IP + " " + destShellScriptDir + " contains file " + "startWithParam.sh :" + filelist.contains("startWithParam.sh") );
    }


}
