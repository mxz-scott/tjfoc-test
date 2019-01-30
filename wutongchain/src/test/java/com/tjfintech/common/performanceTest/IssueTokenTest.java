package com.tjfintech.common.performanceTest;


import com.tjfintech.common.functionTest.MultiTest;
import org.junit.Test;

public class IssueTokenTest {
    MultiTest multiTest=new MultiTest();
    @Test
    public  void Issue50()throws Exception{
        for (int i=0;i<50;i++)
        multiTest.IssueToken(6, "1230");
    }
}
