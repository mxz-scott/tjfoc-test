package com.tjfintech.common.Interface;

import java.util.Map;

public interface Store {
    String GetTransaction(String hash);

    String GetTxDetail(String hash);

    String inLocal(String hash);

    String CreateStore(String Data);

    String CreateStorePwd(String hash, Map keymap);

    String GetStore(String hash);

    String GetStorePost(String hash,String priKey);

    String GetStorePostPwd(String hash,String priKey,String keyPwd);

    String GetTransactionIndex(String hash);

    String GetHeight();

    String GetBlockByHeight(int height);

    String GetBlockByHash(String hash);

    String GetTxSearch(int skip,int size,String regex);

    String GetInlocal(String hash);

    String GetStat(String type);

    String GetTransactionBlock(String hash);

    String GetPeerList();

    String GetApiHealth();
}
