package com.r.himalaya.interfaces;

import com.r.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayListControl;

public interface IPlayerPresenter extends IBasePresenter<IPlayCallback> {
    /**
     * 播放
     */
    void play();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 上一首
     */
    void playPre();

    /**
     * 下一首
     */
    void playNext();

    /**
     * 切换播放模式
     * @param mode
     */
    void switchPlayMode(XmPlayListControl.PlayMode mode);

    /**
     * 获取播放列表
     */
    void getPlayList();

    /**
     * 根据节目位置进行播放
     */
    void playByIndex(int index);

    /**
     * 切换播放进度
     * @param progress
     */
    void seekTo(int progress);

    /**
     * 判断播放器是否在播放
     * @return
     */
    boolean isPlaying();

    /**
     * 把播放列表反转
     */
    void reversPlayList();

    /**
     * 播放专辑第一首
     * @param id
     */
    void playByAlbumId(long id);
}
