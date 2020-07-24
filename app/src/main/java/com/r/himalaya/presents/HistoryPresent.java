package com.r.himalaya.presents;

import com.r.himalaya.base.BaseApplication;
import com.r.himalaya.data.HistoryDao;
import com.r.himalaya.data.IHistoryDao;
import com.r.himalaya.data.IHistoryDaoCallback;
import com.r.himalaya.interfaces.IHistoryCallback;
import com.r.himalaya.interfaces.IHistoryPresent;
import com.r.himalaya.utils.Constants;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * 历史记录最多100条，大于100条删除前面的进行添加
 */
public class HistoryPresent implements IHistoryPresent, IHistoryDaoCallback {
    private List<IHistoryCallback> mCallbacks = new ArrayList<>();
    private static volatile HistoryPresent singleton = null;
    private final IHistoryDao mHistoryDao;
    private List<Track> mCurrentHistories = null;
    private Track mCurrentAddTrack=null;

    private HistoryPresent() {
        mHistoryDao = new HistoryDao();
        mHistoryDao.setCallback(this);
        listHistories();
    }

    public static HistoryPresent getInstance() {
        if (singleton == null) {
            synchronized (HistoryPresent.class) {
                if (singleton == null) {
                    singleton = new HistoryPresent();
                }
            }
        }
        return singleton;
    }

    @Override
    public void listHistories() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (mHistoryDao != null) {
                    mHistoryDao.listHistories();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    private boolean isDoDelAsOutOfSize = false;

    @Override
    public void addHistory(final Track track) {
        //判断记录是否大于100条
        if (mCurrentHistories != null && mCurrentHistories.size() >= Constants.MAX_HISTORY_COUNT) {
            isDoDelAsOutOfSize = true;
            this.mCurrentAddTrack=track;
            //先不添加，删除最前一条后添加
            delHistory(mCurrentHistories.get(mCurrentHistories.size() - 1));
        } else {

            doAddHistory(track);
        }
    }

    private void doAddHistory(final Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (mHistoryDao != null) {
                    mHistoryDao.addHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void delHistory(final Track track) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (mHistoryDao != null) {
                    mHistoryDao.delHistory(track);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void cleanHistory() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (mHistoryDao != null) {
                    mHistoryDao.clearHistory();
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void registerViewCallback(IHistoryCallback iHistoryCallback) {
        //ui注册过来
        if (!mCallbacks.contains(iHistoryCallback)) {
            mCallbacks.add(iHistoryCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(IHistoryCallback iHistoryCallback) {
        //删除ui回调
        mCallbacks.remove(iHistoryCallback);
    }

    @Override
    public void onHistoryAdd(boolean isSuccess) {
        //
        listHistories();
    }

    @Override
    public void onHistoryDel(boolean isSuccess) {
        //
        if (isDoDelAsOutOfSize &&mCurrentAddTrack!=null) {
            isDoDelAsOutOfSize=false;
        //  添加当前数据进数据库
            addHistory(mCurrentAddTrack);
        } else {
            listHistories();
        }

    }

    @Override
    public void onHistoriesLoaded(final List<Track> tracks) {
        this.mCurrentHistories = tracks;
        //通知ui更新数据
        BaseApplication.getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (IHistoryCallback callback : mCallbacks) {
                    callback.onHistoriesLoaded(tracks);
                }
            }
        });
    }

    @Override
    public void onHistoriesClean(boolean isSuccess) {
        //
        listHistories();

    }
}
