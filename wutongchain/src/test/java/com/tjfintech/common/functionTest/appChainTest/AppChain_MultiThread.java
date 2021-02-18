package com.tjfintech.common.functionTest.appChainTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.ids;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_MultiThread extends Thread{

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();

    public String ledgerID = "";

    @Override
    public void run() {
        subLedger = ledgerID;
//        for(int i=0;i<20;i++) {
            String Data = "1521 ledger tx store " + sdf.format(dt) + RandomUtils.nextInt(100000);
            store.CreateStore(Data);
//        }
    }

}
