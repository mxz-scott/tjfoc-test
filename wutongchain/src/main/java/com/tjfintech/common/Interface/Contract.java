package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Contract {
    String Install(String name,String version,String category,String file) throws Exception;
    String Destroy(String name,String version,String category);
    String CreateNewTransaction(String name,String version,String method,List<?> args);
    String Invoke(String name,String version,String category,String method,List<?> args);
    String SearchByKey(String key,String contractName);
    String SearchByPrefix(String prefix,String contractName);

}
