package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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

        int count1 = 0;

        for (int i = 1; i <= blockHeight; i++) {
            //打印区块的时间戳

            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("timestamp");
            long blkTimeStamp = Long.parseLong(timestamp);

            //获取交易列表
            String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getString("txs");
            txsList = txsList.substring(2);
            txsList = StringUtils.substringBefore(txsList, "\"]");
//            log.info(txsList);

            String[] txs = txsList.split("\",\"");

            for (String tx : txs) {

                String txts = JSONObject.fromObject(store.GetTxDetail(tx)).getJSONObject("Data").getJSONObject("Header").getString("Timestamp");

                long txTimestamp = Long.parseLong(txts);
                long diff = blkTimeStamp - txTimestamp;
                long checkInterval = 20;
                //时间戳3.0版本修改为ms级别
                if (txts.length() ==13)  checkInterval = checkInterval*1000;
                if (diff > checkInterval) {
                    count1++;
                    log.error("Block time and tx time in big difference, please check!");
                    log.info("Block height: " + i + "，时间差：" + diff);
                    log.info("区块时间：" + timestamp +  "，交易时间：" + txts);

                }

            }
        }

        assertEquals("区块和交易时间差超过20秒", 0, count1);

    }

    /**
     校验一笔交易打包在多个区块中的问题。
     测试前提条件，运行自动化脚本，过程中不断的启动一个节点，经过一段时间后，运行本用例校验。
     */
    @Test
    public void VerifyIfSameTxInDiffBlocks() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));

        log.info("chain height: " + Integer.toString(blockHeight));

        int count1 = 0;
        int count2 = 0;

        for (int i = 2; i <= blockHeight; i++) {

            //获取交易列表
            String[] txsPrevious = getTxsArray(i-1);
            String[] txsCurrent = getTxsArray(i);

            for (String txp : txsPrevious) {
                for (String txc : txsCurrent) {

                    if (txp.equals(txc)) {
                        count1++;
                        log.error("Same Tx in different blocks. block height: " + i );
                        log.info("tx : " + txp);
                    }
                }
            }


            for (int j = 0; j < txsPrevious.length; j++){
                for (int k = j + 1; k < txsPrevious.length; k++){
                    if (txsPrevious[j].equals(txsPrevious[k])) {
                        count2++;
                        log.error("Same Tx in same blocks. block height: " + (i - 1) );
                        log.info("tx : " + txsPrevious[j]);
                    }

                }

            }

        }

        assertEquals("前后区块中有重复交易", 0, count1);
        assertEquals("同一区块中有重复交易", 0, count2);

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
