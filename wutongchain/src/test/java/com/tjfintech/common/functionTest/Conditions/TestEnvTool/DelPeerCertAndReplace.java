package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class DelPeerCertAndReplace {

//   @Test
    public void test() throws Exception{
       //删除cert目录下文件
       shellExeCmd(PEER1IP,"rm -rf " + PeerPATH + "cert");
       shellExeCmd(PEER2IP,"rm -rf " + PeerPATH + "cert");
       shellExeCmd(PEER4IP,"rm -rf " + PeerPATH + "cert");

       shellExeCmd(PEER1IP,"rm -rf " + PeerPATH + "tls");
       shellExeCmd(PEER2IP,"rm -rf " + PeerPATH + "tls");
       shellExeCmd(PEER4IP,"rm -rf " + PeerPATH + "tls");

       //替换tls证书
       shellExeCmd(PEER1IP,"cp -r " + PeerPATH + "cert " + PeerPATH + "tls" );
       shellExeCmd(PEER2IP,"cp -r " + PeerPATH + "cert " + PeerPATH + "tls" );
       shellExeCmd(PEER4IP,"cp -r " + PeerPATH + "cert " + PeerPATH + "tls" );
       
    }


}
