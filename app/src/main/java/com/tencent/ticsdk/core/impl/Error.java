package com.tencent.ticsdk.core.impl;

/**
 * TIC 错误码及错误信息定义
 * Created by eric on 2018/5/9.
 */
public class Error {

    public final static int ERR_INVALID_PARAMS = 40001;
    public final static String ERR_MSG_INVALID_PARAMS = "invalid config params, pls check.";
    /**
     * 课堂资源不可用
     */
    public final static int ERR_CLASS_ID_NOT_AVAILABLE = 40002;
    public final static String ERR_MSG_CLASS_ID_NOT_AVAILABLE = "class resource not available.";

    public final static int ERR_COS_NOT_CONFIG = 40003;
    public final static String ERR_MSG_COS_NOT_CONFIG = "Cos not available：not found config info.";

    public final static int ERR_UPLOAD_FILE_FAILED = 40004;
    public final static String ERR_MSG_UPLOAD_FILE_FAILED = "upload file failed: ";

    public final static int ERR_NOT_IN_CLASS = 40005;
    public final static String ERR_MSG_NOT_IN_CLASS = "please join class first.";

    public final static int ERR_PERMISSION_DENIED = 40006;
    public final static String ERR_MSG_PERMISSION_DENIED = "permission deny.";

    public final static int ERR_PARSE_CLASSID_INFO_FAILED = 40007;
    public final static String ERR_MSG_PARSE_CLASSID_INFO_FAILED = "parse classid info failed.";

    public final static int ERR_SYNC_CLASS_DATA_FAILED = 40008;
    public final static String ERR_MSG_SYNC_CLASS_DATA_FAILED = "joinclassroom successfully, but sync whiteboard data failed";

    public final static int ERR_START_RECORD_FAILED = 40009;
    public final static String ERR_MSG_START_RECORD_FAILED = "joinclassroom successfully, but startRecord failed";

    public final static int ERR_SYNC_RECORD_TIME_FAILED = 40010;
    public final static String ERR_MSG_SYNC_RECORD_TIME_FAILED = "joinclassroom successfully, but startRecord failed";

    public final static int ERR_NOT_SUPPORT_OPERATION = 40011;
    public final static String ERR_MSG_NOT_SUPPORT_OPERATION = "not support operation";

    public final static int ERR_HTTP_ERROR = 40012;
    public final static String ERR_MSG_HTTP_ERROR = "http exception happend";

}
