package com.tjfintech.common.functionTest.mixTest;

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

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class VerifyTests {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();

    /**
     校验区块时间戳与交易时间戳的差值，为了验证之前可能存在的交易几个小时后才打包问题。
     */
    @Test
    public void VerifyBlockAndTxTimeDiff() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));

        log.info(Integer.toString(blockHeight));
        ArrayList<String> errBlock = new ArrayList<>();

        for (int i = 1; i <= blockHeight; i++) {
            //打印区块的时间戳

            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getJSONObject("header").getString("timestamp");
            long blkTimeStamp = Long.parseLong(timestamp);

            //获取交易列表
            String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getString("txs");
            txsList = txsList.substring(2);
            txsList = StringUtils.substringBefore(txsList, "\"]");
//            log.info(txsList);

            String[] txs = txsList.split("\",\"");

            for (String tx : txs) {

                String txts = JSONObject.fromObject(store.GetTxDetail(tx)).getJSONObject("data").getJSONObject("header").getString("timestamp");

                long txTimestamp = Long.parseLong(txts);
                long diff = blkTimeStamp - txTimestamp;
                long checkInterval = 5;
                if(!subLedger.isEmpty()) checkInterval = 10;//子链检查间隔时间加长 因为子链交易上链平均6s左右 较慢
                //时间戳3.0版本修改为ms级别
                if (txts.length() ==13)  checkInterval = checkInterval*1000;
                if (diff > checkInterval) {
//                    count1++;
                    errBlock.add(subLedger + " Block height " + i + "，区块与交易时间差：" + diff / 1000
                            + "秒, 区块时间：" + timestamp +  "，交易时间：" + txts + " 交易hash " + tx);
//                    log.error("Block time and tx time in big difference, please check!");
//                    log.info("Block height: " + i + "，时间差：" + diff);
//                    log.info("区块时间：" + timestamp +  "，交易时间：" + txts);
                }

            }
        }
        for(int i=0;i<errBlock.size();i++){
            log.info(errBlock.get(i));
        }
        assertEquals("区块和交易时间差超过指定时间的区块与交易个数", 0, errBlock.size());

    }

    /**
     校验一笔交易打包在多个区块中的问题。
     测试前提条件，运行自动化脚本，过程中不断的启动一个节点，经过一段时间后，运行本用例校验。
     */
    @Test
    public void VerifyIfSameTxInDiffBlocks() throws Exception {

        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));

        log.info("chain height: " + Integer.toString(blockHeight));

        int count1 = 0;
        int count2 = 0;

        for (int i = 1; i <= blockHeight; i++) {

            //获取交易列表
            String[] txsPrevious = commonFunc.getTxsArray(i-1);
            String[] txsCurrent = commonFunc.getTxsArray(i);

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

    /**
     跨链交易验证
     */
    @Test
    public void VerifyBlocksAndTransactions() throws Exception {
        commonFunc.verifyBlockAndTransaction(destShellScriptDir);
    }




}
