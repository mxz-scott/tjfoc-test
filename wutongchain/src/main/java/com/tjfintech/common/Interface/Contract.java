package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Contract {
    String Install(String name,String version,String file);
    String Destroy(String name,String version);
    String CreateNewTransaction(String name,String version,String method,List<?> args);

}
