package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetMgToolHashTypeSM3 {

   @Test
    public void setToolHashsm3()throws Exception{
       ArrayList<String> toolList = new ArrayList<>();
       toolList.add(PEER1IP);
       toolList.add(PEER2IP);
       toolList.add(PEER4IP);
       //设置管理工具hashtype为sm3
       sendCmdPeerList(toolList,"sed -i 's/sm3/sha256/g " + ToolPATH + "conf/base.toml");
//       sendCmdPeerList(toolList,"cp " + ToolPATH + "conf/baseOK.toml " + ToolPATH + "conf/base.toml");
    }

}
