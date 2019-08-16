package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.*;


import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncDockerContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    
    String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    String version="1.0";
    String category="docker";


    /**
     * 同步安装合约 120秒
     * 同步销毁合约
     * @throws Exception
     */
    @Test
    public void SynInstall() throws Exception {
        //正常情况下（120000毫秒）
        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.SynInstall(utilsClass.LONGTIMEOUT,name,version,category,data);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat("200",containsString(JSONObject.fromObject(response).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(response).getString("Message")));
        Thread.sleep(SLEEPTIME*3);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));

        SynInvoke("InitMobile");

        log.info("销毁智能合约");
        String response3 = contract.SynDestroy(utilsClass.LONGTIMEOUT,name, version,category);
        assertThat("200",containsString(JSONObject.fromObject(response3).getString("State")));
        String hash2 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");
        Thread.sleep(1000);
        assertThat("200",containsString(JSONObject.fromObject(store.GetTxDetail(hash2)).getString("State")));
        assertThat("success",containsString(JSONObject.fromObject(response3).getString("Message")));
    }
    /**
     * 同步调用智能合约
     */
    //@Test
    public void SynInvoke(String method,String... arg) throws InterruptedException {
        List<String> args = new LinkedList<>();
        for (int i = 0; i < arg.length; i++) {
            args.add(arg[i]);
        }
        String synInvoke = contract.SynInvoke(utilsClass.SHORTMEOUT, name, version, category, method, args);
        String hash= JSONObject.fromObject(synInvoke).getJSONObject("Data").getString("Figure");//获取hash值
        assertThat("200",containsString(JSONObject.fromObject(synInvoke).getString("State")));
        assertThat("200",containsString(JSONObject.fromObject(store.GetTransaction(hash)).getString("State")));

    }
}
