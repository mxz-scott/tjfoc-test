package com.tjfintech.common;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoContract implements Contract {

    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }

    /**安装智能合约
     *
     * @param name  合约名
     * @param version  合约版本
     * @param file  合约内容(base64)
     * @return
     */
        public String Install(String name,String version,String file){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);
        map.put("File",file);

        String result=PostTest.sendPostToJson(SDKADD+"/contract/install",map);
        log.info(result);
        return result ;
    }
    /**安装智能合约
     *
     * @param name  合约名
     * @param version  合约版本

     * @return
     */
    public String Destroy(String name,String version){
        Map<String,Object>map=new HashMap<>();
        map.put("Name",name);
        map.put("Version",version);

        String result=PostTest.sendPostToJson(SDKADD+"/contract/destroy",map);
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
        String result=PostTest.sendPostToJson(SDKADD+"/createnewtransaction",map);
        log.info(result);
        return result ;
    }




    @After
    /**
     * 每次测试结束后都会执行的测试环境结束内容
     * 目前为空
     */
    public void TestAfter(){

    }



}
