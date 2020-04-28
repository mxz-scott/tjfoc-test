package com.tjfintech.common.functionTest.PermissionTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PeerStartNoPermTest {
    TestPermission testPermission = new TestPermission();

    @Test
    public void NoPermTest()throws Exception{
//        assertEquals("111",testPermission.defaultSup());//系统默认不管控接口
        assertEquals("00",testPermission.storePermCheck());//存证类接口
        assertEquals("00000000",testPermission.sysPermCheck());//系统类接口
        assertEquals("000000",testPermission.collManageCheck());//管理类接口
        assertEquals("0000000000",testPermission.utxoPermCheck());//UTXO类接口
        assertEquals("000",testPermission.wvmPermCheck());//WVM相关接口
    }
}
