package com.tjfintech.common;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
public class StoreTest1018 {
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
    public void TC010_CreatePrivateStoreDataIsJson() {
    String pubKey1 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFZnkrUkFjMU02bnpJaUJxSmE4enRtOW41aFQwTApGeFRXMkhkMWRWdkVseWNHcndVQ2Y4ZmRGeWJiMDJuNmt5d1hTbGFqVjc5dXZoSjF6N3E0Yk9FdjBBPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    String pubKey2 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFMzk1S2llUmJ5a2lyaFZXVk5vT0JBTlZvYmpDRwpYWFd1TTl1WTRUNmNia2kxdExwbXJrWTM4aUkzNEZiRnFpSTdSUTlqWVNqZjF6MisrUUhUUndaNzZRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";

    //String a = store.CreateStore();


    }

}
