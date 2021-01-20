package com.tjfintech.common.functionTest.mainAppChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_CommonFunc {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();


    /**
     * 创建两个应用链 与globalAppId1 globalAppId2 对应
     * @param chainName1  对应globalAppId1链名
     * @param chainName2  对应globalAppId2链名
     * @throws Exception
     */
    public void createTwoAppChain(String chainName1,String chainName2) throws Exception {
        log.info("获取所有应用链信息列表");
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        //判断是否存在名为 glbChain01的应用链 存在则不需要另外创建 否则赋值globalAppId1
        if(! resp.contains("\"id\": \"" + globalAppId1 + "\"")) {
            String respCreate = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chainName1,
                    " -t sm3", " -w first", " -c raft",ids);
            globalAppId1 = subLedger;
            log.info("id1 " + globalAppId1);
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"id\": \""+globalAppId1+"\""),
                    true);
        }
//        else {
//            //通过链名获取链id
//            getStrByReg(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + glbChain01),"\"id\":\\s+\"(\\w{10})\"");
//        }

        if(! resp.contains("\"id\": \"" + globalAppId2 + "\"")) {
            String respCreate = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chainName2,
                    " -t sm3", " -w first", " -c raft",ids);
            globalAppId2 = subLedger;
            log.info("id2 " + globalAppId2);
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(
                    mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"id\": \""+globalAppId2+"\""),
                    true);
        }
    }
}
