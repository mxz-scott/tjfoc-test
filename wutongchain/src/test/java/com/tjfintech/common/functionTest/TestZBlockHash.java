package com.tjfintech.common.functionTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestZBlockHash {

    public   final static int   SLEEPTIME=8*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();


    @Test
    public void checkBlockHash()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.initTest();
        int blockHeight = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("Data"));
        String preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(blockHeight)).getJSONObject("Data").getJSONObject("header").getString("previousHash");
        String currentBlockHash="";
        for(int i= blockHeight-1;i>0;i--){

            currentBlockHash  = JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("blockHash");
            assertEquals(currentBlockHash,preBlockHash);
            preBlockHash=JSONObject.fromObject(store.GetBlockByHeight(i)).getJSONObject("Data").getJSONObject("header").getString("previousHash");

        }
    }



}
