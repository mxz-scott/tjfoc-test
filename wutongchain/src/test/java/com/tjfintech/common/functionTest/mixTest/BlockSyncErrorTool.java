package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//import javax.mail.Address;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        String IP1 = "172.16.50.11:6310";
        String IP2 = "172.16.50.14:6310";
        String IP3 = "172.16.50.12:6310";
        getPeerBlockTx(IP1,332,true);
        getPeerBlockTx(IP1,331,true);
        getPeerBlockTx(IP2,332,true);
        getPeerBlockTx(IP2,331,true);
        getPeerBlockTx(IP3,331,true);
    }

    /**
     获取指定区块中的所有交易详情及raw data
     */
    @Test
    public void getBlockTime() throws Exception {
        SDKADD="http://211.144.193.246:60002";
        FileOperation fo = new FileOperation();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        for(int i=52466;i<=57730;i++){
            String blockDetail1 = store.GetBlockByHeight(i);
            String timeStamp = sdf.format(new Date(JSONObject.fromObject(blockDetail1).getJSONObject("data").getJSONObject("header").getLong("timestamp")));
            fo.appendToFile(i + " " + timeStamp,"gdSH_blocktime2.txt");
        }
    }


    public void getPeerBlockTx(String peerIPPort,int queryHeight,boolean bResultOnly) throws Exception {
        urlAddr = peerIPPort;
        FileOperation fo = new FileOperation();
        int blockHeight = queryHeight;
        String blockDetail1 = store.GetBlockByHeight(blockHeight);

        log.info(urlAddr + " check height: " + blockHeight);
        String[] txs = getTxsArray(blockHeight);

        log.info("区块交易个数 " + txs.length);
        for (int i = 0; i < txs.length; i++) {
//            String respTxDetail = store.GetTxDetail(txs[i]);
            String respTxRaw = store.GetTxRaw(txs[i]);

            String write2 = respTxRaw;
            if(bResultOnly){
                write2 = JSONObject.fromObject(respTxRaw).getJSONObject("data").getString("result");
            }

            fo.appendToFile(write2,
                    urlAddr.replace(".","").replace(":","") + blockHeight + "_txraw.txt");
        }


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
