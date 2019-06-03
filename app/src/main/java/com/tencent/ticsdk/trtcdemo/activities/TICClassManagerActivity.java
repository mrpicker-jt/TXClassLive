package com.tencent.ticsdk.trtcdemo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.teduboard.TEduBoardController;
import com.tencent.ticsdk.core.TICClassroomOption;
import com.tencent.ticsdk.core.TICManager;
import com.tencent.ticsdk.trtcdemo.R;
import com.tencent.ticsdk.trtcdemo.TICSDKDemoApp;


import java.util.Locale;
import java.util.Random;


public class TICClassManagerActivity extends BaseActvity {

    // 课堂id
    EditText etRoomIdInput;
    TextView tvClassInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_layout);

        //用户信息
        mUserID = getIntent().getStringExtra(USER_ID);
        mUserSig = getIntent().getStringExtra(USER_SIG);
        mUserRole = getIntent().getIntExtra(USER_ROLE, TICClassroomOption.ROLE_STUDENT);
        mRoomId = resumeFromCache();

        tvLog = (TextView) findViewById(R.id.tv_manager_log);
        etRoomIdInput = (EditText) findViewById(R.id.et_roomid);
        tvClassInfo = (TextView) findViewById(R.id.tv_classInfo);

        etRoomIdInput.setText(String.valueOf(mRoomId));
        tvClassInfo.setText(String.format(Locale.getDefault(), "当前身份： %s", (mUserRole == TICClassroomOption.ROLE_TEACHER)  ? "老师" : "学生"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeToCache(mRoomId);
    }

    public void writeToCache(int roomId) {
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(USER_ROOM, roomId);
        editor.commit();
    }

    public int resumeFromCache() {
        Random ran = new Random(System.currentTimeMillis());
        int randRoom = ran.nextInt(1000); //课堂号假设小于1000
        SharedPreferences sharedPreferences = getSharedPreferences(TAG, 0);
        int roomId = sharedPreferences.getInt(USER_ROOM, randRoom);
        return roomId;
    }

    /**
     * 创建课堂
     */
    public void onCreateClsssroomClick(View v) {

        String inputString = etRoomIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(inputString) || !TextUtils.isDigitsOnly(inputString)) {
            postToast("创建课堂失败, 房间号为空或者非数字:"  + inputString, true);
            return;
        }

        mRoomId = Integer.valueOf(inputString);
        mTicManager.createClassroom(mRoomId, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("创建课堂 成功, 房间号："  + mRoomId, true);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (errCode == 10021) {
                    postToast("该课堂已被他人创建，请\"加入课堂\"", true);
                }
                else if (errCode == 10025) {
                    postToast("该课堂已创建，请\"加入课堂\"", true);
                }
                else {
                    postToast("创建课堂失败, 房间号：" + mRoomId + " err:" + errCode + " msg:" + errMsg, true);
                }

            }
        });
    }

    public void onDestroyClassroomClick(View v) {

        String roomInputId = etRoomIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(roomInputId) || !TextUtils.isDigitsOnly(roomInputId)) {
            postToast("请检查账号信息是否正确");
            return;
        }
        mRoomId = Integer.valueOf(roomInputId);

        mTicManager.destroyClassroom(mRoomId, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object o) {
                postToast("销毁课堂成功: " + mRoomId);

                TEduBoardController board = mTicManager.getBoard();
                if (board != null)
                    board.reset();
            }

            @Override
            public void onError(String s, int errCode, String errMsg) {
                postToast("销毁课堂失败: " + mRoomId + " err:" + errCode + " msg:" + errMsg);
            }
        });
    }

    /**
     * 进入课堂
     */
    public void onJoinClsssroomClick(View v) {

        String roomInputId = etRoomIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(roomInputId) || !TextUtils.isDigitsOnly(roomInputId)) {
            postToast("创建课堂失败, 房间号为空或者非数字:"  + roomInputId, true);
            return;
        }
        mRoomId = Integer.valueOf(roomInputId);
        launcherClassroomLiveActivity();
        postToast("正在进入课堂，请稍等。。。", true);
    }

    private void launcherClassroomLiveActivity() {
        Intent intent = new Intent(this, TICClassMainActivity.class);
        intent.putExtra(USER_ID, mUserID);
        intent.putExtra(USER_SIG, mUserSig);
        intent.putExtra(USER_ROLE, mUserRole);
        intent.putExtra(USER_ROOM, mRoomId);
        startActivity(intent);
    }
}
