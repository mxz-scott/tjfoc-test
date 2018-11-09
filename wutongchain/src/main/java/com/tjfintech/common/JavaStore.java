package com.tjfintech.common;

import com.tjfintech.common.Interface.Store;
import com.tjfoc.sdk.Client;
import com.tjfoc.smartcontact.SmartContract;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static transaction.TransactionOuterClass.TXInput;
import static transaction.TransactionOuterClass.TXOutput;


@Slf4j
public class JavaStore implements Store {
    @Override
    public String GetTransaction(String hash) {
        Client client = new Client();
        String str = client.blockchainGetTransaction_format(hash);
        System.out.println(str);
        return str;

    }


    @Override
    public String CreateStore(String Data) {
        String r = null;
        SmartContract smartContract = new SmartContract();
        String string = Data;
        byte[] any = string.getBytes();
        TXInput[] inputs = {};
        TXOutput[] outputs = {};
        r = smartContract.store_format(any, 0, inputs, outputs);
        log.info(r);
        return r;

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
    public String GetStorePost(String hash, String priKey) {
        return null;
    }

    @Override
    public String GetStorePostPwd(String hash, String priKey, String keyPwd) {
        return null;
    }

    @Override
    public String GetTransactionIndex(String hash) {
        Client client = new Client();
        String str = client.blockchainGetTransactionIndex_format(hash);
        System.out.println(str);
        return str;
    }

    @Override
    public String GetHeight() {
        Client client = new Client();
        String str = client.blockchainGetHeight_format();
        System.out.println(str);
        return str;

    }

    @Override
    public String GetBlockByHeight(int height) {
        Client client = new Client();
        String str = client.blockchainGetBlockByHeight_format(height);
        System.out.println(str);
        return str;
    }

    @Override
    public String GetBlockByHash(String hash) {
        Client client = new Client();
        String str = client.blockchainGetBlockByHash_format(hash);
        System.out.println(str);
        return str;
    }

    @Override
    public String GetTxSearch(int skip, int size, String regex) {
        return null;
    }

    @Override
    public String GetInlocal(String hash) {
        //暂定不实现该接口
        String result = "{\"State\":200,\"Message\":\"success\",\"Data\":219}";
        log.info(result);
        return result;
    }

    @Override
    public String GetStat(String type) {
        //暂定不实现该接口
        String result = "{\"State\":200,\"Message\":\"success\",\"Data\":219}";
        log.info(result);
        return result;
    }

    @Override
    public String inLocal(String hash) {
        //暂定不实现该接口
        String result = "{\"State\":200,\"Message\":\"success\",\"Data\":219}";
        log.info(result);
        return result;
    }

    @Override
    public String GetTransactionBlock(String hash) {

        Client client = new Client();
        String str = client.blockchainGetTransactionBlock_format(hash);
        System.out.println(str);
        return str;
    }
}
