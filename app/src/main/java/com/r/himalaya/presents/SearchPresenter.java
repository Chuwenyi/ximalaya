package com.r.himalaya.presents;

import com.r.himalaya.data.XimalayApi;
import com.r.himalaya.interfaces.ISearchCallback;
import com.r.himalaya.interfaces.ISearchPresenter;
import com.r.himalaya.utils.Constants;
import com.r.himalaya.utils.LogUtil;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.SearchAlbumList;
import com.ximalaya.ting.android.opensdk.model.word.HotWord;
import com.ximalaya.ting.android.opensdk.model.word.HotWordList;
import com.ximalaya.ting.android.opensdk.model.word.QueryResult;
import com.ximalaya.ting.android.opensdk.model.word.SuggestWords;

import java.util.ArrayList;
import java.util.List;

public class SearchPresenter implements ISearchPresenter {
    private List<Album> mSearchResult = new ArrayList<>();
    private static final String TAG = "SearchPresenter";
    //当前搜索的关键字
    private String mCurrentKeyword = null;
    private final XimalayApi mXimalayApi;
    private static final int DEFAULT_PAGE = 1;
    private int mCurrentPage = DEFAULT_PAGE;

    private SearchPresenter() {
        mXimalayApi = XimalayApi.getXimalayApi();
    }

    private static SearchPresenter sSearchPresenter = null;

    public static SearchPresenter getSearchPresenter() {
        if (sSearchPresenter == null) {
            synchronized (SearchPresenter.class) {
                if (sSearchPresenter == null) {
                    sSearchPresenter = new SearchPresenter();
                }
            }
        }
        return sSearchPresenter;
    }

    private List<ISearchCallback> mCallback = new ArrayList<>();

    @Override
    public void doSearch(String keyword) {
        mCurrentPage = DEFAULT_PAGE;
        mSearchResult.clear();
        //用于重新搜索
        //网络不好好时，点击重新搜索
        this.mCurrentKeyword = keyword;
        search(keyword);


    }

    private void search(String keyword) {
        mXimalayApi.searchByKeyword(keyword, mCurrentPage, new IDataCallBack<SearchAlbumList>() {
            @Override
            public void onSuccess(SearchAlbumList searchAlbumList) {
                List<Album> albums = searchAlbumList.getAlbums();
                mSearchResult.addAll(albums);
                if (albums != null) {
                    LogUtil.d(TAG, "albums size-----------" + albums.size());
                    if (mIsLoadMore) {
                        for (ISearchCallback iSearchCallback : mCallback) {
                            if (albums.size() == 0) {

                                iSearchCallback.onLoadMoreResult(mSearchResult, false);
                            } else {
                                iSearchCallback.onLoadMoreResult(mSearchResult, true);

                            }
                        }
                        mIsLoadMore = false;
                    } else {

                        for (ISearchCallback iSearchCallback : mCallback) {
                            iSearchCallback.onSearchResultLoaded(mSearchResult);
                        }
                    }
                } else {
                    LogUtil.d(TAG, "albums is null....");
                }

            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "errorCode----" + i);
                LogUtil.d(TAG, "errorMes----" + s);

                for (ISearchCallback iSearchCallback : mCallback) {
                    if (mIsLoadMore) {
                        iSearchCallback.onLoadMoreResult(mSearchResult, false);
                        mCurrentPage--;
                        mIsLoadMore = false;
                    } else {

                        iSearchCallback.onError(i, s);
                    }
                }

            }
        });
    }

    @Override
    public void reSearch() {
        search(mCurrentKeyword);
    }

    private boolean mIsLoadMore = false;

    @Override
    public void loadMore() {
        //判断有没有必要加载更多
        if (mSearchResult.size() < Constants.COUNT_DEFAULT) {
            for (ISearchCallback iSearchCallback : mCallback) {
                iSearchCallback.onLoadMoreResult(mSearchResult, false);
            }
        } else {

            mIsLoadMore = true;
            mCurrentPage++;
            search(mCurrentKeyword);
        }

    }

    @Override
    public void getHotWord() {
        //做一个热搜缓存

        mXimalayApi.getHotWords(new IDataCallBack<HotWordList>() {
            @Override
            public void onSuccess(HotWordList hotWordList) {
                if (hotWordList != null) {
                    List<HotWord> hotWords = hotWordList.getHotWordList();
                    LogUtil.d(TAG, "hotWords size-----" + hotWords.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onHotWordLoaded(hotWords);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "errorCode----" + i);
                LogUtil.d(TAG, "errorMes----" + s);
            }
        });
    }

    @Override
    public void getRecommendWord(String keyword) {
        mXimalayApi.getSuggestWord(keyword, new IDataCallBack<SuggestWords>() {
            @Override
            public void onSuccess(SuggestWords suggestWords) {
                if (suggestWords != null) {
                    List<QueryResult> keyWordList = suggestWords.getKeyWordList();
                    LogUtil.d(TAG, "keyWordList size---------" + keyWordList.size());
                    for (ISearchCallback iSearchCallback : mCallback) {
                        iSearchCallback.onRecommendWordLoaded(keyWordList);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                LogUtil.d(TAG, "errorCode----" + i);
                LogUtil.d(TAG, "errorMes----" + s);
            }
        });
    }

    @Override
    public void registerViewCallback(ISearchCallback iSearchCallback) {
        if (!mCallback.contains(iSearchCallback)) {
            mCallback.add(iSearchCallback);
        }
    }

    @Override
    public void unRegisterViewCallback(ISearchCallback iSearchCallback) {
        mCallback.remove(iSearchCallback);
    }
}
