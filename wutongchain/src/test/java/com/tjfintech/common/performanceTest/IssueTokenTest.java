package com.tjfintech.common.performanceTest;


import com.tjfintech.common.functionTest.MultiSignTest;
import org.junit.Test;

public class IssueTokenTest {
    MultiSignTest multiTest=new MultiSignTest();
    @Test
    public  void Issue50()throws Exception{
        for (int i=0;i<50;i++)
        multiTest.IssueToken(6, "1230");
    }
}
