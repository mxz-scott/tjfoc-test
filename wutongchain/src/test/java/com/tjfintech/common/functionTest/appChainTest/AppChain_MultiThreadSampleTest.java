package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MultiThreadSampleTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();

//    @Before
    public void beforeConfig() throws Exception {
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }

    @Test
    public void createMultiChains_4Thread30Txs()throws Exception{

        beforeConfig();

        //创建子链，包含两个节点
        String chainName2 = "tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName2,
                " -t sm3"," -w first",
                " -c raft",ids);
        tempLedgerId1 = subLedger;

        //创建子链，包含三个节点
        String chainName3 = "tc1589_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName3,
                " -t sm3"," -w first"," -c raft",ids);

        tempLedgerId2 = subLedger;

        sleepAndSaveInfo(2000);
        //检查可以获取子链列表
        String res2 = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(res2.contains("name"), true);
        assertEquals(res2.contains(chainName2), true);
        assertEquals(res2.contains(chainName3), true);
        for(int i =0 ;i<30;i++) {
            Thread myThread1 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread1).ledgerID = globalAppId1;
            Thread myThread2 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread2).ledgerID = globalAppId2;
            Thread myThread3 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread3).ledgerID = tempLedgerId1;
            Thread myThread4 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread4).ledgerID = tempLedgerId2;

            myThread1.start();
            myThread2.start();
            myThread3.start();
            myThread4.start();

            myThread1.run();
            myThread2.run();
            myThread3.run();
            myThread4.run();
        }
    }

//    @Test
    public void createMultiChainsWithExistedAppChain()throws Exception{

        for(int i =0 ;i<3000;i++) {
            Thread myThread1 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread1).ledgerID = "awyujny9c0";
            Thread myThread2 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread2).ledgerID = "4vv8b5a4kp";
            Thread myThread3 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread3).ledgerID = "zzx0lo893j";
            Thread myThread4 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread4).ledgerID = "7yriovd3rb";

            myThread1.start();
            myThread2.start();
            myThread3.start();
            myThread4.start();

            myThread1.run();
            myThread2.run();
            myThread3.run();
            myThread4.run();
        }
    }


    //    @Test
    public void createMultiChainsWithExisted1AppChain()throws Exception{

        for(int i =0 ;i<3000;i++) {
            Thread myThread1 = new AppChain_MultiThread();
            ((AppChain_MultiThread) myThread1).ledgerID = "awyujny9c0";

            myThread1.start();
            myThread1.run();
        }
    }

//    @Test
    public void getBlockTxs()throws Exception{
        subLedger = "9ce1gylo9s";
        FileOperation fileOperation = new FileOperation();

        int height = JSONObject.fromObject(store.GetHeight()).getInt("data");
        for(int i =1 ;i<height;i++) {
            String log = "height " + i + " txs size " +
                    JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("data").getJSONArray("txs").size();
            fileOperation.appendToFile(log,"height.txt");
        }
    }
}
