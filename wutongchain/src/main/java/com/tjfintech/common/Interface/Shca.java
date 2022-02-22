package com.tjfintech.common.Interface;

import java.util.Map;

public interface Shca {

    //身份链、易居中间件接口
    String DIDlist ();
    String DIDget ();
    String DIDdelete ();
    String DIDadd (String didJson, String id);
    String VCget ();
    String VCadd (String vcId, String vcJson);
    String VCdelete ();
    String VC (String applicantDid, String vcType, Map vcSubject);

//    //DID-SERVER
//    String DIDinit (String password, String pubKey);
//    String DID (String publicKeyId, String pubKey);
//    String DIDid ();
//    //同步VC信息

//    String DIDvcsyn ();

}

