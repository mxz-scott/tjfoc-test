package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetAllPeersDockerImagesClear {
   CommonFunc commonFunc = new CommonFunc();
   UtilsClass utilsClass = new UtilsClass();
   @Test
    public void clearAllPeersDockerImages()throws Exception{
      commonFunc.clearDockerImages(PEER1IP,PEER1RPCPort);
      commonFunc.clearDockerImages(PEER2IP,PEER2RPCPort);
      commonFunc.clearDockerImages(PEER4IP,PEER4RPCPort);
      commonFunc.clearDockerImages(PEER3IP,PEER3RPCPort);
    }

}
