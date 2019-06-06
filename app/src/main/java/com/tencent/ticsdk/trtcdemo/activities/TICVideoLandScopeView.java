package com.tencent.ticsdk.trtcdemo.activities;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ticsdk.trtcdemo.R;

import java.util.ArrayList;

/**
 * Module:   TRTCVideoViewLayout
 * <p>
 * Function: 用于计算每个视频画面的位置排布和大小尺寸
 */
public class TICVideoLandScopeView extends FrameLayout {
    public static final int MAX_USER = 6;
    public static final int MAX_LINEAR_COUNT = 3;
    private final static String TAG = TICVideoLandScopeView.class.getSimpleName();


    View rootView;
    LinearLayout linearLayout1;
    LinearLayout linearLayout2;


    private Context mContext;
    private ArrayList<TXCloudVideoView> mVideoViewList;
    private String mSelfUserId;

    public TICVideoLandScopeView(Context context) {
        this(context, null);
    }

    public TICVideoLandScopeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TICVideoLandScopeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setUserId(String userId) {
        mSelfUserId = userId;
    }

    private void initView(Context context) {
        mContext = context;
        rootView = LayoutInflater.from(mContext).inflate(R.layout.view_ticvideo_landscope, this);
        linearLayout1 = rootView.findViewById(R.id.vtl_ll_1);
        linearLayout2 = rootView.findViewById(R.id.vtl_ll_2);

        initTXCloudVideoView();

        showView();
    }

    private void showView() {
        LinearLayout.LayoutParams layoutParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getScreenHeight(mContext) / MAX_LINEAR_COUNT);

        for (int i = 0; i < mVideoViewList.size(); i++) {
            TXCloudVideoView cloudVideoView = mVideoViewList.get(i);
            int col = i % MAX_LINEAR_COUNT;
            if (col == 0) {
                linearLayout1.addView(cloudVideoView, layoutParams0);
            } else if (col == 1) {
                linearLayout2.addView(cloudVideoView, layoutParams0);
            }
        }
    }

    private int getScreenWidth(Context context) {
        if (context == null) return 0;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    private int getScreenHeight(Context context) {
        if (context == null) return 0;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public void initTXCloudVideoView() {
        mVideoViewList = new ArrayList<TXCloudVideoView>();
        for (int i = 0; i < MAX_USER; i++) {
            TXCloudVideoView cloudVideoView = new TXCloudVideoView(mContext);
            cloudVideoView.setVisibility(GONE);
            cloudVideoView.setId(1000 + i);
            cloudVideoView.setClickable(true);
            cloudVideoView.setTag(i);
            cloudVideoView.setBackgroundColor(Color.BLACK);
            mVideoViewList.add(i, cloudVideoView);
        }
    }

    public TXCloudVideoView getCloudVideoViewByIndex(int index) {
        return mVideoViewList.get(index);
    }

    public TXCloudVideoView getCloudVideoViewByUseId(String userId) {
        for (TXCloudVideoView videoView : mVideoViewList) {
            String tempUserID = videoView.getUserId();
            if (tempUserID != null && tempUserID.equalsIgnoreCase(userId)) {
                return videoView;
            }
        }
        return null;
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 更新进入房间人数
     */
    public TXCloudVideoView onMemberEnter(String userId) {
        Log.e(TAG, "onMemberEnter: userId = " + userId);

        if (TextUtils.isEmpty(userId)) return null;
        TXCloudVideoView videoView = null;
        for (int i = 0; i < mVideoViewList.size(); i++) {
            TXCloudVideoView renderView = mVideoViewList.get(i);
            if (renderView != null) {
                String vUserId = renderView.getUserId();
                if (userId.equalsIgnoreCase(vUserId)) {
                    return renderView;
                }
                if (videoView == null && TextUtils.isEmpty(vUserId)) {
                    renderView.setUserId(userId);
                    videoView = renderView;
                }
            }
        }

        return videoView;
    }

    public void onMemberLeave(String userId) {
        Log.e(TAG, "onMemberLeave: userId = " + userId);

        for (int i = 0; i < mVideoViewList.size(); i++) {
            TXCloudVideoView renderView = mVideoViewList.get(i);
            if (renderView != null && null != renderView.getUserId()) {
                if (renderView.getUserId().equals(userId)) {
                    renderView.setUserId(null);
                    renderView.setVisibility(View.GONE);
                }
            }
        }
    }
}
