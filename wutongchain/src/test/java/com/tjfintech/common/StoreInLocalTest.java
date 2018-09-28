package com.tjfintech.common;


import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.Date;

import static com.tjfintech.common.StoreTest.SLEEPTIME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class StoreInLocalTest {
    Store store=new Store();

    @Test
    //测试存证的性能
    public void TC010_CreatePrivateStoreDataIsJson() {
        long times = new Date().getTime();
        log.info("开始存证"+times);
    String data = store.CreateStore("j344342311365633i2j");
        log.info(data);


        JSONObject jsonObject = JSONObject.fromObject(data);

    String hash=jsonObject.getJSONObject("Data").get("Figure").toString();
    log.info(hash);
        inLocal(hash,times);
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
