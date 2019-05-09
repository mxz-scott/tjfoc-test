package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Contract {
    String Install(String name,String version,String category,String file) throws Exception;
    String SynInstall(Integer timeout,String name,String version,String category,String file);
    String Destroy(String name,String version,String category);
    String SynDestroy(Integer timeout,String name,String version,String category);
    String CreateNewTransaction(String name,String version,String method,List<?> args);
    String Invoke(String name,String version,String category,String method,List<?> args);
    String SynInvoke(Integer timeout,String name,String version,String category,String method,Integer args);

    String SearchByKey(String key,String contractName);
    String SearchByPrefix(String prefix,String contractName);

}
