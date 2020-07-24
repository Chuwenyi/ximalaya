package com.r.himalaya.views;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.r.himalaya.R;
import com.r.himalaya.adapter.PlayListAdapter;
import com.r.himalaya.base.BaseApplication;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.util.List;

public class SobPopWindow extends PopupWindow {

    private final View mPopView;
    private View mCloseBtn;
    private RecyclerView mTracksList;
    private PlayListAdapter mPlayListAdapter;
    private TextView mPlayModeTv;
    private ImageView mPlayModeIv;
    private View mPlayModeContainer;
    private PlayListActionListener mPlayModeClickListener = null;
    private View mOrderBtnContainer;
    private ImageView mOrderIcon;
    private TextView mOrderText;

    public SobPopWindow() {
        //设置宽高
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        //载入View
        mPopView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.pop_play_list, null);
        //设置内容
        setContentView(mPopView);
        //设置窗口进出动画
        setAnimationStyle(R.style.pop_animation);
        initView();
        initEvent();
    }

    private void initView() {
        mCloseBtn = mPopView.findViewById(R.id.play_list_close_btn);
        mTracksList = mPopView.findViewById(R.id.play_list_rv);
        //设置布局管理器
        LinearLayoutManager LayoutManager = new LinearLayoutManager(BaseApplication.getAppContext());
        mTracksList.setLayoutManager(LayoutManager);
        //设置适配器
        mPlayListAdapter = new PlayListAdapter();
        mTracksList.setAdapter(mPlayListAdapter);
        //播放模式相关
        mPlayModeTv = mPopView.findViewById(R.id.play_list_play_mode_tv);
        mPlayModeIv = mPopView.findViewById(R.id.play_list_play_mode_iv);
        mPlayModeContainer = mPopView.findViewById(R.id.play_list_play_mode_container);
        //顺序和逆序
        mOrderBtnContainer = mPopView.findViewById(R.id.play_list_order_container);
        mOrderIcon = mPopView.findViewById(R.id.play_list_order_iv);
        mOrderText = mPopView.findViewById(R.id.play_list_order_tv);
    }

    private void initEvent() {
        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SobPopWindow.this.dismiss();
            }
        });

        mPlayModeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换播放模式
                if (mPlayModeClickListener != null) {
                    mPlayModeClickListener.onPlayModeClick();
                }
            }
        });
        mOrderBtnContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切换顺序和逆序
                mPlayModeClickListener.onOrderClick();
            }
        });
    }

    /**
     * 设置数据
     *
     * @param data
     */
    public void setListData(List<Track> data) {
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setData(data);

        }
    }

    public void setCurrentPlayPosition(int position) {
        if (mPlayListAdapter != null) {
            mPlayListAdapter.setCurrentPlayPosition(position);
            mTracksList.scrollToPosition(position);
        }
    }

    public void setPlayListItemClickListener(PlayListItemClickListener listener) {
        mPlayListAdapter.setOnItemClickListener(listener);
    }

    /**
     * 更新播放列表播放模式
     *
     * @param currentMode
     */

    public void updatePlayMode(XmPlayListControl.PlayMode currentMode) {
        updatePlayModeBtnImg(currentMode);
    }

    /**
     * 更新切换顺序列表文字
     * @param isOrder
     */
    public void updateOrderIcon(boolean isOrder) {
        mOrderIcon.setImageResource(isOrder ? R.drawable.selector_play_mode_list_order
                : R.drawable.selector_play_mode_list_revers);
        mOrderText.setText(isOrder?"顺序":"逆序");
    }

    /**
     * 根据当前播放模式，更改播放图标
     * PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePlayModeBtnImg(XmPlayListControl.PlayMode playMode) {
        int resId = R.drawable.selector_play_mode_list_order;
        int textId = R.string.play_mode_order_text;
        switch (playMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                textId = R.string.play_mode_order_text;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                textId = R.string.play_mode_random_text;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_looper;
                textId = R.string.play_mode_list_play_text;

                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                textId = R.string.play_mode_single_play_text;

                break;
        }
        mPlayModeIv.setImageResource(resId);
        mPlayModeTv.setText(textId);
    }

    public interface PlayListItemClickListener {
        void onItemClick(int position);
    }

    public void setPlayListActionListener(PlayListActionListener playModeListener) {
        mPlayModeClickListener = playModeListener;
    }

    public interface PlayListActionListener {
        //播放模式被点击
        void onPlayModeClick();

        //  顺序和逆序被点击
        void onOrderClick();
    }
}
