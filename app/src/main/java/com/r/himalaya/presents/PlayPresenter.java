package com.r.himalaya.presents;

import android.content.Context;
import android.content.SharedPreferences;

import com.r.himalaya.base.BaseApplication;
import com.r.himalaya.data.XimalayApi;
import com.r.himalaya.interfaces.IPlayCallback;
import com.r.himalaya.interfaces.IPlayerPresenter;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.advertis.Advertis;
import com.ximalaya.ting.android.opensdk.model.advertis.AdvertisList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.advertis.IXmAdsStatusListener;
import com.ximalaya.ting.android.opensdk.player.constants.PlayerConstants;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl.PlayMode.PLAY_MODEL_LIST;

public class PlayPresenter implements IPlayerPresenter, IXmAdsStatusListener, IXmPlayerStatusListener {
    private static final String TAG = "PlayPresenter";
    private List<IPlayCallback> mIPlayCallbacks = new ArrayList<>();
    private final XmPlayerManager mPlayerManager;
    private Track mCurrentTrack;
    public static final int DEFAULT_PLAY_INDEX=0;
    private int mCurrentIndex = DEFAULT_PLAY_INDEX;
    private final SharedPreferences mPlayModsp;
    private XmPlayListControl.PlayMode mCurrentPlayMode = PLAY_MODEL_LIST;
    private boolean mIsReverse = false;
    //
//    PLAY_MODEL_LIST
//    PLAY_MODEL_LIST_LOOP
//    PLAY_MODEL_RANDOM
//    PLAY_MODEL_SINGLE_LOOP
    public static final int PLAY_MODEL_LIST_INT = 0;
    public static final int PLAY_MODEL_LIST_LOOP_INT = 1;
    public static final int PLAY_MODEL_RANDOM_INT = 2;
    public static final int PLAY_MODEL_SINGLE_LOOP_INT = 3;

    //sp key and name
    public static final String PLAY_MODE_SP_NAME = "playMod";
    public static final String PLAY_MODE_SP_KEY = "currentPlayMod";
    private int mProgressDuration=0;
    private int mCurrentProgressPosition=0;

    private PlayPresenter() {

        mPlayerManager = XmPlayerManager.getInstance(BaseApplication.getAppContext());
        //广告接口
        mPlayerManager.addAdsStatusListener(this);
        //播放器状态接口
        mPlayerManager.addPlayerStatusListener(this);
        //需要记住当前的播放模式
        mPlayModsp = BaseApplication.getAppContext().getSharedPreferences(PLAY_MODE_SP_NAME, Context.MODE_PRIVATE);


    }

    private static PlayPresenter sPlayPresenter;

    public static PlayPresenter getPlayPresenter() {

        if (sPlayPresenter == null) {
            synchronized (PlayPresenter.class) {
                if (sPlayPresenter == null) {
                    sPlayPresenter = new PlayPresenter();
                }
            }
        }
        return sPlayPresenter;
    }

    private boolean isPlayListSet = false;

    public void setPlayList(List<Track> list, int playIndex) {
        if (mPlayerManager != null) {
            mPlayerManager.setPlayList(list, playIndex);
            isPlayListSet = true;
            mCurrentTrack = list.get(playIndex);
            mCurrentIndex = playIndex;
        }


    }

    @Override
    public void play() {
        if (isPlayListSet) {
            mPlayerManager.play();
        }

    }

    @Override
    public void pause() {
        if (mPlayerManager != null) {
            mPlayerManager.pause();
        }

    }

    @Override
    public void stop() {

    }

    @Override
    public void playPre() {
        //播放上一首
        if (mPlayerManager != null) {
            mPlayerManager.playPre();
        }

    }

    @Override
    public void playNext() {
        //播放下一首
        if (mPlayerManager != null) {
            mPlayerManager.playNext();
        }

    }

    /**
     * 判断是否有播放列表
     *
     * @return
     */
    public boolean hasPlayList() {
        return isPlayListSet;
    }

    @Override
    public void switchPlayMode(XmPlayListControl.PlayMode mode) {
        if (mPlayerManager != null) {
            mCurrentPlayMode = mode;
            mPlayerManager.setPlayMode(mode);
            //通知ui更新播放模式
            for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
                iPlayCallback.onPlayModeChange(mode);
            }
            //保存到sp里
            SharedPreferences.Editor edit = mPlayModsp.edit();
            edit.putInt(PLAY_MODE_SP_KEY, getIntByPlayMode(mode));
            edit.commit();
        }

    }

    private int getIntByPlayMode(XmPlayListControl.PlayMode mode) {
        switch (mode) {
            case PLAY_MODEL_SINGLE_LOOP:
                return PLAY_MODEL_SINGLE_LOOP_INT;
            case PLAY_MODEL_LIST_LOOP:
                return PLAY_MODEL_LIST_LOOP_INT;
            case PLAY_MODEL_RANDOM:
                return PLAY_MODEL_RANDOM_INT;
            case PLAY_MODEL_LIST:
                return PLAY_MODEL_LIST_INT;
        }
        return PLAY_MODEL_LIST_INT;
    }

    private XmPlayListControl.PlayMode getModByInt(int index) {
        switch (index) {
            case PLAY_MODEL_SINGLE_LOOP_INT:
                return XmPlayListControl.PlayMode.PLAY_MODEL_SINGLE_LOOP;
            case PLAY_MODEL_LIST_LOOP_INT:
                return XmPlayListControl.PlayMode.PLAY_MODEL_LIST_LOOP;
            case PLAY_MODEL_RANDOM_INT:
                return XmPlayListControl.PlayMode.PLAY_MODEL_RANDOM;
            case PLAY_MODEL_LIST_INT:
                return PLAY_MODEL_LIST;
        }
        return PLAY_MODEL_LIST;
    }

    @Override
    public void getPlayList() {
        if (mPlayerManager != null) {

            List<Track> playList = mPlayerManager.getPlayList();
            for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
                iPlayCallback.onListLoaded(playList);
            }
        }

    }

    @Override
    public void playByIndex(int index) {
        if (mPlayerManager != null) {
            mPlayerManager.play(index);
        }
    }

    @Override
    public void seekTo(int progress) {
        //更新播放器的进度
        mPlayerManager.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        //返回当前是否在播放
        return mPlayerManager.isPlaying();
    }

    @Override
    public void reversPlayList() {
        //把播放列表反转
        List<Track> playList = mPlayerManager.getPlayList();
        Collections.reverse(playList);
        mIsReverse = !mIsReverse;
        //第一个参数是播放列表，第二个参数是开始播放的下标
        mCurrentIndex = playList.size() - 1 - mCurrentIndex;
        mPlayerManager.setPlayList(playList, mCurrentIndex);
        //更新ui
        mCurrentTrack = (Track) mPlayerManager.getCurrSound();
        for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
            iPlayCallback.onListLoaded(playList);
            iPlayCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            iPlayCallback.updateListOrder(mIsReverse);
        }
    }

    @Override
    public void playByAlbumId(long id) {
        //获取要播放的列表内容
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                //把专辑内容设置给播放器
                List<Track> tracks = trackList.getTracks();
                if (trackList != null&&tracks.size()>0) {
                    mPlayerManager.setPlayList(tracks,DEFAULT_PLAY_INDEX);
                    isPlayListSet = true;
                    mCurrentTrack = tracks.get(DEFAULT_PLAY_INDEX);
                    mCurrentIndex = DEFAULT_PLAY_INDEX;
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        }, (int) id, 1);


    }


    //-----------------------广告回调方法
    @Override
    public void onStartGetAdsInfo() {

    }

    @Override
    public void onGetAdsInfo(AdvertisList advertisList) {

    }

    @Override
    public void onAdsStartBuffering() {

    }

    @Override
    public void onAdsStopBuffering() {

    }

    @Override
    public void onStartPlayAds(Advertis advertis, int i) {

    }

    @Override
    public void onCompletePlayAds() {

    }

    @Override
    public void onError(int i, int i1) {

    }
    //----------------播放器状态

    @Override
    public void onPlayStart() {
        for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
            iPlayCallback.onPlayStart();
        }

    }

    @Override
    public void onPlayPause() {
        for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
            iPlayCallback.onPlayPause();
        }
    }

    @Override
    public void onPlayStop() {

        for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
            iPlayCallback.onPlayStop();
        }

    }

    @Override
    public void onSoundPlayComplete() {

    }

    @Override
    public void onSoundPrepared() {
        mPlayerManager.setPlayMode(mCurrentPlayMode);
        if (mPlayerManager.getPlayerStatus() == PlayerConstants.STATE_PREPARED) {
            //播放器准备好了，可以播放
            mPlayerManager.play();
        }

    }

    @Override
    public void onSoundSwitch(PlayableModel lastModel, PlayableModel curModel) {
//        if (lastModel!=null) {
//
//        }
        //curModel代表当前播放内容
        //通过getKind（）方法来获取它是什么类型
        //track表示是track类型
        mCurrentIndex = mPlayerManager.getCurrentIndex();
        if (curModel instanceof Track) {
            Track currentTrack = (Track) curModel;
            mCurrentTrack = currentTrack;
            //保存播放记录
            HistoryPresent historyPresent=HistoryPresent.getInstance();
            historyPresent.addHistory(currentTrack);
            // LogUtil.d(TAG,"title----->"+currentTrack.getTrackTitle());
            //更新ui
            for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
                iPlayCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
            }
        }


    }

    @Override
    public void onBufferingStart() {

    }

    @Override
    public void onBufferingStop() {

    }

    @Override
    public void onBufferProgress(int i) {

    }

    @Override
    public void onPlayProgress(int currPos, int duration) {
        this.mCurrentProgressPosition=currPos;
        this.mProgressDuration=duration;
        //单位是毫秒
        for (IPlayCallback iPlayCallback : mIPlayCallbacks) {
            iPlayCallback.onProgressChange(currPos, duration);
        }

    }

    @Override
    public boolean onError(XmPlayerException e) {
        return false;
    }

    @Override
    public void registerViewCallback(IPlayCallback iPlayCallback) {
        if (!mIPlayCallbacks.contains(iPlayCallback)) {
            mIPlayCallbacks.add(iPlayCallback);
        }
        //更新之前，让ui的page有数据
        getPlayList();
        //通知当前的节目
        iPlayCallback.onTrackUpdate(mCurrentTrack, mCurrentIndex);
        iPlayCallback.onProgressChange(mCurrentProgressPosition,mProgressDuration);
        //更新状态
        handlePlayState(iPlayCallback);
        //从sp里拿
        int modIndex = mPlayModsp.getInt(PLAY_MODE_SP_KEY, PLAY_MODEL_LIST_INT);
        mCurrentPlayMode = getModByInt(modIndex);
        iPlayCallback.onPlayModeChange(mCurrentPlayMode);

    }

    private void handlePlayState(IPlayCallback iPlayCallback) {
        int playerStatus = mPlayerManager.getPlayerStatus();
        //根据状态调用接口的方法
        if (PlayerConstants.STATE_STARTED==playerStatus) {
            iPlayCallback.onPlayStart();
        }else {
            iPlayCallback.onPlayPause();
        }
    }

    @Override
    public void unRegisterViewCallback(IPlayCallback iPlayCallback) {
        mIPlayCallbacks.remove(iPlayCallback);

    }
}
