package com.tjfintech.common.performanceTest;


import com.tjfintech.common.GoStore;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertThat;

@Slf4j
public class StoreInLocalTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();


    @Test
    //测试存证的性能
    public void TC010_CreatePrivateStoreDataIsJson() {
        for(int i=0;i<150;i++){
            long times = new Date().getTime();
        log.info("开始存证"+times);
        String data = store.CreateStore("j"+i);
        log.info(data);
        JSONObject jsonObject = JSONObject.fromObject(data);
        String hash=jsonObject.getJSONObject("Data").get("Figure").toString();
        log.info(hash);
        inLocal(hash,times);
        }
    }
    public void inLocal(String hash,long time){
        try {
            long nowTimes;
            Thread.sleep(100);
            String result = store.inLocal(hash);
            nowTimes = new Date().getTime();
            JSONObject jsonObject = JSONObject.fromObject(result);

            String State=jsonObject.get("State").toString();

            if (State.equals("404")){
                log.info("未同步"+(nowTimes-time)+"ms");
                inLocal(hash,time);
            }else{
                log.info("成功同步"+(nowTimes-time)+"ms");
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }
}
