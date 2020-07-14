package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncMultiSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    UtilsClass utilsClass=new UtilsClass();

    /**
     * 同步多签token发行申请(1/2签名)
     * 同步签名多签交易
     */
    @Test
    public void SyncMutiIssueToken() throws InterruptedException {


    }

    /**
     * 同步多签转账(1/2签名)
     */
    @Test
    public void SyncMutiTransfer() throws InterruptedException {

    }

}
