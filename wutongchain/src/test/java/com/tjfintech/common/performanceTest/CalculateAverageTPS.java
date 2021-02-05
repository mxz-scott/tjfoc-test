package com.tjfintech.common.performanceTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.destShellScriptDir;
import static com.tjfintech.common.utils.UtilsClass.subLedger;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CalculateAverageTPS {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();

    /**
     * 计算链上TPS
     */
    @Test
    public void CalculateAverageTPS() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));

        int startBlockHeight;
//        startBlockHeight = 23047;
        startBlockHeight = blockHeight - 20;  // 计算最后20个区块的TPS

        int endBlockHeight = blockHeight;   //手动修改结束高度

        int diff = endBlockHeight - startBlockHeight + 1;
        int count = 0, total = 0;

        for (int i = startBlockHeight; i <= endBlockHeight; i++) {


            //获取区块中的交易个数
            String[] txs = commonFunc.getTxsArray(i);
            count = txs.length;

            total = total + count;

        }


        String timestamp = JSONObject.fromObject(store.GetBlockByHeight(startBlockHeight)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp1 = Long.parseLong(timestamp);
        timestamp = JSONObject.fromObject(store.GetBlockByHeight(endBlockHeight)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp2 = Long.parseLong(timestamp);

        long timeDiff = (blkTimeStamp2 - blkTimeStamp1) / 1000;

        log.info("区块数：" + diff);
        log.info("交易总数：" + total);
        log.info("测试时长：" + timeDiff + "秒");
        log.info("链上TPS：" + total / timeDiff);

    }


}