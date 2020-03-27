package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetMgToolHashTypeSM3 {
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void setToolHashsm3()throws Exception{
       ArrayList<String> toolList = new ArrayList<>();
       toolList.add(PEER1IP);
       toolList.add(PEER2IP);
       toolList.add(PEER4IP);
       //设置管理工具hashtype为sm3
       utilsClass.sendCmdPeerList(toolList,"cp " + ToolPATH + "conf/baseOK.toml " + ToolPATH + "conf/base.toml");
    }

}
