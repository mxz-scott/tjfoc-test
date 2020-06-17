package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import static com.tjfintech.common.utils.UtilsClass.beforeUpgrade;
import static com.tjfintech.common.utils.UtilsClass.txHashList;

@Slf4j
public class GetReleaseVersionDataApi {
    UpgradeDataGetFunc getChainData = new UpgradeDataGetFunc();

    @Test
    public void test()throws Exception {
        beforeUpgrade.clear();
        txHashList = getChainData.getAllTxHashData();
        beforeUpgrade = getChainData.SaveResponseToHashMap_Api(txHashList);
    }
}
