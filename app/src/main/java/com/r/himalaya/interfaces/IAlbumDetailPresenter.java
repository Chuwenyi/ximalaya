package com.r.himalaya.interfaces;

import com.r.himalaya.base.IBasePresenter;

public interface IAlbumDetailPresenter extends IBasePresenter<IAlbumDetailViewCallback> {

    /**
     * 下来刷新
     */
    void pull2RefreshMore();
    /**
     * 上拉加载更多
     */
    void loadMore();

    /**
     * 获取专辑详情
     * @param album
     * @param page
     */
    void getAlbumDetail(int album,int page);



}
