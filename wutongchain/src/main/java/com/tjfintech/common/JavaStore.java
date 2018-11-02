package com.tjfintech.common;

import com.tjfintech.common.Interface.Store;

import java.util.Map;

public class JavaStore implements Store {
    @Override
    public String GetTransaction(String hash) {
        return null;
    }

    @Override
    public String inLocal(String hash) {
        return null;
    }

    @Override
    public String CreateStore(String Data) {
        return null;
    }

    @Override
    public String CreateStorePwd(String hash, Map keymap) {
        return null;
    }

    @Override
    public String GetStore(String hash) {
        return null;
    }

    @Override
    public String GetTransactionBlock(String hash) {
        return null;
    }

    @Override
    public String GetStorePost(String hash, String priKey) {
        return null;
    }

    @Override
    public String GetStorePostPwd(String hash, String priKey, String keyPwd) {
        return null;
    }

    @Override
    public String GetTransactionIndex(String hash) {
        return null;
    }

    @Override
    public String GetHeight() {
        return null;
    }

    @Override
    public String GetBlockByHeight(int height) {
        return null;
    }

    @Override
    public String GetBlockByHash(String hash) {
        return null;
    }

    @Override
    public String GetTxSearch(int skip, int size, String regex) {
        return null;
    }

    @Override
    public String GetInlocal(String hash) {
        return null;
    }

    @Override
    public String GetStat(String type) {
        return null;
    }
}
