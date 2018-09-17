package com.tjfintech.common;


import com.tjfintech.common.untils.GetTest;
import com.tjfintech.common.untils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.*;

import static com.tjfintech.common.MultiTest.SDKADD;

@Slf4j
public class StoreTest {
    public static String PUBKEYS="";
    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }
    @Test

    public void runSDK3Test()throws  Exception{


        int number =1;
        log.info("\n创建存证交易--------------------------------\n");
        String response=CreateStore();
        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash=jsonObject.getJSONObject("Data").get("Figure").toString();
        log.info("\n创建带密码存证交易--------------------------------\n");
        String responsePwd=CreateStorePwd();
        JSONObject jsonObjectPwd=JSONObject.fromObject(responsePwd);
        String hashPwd=jsonObjectPwd.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(1000*5);//休眠5秒
        log.info("\n查询存证交易--------------------------------\n");
        GetStore(hash);
        log.info("\n获取隐私存证--------------------------------\n");
        GetStorePost(hash);
        log.info("\n获取带密码隐私存证--------------------------------\n");
        GetStorePostPwd(hashPwd);
        log.info("\n获取交易索引--------------------------------\n");
        GetTransactionIndex(hash);
        log.info("\n获取区块高度--------------------------------\n");
        GetHeight();
       // GetBlockByHash();
        log.info("\n按高度获取区块信息--------------------------------\n");
        GetBlockByHeight(number);
        log.info("\n交易复杂2查询--------------------------------\n");
        GetTxSearch(2);
        log.info("\n交易复杂1查询--------------------------------\n");
        GetTxSearch(1);
        log.info("\n查询交易是否存在于钱包数据库");
        GetInlocal(hash);
        log.info("\n统计某种交易类型的数量");
        GetStat();
    }





    
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

    public  String   CreateStore(){

        String Data="测试存证内容-chenxu";
        String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWpFZUc0Vm9ETTJkRjAxWnpGQ3NQNkxqTE9zVC8NCkg2YWx5ejBNRXRSU2krazQxbTNzOXFoUVB4UDk1OFFQdGUwS2pZa1VKeUt0MUVBV2NraEI0Wm16eUE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        List<String>PubkeysObjects=new ArrayList<String>();
        PubkeysObjects.add(Pubkeys);
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

    public  String   CreateStorePwd(){

        String Data="测试带密码存证内容-chenxu";
        String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWtiRmlaOW9VaWFaMmh3dTVsS3FYNkQ1OHdXOVYNCmNEQ1BjUEJQWThyTlVTQitNR1ZxMUlyUk8vVVBMaXRqc0RtcWN2MzdKdmVSTC9Ba0FWM1hDd2JGM3c9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        List<String>PubkeysObjects=new ArrayList<String>();
        PubkeysObjects.add(Pubkeys);
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
        String hashEncode=URLEncoder.encode(hash);
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
    public  void GetStorePost(String Hash){

       String Prikey="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tDQpNSUdUQWdFQU1CTUdCeXFHU000OUFnRUdDQ3FCSE05VkFZSXRCSGt3ZHdJQkFRUWdCcmZhbitITXlUU01weTdODQo2WEszTFRnSWlzN1RqSzJiZC9UT1pneVZqWUtnQ2dZSUtvRWN6MVVCZ2kyaFJBTkNBQVNNUjRiaFdnTXpaMFhUDQpWbk1VS3cvb3VNczZ4UDhmcHFYTFBRd1MxRktMNlRqV2JlejJxRkEvRS8zbnhBKzE3UXFOaVJRbklxM1VRQlp5DQpTRUhobWJQSQ0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQ==";
     //  String Pwd="";
       Map<String,Object> map=new HashMap<>();
       map.put("Prikey",Prikey);
       map.put("Hash",Hash);
    //   map.put("Pwd",Pwd);
       //log.info(map);
       log.info(PostTest.sendPostToJson(SDKADD+"/getstore",map));

   }
    /**
     * 获取带密码隐私存证
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public  void GetStorePostPwd(String Hash){

        String Prikey="LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWhra283bEx2ZWtmQUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUxvc2VwNnI2azhsSXM0Tk1DNndGM2NFZ2FBbg0KeE4wWDRadHJsc2pCVG5TOXhjYnM3Wk9lcjFwY25aby9RZ2JqRWtGeThaYVBjSyt5d0NLcDRaMDVnbWgwU2M4Nw0KTVdNbGZvd1pJbXcvSHRoOHQ5Y0Z3eFRZMktiZkJEaWQ1SFpwVGRpRGU2R2tVa3hsajRnQkZhM29xMjg4UnVpOA0KOTIwY3FvQmwrWlZKKy8rZkFlaTA2b1ZqdEJzdWp0SmRjWnd6eGlxMjdzK0V3VUptV2NxaWliTWVqZGtDUWZvdQ0Kam9tQkphajZwS3pwdEhQNnIrbHkNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
        String KeyPwd="123";
        Map<String,Object> map=new HashMap<>();
        map.put("Prikey",Prikey);
        map.put("Hash",Hash);
        map.put("KeyPwd",KeyPwd);
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
    public void GetTxSearch(int num){
       int  skip=0;
       int  size=num;
       Map<String,Object> map=new HashMap<>();
       map.put("skip",skip);
       map.put("size",size);
       Map<String,Object> regexmap=new HashMap<>();
       regexmap.put("$regex","tor");
       Map<String,Object> datamap=new HashMap<>();
       datamap.put("scargs.data",regexmap);
       map.put("qry",datamap);
       log.info("应该查询出"+num+"条数据");
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
   public void GetStat(){
       String type="store";
       Map<String,Object>map=new HashMap<>();
       map.put("type",type);
       String param=GetTest.ParamtoUrl(map);
       log.info(GetTest.SendGetTojson(SDKADD+"/tx/stat"+"?"+param));
   }
    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }






}
