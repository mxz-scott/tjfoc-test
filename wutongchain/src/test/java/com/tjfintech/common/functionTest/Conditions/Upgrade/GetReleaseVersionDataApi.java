package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.beforeUpgrade;

@Slf4j
public class GetReleaseVersionDataApi {
    UpgradeDataGetFunc getChainData = new UpgradeDataGetFunc();
    ArrayList<String> txHashList = new ArrayList<>();

    @Test
    public void test()throws Exception {
        beforeUpgrade.clear();
        txHashList = getChainData.getAllTxHashData();
        beforeUpgrade = getChainData.SaveResponseToHashMap_Api(txHashList);
    }
}
