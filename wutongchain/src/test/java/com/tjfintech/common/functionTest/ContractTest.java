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
import static com.tjfintech.common.utils.UtilsClass.encryptBASE64;
import static com.tjfintech.common.utils.UtilsClass.readInput;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class ContractTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();

    /**
     * 安装合约
     * @throws Exception
     */
    @Test
    public  void installTest()throws  Exception{
        String name="chenxu";//+ Random(5);
        String version="1.0";
        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
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

    /**
     * 销毁合约
     * @throws Exception
     */
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

    /**
     * 调用前需初始化
     * @throws Exception
     */
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

    /**
     * 合约自定义初始化
     * @throws Exception
     */
    @Test
    public void initMobileTest()throws Exception{
        String method ="initMobile";
        invoke(method);
    }

    /**
     * 查询指定ID的手机信息
     * @throws Exception
     */
    @Test
    public void queryMobileTest()throws  Exception{
        String method = "queryMobile";
        String arg = "Mobile8";
        invoke(method,arg);

    }

    /**
     * 发送事务至KAFKA
     * @throws Exception
     */
    @Test
    public void eventTest()throws Exception{
        String method="event";
        invoke(method);
    }

    /**
     * 创建新手机信息
     * 需要6个参数 商标 型号 价格 数量 颜色 ID
     * @throws Exception
     */
    @Test
    public void createMobileTest()throws Exception{
        String method="createMobile";
        String brand="xiaomi";
        String model="Mix2S";
        String price="4000.00";
        String count="black";
        String color="123";
        String mobileID="Mobile8";
        invoke(method,brand,model,price,color,count,mobileID);
    }

    /**
     * 删除指定手机信息。只删除世界状况中信息。链上信息仍存在
     * @throws Exception
     */
    @Test
    public void deleteMobileTest()throws Exception{
        String method ="deleteMobile";
        String arg="Mobile5";
        invoke(method,arg);
    }

    /**
     * 修改指定ID的手机数量信息
     * @throws Exception
     */
    @Test
    public void changeMobileCountTest()throws Exception{
        String method="changeMobileCount";
        String arg="55";
        String arg2="Mobile1";
        invoke(method,arg,arg2);
    }

    /**
     * 遍历所有手机信息
     * @throws Exception
     */
    @Test
    public void getAllMobileTest()throws Exception{
        String method="getAllMobile";

        invoke(method);
    }

    /**
     * 合约Invoke调用方法
     * @param method  方法名
     * @param arg     参数，可多个
     * @throws Exception
     */
    public void invoke(String method, String... arg) throws Exception {
        String name = "chenxu";
        String version = "1.0";
        List<String> args = new LinkedList<>();
        for (int i = 0; i < arg.length; i++) {
            args.add(arg[i]);
        }
        String response = contract.CreateNewTransaction(name, version, method, args);
        String hash = JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        Thread.sleep(SLEEPTIME);
        String result = store.GetTransaction(hash);
        assertThat(result, containsString("200"));
        assertThat(result, containsString("success"));

    }


}
