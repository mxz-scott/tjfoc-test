package com.tjfintech.common.Interface;

import java.util.Map;

public interface Tap {

    String tapProjectInit(String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE,
                          String KAIBIAODATE, String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST,
                          int TBALLOWFILESIZE, String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String ZBRPULICKEY, String BID_SECTION_CODE_EX, Map EXTRA);

    String tapProjectUpdate(String ORDERNO, String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE,
                            String KAIBIAODATE, String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST,
                            int TBALLOWFILESIZE, String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String BID_SECTION_CODE_EX, Map EXTRA);

    String tapProjectDetail(String orderNo);

    String tapTenderVerify(String orderNo, String unitName, String isDownLoad, String isZhiFu, String caType, String userIdentifier_B,
                           String userIdentifier_C, String userIdentifier, String useZBFileGuid, String biaoDuanNo, String sender);

    String tapTenderUpload(String orderNo, String uid, String recordId, String fileHead, String path, int uploadTime);

    String tapTenderRevoke(String data, String orderNo);

    String tapProjectList();

    String tapTenderBack(String uid);

    String tapTenderRecord(String orderNo, String recordId, Boolean detail);

    String tapTenderOpen(String orderNo);

}
