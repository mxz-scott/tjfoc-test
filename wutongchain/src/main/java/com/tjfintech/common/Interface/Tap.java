package com.tjfintech.common.Interface;

import java.util.Map;

public interface Tap {

    String tapProjectInit(long expireDate, long openDate, String publicKey, String identity, int filesize, String name, Map metaData);

    String tapProjectUpdate(String projectId, long expireDate, long openDate, Map metaData, String name, int state, int filesize, String sign);

    String tapProjectDetail(String projectId);

    String tapTenderVerify(String hashvalue, String sender);

    String tapTenderUpload(String projectId, String recordId, String fileHead, String path);

    String tapTenderRevoke(String data, String projectId);

    String tapProjectList();

    String tapTenderRecord(String projectId, String recordId, Boolean detail, String sign);

    String tapTenderOpen(String projectId, String sign);

}
