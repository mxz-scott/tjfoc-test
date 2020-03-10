package com.tjfintech.common.functionTest.PermissionTest;

import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;

import java.util.LinkedList;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

//import static org.hamcrest.Matchers.containsString;

@Slf4j
public class APermfuncSysSetMg {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Contract contract=testBuilder.getContract();
    MgToolCmd mgToolCmd = new MgToolCmd();

    String okMsg="send transaction success";
    String errMsg="does not found Permission";
    String category="wvm";

    String subLedgerName = "";


    public String retAllow(String checkStr)throws Exception{
        String allow="*";
        if(checkStr.contains(okMsg)) {
            allow = "1";
        }
        else if(checkStr.contains(errMsg))
        {
            allow="0";
        }
        return allow;
    }

    public String subLedgerCreate() throws Exception {
        subLedgerName = "permO.l_"+sdf.format(dt).substring(4)+ RandomUtils.nextInt(1000);//尽量将子链名称构造复杂一些
        String response = mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerName,
                " -t sm3", " -w first", " -c raft", ids);
        return retAllow(response);
    }


    public String subLedgerFreeze(String chainName) throws Exception {
        String res = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z " + chainName);
        return retAllow(res);
    }

    public String subLedgerRecover(String chainName) throws Exception {
        String res = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z " + chainName);
        return retAllow(res);
    }

    public String subLedgerDestroy(String chainName) throws Exception {
        String res = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z " + chainName);
        return retAllow(res);
    }

    public String addPeerJoin(String peerIPPort,String addPeerIP,String addPeerPort,String addPeerRpcPort)throws Exception{
        String res = mgToolCmd.addPeer("join",peerIPPort,"/ip4/"+addPeerIP,"/tcp/" + addPeerPort,addPeerRpcPort);
        return retAllow(res);
    }

    public String addPeerObserver(String peerIPPort,String addPeerIP,String addPeerPort,String addPeerRpcPort)throws Exception{
        String res = mgToolCmd.addPeer("observer",peerIPPort,"/ip4/"+addPeerIP,"/tcp/" + addPeerPort,addPeerRpcPort);
        return retAllow(res);
    }

    public String quitPeer(String peer1IPPort,String quitPeer)throws Exception{
        String res = mgToolCmd.quitPeer(peer1IPPort,quitPeer);
        return retAllow(res);
    }
}
