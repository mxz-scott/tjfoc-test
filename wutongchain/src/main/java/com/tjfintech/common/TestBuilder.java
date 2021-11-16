package com.tjfintech.common;

import com.tjfintech.common.Interface.*;

public class TestBuilder {
    private Store store;
    private SoloSign soloSign;
    private MultiSign multiSign;
    private Contract contract;
    private Token token;
    private GuDeng guDeng;
    private Kms kms;
    private Scf scf;
    private Jml jml;
    private Credit credit;
    private SYGT sygt;
    private Tap tap;
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
        guDeng = new GoGuDeng();
        credit = new GoCredit();
        kms = new GoKms();
        scf = new GoScf();
        sygt = new GoSYGT();
        jml = new GoJml();
        tap = new GoTap();
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
        return guDeng;
    }
    public Kms getKms() {return kms;}
    public Scf getScf() {return scf;}
    public Jml getJml() {return jml;}
    public Credit getCredit() {
        return credit;
    }
    public SYGT getSygt() {
        return sygt;
    }
    public Tap getTap() {
        return tap;
    }
}
