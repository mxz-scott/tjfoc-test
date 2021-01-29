package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MaxNo {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    ArrayList listLedgerId = new ArrayList();


@Test
public void testAddApp()throws Exception{
    for(int i =0;i<40;i++){
        log.info("+++++++++++++++++++++++++++++++++++++ " + i);
        TC1613_createWordMultiStr();
        listLedgerId.add(subLedger);
    }

    for(int i=0;i<2000;i++){
        log.info("************************************* " + i);
        for(int k=0;k<40;k++) {
            subLedger = listLedgerId.get(k).toString();
            String response1 = store.CreateStore("testdata");
//            assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        }
    }
}
    @Test
    public void TC1613_createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String word = chainName + " first word";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName," -t sm3",
                " -w \"" + word + "\""," -c raft",ids);

        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains(chainName), true);

        res2 = mgToolCmd.getAppChain(PEER1IP,PEER2IP + ":" + PEER2RPCPort,"");
        assertEquals(res2.contains(chainName), true);

        res2 = mgToolCmd.getAppChain(PEER1IP,PEER4IP + ":" + PEER4RPCPort,"");
        assertEquals(res2.contains(chainName), true);

    }


}
