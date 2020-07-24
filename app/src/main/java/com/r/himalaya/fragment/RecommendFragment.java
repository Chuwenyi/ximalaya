package com.r.himalaya.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.r.himalaya.DetailActivity;
import com.r.himalaya.R;
import com.r.himalaya.adapter.AlbumListAdapter;
import com.r.himalaya.base.BaseFragment;
import com.r.himalaya.interfaces.IRecommendViewCallback;
import com.r.himalaya.presents.AlbumDetialPresenter;
import com.r.himalaya.presents.RecommendPresenter;
import com.r.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class RecommendFragment extends BaseFragment implements IRecommendViewCallback, UILoader.OnRetryClickListener, AlbumListAdapter.OnAlbumItemClickListener {

    private static final String TGA = "RecommendFragment";
    private View mRootView;
    private RecyclerView mRecommendRv;
    private AlbumListAdapter mRecommendListAdapter;
    private RecommendPresenter mRecommendPresenter;
    private UILoader mUiLoader;


    @Override
    protected View onSubViewLoaded(final LayoutInflater layoutInflater, ViewGroup container) {
        mUiLoader = new UILoader(getContext()) {
            @Override
            protected View getSuccessView(ViewGroup container) {
                return createSuccessView(layoutInflater, container);
            }
        };


        //获取到逻辑层的对象

        mRecommendPresenter = RecommendPresenter.getInstance();
        //先设置通知接口的注册
        mRecommendPresenter.registerViewCallback(this);
        //获取推荐列表
        mRecommendPresenter.getRecommendList();
        if (mUiLoader.getParent() instanceof ViewGroup) {
            ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
        }

        mUiLoader.setOnRetryClickListener(this);
        //返回view，给界面显示
        return mUiLoader;
    }

    private View createSuccessView(LayoutInflater layoutInflater, ViewGroup container) {
        //view加载完成
        mRootView = layoutInflater.inflate(R.layout.fragment_recommend, container, false);

        //recyclerview使用
        mRecommendRv = mRootView.findViewById(R.id.recommend_list);
        TwinklingRefreshLayout twinklingRefreshLayout = mRootView.findViewById(R.id.over_scroll_view);
        twinklingRefreshLayout.setPureScrollModeOn();
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecommendRv.setLayoutManager(linearLayoutManager);
        mRecommendRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);

            }
        });
        //设置适配器
        mRecommendListAdapter = new AlbumListAdapter();
        mRecommendRv.setAdapter(mRecommendListAdapter);
        mRecommendListAdapter.setOnAlbumItemClickListener(this);
        return mRootView;
    }


    @Override
    public void onRecommendListLoaded(List<Album> result) {
        //当获取到推荐内容，这个方法被调用成功
        //数据回来后更新ui
        //把数据设置给适配器，并更新ui
        mRecommendListAdapter.setData(result);
        mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
    }

    @Override
    public void onNetworkError() {
        mUiLoader.updateStatus(UILoader.UIStatus.NETWORK_ERROR);
    }

    @Override
    public void onEmpty() {
        mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
    }

    @Override
    public void onLoading() {
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口注册，避免内存泄漏
        if (mRecommendPresenter != null) {
            mRecommendPresenter.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onRetryClick() {
        //网络不佳的时候，用户点击屏幕重试
        //重新获取数据
        if (mRecommendPresenter != null) {
            mRecommendPresenter.getRecommendList();
        }
    }

    @Override
    public void OnItemClick(int position, Album album) {
        AlbumDetialPresenter.getInstance().setTargetAlbum(album);

        //根据位置拿到数据
        //item被点击了,跳转到详情页面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);

    }

}
