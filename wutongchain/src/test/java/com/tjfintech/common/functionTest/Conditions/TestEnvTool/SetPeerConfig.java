package com.tjfintech.common.functionTest.Conditions.TestEnvTool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.setPeerConfig;
import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class SetPeerConfig {

   @Test
    public void test() throws Exception{
      String temp = sdf.format(dt).substring(4)+ RandomUtils.nextInt(1000);
       //首先备份原系统中的config.toml文件
       shellExeCmd(PEER1IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "config" + temp + ".toml");
       shellExeCmd(PEER2IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "config" + temp + ".toml");
       shellExeCmd(PEER4IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "config" + temp + ".toml");

       //设置节点config.toml 文件并同时备份configOK.toml
       //设置节点集群中config.toml为三共识节点信息

       setPeerConfig(PEER1IP);
       setPeerConfig(PEER2IP);
       setPeerConfig(PEER4IP);

       //备份config.toml configOK.toml
       shellExeCmd(PEER1IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "configOK.toml");
       shellExeCmd(PEER2IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "configOK.toml");
       shellExeCmd(PEER4IP,"cp " + PeerPATH + "config.toml " + PeerPATH + "configOK.toml");

    }


}
