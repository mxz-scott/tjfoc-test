package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class BlockSyncErrorTool {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();



    /**
     获取指定区块中的所有交易详情及raw data
     */
    @Test
    public void checkPeerBlockTx() throws Exception {

        //获取第一个节点的区块及交易详情信息
        CommonFunc cf = new CommonFunc();
        UtilsClass uc = new UtilsClass();
        cf.setSDKOnePeer(uc.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true","tjfoc.com");
        uc.setAndRestartSDK();//重启SDK

        int blockHeight = 1625;
        String blockDetail1 = store.GetBlockByHeight(blockHeight);
        log.info(blockDetail1);
        Map<String,String> mapHashDetail = new HashMap<>();
        Map<String,String> mapHashRaw = new HashMap<>();

        log.info(PEER2IP + " check height: " + blockHeight);
        String[] txs = getTxsArray(blockHeight);

        log.info("区块交易个数 " + txs.length);
        for (int i = 0; i < txs.length; i++) {
            mapHashDetail.put(txs[i],store.GetTxDetail(txs[i]));
            mapHashRaw.put(txs[i],store.GetTxRaw(txs[i]));
        }

        //设置第二个节点
        cf.setSDKOnePeer(uc.getIPFromStr(SDKADD),PEER2IP + ":" + PEER2RPCPort,"true","tjfoc.com");
        uc.setAndRestartSDK();//重启SDK

        String blockDetail2 = store.GetBlockByHeight(blockHeight);
        log.info(blockDetail2);
        Map<String,String> mapHashDetail2 = new HashMap<>();
        Map<String,String> mapHashRaw2 = new HashMap<>();

        log.info(PEER2IP + " check height: " + blockHeight);
        String[] txs2 = getTxsArray(blockHeight);

        log.info("区块交易个数 " + txs2.length);

        for (int i = 0; i < txs2.length; i++) {
            mapHashDetail2.put(txs2[i],store.GetTxDetail(txs2[i]));
            mapHashRaw2.put(txs2[i],store.GetTxRaw(txs2[i]));
        }


        log.info("**********************************************************");
        //比较区块详情
        if(blockDetail1 == blockDetail2)  log.info("区块详情一致");
        else {
            log.info("区块详情不一致");
            log.info(blockDetail1);
            log.info(blockDetail2);
        }

        //比较区块内的交易个数是否一致
        if(txs.length == txs2.length)  log.info("区块交易个数一致");
        else log.info("区块交易个数不一致 " + PEER1IP + " 交易个数 " + txs.length +  " " + PEER2IP + " 交易个数 " + txs2.length);

        //比较交易详情
        Boolean detailSame = cf.compareHashMap(mapHashDetail,mapHashDetail2);
        //比较交易raw data
        Boolean rawSame =cf.compareHashMap(mapHashRaw,mapHashRaw2);

        log.info("交易详情是否一致 " + detailSame);
        log.info("交易raw是否一致 " + rawSame);
    }

    //根据区块高度获取区块中的交易列表
    public String[] getTxsArray(int i) {
        String txsList = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getString("txs");
        txsList = txsList.substring(2);
        txsList = StringUtils.substringBefore(txsList, "\"]");
//        log.info(txsList);

        String[] txs = txsList.split("\",\"");
        return txs;
    }


}
