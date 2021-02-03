package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Date;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MaxNo {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    ArrayList listLedgerId = new ArrayList();
    public static int appNo = 0;


//    @After
    public void  recordFinishSysInfo()throws Exception{
        FileOperation fileOperation = new FileOperation();
        recordTimeSysMemFreeInfo(fileOperation);
//        recordTimeSysInfo(fileOperation,"4",false);
    }
@Test
public void testAddApp()throws Exception{
        int No = 10;
    FileOperation fileOperation = new FileOperation();
    fileOperation.appendToFile("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n","mem.txt");
    recordTimeSysMemFreeInfo(fileOperation);
//    recordTimeSysInfo(fileOperation,"4",true);

    for(int i =0;i< No;i++){
        log.info("+++++++++++++++++++++++++++++++++++++ " + i);
        createWordMultiStr();
        appNo = i + 1;
        listLedgerId.add(subLedger);
        recordTimeSysMemFreeInfo(fileOperation);
//        recordTimeSysInfo(fileOperation,"",false);
    }
//    recordTimeSysMemFreeInfo(fileOperation);
//    for(int i =0;i< No;i++){
//        log.info("+++++++++++++++++++++++++++++++++++++ " + i);
//        appNo = i + 1;
//        subLedger = listLedgerId.get(i).toString();
//        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort,subLedger);
//    }

    recordTimeSysMemFreeInfo(fileOperation);


//    for(int i=0;i<2000;i++){
//        log.info("************************************* " + i);
//        for(int k=0;k<40;k++) {
//            subLedger = listLedgerId.get(k).toString();
//            String response1 = store.CreateStore("testdata");
////            assertEquals("200", JSONObject.fromObject(response1).getString("state"));
//        }
//    }
}
//    public void recordTimeSysInfo(FileOperation fileOperation,Boolean bFirst)throws Exception {
//        String cmd = "vmstat|tail -1";
//        if(bFirst) cmd = "vmstat";
//        String res1 = shExeAndReturn(PEER1IP, cmd);
//        long time1 = (new Date()).getTime();
//        cmd = "vmstat|tail -1";
//        String res2 = shExeAndReturn(PEER2IP, cmd);
//        long time2 = (new Date()).getTime();
//        String res4 = shExeAndReturn(PEER4IP, cmd);
//        long time4 = (new Date()).getTime();
//        if(bFirst) fileOperation.appendToFile(time1 + "\n" + res1, PEER1IP + "mem.txt");
//        else fileOperation.appendToFile(time1 + "  " + res1, PEER1IP + "mem.txt");
//        fileOperation.appendToFile(time2 + "  " + res2, PEER2IP + "mem.txt");
//        fileOperation.appendToFile(time4 + "  " + res4, PEER4IP + "mem.txt");
//    }

    public void recordTimeSysInfo(FileOperation fileOperation,String columnNo,Boolean bFirst)throws Exception {

        String cmd = "vmstat|tail -1";
        if(columnNo.isEmpty()) {
            cmd = "vmstat|tail -1";
            if (bFirst) cmd = "vmstat";
        }else {
            cmd = "vmstat|tail -1|awk '{print $" + columnNo +"}'";
        }
        String res1 = shExeAndReturn(PEER1IP, cmd);        long time1 = (new Date()).getTime();
        String res2 = shExeAndReturn(PEER2IP, cmd);        long time2 = (new Date()).getTime();
        String res4 = shExeAndReturn(PEER4IP, cmd);        long time4 = (new Date()).getTime();
        if(bFirst) fileOperation.appendToFile(appNo + time1 + "\n" + res1, PEER1IP + "mem.txt");
        else fileOperation.appendToFile(appNo + time1 + "  " + res1, PEER1IP + "mem.txt");
        if(bFirst) fileOperation.appendToFile(appNo + time2 + "\n" + res2, PEER2IP + "mem.txt");
        else fileOperation.appendToFile(appNo + time2 + "  " + res2, PEER2IP + "mem.txt");
        if(bFirst) fileOperation.appendToFile(appNo + time4 + "\n" + res4, PEER4IP + "mem.txt");
        else fileOperation.appendToFile(appNo + time4 + "  " + res4, PEER4IP + "mem.txt");
    }

    public void recordTimeSysMemFreeInfo(FileOperation fileOperation)throws Exception {

        String cmd = "vmstat|tail -1|awk '{print $4}'";

        String res1 = shExeAndReturn(PEER1IP, cmd);        long time1 = (new Date()).getTime();
        String res2 = shExeAndReturn(PEER2IP, cmd);        long time2 = (new Date()).getTime();
        String res4 = shExeAndReturn(PEER4IP, cmd);        long time4 = (new Date()).getTime();
        String info = appNo + " " + res1 + " " + time1 + " " + res2 + " " + time2 + " " + res4 + " " + time4;
        fileOperation.appendToFile(info, "mem.txt");

    }
    @Test
    public void createWordMultiStr()throws Exception{
        //创建子链，-w "first word"
        String chainName = "tc1613_" + appNo + "_" + sdf.format(dt) + RandomUtils.nextInt(1000);
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
