package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import com.tjfintech.common.functionTest.upgrade.UpgradeTestHistoryData;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.bUpgradePeer;
import static com.tjfintech.common.utils.UtilsClass.beforeUpgrade;

@Slf4j
public class GetReleaseVersionDataSDK {
    UpgradeDataGetFunc getChainData = new UpgradeDataGetFunc();
    ArrayList<String> txHashList = new ArrayList<>();

    @Test
    public void test()throws Exception {
        beforeUpgrade.clear();
        txHashList = getChainData.getAllTxHashData();
        beforeUpgrade = getChainData.SaveResponseToHashMap_SDK(txHashList);
    }
}
