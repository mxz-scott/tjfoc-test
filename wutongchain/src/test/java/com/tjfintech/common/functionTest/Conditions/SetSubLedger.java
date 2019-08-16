package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSubLedger {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

   @Test
    public void createSubledger()throws Exception{
       TestMainSubChain subChain = new TestMainSubChain();
       String ledger = "sub"+sdf.format(dt)+ RandomUtils.nextInt(1000);
       subChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + ledger, " -t sm3", " -w first", " -c raft", ids);
       Thread.sleep(SLEEPTIME*2);
       subLedger = ledger;
       String response = store.CreateStore("test for ok tx");
       Thread.sleep(SLEEPTIME*2);
       String txHash1 = JSONObject.fromObject(response).getJSONObject("Data").get("Figure").toString();
       assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));

    }

}
