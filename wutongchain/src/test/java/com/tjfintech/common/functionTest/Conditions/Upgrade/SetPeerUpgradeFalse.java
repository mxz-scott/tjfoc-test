package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetPeerUpgradeFalse {

   @Test
    public void test()throws Exception {
        bUpgradePeer = false;
   }
}
