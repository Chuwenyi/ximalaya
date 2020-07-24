package com.r.himalaya.interfaces;

import com.r.himalaya.base.IBasePresenter;
import com.ximalaya.ting.android.opensdk.model.album.Album;

/**
 * 订阅有上限，不能超过100个
 */
public interface ISubscriptionPresent extends IBasePresenter<ISubscriptionCallback> {

    /**
     * 添加订阅
     * @param album
     */
    void addSubscription(Album album);

    /**
     * 删除订阅
     * @param album
     */
    void deleteSubscription(Album album);

    /**
     * 获取订阅列表
     */
    void getSubscriptionList();

    /**
     * 判断当前是否被订阅
     * @param album
     */
    boolean isSub(Album album);
}
