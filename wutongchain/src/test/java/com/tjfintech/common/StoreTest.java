package com.tjfintech.common;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class StoreTest {
    Store store=new Store();
//    int number =1;
//        log.info("\n创建存证交易--------------------------------\n");
//    String response=CreateStore();
//    JSONObject jsonObject=JSONObject.fromObject(response);
//    String hash=jsonObject.getJSONObject("Data").get("Figure").toString();
//        log.info("\n创建带密码存证交易--------------------------------\n");
//    String responsePwd=CreateStorePwd();
//    JSONObject jsonObjectPwd=JSONObject.fromObject(responsePwd);
//    String hashPwd=jsonObjectPwd.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(1000*5);//休眠5秒
//        log.info("\n查询存证交易--------------------------------\n");
//    GetStore(hash);
//        log.info("\n获取隐私存证--------------------------------\n");
//    GetStorePost(hash);
//        log.info("\n获取带密码隐私存证--------------------------------\n");
//    GetStorePostPwd(hashPwd);
//        log.info("\n获取交易索引--------------------------------\n");
//    GetTransactionIndex(hash);
//        log.info("\n获取区块高度--------------------------------\n");
//    GetHeight();
//    // GetBlockByHash();
//        log.info("\n按高度获取区块信息--------------------------------\n");
//    GetBlockByHeight(number);
//        log.info("\n交易复杂2查询--------------------------------\n");
//    GetTxSearch(2);
//        log.info("\n交易复杂1查询--------------------------------\n");
//    GetTxSearch(1);
//        log.info("\n查询交易是否存在于钱包数据库");
//    GetInlocal(hash);
//        log.info("\n统计某种交易类型的数量");
//    GetStat();
    @Test
    public void getTransaction() {
    }

    @Test
    public void createStore() {
        String Data = "\"test\":\"json store1\"";
        String Pubkeys="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWpFZUc0Vm9ETTJkRjAxWnpGQ3NQNkxqTE9zVC8NCkg2YWx5ejBNRXRSU2krazQxbTNzOXFoUVB4UDk1OFFQdGUwS2pZa1VKeUt0MUVBV2NraEI0Wm16eUE9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
        String response=store.CreateStore(Data,Pubkeys);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));
    }

    @Test
    public void createStorePwd() {
    }

    @Test
    public void getStore() {
    }

    @Test
    public void getStorePost() {
    }

    @Test
    public void getStorePostPwd() {
    }

    @Test
    public void getTransactionIndex() {
    }

    @Test
    public void getHeight() {
    }

    @Test
    public void getBlockByHeight() {
    }

    @Test
    public void getBlockByHash() {
    }

    @Test
    public void getTxSearch() {
    }

    @Test
    public void getInlocal() {
    }

    @Test
    public void getStat() {
    }
}
