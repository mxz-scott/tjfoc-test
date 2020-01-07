package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetTestVersionLatest;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class VerifyTests {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    /**
        校验区块时间戳与交易时间戳的差值，为了验证之前可能存在的交易几个小时后才打包问题。
     */
    @Test
    public void VerifyBlockAndTxTimeDiff() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));

        log.info(Integer.toString(blockHeight));

        for (int i = 1; i <= blockHeight; i++) {
            Thread.sleep(100);
            //打印区块的时间戳

            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("timestamp");
            int blkTimeStamp = Integer.parseInt(timestamp);
            log.info(timestamp);

            //获取交易列表
            String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getString("txs");
            txsList = txsList.substring(2);
            txsList = StringUtils.substringBefore(txsList, "\"]");
            log.info(txsList);

            String[] txs = txsList.split("\",\"");

            for (String tx : txs) {

                String txts = JSONObject.fromObject(store.GetTxDetail(tx)).getJSONObject("Data").getJSONObject("Header").getString("Timestamp");
                log.info(txts);
                int txTimestamp = Integer.parseInt(txts);

                int diff = blkTimeStamp - txTimestamp;
                log.info("时间差：" + Integer.toString(diff));
                if (diff > 5) {
                    log.error("Block time and tx time in big difference, please check!");
                    log.info("Block height: " + i);
                }

            }
        }
    }

    /**
        校验一笔交易打包在多个区块中的问题。
        测试前提条件，运行自动化脚本，过程中不断的启动一个节点，经过一段时间后，运行本用例校验。
     */
    @Test
    public void VerifyIfSameTxInDiffBlocks() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));

        log.info("chain height: " + Integer.toString(blockHeight));

        for (int i = 2; i <= blockHeight; i++) {

            //获取交易列表
            String[] txsPrevious = getTxsArray(i-1);
            String[] txsCurrent = getTxsArray(i);

            for (String txp : txsPrevious) {
                for (String txc : txsCurrent) {

                    if (txp.equals(txc)) {
                        log.error("Same Tx in different blocks. block height: " + i );
                        log.info("tx : " + txp);
                    }
                    else{
//                        log.info("tx previous: " + txp);
//                        log.info("tx current: " + txc);
                    }
                }

            }
        }

        assertEquals("请查看输出日志，是否存在异常数据！", true, false);
    }

    //根据区块高度获取区块中的交易列表
    public String[] getTxsArray(int i) {
        String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getString("txs");
        txsList = txsList.substring(2);
        txsList = StringUtils.substringBefore(txsList, "\"]");
//        log.info(txsList);

        String[] txs = txsList.split("\",\"");
        return txs;
    }


}
