package com.tencent.ticsdk.core.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;


import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMCustomElem;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMGroupAddOpt;
import com.tencent.imsdk.TIMGroupManager;
import com.tencent.imsdk.TIMGroupSystemElem;
import com.tencent.imsdk.TIMGroupSystemElemType;
import com.tencent.imsdk.TIMGroupTipsElem;
import com.tencent.imsdk.TIMGroupTipsType;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMSdkConfig;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.teduboard.TEduBoardController;
import com.tencent.ticsdk.core.TICClassroomOption;
import com.tencent.ticsdk.core.TICManager;
import com.tencent.ticsdk.core.impl.observer.TICEventObservable;
import com.tencent.ticsdk.core.impl.observer.TICIMStatusObservable;
import com.tencent.ticsdk.core.impl.observer.TICMessageObservable;
import com.tencent.ticsdk.core.impl.utils.CallbackUtil;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCStatistics;
import com.tencent.trtc.TRTCCloudListener;
import com.tencent.liteav.basic.log.TXCLog;

public class TICManagerImpl  extends TICManager{

    private final static String TAG = "TICManager";
    TICCallback mEnterRoomCallback; // 进房callback

    //TRTC
    private TRTCCloud mTrtcCloud;              /// TRTC SDK 实例对象
    private TRTCCloudListener mTrtcListener;    /// TRTC SDK 回调监听

    //IM
    private TIMMessageListener mTIMListener;

    //Board
    private TEduBoardController mBoard;

    //Recorder
    private TICRecorder mRecorder;

    private final static byte[] SYNC = new byte[1];

    private Context mAppContext;
    private int sdkAppId = 0;
    private UserInfo userInfo;
    private TICClassroomOption classroomOption;

    private TICEventObservable mEventListner;
    private TICIMStatusObservable mStatusListner;
    private TICMessageObservable mMessageListner;

/////////////////////////////////////////////////////////////////////////////////
//
//                      （一）初始和终止接口函数
//
/////////////////////////////////////////////////////////////////////////////////
    private static volatile TICManager instance;
    public static TICManager sharedInstance() {
        if (instance == null) {
            synchronized (SYNC) {
                if (instance == null) {
                    instance = new TICManagerImpl();
                }
            }
        }
        return instance;
    }

    private TICManagerImpl() {
        userInfo = new UserInfo();

        mEventListner = new TICEventObservable();
        mStatusListner = new TICIMStatusObservable();
        mMessageListner = new TICMessageObservable();

        TXCLog.i(TAG, "TICManager: constructor ");
    }

    @Override
    public int init(Context context, int appId) {

        TXCLog.i(TAG, "TICManager: init, context:" + context + " appid:" + appId);

        //0、给值
        sdkAppId = appId;
        mAppContext = context.getApplicationContext();

        //1、 TIM SDK初始化
        TIMSdkConfig timSdkConfig = new TIMSdkConfig(appId)
                .enableCrashReport(true)
                .enableLogPrint(true)
                .setLogLevel(TIMLogLevel.DEBUG);
        TIMManager.getInstance().init(context, timSdkConfig);

        mTIMListener = new TIMMessageListener() {
            @Override
            public boolean onNewMessages(List<TIMMessage> list) {
                return handleNewMessages(list);
            }
        };

        //2. TRTC SDK初始化
        if (mTrtcCloud == null) {
            mTrtcListener = new TRTCCloudListenerImpl();
            mTrtcCloud = TRTCCloud.sharedInstance(mAppContext);
            mTrtcCloud.setListener(mTrtcListener);
        }

        //3. TEdu Board
        if (mBoard == null) {
            mBoard = new TEduBoardController(mAppContext);
        }

        //4. Recorder
        if (mRecorder == null) {
            mRecorder = new TICRecorder(this);
        }

        return 0;
    }

    @Override
    public int unInit() {
        TXCLog.i(TAG, "TICManager: unInit");

        //1、销毁trtc
        if (mTrtcCloud != null) {
            TRTCCloud.destroySharedInstance();
            mTrtcCloud = null;
        }

        return 0;
    }


    public TRTCCloud getTRTCClound() {
        if (mTrtcCloud == null) {
            TXCLog.e(TAG, "TICManager: getTRTCClound null, Do you call init?");
        }

        return mTrtcCloud;
    }

    public TEduBoardController getBoard() {
        if (mBoard == null) {
            TXCLog.e(TAG, "TICManager: getBoard null, Do you call init?");
        }
        return mBoard;
    }

    @Override
    public void addEventListener(TICEventListener callback) {
        TXCLog.i(TAG, "TICManager: addEventListener:" + callback);
        mEventListner.addObserver(callback);
    }

    @Override
    public void removeEventListener(TICEventListener callback) {
        TXCLog.i(TAG, "TICManager: removeEventListener:" + callback);
        mEventListner.removeObserver(callback);
    }

    @Override
    public  void addIMStatusListener(TICIMStatusListener callback) {
        TXCLog.i(TAG, "TICManager: addIMStatusListener:" + callback);
        mStatusListner.addObserver(callback);
    }

    @Override
    public  void removeIMStatusListener(TICIMStatusListener callback) {
        TXCLog.i(TAG, "TICManager: removeIMStatusListener:" + callback);
        mStatusListner.removeObserver(callback);
    }

    @Override
    public void addIMMessageListener(TICMessageListener callback) {
        TXCLog.i(TAG, "TICManager: addIMMessageListener:" + callback);
        mMessageListner.addObserver(callback);
    }

    @Override
    public void removeIMMessageListener(TICMessageListener callback) {
        TXCLog.i(TAG, "TICManager: removeIMMessageListener:" + callback);
        mMessageListner.removeObserver(callback);
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      （二）TIC登录/登出/创建销毁课堂/进入退出课堂接口函数
    //
    /////////////////////////////////////////////////////////////////////////////////
    @Override
    public void login(final String userId, final String userSig, final TICCallback callBack) {

        TXCLog.i(TAG, "TICManager: login userid:" + userId + " sig:" + userSig);

        // IM 登陆
        setUserInfo(userId, userSig);

        TIMManager.getInstance().login(userId, userSig, new TIMCallBack() {
            @Override
            public void onSuccess() {
                TXCLog.i(TAG, "TICManager: login onSuccess:" + userId);
                //成功登录后，加入消息和状态监听
                TIMManager.getInstance().getUserConfig().setUserStatusListener(mStatusListner);
                TIMManager.getInstance().addMessageListener(mTIMListener);

//                reportConfig.setUserId(userId).setUserSig(userSig);
                if (null != callBack) {
                    callBack.onSuccess("");
                }
            }

            @Override
            public void onError(int errCode, String errMsg) {
                TXCLog.i(TAG, "TICManager: login onError:" + errCode + " msg:"  +errMsg);
                if (null != callBack) {
                    callBack.onError(MODULE_IMSDK, errCode, "login failed: " + errMsg);
                }
            }
        });
    }

    @Override
    public void logout(final TICCallback callback) {
        TXCLog.i(TAG, "TICManager: logout callback:" + callback);

        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onSuccess() {
                TXCLog.i(TAG, "TICManager: logout onSuccess");

                if (null != callback) {
                    callback.onSuccess("");
                }
            }
            @Override
            public void onError(int errCode, String errMsg) {
                TXCLog.i(TAG, "TICManager: logout onError:" + errCode + " msg:" + errMsg);
                if (null != callback) {
                    callback.onError(MODULE_IMSDK, errCode, "logout failed: " + errMsg);
                }
            }
        });

        //退出登录后，去掉消息的监听
        TIMManager.getInstance().removeMessageListener(mTIMListener);
        TIMManager.getInstance().getUserConfig().setUserStatusListener(null);
    }

    @Override
    public void createClassroom(final int classId, final TICCallback callback) {
        TXCLog.i(TAG, "TICManager: createClassroom classId:" + classId + " callback:" + callback);

        // 为了减少用户操作成本（收到群进出等通知需要配置工单才生效）群组类型由ChatRoom改为Public
        final String groupId = String.valueOf(classId);
        final String groupName = "interact group";
        final String groupType = "Public";

        TIMGroupManager.CreateGroupParam param = new TIMGroupManager.CreateGroupParam(groupType, groupName);
        param.setGroupId(groupId);
        param.setAddOption(TIMGroupAddOpt.TIM_GROUP_ADD_ANY); //
        TIMGroupManager.getInstance().createGroup(param, new TIMValueCallBack<String>() {

            @Override
            public void onSuccess(String s) {
                TXCLog.i(TAG, "TICManager: createClassroom onSuccess:" + classId + " msg:" + s);
                if (null != callback) {
                    callback.onSuccess(classId);
                }
            }

            @Override
            public void onError(int errCode, String errMsg) {
                if (null != callback) {
                    if (errCode == 10025) { // 群组ID已被使用，并且操作者为群主，可以直接使用。
                        TXCLog.i(TAG, "TICManager: createClassroom 10025 onSuccess:" + classId);
                        callback.onSuccess(classId);
                    }
                    else {
                        TXCLog.i(TAG, "TICManager: createClassroom onError:" + classId + " msg:" + errMsg);
                        callback.onError(MODULE_IMSDK, errCode,  errMsg);
                    }
                }
            }
        });
    }

    @Override
    public void destroyClassroom(final int classId, final TICCallback callback) {

        TXCLog.i(TAG, "TICManager: destroyClassroom classId:" + classId + " callback:" + callback);

        final String groupId = String.valueOf(classId);

        TIMGroupManager.getInstance().deleteGroup(groupId, new TIMCallBack() {
            @Override
            public void onError(int errorCode, String errInfo) {
                TXCLog.i(TAG, "TICManager: destroyClassroom onError:" + errorCode + " msg:" + errInfo);
                CallbackUtil.notifyError(callback, MODULE_IMSDK, errorCode, errInfo);
            }

            @Override
            public void onSuccess() {
                TXCLog.i(TAG, "TICManager: destroyClassroom onSuccess" );

            }
        });

    }

    @Override
    public void joinClassroom(final TICClassroomOption option, final TICCallback callback) {

        if (option == null || option.getClassId() < 0) {
            TXCLog.i(TAG, "TICManager: joinClassroom Para Error");
            CallbackUtil.notifyError(callback, MODULE_TIC_SDK, Error.ERR_INVALID_PARAMS, Error.ERR_MSG_INVALID_PARAMS);
            return;
        }

        TXCLog.i(TAG, "TICManager: joinClassroom classId:" + option.toString() + " callback:" + callback);

        classroomOption = option;

        final int classId = classroomOption.getClassId();
        final String groupId = String.valueOf(classId);
        final String desc = "board group";

        TIMGroupManager.getInstance().applyJoinGroup(groupId, desc + groupId, new TIMCallBack() {
            @Override
            public void onSuccess() {

                TXCLog.i(TAG, "TICManager: joinClassroom onSuccess ");

                onJoinClassroomSuccessfully(callback);
            }

            @Override
            public void onError(int errCode, String errMsg) {
                if (callback != null) {
                    if (errCode == 10013) { //you are already group member.
                        TXCLog.i(TAG, "TICManager: joinClassroom 10013 onSuccess");
                        onJoinClassroomSuccessfully(callback);
                    }
                    else {
                        TXCLog.i(TAG, "TICManager: joinClassroom onError");
                        callback.onError(MODULE_IMSDK, errCode, errMsg);
                    }
            }
            }
        });
    }

    @Override
    public void quitClassroom(boolean clearBoard, final TICCallback callback) {
        TXCLog.i(TAG, "TICManager: quitClassroom " + clearBoard + "|" + callback);

        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: quitClassroom para Error.");
            CallbackUtil.notifyError(callback, MODULE_TIC_SDK, Error.ERR_NOT_IN_CLASS, Error.ERR_MSG_NOT_IN_CLASS);
            return;
        }

        //清除board中所有的历史数据，下次进来时看到的都是全新白板
        if (clearBoard && mBoard != null) {
            mBoard.reset();
        }

        int classId = classroomOption.getClassId();
        final String groupId = String.valueOf(classId);
        TIMGroupManager.getInstance().quitGroup(groupId, new TIMCallBack() {//NOTE:在被挤下线时，不会回调
            @Override
            public void onError(int errorCode, String errInfo) {
                TXCLog.e(TAG, "TICManager: quitClassroom onError, err:" + errorCode + " msg:" + errInfo);
                mTrtcCloud.exitRoom();

                if (callback != null) {
                    if (errorCode == 10009) {
                        callback.onSuccess(0);
                    }
                    else {
                        callback.onError(MODULE_IMSDK, errorCode, errInfo);
                    }
                }
            }

            @Override
            public void onSuccess() {
                TXCLog.e(TAG, "TICManager: quitClassroom onSuccess");
                    mTrtcCloud.exitRoom();
                    CallbackUtil.notifySuccess(callback, 0);
            }
        });

        releaseClass();
    }

    private void onJoinClassroomSuccessfully(final TICCallback callback) {
        if (classroomOption == null || classroomOption.getClassId() < 0) {
            CallbackUtil.notifyError(callback, MODULE_TIC_SDK, Error.ERR_INVALID_PARAMS, Error.ERR_MSG_INVALID_PARAMS);
            return;
        }

        //TRTC进房
        mEnterRoomCallback = callback;
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams(sdkAppId, userInfo.getUserId(), userInfo.getUserSig(), classroomOption.getClassId(), "", "");     /// TRTC SDK 视频通话房间进入所必须的参数
        mTrtcCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
    }

    private void releaseClass() {
        classroomOption = null;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //
    //                      （五) IM消息
    //
    /////////////////////////////////////////////////////////////////////////////////
    @Override
    public void sendTextMessage(String userId, final String text, TICCallback<TIMMessage> callBack) {
        TXCLog.i(TAG, "TICManager: sendTextMessage user:" + userId + " text:" + text);

        TIMMessage message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(text);
        message.addElement(elem);

        sendMessage(userId, message, callBack);
    }

    @Override
    public void sendCustomMessage(String userId, final byte[] data, TICCallback<TIMMessage> callBack) {
        TXCLog.i(TAG, "TICManager: sendCustomMessage user:" + userId + " data:" + data.length);

        TIMMessage message = new TIMMessage();
        TIMCustomElem customElem = new TIMCustomElem();
        customElem.setData(data);
        message.addElement(customElem);
        sendMessage(userId, message, callBack);
    }

    @Override
    public void sendMessage(String userId, TIMMessage message, final TICCallback<TIMMessage> callBack) {
        TXCLog.e(TAG, "TICManager: sendMessage user:" + userId + " message:" + message.toString());
        if (classroomOption == null || classroomOption.getClassId() == -1) {
            TXCLog.e(TAG, "TICManager: sendMessage: " + Error.ERR_MSG_NOT_IN_CLASS);
            CallbackUtil.notifyError(callBack, MODULE_IMSDK, Error.ERR_NOT_IN_CLASS, Error.ERR_MSG_NOT_IN_CLASS);
            return;
        }

        TIMConversation conversation;
        if (TextUtils.isEmpty(userId)) {
            conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, String.valueOf(classroomOption.getClassId()));
        } else {
            conversation = TIMManager.getInstance().getConversation(TIMConversationType.C2C, String.valueOf(userId));
        }

        conversation.sendMessage(message, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int errCode, String errMsg) {
                TXCLog.e(TAG, "TICManager: sendMessage onError:" + errCode + " errMsg:" + errMsg);
                CallbackUtil.notifyError(callBack, MODULE_IMSDK, errCode, "send im message failed: " + errMsg);
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                TXCLog.e(TAG, "TICManager: sendMessage onSuccess:");
                CallbackUtil.notifySuccess(callBack, timMessage);
            }
        });
    }

    @Override
    public void sendGroupTextMessage(String groupId, final String text, TICCallback callBack) {
        TXCLog.e(TAG, "TICManager: sendGroupTextMessage user:" + groupId + " text:" + text);

        groupId = String.valueOf(classroomOption.getClassId()); //暂时等于内部群

        TIMMessage message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(text);
        message.addElement(elem);

        sendGroupMessage(groupId, message, callBack);
    }

    @Override
    public void sendGroupCustomMessage(String groupId, final byte[] data, TICCallback callBack){
        sendGroupCustomMessage(groupId, "", data, callBack);
    }

    @Override
    public void sendGroupMessage(final String groupId, TIMMessage message, final TICCallback callBack) {
        TXCLog.e(TAG, "TICManager: sendMessage groupId:" + groupId );
        if (classroomOption == null || classroomOption.getClassId() == -1) {
            TXCLog.e(TAG, "TICManager: sendMessage: " + Error.ERR_MSG_NOT_IN_CLASS);
            CallbackUtil.notifyError(callBack, MODULE_IMSDK, Error.ERR_NOT_IN_CLASS, Error.ERR_MSG_NOT_IN_CLASS);
            return;
        }

        TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, groupId);

        conversation.sendMessage(message, new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int errCode, String errMsg) {
                TXCLog.e(TAG, "TICManager: sendMessage onError:" + errCode + " errMsg:" + errMsg);
                CallbackUtil.notifyError(callBack, MODULE_IMSDK, errCode, "send im message failed: " + errMsg);
            }

            @Override
            public void onSuccess(TIMMessage timMessage) {
                TXCLog.e(TAG, "TICManager: sendMessage onSuccess:");
                CallbackUtil.notifySuccess(callBack, timMessage);
            }
        });
    }

    void sendGroupCustomMessage(String groupId, String ext, final byte[] data, TICCallback callBack){
        TXCLog.e(TAG, "TICManager: sendGroupCustomMessage groupId:" + groupId + " data:" + data);

        groupId = String.valueOf(classroomOption.getClassId()); //暂时等于内部群

        TIMMessage message = new TIMMessage();
        TIMCustomElem customElem = new TIMCustomElem();
        customElem.setData(data);
        if (!TextUtils.isEmpty(ext)) {
            customElem.setExt(ext.getBytes());
        }
        message.addElement(customElem);
        sendGroupMessage(groupId, message, callBack);
    }

    private boolean handleNewMessages(List<TIMMessage> list) {
        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: handleNewMessages: not in class now.");
            return false;
        }


        for (final TIMMessage message : list) {
                String ext = "";
                if (message.getOfflinePushSettings() != null) {
                    ext = new String(message.getOfflinePushSettings().getExt());
                }
                if (!TextUtils.isEmpty(ext) && ext.equals(TICSDK_WHITEBOARD_CMD)) {
                    // 白板消息
    //                handleWhiteboardMessage(message);
                } else {

                    TIMConversationType type = message.getConversation().getType();
                    if (type == TIMConversationType.C2C || type == TIMConversationType.Group) {
                        // 私聊消息
                        if (type == TIMConversationType.Group ) { //过滤其他群组的消息
                            final int classId = classroomOption.getClassId();
                            String groupId =  message.getConversation().getPeer();
                            if (TextUtils.isEmpty(groupId) || !groupId.equals(String.valueOf(classId))) {
                                continue;
                            }
                        }

                        handleChatMessage(message);

                    } else if (type == TIMConversationType.System) {
                        handleGroupSystemMessage(message);
                    }
                    mMessageListner.onTICRecvMessage(message);
                }

        }
        return false;
    }

    private void handleGroupSystemMessage(TIMMessage message) {
        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: handleGroupSystemMessage: not in class now.");
            return;
        }
        for (int i = 0; i < message.getElementCount(); i++) {
            TIMElem elem = message.getElement(i);
            switch (elem.getType()) {
                case GroupSystem:
                    TIMGroupSystemElem systemElem = (TIMGroupSystemElem) elem;

                    String groupId = systemElem.getGroupId();
                    if (!groupId.equals(String.valueOf(classroomOption.getClassId()))) {
                        TXCLog.e(TAG, "TICManager:handleGroupSystemMessage-> not in current group");
                        continue;
                    }

                    TIMGroupSystemElemType subtype = systemElem.getSubtype();
                    if (subtype == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE ||
                            subtype == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_REVOKE_GROUP_TYPE) {
                        quitClassroom(false,null);
                        mEventListner.onTICClassroomDestroy();
                    }
                    else if (subtype == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_KICK_OFF_FROM_GROUP_TYPE) {
                        TXCLog.e(TAG, "TICManager: handleGroupSystemMessage TIM_GROUP_SYSTEM_KICK_OFF_FROM_GROUP_TYPE: " + groupId + "| " + systemElem.getOpReason());
                        quitClassroom(false,null);
                         mEventListner.onTICMemberQuit(Collections.singletonList(TIMManager.getInstance().getLoginUser()));
                    }
                    break;
                default:
                    TXCLog.e(TAG, "TICManager: handleGroupSystemMessage: elemtype : " + elem.getType());
                    break;
            }
        }
    }

    private void handleChatMessage(TIMMessage message) {
        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: onChatMessageReceived: not in class now.");
            return;
        }

        for (int i = 0; i < message.getElementCount(); i++) {
            TIMElem elem = message.getElement(i);
            switch (elem.getType()) {
                case Text:
                case Custom:
                    onChatMessageReceived(message, elem);
                    break;
                case GroupTips:
                    onGroupTipMessageReceived((TIMGroupTipsElem) elem);
                    continue;
                default:
                    break;
            }
        }
    }

    private void onGroupTipMessageReceived(TIMGroupTipsElem tipsElem) {
        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: onGroupTipMessageReceived: not in class now.");
            return;
        }

        TIMGroupTipsType tipsType = tipsElem.getTipsType();
        String groupId = tipsElem.getGroupId();

        if (!groupId.equals(String.valueOf(classroomOption.getClassId()))) {
            TXCLog.e(TAG, "TICManager: onGroupTipMessageReceived-> not in current group");
            return;
        }

        if (tipsType == TIMGroupTipsType.Join) {
            mEventListner.onTICMemberJoin(tipsElem.getUserList());
        } else if (tipsType == TIMGroupTipsType.Quit || tipsType == TIMGroupTipsType.Kick) {
            if (tipsElem.getUserList().size() <= 0) {
                mEventListner.onTICMemberQuit(Collections.singletonList(tipsElem.getOpUser()));
            } else {
                mEventListner.onTICMemberQuit(tipsElem.getUserList());
            }
        }
    }

    // TODO: 2018/11/30 parse chat  message
    private void onChatMessageReceived(TIMMessage message, TIMElem elem) {

        if (classroomOption == null) {
            TXCLog.e(TAG, "TICManager: onChatMessageReceived: not in class now.");
            return;
        }

        switch (message.getConversation().getType()) {
            case C2C:       // C2C消息
                if (elem.getType() == TIMElemType.Text) {
                   mMessageListner.onTICRecvTextMessage(message.getSender(), ((TIMTextElem) elem).getText());
                } else if (elem.getType() == TIMElemType.Custom) {
                    mMessageListner.onTICRecvCustomMessage(message.getSender(), ((TIMCustomElem) elem).getData());
                }
                break;
            case Group:
                // 群组义消息
                if (elem.getType() == TIMElemType.Text) {
                    mMessageListner.onTICRecvGroupTextMessage(message.getSender(), ((TIMTextElem) elem).getText());
                } else if (elem.getType() == TIMElemType.Custom) {

                    String ext = "";
                    TIMCustomElem customElem = (TIMCustomElem) elem;
                    if (customElem.getExt() != null) {
                        ext = new String(customElem.getExt());
                    }
                    if (!TextUtils.isEmpty(ext) && ext.equals(TICSDK_WHITEBOARD_CMD)) {
                        // 白板消息
//                        decodeBoardMsg(message.getSender(), customElem.getData());
                    } else {
                        mMessageListner.onTICRecvGroupCustomMessage(message.getSender(), customElem.getData());
                    }
                }
                break;
            default:
                TXCLog.e(TAG, "TICManager: onChatMessageReceived-> message type: " + message.getConversation().getType());
                break;
        }
    }


/////////////////////////////////////////////////////////////////////////////////
//
//                      （五）TRTC SDK内部状态回调
//
/////////////////////////////////////////////////////////////////////////////////

    class TRTCCloudListenerImpl extends TRTCCloudListener {
        @Override
        public void onEnterRoom(long elapsed) {
            TXCLog.i(TAG, "TICManager: TRTC onEnterRoom elapsed: " + elapsed);
            if (mEnterRoomCallback != null) {
                //
                mEnterRoomCallback.onSuccess("succ");
            }
            if (mRecorder != null && classroomOption != null) {
                TEduBoardController.TEduBoardAuthParam authParam = new TEduBoardController.TEduBoardAuthParam(sdkAppId, userInfo.getUserId(), userInfo.getUserSig());
                mRecorder.start(authParam, classroomOption.getClassId(), classroomOption.ntpServer);
            }
            else {
                TXCLog.i(TAG, "TICManager: TRTC onEnterRoom: " + mRecorder +  "|" + classroomOption);
            }
        }

        @Override
        public void onExitRoom(int reason) {
            TXCLog.i(TAG, "TICManager: TRTC onExitRoom :" + reason);
        }

        @Override
        public void onUserVideoAvailable(final String userId, boolean available) {
            TXCLog.i(TAG, "TICManager: onUserVideoAvailable->render userId: " + userId + ", available:" + available);
            mEventListner.onTICUserVideoAvailable(userId, available);
        }

        @Override
        public void onUserSubStreamAvailable(String userId, boolean available) {
            TXCLog.i(TAG, "TICManager: onUserSubStreamAvailable :" + userId + "|" + available);
            mEventListner.onTICUserSubStreamAvailable(userId, available);
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {
            TXCLog.i(TAG, "TICManager: onUserAudioAvailable :" + userId + "|" + available);
            mEventListner.onTICUserAudioAvailable(userId, available);
        }

        @Override
        public void onUserEnter(String userId) {
            TXCLog.i(TAG, "onUserEnter: " + userId);
        }

        @Override
        public void onUserExit(String userId, int reason) {
            TXCLog.i(TAG, "TICManager: onUserExit: " + userId);
            mEventListner.onTICUserVideoAvailable(userId, false);
            mEventListner.onTICUserAudioAvailable(userId, false);
            mEventListner.onTICUserSubStreamAvailable(userId, false);
        }

        /**
         * 1.1 错误回调: SDK不可恢复的错误，一定要监听，并分情况给用户适当的界面提示
         *
         * @param errCode   错误码 TRTCErrorCode
         * @param errMsg    错误信息
         * @param extraInfo 额外信息，如错误发生的用户，一般不需要关注，默认是本地错误
         */
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            TXCLog.i(TAG, "TICManager: sdk callback onError");

//            if(errCode == ERR_ROOM_ENTER_FAIL
//                    || errCode == ERR_ENTER_ROOM_PARAM_NULL
//                    || errCode == ERR_SDK_APPID_INVALID
//                    || errCode == ERR_ROOM_ID_INVALID
//                    || errCode == ERR_USER_ID_INVALID
//                    || errCode == ERR_USER_SIG_INVALID){
//            [[TRTCCloud sharedInstance] exitRoom];
//                TICBLOCK_SAFE_RUN(self->_enterCallback, kTICMODULE_TRTC, errCode, errMsg);
//            }
        }

        /**
         * 1.2 警告回调
         *
         * @param warningCode 错误码 TRTCWarningCode
         * @param warningMsg  警告信息
         * @param extraInfo   额外信息，如警告发生的用户，一般不需要关注，默认是本地错误
         */
        @Override
        public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
            TXCLog.i(TAG, "TICManager: sdk callback onWarning");
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> var1, int var2) {
        }
        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality var1, ArrayList<TRTCCloudDef.TRTCQuality> var2) {

        }
        @Override
        public void onStatistics(TRTCStatistics var1) {
        }
        @Override
        public void onFirstAudioFrame(String var1) {
        }
        @Override
        public void onConnectionLost() {
        }
        @Override
        public void onTryToReconnect() {
        }
        @Override
        public void onConnectionRecovery() {
        }
        @Override
        public void onSpeedTest(TRTCCloudDef.TRTCSpeedTestResult var1, int var2, int var3) {
        }
        @Override
        public void onCameraDidReady() {
        }
        @Override
        public void onAudioRouteChanged(int var1, int var2) {
        }

    }

    /////////////////

    public void setUserInfo(@NonNull final String userId, @NonNull final String userSig) {
        userInfo.setUserInfo(userId, userSig);
    }

    public void trigleOffLineRecordCallback(int code, final String msg) {
        mEventListner.onTICSendOfflineRecordInfo(code, msg);
    }
}
