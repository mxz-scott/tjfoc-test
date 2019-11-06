package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.bUpgradePeer;

@Slf4j
public class SetTestVersionRelease {

    @Test
    public void test()throws Exception {
        SetPeerVerRelease setPeerVerRelease = new SetPeerVerRelease();
        SetSDKVerRelease setSDKVerRelease = new SetSDKVerRelease();
        setPeerVerRelease.test();
        setSDKVerRelease.test();
    }
}
