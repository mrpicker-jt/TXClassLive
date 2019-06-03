package com.tencent.ticsdk.core.impl;

import android.support.annotation.NonNull;

public class UserInfo {

    private String userId = "";
    private String userSig = "";

    public UserInfo() {
    }

    public UserInfo(String userId, String userSig) {
        this.userId = userId;
        this.userSig = userSig;
    }

    public void setUserInfo(@NonNull final String userId, @NonNull final String userSig) {
        this.userId = userId;
        this.userSig = userSig;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserSig() {
        return userSig;
    }
}
