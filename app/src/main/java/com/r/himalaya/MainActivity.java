package com.r.himalaya;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.r.himalaya.adapter.IndicatorAdapter;
import com.r.himalaya.adapter.MainContentAdapter;
import com.r.himalaya.interfaces.IPlayCallback;
import com.r.himalaya.presents.PlayPresenter;
import com.r.himalaya.presents.RecommendPresenter;
import com.r.himalaya.utils.LogUtil;
import com.r.himalaya.views.RoundRectImageView;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

import java.util.List;

public class MainActivity extends FragmentActivity implements IPlayCallback {
    private static final String TAG = "MainActivity";
    private MagicIndicator mMagicIndicator;
    private ViewPager mContentPager;
    private IndicatorAdapter mIndicatorAdapter;
    private RoundRectImageView mRoundRectImageView;
    private TextView mHeaderTitle;
    private TextView mSubTitle;
    private ImageView mPlayControl;
    private PlayPresenter mPlayPresenter;
    private View mPlayControlItem;
    private View mSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        //
        initPresenter();
    }

    private void initPresenter() {
        mPlayPresenter = PlayPresenter.getPlayPresenter();
        mPlayPresenter.registerViewCallback(this);
    }

    private void initEvent() {
        mIndicatorAdapter.setOnIndicatorTopClickListener(new IndicatorAdapter.OnIndicatorTopClickListener() {
            @Override
            public void onTabClick(int index) {
                LogUtil.d(TAG, "click index is----" + index);
                if (mContentPager != null) {
                    mContentPager.setCurrentItem(index);
                }
            }
        });
        mPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayPresenter != null) {
                    boolean hasPlayList = mPlayPresenter.hasPlayList();
                    if (!hasPlayList) {
                        //没有播放列表默认播放第一个专辑
                        playFirstRecommend();
                    } else {

                        if (mPlayPresenter.isPlaying()) {
                            mPlayPresenter.pause();
                        } else {
                            mPlayPresenter.play();
                        }
                    }
                }
            }
        });
        mPlayControlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasPlayList = mPlayPresenter.hasPlayList();
                if (!hasPlayList) {
                    playFirstRecommend();
                }
                //跳转到播放列表
                startActivity(new Intent(MainActivity.this, PlayActivity.class));

            }
        });

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SearchActivity.class));
            }
        });
    }

    /**
     * 播放第一个专辑内容
     */

    private void playFirstRecommend() {
        List<Album> currentRecommend = RecommendPresenter.getInstance().getCurrentRecommend();
        if (currentRecommend != null && currentRecommend.size() > 0) {
            Album album = currentRecommend.get(0);
            long albumId = album.getId();
            mPlayPresenter.playByAlbumId(albumId);

        }
    }

    private void initView() {
        mMagicIndicator = this.findViewById(R.id.mian_indicaton);
        mMagicIndicator.setBackgroundColor(this.getResources().getColor(R.color.main_color));
        //创建适配器
        mIndicatorAdapter = new IndicatorAdapter(this);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdapter(mIndicatorAdapter);
        commonNavigator.setAdjustMode(true);
        //设置要显示的内容
        //viewpage
        mContentPager = this.findViewById(R.id.content_pager);
        //创建适配器
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        MainContentAdapter mainContentAdapter = new MainContentAdapter(supportFragmentManager);
        mContentPager.setAdapter(mainContentAdapter);
        //绑定indicator和viewpage
        mMagicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mMagicIndicator, mContentPager);

        //播放控制相关
        mRoundRectImageView = this.findViewById(R.id.main_track_cover);
        mHeaderTitle = this.findViewById(R.id.main_head_title);
        mHeaderTitle.setSelected(true);
        mSubTitle = this.findViewById(R.id.main_sub_title);
        mPlayControl = this.findViewById(R.id.main_play_control);
        mPlayControlItem = this.findViewById(R.id.main_play_control_item);
        //搜索
        mSearchBtn = this.findViewById(R.id.search_btn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayPresenter != null) {
            mPlayPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onPlayStart() {
        updatePlayControl(true);
    }

    private void updatePlayControl(boolean isPlaying) {
        if (mPlayControl != null) {
            mPlayControl.setImageResource(isPlaying ? R.drawable.selector_play_stop
                    : R.drawable.selector_play_play);
        }
    }

    @Override
    public void onPlayPause() {
        updatePlayControl(false);

    }

    @Override
    public void onPlayStop() {
        updatePlayControl(false);

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

    }

    @Override
    public void onPlayModeChange(XmPlayListControl.PlayMode PlayMode) {

    }

    @Override
    public void onProgressChange(int currentProgress, int total) {

    }

    @Override
    public void onAdLoading() {

    }

    @Override
    public void onAdFinished() {

    }

    @Override
    public void onTrackUpdate(Track track, int playIndex) {
        if (track != null) {
            String trackTitle = track.getTrackTitle();
            String nickname = track.getAnnouncer().getNickname();
            String coverUrlMiddle = track.getCoverUrlMiddle();
            if (mHeaderTitle != null) {
                mHeaderTitle.setText(trackTitle);
            }
            if (mSubTitle != null) {
                mSubTitle.setText(nickname);
            }
            // Picasso.with(this).load(coverUrlMiddle).into(mRoundRectImageView);
            Glide.with(this).load(coverUrlMiddle).into(mRoundRectImageView);

        }
    }

    @Override
    public void updateListOrder(boolean isReverse) {

    }
}
