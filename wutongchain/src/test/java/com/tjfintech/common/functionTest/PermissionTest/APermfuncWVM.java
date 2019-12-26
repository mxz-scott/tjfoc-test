package com.tjfintech.common.functionTest.PermissionTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.version;

//import static org.hamcrest.Matchers.containsString;

@Slf4j
public class APermfuncWVM {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    String version ="2.0";
    String wvmhash ="0111";

    String okCode="200";
    String okMsg="success";

    String errCode="404";
    String errMsg="does not found Permission";
    String category="wvm";


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

    public String wvmInstallTest(String wvmfile,String Prikey) throws Exception {
        if(wvmfile == "") return contract.InstallWVM("",category,Prikey);

        String filePath = resourcePath + wvmfile;
        log.info("filepath "+ filePath);
        String file = readInput(filePath).toString().trim();
        String data = encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        log.info("base64 data: " + data);
        String response=contract.InstallWVM(data,category,Prikey);
        wvmhash = JSONObject.fromObject(response).getJSONObject("Data").getString("Name");
        return retAllow(response);
    }


    public String wvmDestroyTest(String cthash) throws Exception {
        String response = contract.DestroyWVM(cthash,category);
        return retAllow(response);

    }


    public String invokeNew(String cthash, String method, Object... arg) throws Exception {
        List<Object> args = new LinkedList<>();
        for (Object obj:arg){
            args.add(obj);
        }
        String response = contract.Invoke(cthash, version, category,method,"", args);
        return retAllow(response);
    }

}
