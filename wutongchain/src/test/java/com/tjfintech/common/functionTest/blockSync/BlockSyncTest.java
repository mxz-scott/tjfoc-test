package com.tjfintech.common.functionTest.blockSync;

import com.tjfintech.common.BeforeCondition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class BlockSyncTest {

    boolean bRe=false;
    //@Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            //String newDB="newDB"+System.currentTimeMillis();
            //初始清空节点数据库及使用新的sdk数据库
            setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db ");
            setAndRestartSDK("sed -i \"s/newDB/newDB1/g\" "+ PTPATH+"sdk/conf/configNewDB.toml ","cp "+PTPATH+"sdk/conf/configNewDB.toml "+PTPATH+"sdk/conf/"+SDKConfig+".toml");
            BeforeCondition bf = new BeforeCondition();
            bf.initTest();//赋值权限999
            bf.updatePubPriKey();//更新全局pub、prikey
            bf.collAddressTest();//添加归集地址和发行地址的注册
            Thread.sleep(8000);
            bRe=true;
        }
    }


}
