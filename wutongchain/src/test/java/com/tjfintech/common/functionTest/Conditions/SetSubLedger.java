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
       String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
       String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
       String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
       String ids = " -m "+ id1+","+ id2+","+ id3;
       MgToolCmd mgToolCmd = new MgToolCmd();
       //20200415 子链不支持包含.
//       String ledger = "sO.l_"+sdf.format(dt).substring(4)+ RandomUtils.nextInt(1000);//尽量将子链名称构造复杂一些
       String ledger = "sOl_"+sdf.format(dt).substring(4)+ RandomUtils.nextInt(1000);//尽量将子链名称构造复杂一些
       mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -c " + ledger,
               " -t sm3", " -w first", " -c raft", ids);
       Thread.sleep(SLEEPTIME*2);
       subLedger = ledger;
       String response = store.CreateStore("test for ok tx");
       Thread.sleep(SLEEPTIME*2);
       String txHash1 = JSONObject.fromObject(response).getString("data");
       assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));

    }

}
