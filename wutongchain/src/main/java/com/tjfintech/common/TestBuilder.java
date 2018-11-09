package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Contract;
public class TestBuilder {
    private Store store;
    private SoloSign soloSign;
    private MultiSign multiSign;
    private Contract contract;
    TestBuilder() {
        setGoTest();
    // SetJavaTest();
    }

    public static TestBuilder getInstance() {
        return TestBuild.INSTANCE.getInstance();
    }

    public enum TestBuild {
        INSTANCE;
        private TestBuilder testBuilder;

        TestBuild() {
            testBuilder = new TestBuilder();
        }

        public TestBuilder getInstance() {
            return testBuilder;
        }
    }

    public void setGoTest() {
        store = new GoStore();
        soloSign = new GoSoloSign();
        multiSign = new GoMultiSign();
    }

    public void SetJavaTest() {
        store = new JavaStore();
        soloSign = new JavaSoloSign();
        multiSign = new JavaMultiSign();
    }

    public Store getStore() {
        return store;
    }
    public Contract getContract() {return contract;}
    public SoloSign getSoloSign() {
        return soloSign;
    }

    public MultiSign getMultiSign() {
        return multiSign;
    }
}
