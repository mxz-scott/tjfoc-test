package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tjfintech.common.CommonFunc.checkProgramActive;
import static com.tjfintech.common.CommonFunc.uploadFileToPeer;
import static com.tjfintech.common.utils.FileOperation.getTokenApiConfigValueByShell;
import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.MysqlOperation.createDBAndLoadTableFromFile;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenApiDatabaseMysql {

    //match just one
    public String getStrByReg(String src,String regPattern) {
        String matchStr = "";
        Pattern p = Pattern.compile(regPattern);
//        Pattern p = Pattern.compile("(?<=//|)\\/(\\w+)\\?(?<=//|)");
//        String src = "root:root@tcp(10.1.3.246:3306)/wallet22?charset=utf8";
        Matcher matcher = p.matcher(src);
        if (matcher.find()) {
            matchStr = matcher.group(1);
            log.info("match info: " + matchStr);
        }
        return matchStr;
    }

//    @Test
    public void tet() throws Exception{
        getStrByReg("root:root@tcp(10.1.3.246:3306)/wallet22?charset=utf8","\\/(.*?)\\?");
    }
   @Test
    public void test()throws Exception{
       //清空tokendb数据
       shellExeCmd(getIPFromStr(TOKENADD),killTokenApiCmd);
       String dbConfig = getTokenApiConfigValueByShell(getIPFromStr(TOKENADD),"DB","Connection");
       String database = getStrByReg(dbConfig,"\\/(.*?)\\?");
       String mysqlIP = getIPFromStr(dbConfig);
       uploadFiletoDestDirByssh(resourcePath + "mysql/"+tokenSqlTableFile,mysqlIP,USERNAME,PASSWD,destShellScriptDir,"");//文件上传到tokenDBTableSql即/root/tjshell目录下
       createDBAndLoadTableFromFile(mysqlIP,database,tokenDBTableSql);//该函数会先执行删除db再创建db 导入表单

        /*
      //设置节点 清空db数据 并重启
//       setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
      shellExeCmd(getIPFromStr(TOKENADD),startTokenApiCmd);

       //检查节点及sdk启动无异常
       checkProgramActive(PEER1IP,PeerTPName);
       checkProgramActive(PEER2IP,PeerTPName);
       checkProgramActive(PEER4IP,PeerTPName);
       checkProgramActive(PEER1IP,TokenTPName);

       //设置管理工具sm3
       shellExeCmd(PEER1IP,"sed -i 's/sha256/sm3/g' " + ToolPATH + "conf/base.toml");
       subLedger = "";
       */

    }
}
