package com.tjfintech.common;

import com.tjfintech.common.Interface.*;

public class TestBuilder {
    private Store store;
    private SoloSign soloSign;
    private MultiSign multiSign;
    private Contract contract;
    private Token token;
    private GuDengV1 guDengV1;
    private GuDeng guDeng;
    private Credit credit;
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
        guDengV1 = new GoGuDengV1();
        guDeng = new GoGuDeng();
        credit = new GoCredit();
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
    public GuDengV1 getGuDengV1() {
        return guDengV1;
    }
    public GuDeng getGuDeng() {
        return guDeng;
    }
    public Credit getCredit() {
        return credit;
    }
}
