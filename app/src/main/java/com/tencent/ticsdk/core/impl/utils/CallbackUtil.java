package com.tencent.ticsdk.core.impl.utils;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.ticsdk.core.TICManager;
import com.tencent.ticsdk.core.impl.TICProgressCallback;

public class CallbackUtil {

    public static void notifySuccess(TICManager.TICCallback callBack, Object data) {
        if (null != callBack) {
            callBack.onSuccess(data);
        }
    }

    public static void notifyError(TICManager.TICCallback callBack, String module, int errCode, String errMsg) {
        if (null != callBack) {
            callBack.onError(module, errCode, errMsg);
        }
        TXCLog.e(module, errMsg);
    }

    public static void notifySuccess(TICProgressCallback callBack, Object data) {
        if (null != callBack) {
            callBack.onSuccess(data);
        }
    }

    public static void notifyProgress(TICProgressCallback callBack, int percent) {
        if (null != callBack) {
            callBack.onPrgress(percent);
        }
    }

    public static void notifyError(TICProgressCallback callBack, String module, int errCode, String errMsg) {
        if (null != callBack) {
            callBack.onError(module, errCode, errMsg);
        }
    }
}
