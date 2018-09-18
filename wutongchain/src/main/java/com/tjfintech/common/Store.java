package com.tjfintech.common;

import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.MultiTest.SDKADD;

@Slf4j
public class Store {
    /**
     * 获取交易详情
     * @author chenxu
     * @version 1.0
     * @method  GET
     */
    public void GetTransaction(){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("hashData","");
        param= GetTest.ParamtoUrl(map);

        log.info(GetTest.SendGetTojson(SDKADD+"/gettransaction"+"?"+param));
    }

    /**
     * 创建存证交易
     * @author chenxu
     * @version 1.0
     * @method  POST
     */

    public  String   CreateStore(String Data,Map keyMap){
       // String Data = "\"test\":\"json store1\"";
        // String Data="测试存证内容-chenxu";
       // String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWpFZUc0Vm9ETTJkRjAxWnpGQ3NQNkxqTE9zVC8NCkg2YWx5ejBNRXRSU2krazQxbTNzOXFoUVB4UDk1OFFQdGUwS2pZa1VKeUt0MUVBV2NraEI0Wm16eUE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        List<Object>PubkeysObjects=new ArrayList<>();
        for (Object value : keyMap.values()) {
            PubkeysObjects.add(value);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("Data",Data);
        map.put("Pubkeys",PubkeysObjects);
        String result= PostTest.sendPostToJson(SDKADD+"/store", map);
        log.info(result);
        return result;
    }

    /**
     * 创建带密码存证交易
     * @author chenxu
     * @version 1.0
     * @method  POST
     */

    public  String   CreateStorePwd(String  Data,String pubKeys){

     //   String Data="测试带密码存证内容-chenxu";
      //  String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWtiRmlaOW9VaWFaMmh3dTVsS3FYNkQ1OHdXOVYNCmNEQ1BjUEJQWThyTlVTQitNR1ZxMUlyUk8vVVBMaXRqc0RtcWN2MzdKdmVSTC9Ba0FWM1hDd2JGM3c9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        List<String>PubkeysObjects=new ArrayList<String>();
        PubkeysObjects.add(pubKeys);
        Map<String,Object> map=new HashMap<>();
        map.put("Data",Data);
        map.put("Pubkeys",PubkeysObjects);
        String result=PostTest.sendPostToJson(SDKADD+"/store",map);
        log.info(result);
        return result;
    }




    /***
     * 查询存证交易
     * @author chenxu
     * @version 1.0
     * @method  GET
     */
    public void  GetStore(String hash){
        String param;
        String hashEncode= URLEncoder.encode(hash);
        //hash需要urlEncode编码
        Map<String,Object>map=new HashMap<>();
        map.put("hash",hashEncode);
        param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/getstore"+"?"+param));

    }

    /***
     * 获取隐私存证
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public  void GetStorePost(String Hash,String priKey){
     //   String Prikey="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tDQpNSUdUQWdFQU1CTUdCeXFHU000OUFnRUdDQ3FCSE05VkFZSXRCSGt3ZHdJQkFRUWdCcmZhbitITXlUU01weTdODQo2WEszTFRnSWlzN1RqSzJiZC9UT1pneVZqWUtnQ2dZSUtvRWN6MVVCZ2kyaFJBTkNBQVNNUjRiaFdnTXpaMFhUDQpWbk1VS3cvb3VNczZ4UDhmcHFYTFBRd1MxRktMNlRqV2JlejJxRkEvRS8zbnhBKzE3UXFOaVJRbklxM1VRQlp5DQpTRUhobWJQSQ0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQ==";
        Map<String,Object> map=new HashMap<>();
        map.put("Prikey",priKey);
        map.put("Hash",Hash);
        log.info(PostTest.sendPostToJson(SDKADD+"/getstore",map));

    }
    /**
     * 获取带密码隐私存证
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public  void GetStorePostPwd(String Hash,String priKey,String keyPwd){

      //  String Prikey="LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWhra283bEx2ZWtmQUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUxvc2VwNnI2azhsSXM0Tk1DNndGM2NFZ2FBbg0KeE4wWDRadHJsc2pCVG5TOXhjYnM3Wk9lcjFwY25aby9RZ2JqRWtGeThaYVBjSyt5d0NLcDRaMDVnbWgwU2M4Nw0KTVdNbGZvd1pJbXcvSHRoOHQ5Y0Z3eFRZMktiZkJEaWQ1SFpwVGRpRGU2R2tVa3hsajRnQkZhM29xMjg4UnVpOA0KOTIwY3FvQmwrWlZKKy8rZkFlaTA2b1ZqdEJzdWp0SmRjWnd6eGlxMjdzK0V3VUptV2NxaWliTWVqZGtDUWZvdQ0Kam9tQkphajZwS3pwdEhQNnIrbHkNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
     //   String KeyPwd="123";
        Map<String,Object> map=new HashMap<>();
        map.put("Prikey",priKey);
        map.put("Hash",Hash);
        map.put("KeyPwd",keyPwd);
        //log.info(map);
        log.info(PostTest.sendPostToJson(SDKADD+"/getstore",map));

    }

    /**
     *  获取交易索引
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public  void  GetTransactionIndex(String hashData){

        String param;
        String hashencode=URLEncoder.encode(hashData);
        Map<String,Object>map =new HashMap<>();
        map.put("hashData",hashencode);
        param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/gettransactionindex"+"?"+param));

    }

    /***
     * 获取区块高度
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public  void  GetHeight(){
        log.info(GetTest.SendGetTojson(SDKADD+"/getheight"));
    }

    /**
     * 按高度获取区块信息
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public void  GetBlockByHeight(int height){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("number",height);
        param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/getblockbyheight"+"?"+param));
    }

    /**
     * 按哈希获取区块信息
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public void  GetBlockByHash(){
        String param;
        String hash="";
        Map<String,Object>map=new HashMap<>();
        map.put("hash",hash);
        param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/getblockbyhash"+"?"+param));
    }


    /**
     * 交易复杂查询
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public void GetTxSearch(int skip,int size,String regex){
//        int  skip=0;
//        int  size=5;
//        String regex="tor";
        Map<String,Object> map=new HashMap<>();
        map.put("skip",skip);
        map.put("size",size);
        Map<String,Object> regexmap=new HashMap<>();
        regexmap.put("$regex",regex);
        Map<String,Object> datamap=new HashMap<>();
        datamap.put("scargs.data",regexmap);
        map.put("qry",datamap);
        log.info("应该查询出"+size+"条数据");
        log.info(PostTest.sendPostToJson(SDKADD+"/tx/search",map));

    }
    /**
     * 查询交易是否存在于钱包数据库
     * @author  chenxu
     * @version 1.0
     * @method Get
     */
    public  void  GetInlocal(String hashcode){
        String hash=URLEncoder.encode(hashcode);
        Map<String,Object>map=new HashMap<>();
        map.put("hash",hash);
        String param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/tx/inlocal"+"?"+param));
    }
    /**
     * 统计某种交易类型的数量
     * @author  chenxu
     * @version  1.0
     * @method GET
     */
    public void GetStat(String type){
       // String type="store";
        Map<String,Object>map=new HashMap<>();
        map.put("type",type);
        String param=GetTest.ParamtoUrl(map);
        log.info(GetTest.SendGetTojson(SDKADD+"/tx/stat"+"?"+param));
    }


}
