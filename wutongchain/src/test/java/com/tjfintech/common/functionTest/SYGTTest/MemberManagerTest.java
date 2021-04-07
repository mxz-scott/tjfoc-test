package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.Random;

import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class MemberManagerTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();


    /**
     * 申请加入 查询成员列表 检查审批列表 审批通过 查询成员列表 检查审批列表 退出申请 检查审批列表 同意退出
     * @throws Exception
     */
    @Test
    public void TC01_MemberApply() throws Exception {

        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(false, response.contains("join"));
        assertEquals(false, response.contains("exit"));
        int joinNum = StringUtils.countOccurrencesOf(response,"\"join\"");
        int exitNum = StringUtils.countOccurrencesOf(response,"\"exit\"");

        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code1,name1,endPoint1,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(false, response.contains(code1));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code1);
        assertEquals(false, response.contains(code1));

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,"\"exit\""));

        //审批通过
        response = sygt.SSMemberJoinApprove(code1,true);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(true, response.contains(code1));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code1);
        assertEquals(true, response.contains(code1));

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,"\"exit\""));

    }
    

}
