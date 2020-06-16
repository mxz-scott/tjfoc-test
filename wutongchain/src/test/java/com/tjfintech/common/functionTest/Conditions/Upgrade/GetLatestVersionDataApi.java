package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class GetLatestVersionDataApi {
    UpgradeDataGetFunc getChainData = new UpgradeDataGetFunc();

    @Test
    public void test()throws Exception {
        afterUpgrade.clear();
        txHashList = getChainData.getAllTxHashData();
        afterUpgrade = getChainData.SaveResponseToHashMap_Api(txHashList);
    }
}
