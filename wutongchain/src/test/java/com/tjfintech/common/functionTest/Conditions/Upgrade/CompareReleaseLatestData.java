package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.CommonFunc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class CompareReleaseLatestData {
    CommonFunc commonFunc = new CommonFunc();

    @Test
    public void test()throws Exception {
        log.info("start compare release data and latest data");
        assertEquals("判断升级前后内容是否一致",true,commonFunc.compareHashMap(beforeUpgrade,afterUpgrade,"升级测试"));
    }
}
