package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class ContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    @Test
    public  void installTest()throws  Exception{
        String name="chenxu";//+ Random(5);
        String version="1.0";
        String filePath=System.getProperty("user.dir")+"/src/main/resources/wttest.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.Install(name,version,data);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response,containsString("success"));
        Thread.sleep(SLEEPTIME*10);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }
    @Test
    public void destroyTest()throws  Exception{
        String name="chenxu";
        String version="1.0";
        String response=contract.Destroy(name,version);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        assertThat(response,containsString("success"));
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));

    }
    @Test
    public void initTest()throws Exception{
        String name="chenxu";
        String version="1.0";
        String method="init";
        List<String>args=new LinkedList<String>();
        args.add("a");
        args.add("200");
        args.add("b");
        args.add("233");
       String response=contract.CreateNewTransaction(name,version,method,args);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }
    @Test
    public void queryTest()throws  Exception{
        String name="chenxu";
        String version="1.0";
        String method="query";
        List<String>args=new LinkedList<String>();
        args.add("a");
        String response=contract.CreateNewTransaction(name,version,method,args);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String response2=store.GetTransaction(hash);
        assertThat(response2,containsString("200"));
        assertThat(response2,containsString("success"));
    }
    @Test
    public void invokeTest()throws Exception{
        String name="chenxu";
        String version="1.0";
        String method="invoke";
        List<String>args=new LinkedList<>();
        args.add("a");
        args.add("b");
        args.add("10");
        args.add("none");//合约里需要4个参数。但最后一个参数没用实际用途
        String response=contract.CreateNewTransaction(name,version,method,args);
        String hash= JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String result=store.GetTransaction(hash);
        assertThat(result,containsString("200"));
        assertThat(result,containsString("success"));
    }


}
