package com.r.himalaya.presents;

import com.r.himalaya.data.XimalayApi;
import com.r.himalaya.interfaces.IRecommendPresenter;
import com.r.himalaya.interfaces.IRecommendViewCallback;
import com.r.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.GussLikeAlbumList;

import java.util.ArrayList;
import java.util.List;

public class RecommendPresenter implements IRecommendPresenter {
    private static final String TGA = "RecommendPresenter";
    private List<IRecommendViewCallback> mCallbacks = new ArrayList<>();
    private List<Album> mCurrentRecommend=null;
    private List<Album> mRecommendList;

    private RecommendPresenter() {

    }

    private static RecommendPresenter sInstance = null;

    /**
     * 获取单例对象
     *
     * @return
     */
    public static RecommendPresenter getInstance() {
        if (sInstance == null) {
            synchronized (RecommendPresenter.class) {
                if (sInstance == null) {
                    sInstance = new RecommendPresenter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取当前专辑推荐列表
     * @return
     */

    public List<Album>getCurrentRecommend(){
        return mCurrentRecommend;
    }

    /**
     * 获取推荐内容
     * 接口3.10.6 获取猜你喜欢专辑
     */
    @Override
    public void getRecommendList() {
        //如果内容不空的话，那么直接使用当前的内容
        if(mRecommendList != null && mRecommendList.size() > 0) {
            handlerRecommendResult(mRecommendList);
            return;
        }
        //获取推荐内容
        //封装参数
        updateLoading();
        XimalayApi ximalayApi = XimalayApi.getXimalayApi();
        ximalayApi.getRecommendList(new IDataCallBack<GussLikeAlbumList>() {
            @Override
            public void onSuccess(GussLikeAlbumList gussLikeAlbumList) {
                //数据获取成功
                if (gussLikeAlbumList != null) {
                    mRecommendList = gussLikeAlbumList.getAlbumList();
                    //数据回来后更新ui
                    // upRecommendUI(albumList);
                    handlerRecommendResult(mRecommendList);
                }
            }

            @Override
            public void onError(int i, String s) {
                //数据获取出错
                LogUtil.d(TGA, "error-----" + i);
                LogUtil.d(TGA, "error msg-----" + s);
                handlerError();

            }
        });

    }

    private void handlerError() {
        if (mCallbacks != null) {
            for (IRecommendViewCallback callback : mCallbacks) {
                callback.onNetworkError();

            }
        }
    }

    private void handlerRecommendResult(List<Album> albumList) {
        //通知ui更新
        if (albumList != null) {
            //测试数据为空
            // albumList.clear();
            if (albumList.size() == 0) {
                for (IRecommendViewCallback callback : mCallbacks) {
                    callback.onEmpty();
                }
            } else {
                for (IRecommendViewCallback callback : mCallbacks) {
                    callback.onRecommendListLoaded(albumList);
                }
                this.mCurrentRecommend=albumList;
            }
        }

    }

    private void updateLoading() {
        for (IRecommendViewCallback callback : mCallbacks) {
            callback.onLoading();

        }

    }


    @Override
    public void pull2RefreshMore() {

    }

    @Override
    public void loadMore() {

    }

    @Override
    public void registerViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
        }
    }

    @Override
    public void unRegisterViewCallback(IRecommendViewCallback callback) {
        if (mCallbacks != null) {
            mCallbacks.remove(callback);
        }
    }
}
