package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.functionTest.Conditions.Upgrade.SetContractSysRelease;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetPeerVerRelease;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetSDKVerRelease;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetTestVersionRelease {

    @Test
    public void test()throws Exception {
        SetPeerVerRelease setPeerVerRelease = new SetPeerVerRelease();
        SetSDKVerRelease setSDKVerRelease = new SetSDKVerRelease();
        SetContractSysRelease setContractSysRelease = new SetContractSysRelease();
        SetTokenApiVerRelease setTokenApiVerRelease = new SetTokenApiVerRelease();
        setPeerVerRelease.test();
        setSDKVerRelease.test();
        setContractSysRelease.test();
        setTokenApiVerRelease.test();
    }
}
