package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Contract {
    String InstallWVM(String file,String category,String prikey) throws Exception;
    String QueryWVM(String name,String version,String category,String method,String caller,List<?> args);
    String DestroyWVM(String name,String category)throws Exception;
    String SearchByKey(String key,String contractName);
    String SearchByPrefix(String prefix,String contractName);
    String Invoke(String name,String version,String category,String method,String caller,List<?> args);
    String Destroy(String name,String version,String category);
    String Install(String name,String version,String category,String file) throws Exception;
    String Invoke(String name,String version,String category,String method,List<?> args);

    String SynInstall(Integer timeout,String name,String version,String category,String file);
    String SynDestroy(Integer timeout,String name,String version,String category);
    String SynInvoke(Integer timeout,String name,String version,String category,String method,List<?> args);

    @Deprecated
    String CreateNewTransaction(String name,String version,String method,List<?> args);

}
