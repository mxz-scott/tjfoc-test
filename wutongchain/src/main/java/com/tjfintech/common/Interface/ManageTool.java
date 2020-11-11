package com.tjfintech.common.Interface;

public interface ManageTool {
    String setPeerPerm(String netPeerIP,String sdkID,String permStr,String...ShowName)throws Exception;
    String getPeerPerm(String netPeerIP,String sdkID)throws Exception;
    String getID(String queryIP,String keyPath,String cryptType)throws Exception;
    String addPeer(String addPeerType,String queryIPPort,String joinPeerIP,String joinTcpPort,String joinRpcPort)throws Exception;
    String quitPeer(String queryIPPort,String peerIP)throws Exception;
    String queryPeerUnconfirmedTx(String queryIPPort)throws Exception;
    String queryBlockHeight(String queryIPPort)throws Exception;
    String queryBlockByHeight(String queryIPPort,String height)throws Exception;
    String sendNewTx(String queryIPPort,String txNo,String txType)throws Exception;
    String checkPeerHealth(String queryIPPort)throws Exception;
    String getPeerSimpleInfo(String queryIPPort)throws Exception;
    String getPeerDetails(String queryIPPort)throws Exception;
    String queryMemberList(String queryIPPort) throws Exception;

    //证书相关
    String genLicence(String shellIP,String macAddr,String ipAddr,String validPeriod,String maxPeerNo,String version)throws Exception;
    String deLicence(String shellIP,String licPath)throws Exception;

    //子链相关
    String createSubChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                 String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception;
    String createSubChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                       String firstBlockInfoParam,String consensusParam,String peeridsParam)throws Exception;
    String getSubChain(String shellIP,String rpcPort,String chainNameParam);
    String freezeSubChain(String shellIP,String rpcPort,String chainNameParam);
    String recoverSubChain(String shellIP,String rpcPort,String chainNameParam);
    String destroySubChain(String shellIP,String rpcPort,String chainNameParam);

    //应用链相关
    String createAppChain(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                          String firstBlockInfoParam,String consensusParam,String peeridsParam,String outPeerList)throws Exception;
    String createAppChainNoPerm(String shellIP,String rpcPort,String chainNameParam,String hashTypeParam,
                                String firstBlockInfoParam,String consensusParam,String peeridsParam,String outPeerList)throws Exception;
    String getAppChain(String shellIP,String rpcPort,String chainNameParam);
    String freezeAppChain(String shellIP,String rpcPort,String chainNameParam);
    String recoverAppChain(String shellIP,String rpcPort,String chainNameParam);
    String destroyAppChain(String shellIP,String rpcPort,String chainNameParam);
}
