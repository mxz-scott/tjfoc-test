package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.checkProgramActive;
import static com.tjfintech.common.utils.FileOperation.getTokenApiConfigValueByShell;
import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenApiDatabaseMysql {
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void test()throws Exception{

        String shCreate = "tokenapi_Create.sh";
        String sqlCreate= "tokenapi_mysql.sql";
        //清空tokendb数据
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD),killTokenApiCmd);
        String dbConfig = getTokenApiConfigValueByShell(utilsClass.getIPFromStr(TOKENADD),"DB","Connection");
        String database = getStrByReg(dbConfig,"\\/(.*?)\\?");
        String mysqlIP = utilsClass.getIPFromStr(dbConfig);
        uploadFiletoDestDirByssh(resourcePath + "mysql\\" + sqlCreate,mysqlIP,USERNAME,PASSWD,destShellScriptDir,"");//文件上传到tokenDBTableSql即/root/tjshell目录下
        uploadFiletoDestDirByssh(resourcePath + "mysql\\" + shCreate,mysqlIP,USERNAME,PASSWD,destShellScriptDir,"");//文件上传到tokenDBTableSql即/root/tjshell目录下
        shExeAndReturn(mysqlIP,"sh " + destShellScriptDir + shCreate + " " + database + " " + destShellScriptDir + sqlCreate);


       //重启token
       shellExeCmd(utilsClass.getIPFromStr(TOKENADD),startTokenApiCmd);

       //检查节点及sdk启动无异常
       checkProgramActive(PEER1IP,PeerTPName);
       checkProgramActive(PEER2IP,PeerTPName);
       checkProgramActive(PEER4IP,PeerTPName);
       checkProgramActive(utilsClass.getIPFromStr(rSDKADD),SDKTPName);
       checkProgramActive(utilsClass.getIPFromStr(TOKENADD),TokenTPName);

       subLedger = "";

    }
}
