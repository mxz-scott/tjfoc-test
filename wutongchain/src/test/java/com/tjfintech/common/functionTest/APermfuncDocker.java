package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.lang.*;

import static com.tjfintech.common.utils.UtilsClass.encryptBASE64;
import static com.tjfintech.common.utils.UtilsClass.readInput;
//import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class APermfuncDocker {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    Store store=testBuilder.getStore();
    String version ="2.0";
    String name ="0111";

    String okCode="200";
    String okMsg="success";

    String errCode="404";
    String errMsg="does not found Permission";

    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        if(checkStr.contains(okCode)) {
            allow = "1";
        }
        else if(checkStr.contains(errCode)&&checkStr.contains(errMsg))
        {
            allow="0";
        }
        return allow;
    }

    /**
     * 安装合约
     * @throws Exception
     */
    public  String installContract()throws  Exception{
        String filePath = System.getProperty("user.dir") + "/src/main/resources/simple.go";
        String file=readInput(filePath).toString();
        String data = encryptBASE64(file.getBytes());//BASE64编码
        String response=contract.Install(name,version,data);
        return retAllow(response);
    }

    /**
     * 销毁合约
     * @throws Exception
     */
    public String destroyContract()throws  Exception{
        String response=contract.Destroy(name,version);
        return retAllow(response);
    }

    /**
     * 合约自定义初始化
     * @throws Exception
     */
    public String initMobileTest()throws Exception{
        String method ="initMobile";
        return invoke(method);
    }

    /**
     * 查询指定ID的手机信息
     * @throws Exception
     */
    public String queryMobileTest()throws  Exception{
        String method = "queryMobile";
        String arg = "Mobile8";
        return invoke(method,arg);

    }

    /**
     * 发送事务至KAFKA
     * @throws Exception
     */

    public String eventTest()throws Exception{
        String method="event";
        return invoke(method);
    }

    /**
     * 创建新手机信息
     * 需要6个参数 商标 型号 价格 数量 颜色 ID
     * @throws Exception
     */

    public String createMobileTest()throws Exception{
        String method="createMobile";
        String brand="xiaomi";
        String model="Mix2S";
        String price="4000.00";
        String count="black";
        String color="123";
        String mobileID="Mobile8";
        return invoke(method,brand,model,price,color,count,mobileID);
    }

    /**
     * 删除指定手机信息。只删除世界状况中信息。链上信息仍存在
     * @throws Exception
     */

    public String deleteMobileTest()throws Exception{
        String method ="deleteMobile";
        String arg="Mobile5";
        return invoke(method,arg);
    }

    /**
     * 修改指定ID的手机数量信息
     * @throws Exception
     */
    public String changeMobileCountTest()throws Exception{
        String method="changeMobileCount";
        String arg="55";
        String arg2="Mobile1";
        return invoke(method,arg,arg2);
    }

    /**
     * 遍历所有手机信息
     * @throws Exception
     */
    @Test
    public String getAllMobileTest()throws Exception{
        String method="getAllMobile";
        return invoke(method);
    }

    /**
     * 合约Invoke调用方法
     * @param method  方法名
     * @param arg     参数，可多个
     * @throws Exception
     */
    public String invoke(String method,String... arg) throws Exception {
//        String name = "0111";
//        String version = "1.0";
        List<String> args = new LinkedList<>();
        for (int i = 0; i < arg.length; i++) {
            args.add(arg[i]);
        }
        String response = contract.CreateNewTransaction(name, version, method, args);
        return retAllow(response);

    }

    public String searchByKey(String key,String contractName)throws Exception{
        String response = contract.SearchByKey(key,contractName);
        return retAllow(response);
    }
    public String searchByPrefix(String prefix,String contractName)throws Exception{
        String response = contract.SearchByPrefix(prefix,contractName);
        return retAllow(response);
    }

}
