package com.tjfintech.common.functionTest.PermissionTest;

import com.tjfintech.common.BeforeCondition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PeerStartNoPermTest {
    TestPermission testPermission = new TestPermission();

    @Test
    public void NoPermTest()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
        assertEquals("00",testPermission.storePermCheck());//存证类接口
    }
}
