package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class GetReleaseVersionDataSDK {
    UpgradeDataGetFunc getChainData = new UpgradeDataGetFunc();

    @Test
    public void test()throws Exception {
        log.info("start collect before info");
        beforeUpgrade.clear();
        txHashList = getChainData.getAllTxHashData();
        beforeUpgrade = getChainData.SaveResponseToHashMap_SDK(txHashList);
        //备份节点db信息
        shExeAndReturn(PEER1IP,"cp -r " + PeerPATH + "peerdb " +  PeerPATH + "peerdbbefore ");
        shExeAndReturn(PEER2IP,"cp -r " + PeerPATH + "peerdb " +  PeerPATH + "peerdbbefore ");
        shExeAndReturn(PEER4IP,"cp -r " + PeerPATH + "peerdb " +  PeerPATH + "peerdbbefore ");
    }
}
