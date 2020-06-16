package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.functionTest.upgrade.UpgradeDataGetFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class CompareReleaseLatestData {
    CommonFunc commonFunc = new CommonFunc();

    @Test
    public void test()throws Exception {
        log.info("start compare release data and latest data");
        commonFunc.compareHashMap(beforeUpgrade,afterUpgrade);
    }
}
