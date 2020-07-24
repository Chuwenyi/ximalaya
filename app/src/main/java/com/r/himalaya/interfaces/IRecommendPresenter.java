package com.r.himalaya.interfaces;

import com.r.himalaya.base.IBasePresenter;

public interface IRecommendPresenter extends IBasePresenter<IRecommendViewCallback> {
/**
 * 获取推荐内容
 */
    void getRecommendList();
/**
 * 下来刷新
 */
    void pull2RefreshMore();
    /**
     * 上拉加载更多
     */
    void loadMore();


}
