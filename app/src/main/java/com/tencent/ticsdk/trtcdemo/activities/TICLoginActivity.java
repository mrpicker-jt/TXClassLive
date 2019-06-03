package com.tencent.ticsdk.trtcdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tencent.ticsdk.core.TICManager;
import com.tencent.ticsdk.trtcdemo.Constants;
import com.tencent.ticsdk.trtcdemo.R;
import com.tencent.ticsdk.trtcdemo.TRTCGetUserIDAndUserSig;

import java.util.ArrayList;

public class TICLoginActivity extends BaseActvity implements CompoundButton.OnCheckedChangeListener {
    private TRTCGetUserIDAndUserSig mUserInfoLoader;

    ToggleButton tbRole;
    Spinner spinnerAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserRole = Constants.Role_Student;

        setContentView(R.layout.activity_login_layout);
        tvLog = (TextView) findViewById(R.id.tv_login_log);

        tbRole = (ToggleButton) findViewById(R.id.tb_role);
        tbRole.setOnCheckedChangeListener(this);

        spinnerAccount = (Spinner) findViewById(R.id.spinner_account);

        // 如果配置有config文件，则从config文件中选择userId
        mUserInfoLoader = new TRTCGetUserIDAndUserSig(this);
        final ArrayList<String> userIds = mUserInfoLoader.getUserIdFromConfig();
        final ArrayList<String> userSigs = mUserInfoLoader.getUserSigFromConfig();

        if (userIds != null && userIds.size() > 0) {
            ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userIds);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerAccount.setAdapter(spinnerAdapter);
            spinnerAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mUserID = userIds.get(position);
                    mUserSig = userSigs.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            onLogoutClick(null);
        }
    }

    /**
     * 登录
     */
    public void onLoginClick(View v) {
        // 默认走的是云上环境
        if (TextUtils.isEmpty(mUserID) || TextUtils.isEmpty(mUserSig)) {
            postToast("请检查账号信息是否正确");
            return;
        }

        mTicManager.login(mUserID, mUserSig, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast(mUserID + ":登录成功" , true);
                launchClassManagerActivity();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast(mUserID+ ":登录失败, err:" + errCode + "  msg: " + errMsg);
            }
        });
    }

    public void onLogoutClick(View v) {

        mTicManager.logout(new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast(mUserID + ":登出成功" , true);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("登出失败, err:" + errCode + " msg: " + errMsg);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {

            case R.id.tb_role:
                mUserRole = isChecked ? Constants.Role_Teather :  Constants.Role_Student;
                break;
        }
    }

    private void launchClassManagerActivity() {
        Intent intent = new Intent(this, TICClassManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(TICClassManagerActivity.USER_ID, mUserID);
        intent.putExtra(TICClassManagerActivity.USER_SIG, mUserSig);
        intent.putExtra(TICClassManagerActivity.USER_ROLE, mUserRole);
        startActivity(intent);
    }

}
