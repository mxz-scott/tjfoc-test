package com.tjfintech.common;

import com.tjfintech.common.Interface.*;

public class TestBuilder {
    private Store store;
    private SoloSign soloSign;
    private MultiSign multiSign;
    private Contract contract;
    private Token token;
    private GuDeng gudeng;
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
        contract=new GoContract();
        token = new GoToken();
        gudeng = new GoGuDeng();

    }

//    public void SetJavaTest() {
//        store = new JavaStore();
//        soloSign = new JavaSoloSign();
//        multiSign = new JavaMultiSign();
//    }

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
    public Token getToken() {
        return token;
    }
    public GuDeng getGuDeng() {
        return gudeng;
    }

}
