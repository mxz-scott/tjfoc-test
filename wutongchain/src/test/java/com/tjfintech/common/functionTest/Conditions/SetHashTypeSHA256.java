package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetHashTypeSHA256 {
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
   @Test
    public void setHashSHA256()throws Exception{
       //设置SDK 使用SHA256 清空数据库
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),resetSDKConfig,"sed -i 's/sm3/sha256/g' " + SDKConfigPath,killSDKCmd);
       utilsClass.delDataBase();//清空sdk当前使用数据库数据

       //设置节点使用sha256 清空db数据 并重启
       commonFunc.setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase,"sed -i 's/sm3/sha256/g' " + PeerBaseConfigPath);

       SetAllPeersDockerImagesClear setAllPeersDockerImagesClear = new SetAllPeersDockerImagesClear();
       setAllPeersDockerImagesClear.clearAllPeersDockerImages();

       //重启SDK
      shellExeCmd(utilsClass.getIPFromStr(SDKADD),startSDKCmd);

       //检查节点及sdk启动无异常
       commonFunc.checkProgramActive(PEER1IP,PeerTPName);
       commonFunc.checkProgramActive(PEER2IP,PeerTPName);
       commonFunc.checkProgramActive(PEER4IP,PeerTPName);
       commonFunc.checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具hashtype为sha256
       shellExeCmd(PEER1IP,"sed -i 's/sm3/sha256/g' " + ToolPATH + "conf/base.toml");
    }

}
