package com.tjfintech.common.functionTest.PermissionTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;


@Slf4j
public class APermfuncWVM {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    UtilsClass utilsClass = new UtilsClass();
    String version ="1.0.0";
    String wvmhash ="0111";

    String okCode="200";
    String okMsg="success";

    String category="wvm";


    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        boolean bNoPerm = false;
        if(checkStr.contains(NoPermErrCode)&&checkStr.contains(NoPermErrMsg)){
            bNoPerm = true;
        }
        if(checkStr.contains(okCode)) {
            allow = "1";
        }
        else if(bNoPerm)
        {
            allow="0";
        }
        return allow;
    }

    public String wvmInstallTest(String wvmfile,String Prikey) throws Exception {
        if(wvmfile == "") return contract.InstallWVM("",category,Prikey);

        String filePath = tempWVMDir + wvmfile;
        log.info("filepath "+ filePath);
        String file = utilsClass.readInput(filePath).toString().trim();
        String data = utilsClass.encryptBASE64(file.getBytes()).replaceAll("\r\n", "");//BASE64编码
        log.info("base64 data: " + data);
        String response=contract.InstallWVM(data,category,Prikey);
        if(response.contains("name:")) {
            wvmhash = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        }
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
        String response = contract.Invoke(cthash, "", category,method,"", args);
        return retAllow(response);
    }

}
