package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.MULITADD1;

@Slf4j
public class SetCertSM2 {

   @Test
    public void test(){
        UtilsClass.certPath = "SM2";
        MULITADD1 =""; //utxo测试时是否执行更新key以及重新生成地址的判断条件
    }

}
