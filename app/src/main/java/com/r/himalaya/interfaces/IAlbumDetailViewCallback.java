package com.r.himalaya.interfaces;

import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

public interface IAlbumDetailViewCallback {
    /**
     * 专辑详情内容加载出来
     * @param tracks
     */

    void onDetailListLoad(List<Track> tracks);

    /**
     * 把album传给ui
     * @param album
     */
    void onAlbumLoaded(Album album);

    /**
     * 加载更多的结果
     * @param size true表示成功，false表示失败
     */
    void onLoaderMoreFinished(int size);


    /**
     * 下拉加载更多的结果
     * @param size size>0 true表示成功，false表示失败
     */
    void onRefreshFinished(int size);
}
