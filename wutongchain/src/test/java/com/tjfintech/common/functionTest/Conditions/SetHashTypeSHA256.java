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
       //设置SDK 使用 sha256 清空数据库
       String sdkIP = utilsClass.getIPFromStr(SDKADD);
       shellExeCmd(sdkIP,killSDKCmd);

       //设置sdk hash算法为 sha256
       shellExeCmd(sdkIP,"sed -i 's/\\\"sm3\\\"/\\\"sha256\\\"/g' " + SDKConfigPath);
       //重启SDK
       utilsClass.setAndRestartSDK();

       //检查节点及sdk启动无异常
       commonFunc.checkProgramActive(PEER1IP,PeerTPName);
       commonFunc.checkProgramActive(PEER2IP,PeerTPName);
       commonFunc.checkProgramActive(PEER4IP,PeerTPName);
       commonFunc.checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具hashtype为 sha256
       shellExeCmd(PEER1IP,"sed -i 's/\\\"sm3\\\"/\\\"sha256\\\"/g' " + ToolPATH + "conf/base.toml");
    }

}
