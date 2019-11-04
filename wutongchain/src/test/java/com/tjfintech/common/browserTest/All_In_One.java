package com.tjfintech.common.browserTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,
        BeforeCondition.class,  //区块高度1，1笔存证交易（创世区块），1笔系统交易（权限变更），共2笔交易。

        StoreTest.class,    //区块高度7，8笔存证交易，1笔系统交易，共9笔交易。
        PrivateStoreTest.class, //区块高度12，11笔存证交易，1笔系统交易，2笔WVM合约交易，共14笔交易。
        MultiTest.class,    //区块高度19，11笔存证交易，17笔系统交易，2笔WVM合约交易，5笔UTXO交易，共35笔交易。
        SoloTest.class  //区块高度28，11笔存证交易，17笔系统交易，2笔WVM合约交易，18笔UTXO交易，共48笔交易。

        //区块高度28,交易总数48，UTXO转账笔数28，UTXO地址总数8
        //UTXO交易数18，存证交易数11，合约交易数2，系统交易数17
        //单签UTXO最大层数5和6
        //多签UTXO最大层数1和3
})

//Build Validation Test
public class All_In_One {
    //执行这个类将执行suiteClass中的测试项

}
