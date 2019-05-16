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

    //@Test
    public void testShortTimeoutForAdmin() throws Exception{
        //设置打包时间为500ms 使得各种类型的交易同时打包
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/basePkTm500ms.toml "+ PTPATH +"peer/conf/base.toml");
        //testSyncAdmin(String.valueOf(SHORTTIMEOUT),errCode,errMessage);
        testSyncAdmin("1000",errCode,errMessage);
    }

    @Test
    public void testLongTimeoutForAdmin() throws Exception{
        //设置打包时间为500ms 使得各种类型的交易同时打包
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/basePkTm500ms.toml "+ PTPATH +"peer/conf/base.toml");
        //testSyncAdmin(String.valueOf(LONGTIMEOUT),okCode,okMessage);
        testSyncAdmin("2000",okCode,okMessage);
    }

    @After
    public void resetEnv()throws Exception{
        setAndRestartPeerList("cp "+ PTPATH + "peer/conf/baseOK.toml "+ PTPATH +"peer/conf/base.toml");
    }

    public void testSyncAdmin(String timeout,String code,String message)throws Exception{
        String tokenType = "FreezeToken-"+ UtilsClass.Random(6);
        log.info("issue token");
        String respon= soloSign.issueToken(PRIKEY1,tokenType,"100","单签"+ADDRESS1+"发行token "+tokenType,ADDRESS1);

        //预先做删除归集地址、删除发行地址操作、解除token锁定，以便后续操作正常进行
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.recoverFrozenToken(PRIKEY1,tokenType),containsString("200"));
        Thread.sleep(6000);
        log.info("timeout test for mg interfaces");

        String response1= multiSign.SyncCollAddress(timeout,ADDRESS6);
        String response2= multiSign.SyncAddissueaddress(timeout,ADDRESS6);
        String response3=multiSign.SyncFreezeToken(PRIKEY1,timeout,tokenType);
        Thread.sleep(2000);

        //删除归集地址
        String response4= multiSign.SyncDelCollAddress(timeout,ADDRESS6);
        //删除发行地址
        String response5= multiSign.SyncDelissueaddress(timeout,ADDRESS6);
        //解除冻结token
        String response6=multiSign.SyncRecoverFrozenToken(PRIKEY1,timeout,tokenType);
        Thread.sleep(3000);

        assertEquals(code,JSONObject.fromObject(response1).getString("State"));
        assertEquals(code,JSONObject.fromObject(response2).getString("State"));
        assertEquals(code,JSONObject.fromObject(response3).getString("State"));
        assertEquals(message,JSONObject.fromObject(response1).getString("Message"));
        assertEquals(message,JSONObject.fromObject(response2).getString("Message"));
        assertEquals(message,JSONObject.fromObject(response3).getString("Message"));

        assertEquals(code,JSONObject.fromObject(response4).getString("State"));
        assertEquals(code,JSONObject.fromObject(response5).getString("State"));
        assertEquals(code,JSONObject.fromObject(response6).getString("State"));
        assertEquals(message,JSONObject.fromObject(response4).getString("Message"));
        assertEquals(message,JSONObject.fromObject(response5).getString("Message"));
        assertEquals(message,JSONObject.fromObject(response6).getString("Message"));



    }

}
