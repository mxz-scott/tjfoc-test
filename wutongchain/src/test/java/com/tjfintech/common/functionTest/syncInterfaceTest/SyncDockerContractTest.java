package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.SimpleDateFormat;
import java.util.*;


import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.encryptBASE64;
import static com.tjfintech.common.utils.UtilsClass.readInput;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncDockerContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    Date dt=new Date();
    SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");
    String name=sdf.format(dt)+ RandomUtils.nextInt(100000);
    String version="1.0";
    String category="docker";


    /**
     * 同步安装智能合约 120000秒
     * @throws Exception
     */
    @Test
    public void SynInstall() throws Exception {

        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.SynInstall(utilsClass.LONGTIMEOUT,name,version,category,data);
//        String response=contract.Install(name,version,category,data);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response,containsString("success"));
        Thread.sleep(SLEEPTIME*10);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));

    }
//    /**
//     * 同步调用智能合约
//     */
//    @Test
//    public void SynInvoke(){
//        String method="addSalesInfo";
//        Map<String, Object> map = new HashMap<>();
//        map.put("CompanyID","Company01");
//        map.put("Sales", 123456);
//        JSONObject json = JSONObject.fromObject(map);
//        String args ="\""+json.toString()+"\"";
//        String synInvoke = contract.SynInvoke(utilsClass.LONGTIMEOUT, name, version, category, method, args);
//        String hash= JSONObject.fromObject(synInvoke).getJSONObject("Data").getString("Figure");//获取hash值
//
//    }
////
//
    /**
     * 同步销毁智能合约
     */
    @Test
    public void SynDestroy() throws Exception {
        String response = contract.SynDestroy(utilsClass.LONGTIMEOUT,name, version,category);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response, containsString("success"));
        Thread.sleep(SLEEPTIME);
    }




}
