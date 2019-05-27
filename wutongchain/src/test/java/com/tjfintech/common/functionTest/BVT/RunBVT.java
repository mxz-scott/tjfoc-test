package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.functionTest.dockerContract.ContractTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTestInvalid;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StoreTest.class,
        MultiTest.class,
        SoloTest.class,
//        ContractTest.class

})

//Build Validation Test
public class RunBVT {
    //执行这个类将执行suiteClass中的测试项
}
