package com.tjfintech.common.functionTest.store;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    public String hash;
    public String privacyhash;


    /**
     * 创建存证交易
     */
    @Before
    public void TC1380_store() throws InterruptedException {
        String createstore;
        createstore = store.CreateStore("创建普通存证");
        Thread.sleep(SLEEPTIME);
        hash = JSONObject.fromObject(createstore).getJSONObject("Data").getString("Figure");
        log.info("获取普通存证交易hash"+hash);

        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        createstore = store.CreatePrivateStore("创建隐私存证",map);
        Thread.sleep(SLEEPTIME);
        privacyhash = JSONObject.fromObject(createstore).getJSONObject("Data").getString("Figure");
        log.info("获取隐私存证交易hash"+privacyhash);
    }

    /**
     * 获取交易详情
     */
    @Test
    public void TC1370_gettxdetail(){
        String gettxdetail;
        gettxdetail = store.GetTxDetail("");
        assertThat(gettxdetail,containsString("Invalid parameter"));//hashData字段为空值
        gettxdetail = store.GetTxDetail("111");
        assertThat(gettxdetail,containsString("hashData must be url encode string"));//hashData为非法字符
        log.info("通过hash获取交易详情"+hash);
        gettxdetail = store.GetTxDetail(hash);
        assertThat(gettxdetail,containsString("200"));//hashData字段不为URL encode编码
    }


    /**
     *查询存证交易
     */
    @Test
    public void TC1382_getStore(){
        String getStore;
        getStore = store.GetStore("");
        assertThat(getStore,containsString("Invalid parameter"));//hash字段为空值
        getStore = store.GetStore("Invalid parameter");
        assertThat(getStore,containsString("Invalid parameter"));//hash字段为非法字符
        getStore = store.GetStore(hash);
        assertThat(getStore,containsString("200"));//hash字段不为urlEncode编码
    }
    /**
     * 获取隐私存证
     */
    @Test
    public void TC1396_getStore(){
        String GetStorePostPwd;
        GetStorePostPwd = store.GetStorePostPwd(privacyhash, UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("200"));
        GetStorePostPwd = store.GetStorePostPwd("", UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("Invalid parameter"));//Hash字段为空值
        GetStorePostPwd = store.GetStorePostPwd("111", UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("Hash must be base64 string"));//Hash字段为非法字符
        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "", "111");
        assertThat(GetStorePostPwd,containsString("200"));//Prikey字段为空值
        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "111", "111");
        assertThat(GetStorePostPwd,containsString("illegal base64 data at input byte 0"));//Prikey字段为非base64格式的字符
        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "YWJjeHg=", "111");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//Prikey字段为无效的base64格式的字符
        GetStorePostPwd = store.GetStorePostPwd(privacyhash,  UtilsClass.PRIKEY6, "");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//KeyPwd字段为空
        GetStorePostPwd = store.GetStorePostPwd(privacyhash,  UtilsClass.PRIKEY6, "22333");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//KeyPwd字段为非法字符
    }

    /**
     * 获取交易索引
     */
    @Test
    public void TC1403_gettransactionindex(){
        String gettransactionindex;
        gettransactionindex = store.GetTransactionIndex(privacyhash);
        assertThat(gettransactionindex,containsString("200"));
        gettransactionindex = store.GetTransactionIndex("");
        assertThat(gettransactionindex,containsString("Invalid parameter"));
        gettransactionindex = store.GetTransactionIndex("11");
        assertThat(gettransactionindex,containsString("hashData must be url encode string"));

    }
    /**
     * 获取区块高度
     */
    @Test
    public void TC1406_gettransactionblock(){
        String gettransactionblock;
        gettransactionblock = store.GetTransactionBlock(hash);
        assertThat(gettransactionblock,containsString("200"));
        gettransactionblock = store.GetTransactionBlock("");
        assertThat(gettransactionblock,containsString("Invalid parameter"));
        gettransactionblock = store.GetTransactionBlock("11");
        assertThat(gettransactionblock,containsString("hashData must be url encode string"));

    }

    /**
     * 按高度获取区块时
     */
    @Test
    public void TC1407_getblockbyheight(){
        String getblockbyheight;
        getblockbyheight = store.GetBlockByHeight(4);
        assertThat(getblockbyheight,containsString("200"));
        getblockbyheight = store.GetBlockByHeight(-12);
//        assertThat(getblockbyheight,containsString("rpc error: code = Unknown desc = BlockchainGetBlockByHeight: failed to find block:1234567844"));
    }


    /**
     * Tc16 发送存证交易，data为空字符串
     * 预期返回400，提示空data
     */
    @Test
    public void TC16_CreateStoreNull(){
        String data = "";
        String  response = store.CreateStore(data);
        String message= JSONObject.fromObject(response).getString("Message");
        assertThat(message,equalTo("Data is mandatory"));
        assertEquals(response.contains("400"),true);
    }

    /**
     * TC17创建两笔数据一样的存证
     * 预期：两者返回相同哈希，返回500 提示重复存证
     * @throws Exception
     */
    @Test
    public void TC17_CreateStoreDouble()throws Exception{
        String data="cxTest-"+ UtilsClass.Random(2);
        String response= store.CreateStore(data);
        Thread.sleep(1*1000);
        String response2= store.CreateStore(data);
        Thread.sleep(SLEEPTIME);
        String hash1=JSONObject.fromObject(response).getJSONObject("Data").getString("Figure");
        String hash2=JSONObject.fromObject(response2).getString("Message");
        assertThat(response, CoreMatchers.containsString("200"));
        assertThat(response2, CoreMatchers.containsString("500"));
        assertThat(hash2, CoreMatchers.containsString("Duplicate transaction"));
        assertThat(hash2, CoreMatchers.containsString(hash1));
    }
}
