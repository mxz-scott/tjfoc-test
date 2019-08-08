package com.tjfintech.common;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public class GoContract implements Contract {

    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }

    /**
     * 安装wvm合约
     * @param file wvm合约文件名
     * @param prikey 合约所有人的私钥，用于升级合约时的验证。仅wvm
     * @return
     * @throws Exception
     */
    public String InstallWVM(String file,String prikey) throws Exception{
        Map<String,Object>map=new HashMap<>();
        map.put("Category","wvm");
        map.put("File",file);
        map.put("PriKey",prikey);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.postMethod(SDKADD+"/contract/install"+param,map);
        log.info(result);
        return result ;
    }

    /**安装docker智能合约
     *
     * @param name  合约名
     * @param version  合约版本
     * @param file  合约内容(base64)
     * @return
     */
    public String Install(String name,String version,String category,String file) throws Exception{
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("File",file);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;

//        String result=PostTest.sendPostToJson(SDKADD+"/contract/install",map);
        String result=PostTest.postMethod(SDKADD+"/contract/install"+param,map);
        log.info(result);
        return result ;
    }

    /**
     * 同步安装智能合约
     * @param timeout 设置时间
     * @param name
     * @param version
     * @param category
     * @param file
     * @return
     */
    @Override
    public String SynInstall(Integer timeout, String name, String version, String category, String file) {
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("File",file);
        String result=PostTest.sendPostToJson(SDKADD+"/sync/contract/install?timeout="+timeout,map);
        log.info(result);
        return result ;
    }

    /**
     * 销毁docker智能合约
     * @param name 销毁的智能合约名称
     * @param version 合约版本
     * @param category 合约类型 该函数中特定为docker
     * @return
     */
    public String Destroy(String name,String version,String category){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;

        String result=PostTest.sendPostToJson(SDKADD+"/contract/destroy"+param,map);
        log.info(result);
        return result ;
    }

    /**
     * 销毁wvm智能合约
     * @param name  销毁wvm合约名 需要从安装合约时获取
     * @return
     */
    public String DestroyWVM(String name){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Category","wvm");
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;

        String result=PostTest.sendPostToJson(SDKADD+"/contract/destroy"+param,map);
        log.info(result);
        return result ;
    }

    /**
     * 同步销毁合约
     * @param name
     * @param version
     * @param category
     * @return
     */
    @Override
    public String SynDestroy(Integer timeout,String name, String version, String category) {

        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        String result=PostTest.sendPostToJson(SDKADD+"/sync/contract/destroy?timeout="+timeout,map);
        log.info(result);
        return result ;
    }

    /**安装智能合约
     *
     * @param name  合约名
     * @param version  合约版本

     * @return
     */
    public String CreateNewTransaction(String name,String version,String method,List<?> args){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Method",method);
        map.put("Args",args);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/createnewtransaction"+param,map);
        log.info(result);
        return result ;
    }

    /**安装智能合约
     *
     * @param name  合约名
     * @param version  合约版本

     * @return
     */
    public String Invoke(String name,String version,String category,String method,List<?> args){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("Method",method);
        map.put("Args",args);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/contract/invoke"+param,map);
        log.info(result);
        return result ;
    }
    //此函数兼容wvm和docker两种类型
    public String Invoke(String name,String version,String category,String method,String caller,List<?> args){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("Method",method);
        map.put("Caller",caller);
        map.put("Args",args);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/contract/invoke"+param,map);
        log.info(result);
        return result ;
    }

    //此函数为WVM接口，支持执行合约内交易，但交易不会上链 同invoke，只是交易不会上链
    public String QueryWVM(String name,String version,String category,String method,String caller,List<?> args){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("Method",method);
        map.put("Caller",caller);
        map.put("Args",args);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/contract/query"+param,map);
        log.info(result);
        return result ;
    }

    /**
     * 同步调用智能合约
     * @param timeout
     * @param name
     * @param version
     * @param category
     * @param method
     * @param args
     * @return
     */
    @Override
    public String SynInvoke(Integer timeout, String name, String version, String category, String method, List<?> args) {
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("Category",category);
        map.put("Method",method);
        map.put("Args",args);
        String result=PostTest.sendPostToJson(SDKADD+"/sync/contract/invoke?timeout="+timeout,map);
        log.info(result);
        return result ;
    }


    @Override
    public String SearchByKey(String key, String contractName) {
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;
        String result=GetTest.SendGetTojson(SDKADD+"/search/bykey"+"?key="+key+"&cn="+contractName+param);
        log.info(result);
        return result;
    }

    @Override
    public String SearchByPrefix(String prefix, String contractName) {
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;
        String result=GetTest.SendGetTojson(SDKADD+"/search/byprefix"+"?prefix="+prefix+"&cn="+contractName+param);
        log.info(result);
        return result;
    }

    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
