package com.r.himalaya;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.r.himalaya.adapter.PlayerTrackPagerAdapter;
import com.r.himalaya.base.BaseActivity;
import com.r.himalaya.interfaces.IPlayCallback;
import com.r.himalaya.presents.PlayPresenter;
import com.r.himalaya.views.SobPopWindow;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;

public class PlayActivity extends BaseActivity implements IPlayCallback, ViewPager.OnPageChangeListener {

    private ImageView mControlBtn;
    private PlayPresenter mPlayPresenter;
    private SimpleDateFormat mMinFormat = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat mHourFormat = new SimpleDateFormat("hh:mm:ss");
    private TextView mTotalDuration;
    private TextView mCurrentPosition;
    private SeekBar mDurationBar;
    private int mCurrentProgress = 0;
    private boolean mIsUserTouchProgressBar = false;
    private ImageView mPlayNextBtn;
    private ImageView mPlayPreBtn;
    private TextView mTrackTitleTv;
    private String mTrackTitleText;
    private ViewPager mTrackPagerView;
    private PlayerTrackPagerAdapter mTrackPagerAdapter;
    private boolean mIsUserSlidePager = false;
    private ImageView mPlayerModeSwitchBtn;
    private XmPlayListControl.PlayMode mCurrentMode = PLAY_MODEL_LIST;
    private static Map<XmPlayListControl.PlayMode, XmPlayListControl.PlayMode> sPlayModeRule = new HashMap<>();


    //默认    PLAY_MODEL_LIST
    //列表循环PLAY_MODEL_LIST_LOOP
    //随机播放PLAY_MODEL_RANDOM
    //单曲循环PLAY_MODEL_SINGLE_LOOP
    static {
        sPlayModeRule.put(PLAY_MODEL_LIST, PLAY_MODEL_LIST_LOOP);
        sPlayModeRule.put(PLAY_MODEL_LIST_LOOP, PLAY_MODEL_RANDOM);
        sPlayModeRule.put(PLAY_MODEL_RANDOM, PLAY_MODEL_SINGLE_LOOP);
        sPlayModeRule.put(PLAY_MODEL_SINGLE_LOOP, PLAY_MODEL_LIST);
    }

    private View mPlayListBtn;
    private SobPopWindow mSobPopWindow;
    private ValueAnimator mEnterBgAnimator;
    private ValueAnimator mOutBgAnimator;
    private final int BG_ANIMATOR_DURATION = 500;


    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
//        //播放测试
        initView();
        mPlayPresenter = PlayPresenter.getPlayPresenter();
        mPlayPresenter.registerViewCallback(this);
        initEvent();
        inBgAnimation();
    }

    private void inBgAnimation() {
        mEnterBgAnimator = ValueAnimator.ofFloat(1.0f, 0.7f);
        mEnterBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mEnterBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                //处理一下背景，给透明度
                updateBgAlpha(value);
            }
        });
        //退出的
        mOutBgAnimator = ValueAnimator.ofFloat(0.7f, 1.0f);
        mOutBgAnimator.setDuration(BG_ANIMATOR_DURATION);
        mOutBgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                updateBgAlpha(value);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        if (mPlayPresenter != null) {
            mPlayPresenter.unRegisterViewCallback(this);
            mPlayPresenter = null;
        }

    }

    /**
     * 给控件相关事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter.isPlaying()) {
                    mPlayPresenter.pause();
                } else {
                    mPlayPresenter.play();
                }
            }
        });
        mDurationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
                if (isFromUser) {
                    mCurrentProgress = progress;

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgressBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsUserTouchProgressBar = false;
                //手离开时更新进度条
                mPlayPresenter.seekTo(mCurrentProgress);
            }
        });
        mPlayNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放下一首歌
                if (mPlayPresenter != null) {
                    mPlayPresenter.playNext();
                }
            }
        });

        mPlayPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //播放上一首歌
                if (mPlayPresenter != null) {
                    mPlayPresenter.playPre();
                }
            }
        });
        mTrackPagerView.addOnPageChangeListener(this);

        mTrackPagerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mIsUserSlidePager = true;
                        break;
                }
                return false;
            }
        });
        mPlayerModeSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchPlayMode();
            }
        });


        mPlayListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //展示播放列表
                mSobPopWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                //设置背景动画渐变过程
                mEnterBgAnimator.start();
            }
        });
        mSobPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //pop窗体恢复后，消失透明度
                mOutBgAnimator.start();
            }
        });

        mSobPopWindow.setPlayListItemClickListener(new SobPopWindow.PlayListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //播放列表里的item被点击了
                if (mPlayPresenter != null) {
                    mPlayPresenter.playByIndex(position);
                }
            }
        });
        mSobPopWindow.setPlayListActionListener(new SobPopWindow.PlayListActionListener() {
            @Override
            public void onPlayModeClick() {
                //切换播放模式
                switchPlayMode();
            }

            @Override
            public void onOrderClick() {
                //切换顺序和逆序
                if (mPlayPresenter != null) {
                    mPlayPresenter.reversPlayList();
                }
            }
        });
    }

    private void switchPlayMode() {
        //处理播放模式
        //根据当前mode获取下一个mode
        XmPlayListControl.PlayMode playMode = sPlayModeRule.get(mCurrentMode);
        //修改播放模式
        if (mPlayPresenter != null) {
            mPlayPresenter.switchPlayMode(playMode);
        }
    }

    public void updateBgAlpha(float alpha) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.alpha = alpha;
        window.setAttributes(attributes);
    }

    /**
     * 根据当前播放模式，更改播放图标
     * PLAY_MODEL_LIST
     * PLAY_MODEL_LIST_LOOP
     * PLAY_MODEL_RANDOM
     * PLAY_MODEL_SINGLE_LOOP
     */
    private void updatePlayModeBtnImg() {
        int resId = R.drawable.selector_play_mode_list_order;
        switch (mCurrentMode) {
            case PLAY_MODEL_LIST:
                resId = R.drawable.selector_play_mode_list_order;
                break;
            case PLAY_MODEL_RANDOM:
                resId = R.drawable.selector_play_mode_random;
                break;
            case PLAY_MODEL_LIST_LOOP:
                resId = R.drawable.selector_play_mode_list_looper;
                break;
            case PLAY_MODEL_SINGLE_LOOP:
                resId = R.drawable.selector_play_mode_single_loop;
                break;
        }
        mPlayerModeSwitchBtn.setImageResource(resId);
    }

    private void initView() {
        mControlBtn = this.findViewById(R.id.play_or_pause_btn);
        mTotalDuration = this.findViewById(R.id.track_duration);
        mCurrentPosition = this.findViewById(R.id.current_position);
        mDurationBar = this.findViewById(R.id.track_seek_bar);
        mPlayNextBtn = this.findViewById(R.id.play_next);
        mPlayPreBtn = this.findViewById(R.id.play_pre);
        mTrackTitleTv = this.findViewById(R.id.track_title);

        if (!TextUtils.isEmpty(mTrackTitleText)) {
            mTrackTitleTv.setText(mTrackTitleText);
        }
        mTrackPagerView = this.findViewById(R.id.track_pager_view);
        //创建适配器
        mTrackPagerAdapter = new PlayerTrackPagerAdapter();
        //设置适配器
        mTrackPagerView.setAdapter(mTrackPagerAdapter);
        //切换播放模式按钮
        mPlayerModeSwitchBtn = findViewById(R.id.player_mode_switch_btn);
        //播放列表
        mPlayListBtn = findViewById(R.id.play_list);
        mSobPopWindow = new SobPopWindow();


    }

    @Override
    public void onPlayStart() {
        //修改ui成暂停按钮
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.mipmap.stop_normal);
        }

    }

    @Override
    public void onPlayPause() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.mipmap.play_normal);
        }

    }

    @Override
    public void onPlayStop() {
        if (mControlBtn != null) {
            mControlBtn.setImageResource(R.mipmap.stop_normal);
        }

    }

    @Override
    public void onPlayError() {

    }

    @Override
    public void nextPlay(Track track) {

    }

    @Override
    public void onPrePlay(Track track) {

    }

    @Override
    public void onListLoaded(List<Track> list) {
        //把数据设置到适配器里
        if (mTrackPagerAdapter != null) {

            mTrackPagerAdapter.setData(list);
        }
        //数据回来后也要给节目列表一份
        if (mSobPopWindow != null) {

            mSobPopWindow.setListData(list);
        }

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode PlayMode) {
        //更新播放模式,并修改ui
        mCurrentMode = PlayMode;
        //更新pop里的播放模式
        mSobPopWindow.updatePlayMode(mCurrentMode);
        updatePlayModeBtnImg();

    }

    @Override
    public void onProgressChange(int currentDuration, int total) {
        mDurationBar.setMax(total);
        //更新播放进度，更新进度条
        String totalDuration;
        String currentPosition;
        if (total > 1000 * 60 * 60) {
            totalDuration = mHourFormat.format(total);
            currentPosition = mHourFormat.format(currentDuration);

        } else {
            totalDuration = mMinFormat.format(total);
            currentPosition = mMinFormat.format(currentDuration);

        }
        if (mTotalDuration != null) {
            mTotalDuration.setText(totalDuration);
        }
        //更新进度
        //计算当前进度
        if (!mIsUserTouchProgressBar) {
            //int percent = (int)(currentDuration * 1.0f / total * 100);
            mDurationBar.setProgress(currentDuration);
        }

        //更新当前时间
        if (mCurrentPosition != null) {
            mCurrentPosition.setText(currentPosition);
        }


    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track == null) {
            return;
        }
        this.mTrackTitleText = track.getTrackTitle();
        if (mTrackTitleTv != null) {
            //设置当前的节目的标题
            mTrackTitleTv.setText(mTrackTitleText);
        }
        //当节目改变时，获取当前节目播放中的播放位置
        //当前的节目改变后，要修改页面的图片
        if (mTrackPagerView != null) {
            mTrackPagerView.setCurrentItem(playIndex, true);
        }

        //设置播放器列表里当前播放位置
        if (mSobPopWindow != null) {
            mSobPopWindow.setCurrentPlayPosition(playIndex);
        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {
        mSobPopWindow.updateOrderIcon(isReverse);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        if (mPlayPresenter != null && mIsUserSlidePager) {

            mPlayPresenter.playByIndex(i);
        }
        mIsUserSlidePager = false;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
