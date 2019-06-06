package com.tencent.ticsdk.trtcdemo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMMessage;
import com.tencent.rtmp.TXLog;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.teduboard.TEduBoardController;
import com.tencent.ticsdk.core.TICClassroomOption;
import com.tencent.ticsdk.core.TICManager;
import com.tencent.ticsdk.trtcdemo.Constants;
import com.tencent.ticsdk.trtcdemo.R;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TICClassMainLandScopeActivity extends BaseActvity
        implements View.OnClickListener,
        TICManager.TICMessageListener,
        TICManager.TICEventListener {
    private final static String TAG = "TICClassMainActivity";
    private static final int CROP_CHOOSE = 10;

    TICMenuDialog moreDlg;
    MySettingCallback mySettingCallback;
    boolean mEnableAudio = true;
    boolean mEnableCamera = true;
    boolean mEnableFrontCamera = true;
    boolean mCanRedo = false;
    boolean mCanUndo = false;

    /**
     * 白板视图控件
     */
    FrameLayout mBoardContainer;
    TEduBoardController mBoard;
    MyBoardCallback mBoardCallback;
    boolean mHistroyDataSyncCompleted = false;
    //trtc
    TRTCCloud mTrtcCloud;

    // 实时音视频视图控件
    TICVideoLandScopeView mTrtcRootView;

    //Main VideoView
    TXCloudVideoView mainVideoView;

    // 消息输入
    EditText etMessageInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_class_ex_landscope);

        //1、获取用户信息
        mUserID = getIntent().getStringExtra(USER_ID);
        mUserSig = getIntent().getStringExtra(USER_SIG);
        mUserRole = getIntent().getIntExtra(USER_ROLE, TICClassroomOption.ROLE_STUDENT);
        mRoomId = getIntent().getIntExtra(USER_ROOM, 0);

        //检查权限
        checkCameraAndMicPermission();

        //2.白板
        initTEduBoard();

        //3、初始化View
        initView();

        initTrtc();

        joinClass();

        mTicManager.addIMMessageListener(this);
        mTicManager.addEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unInitTrtc();
        unitTEduBoard();

        mTicManager.removeIMMessageListener(this);
        mTicManager.removeEventListener(this);
    }

    private void initView() {
        //Title
        findViewById(R.id.tv_double_room_back_button).setOnClickListener(this);
        findViewById(R.id.tv_memu).setOnClickListener(this);
        ((TextView) findViewById(R.id.tv_room_id)).setText(String.valueOf(mRoomId));

        mainVideoView = findViewById(R.id.main_videoView);

        tvLog = (TextView) findViewById(R.id.tv_log);
        etMessageInput = (EditText) findViewById(R.id.et_message_input);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        //发送消息
        findViewById(R.id.btn_send).setOnClickListener(this);
    }

    //---------trtc--------------

    private void initTrtc() {
        //1、获取trtc
        mTrtcCloud = mTicManager.getTRTCClound();

        //2、TRTC View
        mTrtcRootView = (TICVideoLandScopeView) findViewById(R.id.trtc_root_view);
        mTrtcRootView.setUserId(mUserID);
        TXCloudVideoView localVideoView = mTrtcRootView.getCloudVideoViewByIndex(0);
        localVideoView.setUserId(mUserID);

        //3、开始本地视频图像
        startLocalVideo(true);
    }

    private void unInitTrtc() {
        //3、停止本地视频图像
        mTrtcCloud.stopLocalPreview();
    }

    private void startLocalVideo(boolean enable) {
        TXCloudVideoView localVideoView = mTrtcRootView.getCloudVideoViewByUseId(mUserID);
        localVideoView.setUserId(mUserID);
        localVideoView.setVisibility(View.VISIBLE);
        if (enable) {
            mTrtcCloud.startLocalPreview(mEnableFrontCamera, localVideoView);
        } else {
            mTrtcCloud.stopLocalPreview();
        }
    }

    private void enableAudioCapture(boolean bEnable) {
        if (bEnable) {
            mTrtcCloud.startLocalAudio();
        } else {
            mTrtcCloud.stopLocalAudio();
        }
    }

    //------------------------  ttlx functions start  ------------------------//
    private void upWall(String userId) {
        if (userId.equals(mainVideoView.getUserId())) {
            return;
        }
        if (mainVideoView.getUserId() != null) {
            downWall(mainVideoView.getUserId());
        }

        if (mUserID.equals(userId)) {

        } else {

        }
    }

    private void downWall(String userId) {

    }

    //------------------------  ttlx functions end  ------------------------//

    //------------  From TICEventListener  ------//
    @Override
    public void onTICUserVideoAvailable(final String userId, boolean available) {

        Log.i(TAG, "onTICUserVideoAvailable:" + userId + "|" + available);

        if (available) {
            final TXCloudVideoView renderView = mTrtcRootView.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            if (renderView != null) {
                // 启动远程画面的解码和显示逻辑，FillMode 可以设置是否显示黑边
                mTrtcCloud.setRemoteViewFillMode(userId, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
                mTrtcCloud.startRemoteView(userId, renderView);
                renderView.setUserId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            }

        } else {
            mTrtcCloud.stopRemoteView(userId);
            mTrtcRootView.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
        }
    }

    @Override
    public void onTICUserSubStreamAvailable(String userId, boolean available) {

        if (available) {
            final TXCloudVideoView renderView = mTrtcRootView.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
            if (renderView != null) {
                renderView.setUserId(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
                mTrtcCloud.setRemoteViewFillMode(userId, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT);
                mTrtcCloud.startRemoteSubStreamView(userId, renderView);
            }
        } else {
            mTrtcCloud.stopRemoteSubStreamView(userId);
            mTrtcRootView.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB);
        }
    }

    @Override
    public void onTICUserAudioAvailable(String userId, boolean available) {
        if (available) {
            final TXCloudVideoView renderView = mTrtcRootView.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            if (renderView != null) {
                renderView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onTICMemberJoin(List<String> userList) {

        for (String user : userList) {
            // 创建一个View用来显示新的一路画面，在自已进房间时，也会给这个回调
            if (!user.equals(mUserID)) {
                TXCloudVideoView renderView = mTrtcRootView.onMemberEnter(user + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
                if (renderView != null) {
                    renderView.setVisibility(View.VISIBLE);
                }
                postToast(user + " join.", false);
            }
        }
    }

    @Override
    public void onTICMemberQuit(List<String> userList) {
        for (String user : userList) {
            final String userID_Big = user.equals(mUserID) ? mUserID : user + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG;
            //停止观看画面
            mTrtcCloud.stopRemoteView(userID_Big);
            mTrtcRootView.onMemberLeave(userID_Big);

            final String userID_Sub = user.equals(mUserID) ? mUserID : user + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_SUB;
            mTrtcCloud.stopRemoteSubStreamView(userID_Sub);
            mTrtcRootView.onMemberLeave(userID_Sub);

            postToast(user + " quit.", false);
        }
    }

    void initBoardView() {
        mBoardContainer = (FrameLayout) findViewById(R.id.board_view_container);
        View boardview = mBoard.getBoardRenderView();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mBoardContainer.addView(boardview, layoutParams);
        postToast("正在使用白板：" + mBoard.getVersion(), true);
    }

    private void initTEduBoard() {
        //获取白板对象
        mBoard = mTicManager.getBoard();

        //生成一个继承于TEduBoardController.TEduBoardCallback事件监听，交给白板对象，用于处理白板事件响应。
        mBoardCallback = new MyBoardCallback(this);
        mBoard.addCallback(mBoardCallback);

        //如果用户希望白板显示出来时，不使用系统默认的参数，就需要设置特性缺省参数，如是使用默认参数，则填null。
        TEduBoardController.TEduBoardAuthParam authParam = new TEduBoardController.TEduBoardAuthParam(Constants.APPID, mUserID, mUserSig);
        TEduBoardController.TEduBoardInitParam initParam = new TEduBoardController.TEduBoardInitParam();
        initParam.brushColor = new TEduBoardController.TEduBoardColor(0, 255, 0, 255);

        //调用初始化函数
        mBoard.init(authParam, mRoomId, initParam);
    }

    private void unitTEduBoard() {
        if (mBoard != null) {
            View boardview = mBoard.getBoardRenderView();
            if (mBoardContainer != null && boardview != null) {
                mBoardContainer.removeView(boardview);
            }

            mBoard.removeCallback(mBoardCallback);
        }
    }

    private void onTEBHistroyDataSyncCompleted() {
        mHistroyDataSyncCompleted = true;
        postToast("历史数据同步完成", false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tv_double_room_back_button: //返回
                quitClass();
                break;

            case R.id.tv_memu: //菜单
            {
                if (!mHistroyDataSyncCompleted) { //
                    postToast("请在历史数据同步完成后开始测试", true);
                    return;
                }
                if (mySettingCallback == null) {
                    mySettingCallback = new MySettingCallback();
                }

                if (moreDlg == null) {
                    moreDlg = new TICMenuDialog(this, mySettingCallback);
                }


                TICMenuDialog.SettingCacheData settingCacheData = new TICMenuDialog.SettingCacheData();

                //trtc
                settingCacheData.AudioEnable = mEnableAudio;
                settingCacheData.CameraEnable = mEnableCamera;
                settingCacheData.CameraFront = mEnableFrontCamera;

                //board
                settingCacheData.isDrawEnable = mBoard.isDrawEnable();
                settingCacheData.ToolType = mBoard.getToolType();
                settingCacheData.BrushThin = mBoard.getBrushThin();
                settingCacheData.BrushColor = mBoard.getBrushColor().toInt();
                settingCacheData.TextColor = mBoard.getTextColor().toInt();
                settingCacheData.BackgroundColor = mBoard.getBackgroundColor().toInt();
                settingCacheData.GlobalBackgroundColor = mBoard.getGlobalBackgroundColor().toInt();
                settingCacheData.TextSize = mBoard.getTextSize();
                settingCacheData.TextStyle = mBoard.getTextStyle();
                settingCacheData.TextFamily = mBoard.getTextFamily();
                settingCacheData.canRedo = mCanRedo;
                settingCacheData.canUndo = mCanUndo;

                settingCacheData.currentBoardId = mBoard.getCurrentBoard();
                settingCacheData.boardIds = mBoard.getBoardList();

                settingCacheData.currentFileId = mBoard.getCurrentFile();
                settingCacheData.files = mBoard.getFileInfoList();

                moreDlg.show(settingCacheData);
            }
            break;

            case R.id.btn_send: //发送按钮
                final String msg = ((EditText) findViewById(R.id.et_message_input)).getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    sendGroupMessage(msg);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        quitClass();
    }

    /**
     * 进入课堂
     */
    private void joinClass() {
        TICClassroomOption classroomOption = new TICClassroomOption().setClassId(mRoomId);

        mTicManager.joinClassroom(classroomOption, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("进入课堂成功:" + mRoomId);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if (errCode == 10015) {
                    postToast("课堂不存在:" + mRoomId + " err:" + errCode + " msg:" + errMsg);
                } else {
                    postToast("进入课堂失败:" + mRoomId + " err:" + errCode + " msg:" + errMsg);
                }
            }
        });
    }

    private void quitClass() {

        //如果是老师，可以清除；
        //如查是学生一般是不要清除数据
        boolean clearBoard = false;
        mTicManager.quitClassroom(clearBoard, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("quitClassroom#onSuccess: " + data, true);
                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("quitClassroom#onError: errCode = " + errCode + "  description " + errMsg);
                finish();
            }
        });
    }

    /**
     * 退出课堂
     */
    public void onQuitClsssroomClick(View v) {
        quitClass();
    }

    // ------------ FROM TICMessageListener ---------------------
    @Override
    public void onTICRecvTextMessage(String fromId, String text) {
        postToast(String.format("[%s]（C2C）说: %s", fromId, text));
    }

    @Override
    public void onTICRecvCustomMessage(String fromId, byte[] data) {
        postToast(String.format("[%s]（C2C:Custom）说: %s", fromId, new String(data)));
    }

    @Override
    public void onTICRecvGroupTextMessage(String fromId, String text) {
        postToast(String.format("[%s]（Group:Custom）说: %s", fromId, text));
    }

    @Override
    public void onTICRecvGroupCustomMessage(String fromUserId, byte[] data) {
        postToast(String.format("[%s]（Group:Custom）说: %s", fromUserId, new String(data)));
    }

    @Override
    public void onTICRecvMessage(TIMMessage message) {
        handleTimElement(message);
    }

    private void handleTimElement(TIMMessage message) {

        for (int i = 0; i < message.getElementCount(); i++) {
            TIMElem elem = message.getElement(i);
            switch (elem.getType()) {
                case Text:
                    postToast("This is Text message.");
                    break;
                case Custom:
                    postToast("This is Custom message.");
                    break;
                case GroupTips:
                    postToast("This is GroupTips message.");
                    continue;
                default:
                    postToast("This is other message");
                    break;
            }
        }
    }

    //---------------------- TICEventListener-----------------
    @Override
    public void onTICVideoDisconnect(int errCode, String errMsg) {
    }

    private void sendGroupMessage(final String msg) {
        mTicManager.sendGroupTextMessage("", msg, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("[我]说: " + msg);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("sendGroupMessage##onError##" + errMsg);

            }
        });
    }

    private void sendGroupMessage(final byte[] msg) {
        mTicManager.sendGroupCustomMessage(null, msg, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("[我]说: " + new String(msg));
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("sendGroupMessage##onError##" + errMsg);
            }
        });
    }

    private void sendCustomMessage(final String usrid, final byte[] msg) {
        mTicManager.sendCustomMessage(usrid, msg, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("[我]对[" + usrid + "]说: " + msg);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("sendGroupMessage##onError##" + errMsg);

            }
        });
    }

    @Override
    public void onTICForceOffline() {
        super.onTICForceOffline();

        //1、退出TRTC
        if (mTrtcCloud != null) {
            mTrtcCloud.exitRoom();
        }

        //2.退出房间
        mTicManager.quitClassroom(false, new TICManager.TICCallback() {
            @Override
            public void onSuccess(Object data) {
                postToast("onForceOffline##quitClassroom#onSuccess: " + data);
                Intent intent = new Intent(TICClassMainLandScopeActivity.this, TICLoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                postToast("onForceOffline##quitClassroom#onError: errCode = " + errCode + "  description " + errMsg);
            }
        });

    }

    @Override
    public void onTICClassroomDestroy() {
        postToast("课堂已销毁");
    }

    @Override
    public void onTICSendOfflineRecordInfo(int code, String desc) {
        postToast("同步录制信息失败");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {


        }
    }

    protected void checkCameraAndMicPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        List<String> permissionList = new ArrayList();
        if (!checkPermissionAudioRecorder()) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!checkPermissionCamera()) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        if (!checkPermissionStorage()) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionList.size() < 1) {
            return;
        }
        String[] permissions = permissionList.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }


    //--------------------权限检查-----------------------//

    private boolean checkPermissionAudioRecorder() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private boolean checkPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private boolean checkPermissionStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    //判断是否勾选禁止后不再询问
                    boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                    if (showRequestPermission) {
                        postToast(permissions[i] + " 权限未申请");
                    }
                }
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Board Callback
    static private class MyBoardCallback implements TEduBoardController.TEduBoardCallback {
        WeakReference<TICClassMainLandScopeActivity> mActivityRef;

        MyBoardCallback(TICClassMainLandScopeActivity activityEx) {
            mActivityRef = new WeakReference<>(activityEx);
        }

        @Override
        public void onTEBError(int code, String msg) {

        }

        @Override
        public void onTEBWarning(int code, String msg) {

        }

        @Override
        public void onTEBInit() {
            TICClassMainLandScopeActivity activityEx = mActivityRef.get();
            if (activityEx != null) {
                activityEx.initBoardView();
            }
        }

        @Override
        public void onTEBHistroyDataSyncCompleted() {
            TICClassMainLandScopeActivity activityEx = mActivityRef.get();
            if (activityEx != null) {
                activityEx.onTEBHistroyDataSyncCompleted();
            }
        }

        @Override
        public void onTEBSyncData(String data) {

        }

        @Override
        public void onTEBImageStatusChanged(String boardId, String url, int status) {

        }

        @Override
        public void onTEBAddBoard(String boardId, final String fileId) {
            TXLog.i(TAG, "onTEBAddBoard:" + fileId);
        }

        @Override
        public void onTEBDeleteBoard(String boardId, final String fileId) {

        }

        @Override
        public void onTEBGotoBoard(String boardId, final String fileId) {

        }

        @Override
        public void onTEBAddFile(String fileId) {

        }

        @Override
        public void onTEBDeleteFile(String fileId) {

        }

        @Override
        public void onTEBSwitchFile(String fileId) {

        }

        @Override
        public void onTEBAddH5PPTFile(String fileId) {

        }


        @Override
        public void onTEBUndoStatusChanged(boolean canUndo) {
            TICClassMainLandScopeActivity activityEx = mActivityRef.get();
            if (activityEx != null) {
                activityEx.mCanUndo = canUndo;
            }
        }

        @Override
        public void onTEBRedoStatusChanged(boolean canredo) {
            TICClassMainLandScopeActivity activityEx = mActivityRef.get();
            if (activityEx != null) {
                activityEx.mCanRedo = canredo;
            }
        }

        @Override
        public void onTEBFileUploadProgress(final String path, int currentBytes, int totalBytes, int uploadSpeed) {
        }

        @Override
        public void onTEBFileUploadStatus(String path, int status, String statusMsg) {
            TXLog.i(TAG, "onTEBFileUploadStatus:" + path + " status:" + status);
        }

        @Override
        public void onTEBSetBackgroundImage(final String url) {
        }

        @Override
        public void onTEBBackgroundH5StatusChanged(String boardId, String url, int status) {
            TXLog.i(TAG, "onTEBBackgroundH5StatusChanged:" + boardId + " url:" + boardId + " status:" + status);
        }
    }

    //------回调设置的处理------
    class MySettingCallback implements TICMenuDialog.IMoreListener {

        @Override
        public void onEnableAudio(boolean bEnableAudio) {
            mEnableAudio = bEnableAudio;
            enableAudioCapture(mEnableAudio);
        }

        @Override
        public void onEnableCamera(boolean bEnableCamera) {
            mEnableCamera = bEnableCamera;
            startLocalVideo(mEnableCamera);
        }

        @Override
        public void onSwitchCamera(boolean bFrontCamera) {
            mEnableFrontCamera = bFrontCamera;
            mTrtcCloud.switchCamera();
        }

        //------------board------------
        @Override
        public void onSetDrawEnable(boolean SetDrawEnable) {
            mBoard.setDrawEnable(SetDrawEnable);
        }

        @Override
        public void onSetToolType(int type) {
            mBoard.setToolType(type);
        }

        @Override
        public void onBrushThin(int size) {
            mBoard.setBrushThin(size);
        }

        @Override
        public void onSetTextSize(int size) {
            mBoard.setTextSize(size);
        }

        @Override
        public void onSetBrushColor(int color) {
            TEduBoardController.TEduBoardColor eduBoardColor = new TEduBoardController.TEduBoardColor(color);
            mBoard.setBrushColor(eduBoardColor);
        }

        @Override
        public void onSetTextColor(int color) {
            TEduBoardController.TEduBoardColor eduBoardColor = new TEduBoardController.TEduBoardColor(color);
            mBoard.setTextColor(eduBoardColor);
        }

        @Override
        public void onSetTextStyle(int style) {
            mBoard.setTextStyle(style);
        }

        @Override
        public void onSetTextFamily(String family) {
            mBoard.setTextFamily(family);
        }

        @Override
        public void onSetBackgroundColore(int color) {
            TEduBoardController.TEduBoardColor eduBoardColor = new TEduBoardController.TEduBoardColor(color);
            mBoard.setBackgroundColor(eduBoardColor);
        }

        @Override
        public void onSetBackgroundImage(String path) {
            if (!TextUtils.isEmpty(path)) {
                mBoard.setBackgroundImage(path, TEduBoardController.TEduBoardImageFitMode.TEDU_BOARD_IMAGE_FIT_MODE_CENTER);
            }
        }

        @Override
        public void onSetBackgroundH5(String url) {
            if (!TextUtils.isEmpty(url)) {
                mBoard.setBackgroundH5(url);
            }
        }

        @Override
        public void onUndo() {
            mBoard.undo();
        }

        @Override
        public void onRedo() {
            mBoard.redo();
        }

        @Override
        public void onClear() {
            mBoard.clear(true);
        }

        @Override
        public void onReset() {
            mBoard.reset();
        }

        @Override
        public void onAddBoard(String id) {
            mBoard.addBoard(id);
        }

        @Override
        public void onDeleteBoard(String boardId) {
            mBoard.deleteBoard(boardId);
        }

        @Override
        public void onGotoBoard(String boardId) {
            mBoard.gotoBoard(boardId);
        }

        @Override
        public void onPrevStep() {
            mBoard.prevStep();
        }

        @Override
        public void onNextStep() {
            mBoard.nextStep();
        }

        @Override
        public void onPrevBoard() {
            mBoard.prevBoard();
        }

        @Override
        public void onNextBoard() {
            mBoard.nextBoard();
        }

        @Override
        public void onAddFile(String url) {
            mBoard.addFile(url);
        }

        @Override
        public void onAddH5File(String url) {
            mBoard.addH5PPTFile(url);
        }

        @Override
        public void onDeleteFile(String fileId) {
            mBoard.deleteFile(fileId);
        }

        @Override
        public void onGotoFile(String fid) {
            mBoard.switchFile(fid);
        }
    }


}
