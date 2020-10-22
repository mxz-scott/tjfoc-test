package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.MULITADD1;

@Slf4j
public class SetCertMIX1 {

   @Test
    public void test(){
        UtilsClass.certPath =  "cert/" + "MIX1";
        MULITADD1 =""; //utxo测试时是否执行更新key以及重新生成地址的判断条件
    }

}
