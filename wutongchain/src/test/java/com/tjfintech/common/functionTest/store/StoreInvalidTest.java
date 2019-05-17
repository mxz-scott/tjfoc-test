package com.tjfintech.common.functionTest.store;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreInvalidTest {
    public String createstore1;
    public String createstore2;
    public String figure1; //获取创建普通存证时获取的交易hash
    public String figure2; //获取创建普通存证时获取的交易hash
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();

    /**
     * 创建存证交易
     */
    @Before
    public void TC1380_store(){
        log.info("创建存证交易");
        createstore1 = store.CreateStore("创建普通存证");
        assertThat(createstore1,containsString("200"));//Data字段为空
        log.info("创建隐私存证");
        createstore2 = store.CreateStore("创建隐私存证",UtilsClass.PUBKEY6);
        assertThat(createstore2,containsString("200"));//Data字段为空
        log.info("获取创建存证的交易hash");
        figure1 =JSONObject.fromObject(createstore1).getJSONObject("Data").getString("Figure");
        log.info("普通存证的交易hash"+figure1);
        figure2 =JSONObject.fromObject(createstore2).getJSONObject("Data").getString("Figure");
        log.info("隐私存证的交易hash"+figure2);
    }

    /**
     * 获取交易详情
     */
    @Test
    public void TC1370_gettxdetail(){
        String gettxdetail;
//        log.info("获取交易详情_hashData字段为空值");
//        gettxdetail = store.Gettxdetail("");
//        assertThat(gettxdetail,containsString("Invalid parameter"));
//        log.info("获取交易详情_hashData为非法字符");
//        gettxdetail = store.Gettxdetail("111");
//        assertThat(gettxdetail,containsString("hashData must be url encode string"));
//        log.info(figure1);
        gettxdetail = store.GetTxDetail(figure1);

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
        getStore = store.GetStore(figure1);
//        assertThat(getStore,containsString("200"));//hash字段不为urlEncode编码
    }
    /**
     * 获取隐私存证
     */
    @Test
    public void TC1396_getStore(){
        String GetStorePostPwd;
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=", UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("200"));
        GetStorePostPwd = store.GetStorePostPwd("", UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("Invalid parameter"));//Hash字段为空值
        GetStorePostPwd = store.GetStorePostPwd("111", UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("Hash must be base64 string"));//Hash字段为非法字符
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=", "", "111");
        assertThat(GetStorePostPwd,containsString("200"));//Prikey字段为空值
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=", "111", "111");
        assertThat(GetStorePostPwd,containsString("illegal base64 data at input byte 0"));//Prikey字段为非base64格式的字符
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=", "YWJjeHg=", "111");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//Prikey字段为无效的base64格式的字符
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=",  UtilsClass.PRIKEY6, "");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//KeyPwd字段为空
        GetStorePostPwd = store.GetStorePostPwd("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=",  UtilsClass.PRIKEY6, "22333");
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));//KeyPwd字段为非法字符
    }

    /**
     * 获取交易索引
     */
    @Test
    public void TC1403_gettransactionindex(){
        String gettransactionindex;
        gettransactionindex = store.GetTransactionIndex("LU2tocnnvUFXNcvYdPs84M5p7FhoduejfU5Ha/uj0jE=");
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
        gettransactionblock = store.GetTransactionBlock("3nq6aRvklIrbLhBRKBOLyq2mhByiP4FHDImshQMQXfE%253d%255c");   //使用Postman访问该接口时不管是否使用URLcode都可以访问写代码时只有使用非URLencode编码时才可以访问
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
}
