package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.FileOperation.getTokenApiConfigValueByShell;
import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenApiDatabaseMysql {
    CommonFunc commonFunc = new CommonFunc();
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
        uploadFiletoDestDirByssh(testDataPath + "mysql\\" + sqlCreate,mysqlIP,USERNAME,PASSWD,destShellScriptDir,"");//文件上传到tokenDBTableSql即/root/tjshell目录下
        uploadFiletoDestDirByssh(testDataPath + "mysql\\" + shCreate,mysqlIP,USERNAME,PASSWD,destShellScriptDir,"");//文件上传到tokenDBTableSql即/root/tjshell目录下
        shExeAndReturn(mysqlIP,"sed -i 's/\\\r//g' " + destShellScriptDir + "*.sh");
        shExeAndReturn(mysqlIP, "chmod +x " + destShellScriptDir + "*.sh");
        shExeAndReturn(mysqlIP,"sh " + destShellScriptDir + shCreate + " " + database + " " + destShellScriptDir + sqlCreate);


       //重启token
       shellExeCmd(utilsClass.getIPFromStr(TOKENADD),startTokenApiCmd);

       //检查节点及sdk启动无异常
       commonFunc.checkProgramActive(PEER1IP,PeerTPName);
       commonFunc.checkProgramActive(PEER2IP,PeerTPName);
       commonFunc.checkProgramActive(PEER4IP,PeerTPName);
       commonFunc.checkProgramActive(utilsClass.getIPFromStr(rSDKADD),SDKTPName);
       commonFunc.checkProgramActive(utilsClass.getIPFromStr(TOKENADD),TokenTPName);

       subLedger = "";

    }
}
