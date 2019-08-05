package com.tjfintech.common.functionTest.syncInterfaceTest;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncManageTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign =testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();

    String okCode ="200";
    String errCode="504";
    String okMessage="success";
    String errMessage="timeout";



    @Test
    public void testTimeoutForAdmin() throws Exception{
        //设置打包时间为500ms 使得各种类型的交易同时打包
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/basePkTm500ms.toml "+ PTPATH +"peer/conf/base.toml");
        setAndRestartSDK(resetSDKConfig);
        testSyncAdmin(String.valueOf(UTXOSHORTMEOUT),okCode);
    }

    //@After
    public void resetEnv()throws Exception{
        setAndRestartPeerList(resetPeerBase);
        setAndRestartSDK(resetSDKConfig);
    }

    public void testSyncAdmin(String timeout,String code)throws Exception{
        String tokenType = "FreezeToken-"+ UtilsClass.Random(6);
        log.info("issue token");
        String respon= soloSign.issueToken(PRIKEY1,tokenType,"100","单签"+ADDRESS1+"发行token "+tokenType,ADDRESS1);

        Thread.sleep(SLEEPTIME);

        //预先做删除归集地址、删除发行地址操作、解除token锁定，以便后续操作正常进行
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.recoverFrozenToken(PRIKEY1,tokenType),containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("timeout test for mg interfaces");

        //添加归集地址
        String response1= multiSign.SyncCollAddress(timeout,ADDRESS6);
        assertEquals(code,JSONObject.fromObject(response1).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure"))).getString("State"));
        //添加发行地址
        String response2= multiSign.SyncAddissueaddress(timeout,ADDRESS6);
        assertEquals(code,JSONObject.fromObject(response2).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure"))).getString("State"));
        //冻结token
        String response3=multiSign.SyncFreezeToken(timeout,tokenType);
        assertEquals(code,JSONObject.fromObject(response3).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure"))).getString("State"));

        //删除归集地址
        String response4= multiSign.SyncDelCollAddress(timeout,ADDRESS6);
        assertEquals(code,JSONObject.fromObject(response4).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure"))).getString("State"));
        //删除发行地址
        String response5= multiSign.SyncDelissueaddress(timeout,ADDRESS6);
        assertEquals(code,JSONObject.fromObject(response5).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response5).getJSONObject("Data").getString("Figure"))).getString("State"));
        //解除冻结token
        String response6=multiSign.SyncRecoverFrozenToken(timeout,tokenType);
        assertEquals(code,JSONObject.fromObject(response6).getString("State"));
        assertEquals(code,JSONObject.fromObject(store.GetTxDetail(JSONObject.fromObject(response6).getJSONObject("Data").getString("Figure"))).getString("State"));

    }

}
