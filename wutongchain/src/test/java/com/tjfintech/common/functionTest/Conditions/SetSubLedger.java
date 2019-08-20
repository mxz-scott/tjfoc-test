package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSubLedger {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

   @Test
    public void createSubledger()throws Exception{
       MgToolCmd mgToolCmd = new MgToolCmd();
       String ledger = "sub"+sdf.format(dt)+ RandomUtils.nextInt(1000);
       mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + ledger,
               " -t sm3", " -w first", " -c raft", ids);
       Thread.sleep(SLEEPTIME*2);
       subLedger = ledger;
       String response = store.CreateStore("test for ok tx");
       Thread.sleep(SLEEPTIME*2);
       String txHash1 = JSONObject.fromObject(response).getJSONObject("Data").get("Figure").toString();
       assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("State"));

    }

}
