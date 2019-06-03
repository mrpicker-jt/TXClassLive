package com.tencent.ticsdk.core.impl.utils;

import com.tencent.imsdk.TIMMessage;

import java.math.BigInteger;

public class TimeUtil {

    private final static String TAG = "TimeUtil";

    public static long getAVSDKTimeStamp() {
        //version 1;
        long currentTime = System.currentTimeMillis();
        return currentTime & 0xFFFFFFFF;
    }

    public static long getAVSDKTimestampV2() {
        // version 2:
        long millSec = System.currentTimeMillis();
        long millSecInt = (int) millSec & 0x00000000FFFFFFFF;

        BigInteger unsignedNum = BigInteger.valueOf(millSecInt);
        if (millSecInt < 0) {
            unsignedNum = unsignedNum.add(BigInteger.ZERO.flipBit(32));
        }
        long result = unsignedNum.longValue();
        return result;
    }

    public static long getServerTimestamp() {
        TIMMessage message = new TIMMessage();
        return message.timestamp();
    }
}
