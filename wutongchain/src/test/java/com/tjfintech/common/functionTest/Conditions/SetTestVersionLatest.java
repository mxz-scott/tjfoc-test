package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class SetTestVersionLatest {

    @Test
    public void test()throws Exception {
        assertEquals("至少需要升级节点或者sdk，当前flag全为false",true,bUpgradePeer || bUpgradeSDK);
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
    }
}
