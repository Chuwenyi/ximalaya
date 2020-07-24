package com.r.himalaya.presents;

import com.r.himalaya.data.XimalayApi;
import com.r.himalaya.interfaces.IAlbumDetailPresenter;
import com.r.himalaya.interfaces.IAlbumDetailViewCallback;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetialPresenter implements IAlbumDetailPresenter {

    private List<IAlbumDetailViewCallback> mCallbacks = new ArrayList<>();
    private List<Track> mTracks = new ArrayList<>();

    private Album mtargetAlbum = null;
    //当前专辑id
    private int mCurrentAlbumId = -1;
    //当前页
    private int mCurrentPageIndex = 0;

    private AlbumDetialPresenter() {

    }

    private static AlbumDetialPresenter sInstance = null;

    public static AlbumDetialPresenter getInstance() {
        if (sInstance == null) {
            synchronized (AlbumDetialPresenter.class) {
                if (sInstance == null) {
                    sInstance = new AlbumDetialPresenter();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {
        //去加载更多内容
        mCurrentPageIndex++;
        //传入true加载内容
        doLoaded(true);

    }

    private void doLoaded(final boolean isLoadMore) {
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getAlbumDetail(new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null) {
                    List<Track> tracks = trackList.getTracks();
                    if (isLoadMore) {
                        //上拉加载，结果放到后面
                        mTracks.addAll(mTracks.size() - 1, tracks);
                        int size = tracks.size();
                        handlerLoaderMoreResult(size);
                    } else {
                        //下拉加载，放到前面去
                        mTracks.addAll(0, tracks);
                    }
                    handlerAlbumDetailResult(mTracks);
                }

            }

            @Override
            public void onError(int i, String s) {
                if (isLoadMore) {
                    mCurrentPageIndex--;
                }
            }
        }, mCurrentAlbumId, mCurrentPageIndex);
    }

    /**
     * 处理加载更多的结果
     *
     * @param size
     */

    private void handlerLoaderMoreResult(int size) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onLoaderMoreFinished(size);
        }
    }

    @Override
    public void getAlbumDetail(int albumId, int page) {
        mTracks.clear();
        this.mCurrentAlbumId = albumId;
        this.mCurrentPageIndex = page;
        //根据页面和专辑id获取数据
        doLoaded(false);

    }

    private void handlerAlbumDetailResult(List<Track> tracks) {
        for (IAlbumDetailViewCallback callback : mCallbacks) {
            callback.onDetailListLoad(tracks);

        }
    }

    @Override
    public void registerViewCallback(IAlbumDetailViewCallback detailViewCallback) {

        if (!mCallbacks.contains(detailViewCallback)) {
            mCallbacks.add(detailViewCallback);
            if (mtargetAlbum != null) {
                detailViewCallback.onAlbumLoaded(mtargetAlbum);
            }

        }
    }

    @Override
    public void unRegisterViewCallback(IAlbumDetailViewCallback detailViewCallback) {
        mCallbacks.remove(detailViewCallback);
    }

    public void setTargetAlbum(Album targetAlbum) {
        this.mtargetAlbum = targetAlbum;
    }
}
