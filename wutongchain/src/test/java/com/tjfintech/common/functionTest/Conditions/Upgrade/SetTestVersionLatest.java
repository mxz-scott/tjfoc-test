package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.Conditions.Upgrade.SetContractSysLatest;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetPeerVerLatest;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetSDKVerLatest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class SetTestVersionLatest {

    @Test
    public void test()throws Exception {
        assertEquals("至少需要升级一种版本，当前flag全为false",
                true,bUpgradePeer || bUpgradeSDK || bUpgradeTokenApi || bUpgradeContractSys);

        if(bUpgradeContractSys){
            SetContractSysLatest setContractSysLatest = new SetContractSysLatest();
            setContractSysLatest.test();
        }

        if(bUpgradePeer) {
            SetPeerVerLatest setPeerVerLatest = new SetPeerVerLatest();
            setPeerVerLatest.test();
        }
        if(bUpgradeSDK) {
            SetSDKVerLatest setSDKVerLatest = new SetSDKVerLatest();
            setSDKVerLatest.test();
        }

        if(bUpgradeTokenApi) {
            SetTokenApiVerLatest setTokenApiVerLatest = new SetTokenApiVerLatest();
            setTokenApiVerLatest.test();
        }
    }
}
