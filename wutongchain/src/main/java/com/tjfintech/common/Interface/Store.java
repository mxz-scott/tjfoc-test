package com.tjfintech.common.Interface;

import java.util.Map;

public interface Store {

    String CreateStore(String Data);
    String CreateStore(String Data,String... PubKeys);
    String CreateStorePwd(String Data, Map keymap);

    String GetStore(String hash);
    String GetStore2(String hash);
    String GetStorePost(String hash,String priKey);
    String GetStorePostPwd(String hash,String priKey,String keyPwd);

    String GetTransactionIndex(String hash);
    String GetHeight();
    String GetBlockByHeight(int height);
    String GetBlockByHash(String hash);
    String GetTransactionBlock(String hash);
    String GetTransaction(String hash);
    String GetTxDetail(String hash);

    String GetTxSearch(int skip,int size,String regex);
    String GetInlocal(String hash);

    String GetPeerList();
    String GetApiHealth();

    String SynCreateStore(Integer timeout ,String Data);  //同步创建存证接口
    String SynCreateStore(Integer timeout ,String Data,String... PubKeys);  //同步创建存证接口
    String SynCreateStorePwd(Integer timeout ,String hash, Map keymap);//同步创建隐私存证

    String SynGetStorePost(Integer timeout ,String Hash,String priKey,String keyPwd);  //同步获取隐私存证

//    String GetStat(String type);

}
