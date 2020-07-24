package com.r.himalaya.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.r.himalaya.DetailActivity;
import com.r.himalaya.R;
import com.r.himalaya.adapter.AlbumListAdapter;
import com.r.himalaya.base.BaseApplication;
import com.r.himalaya.base.BaseFragment;
import com.r.himalaya.interfaces.ISubscriptionCallback;
import com.r.himalaya.presents.AlbumDetialPresenter;
import com.r.himalaya.presents.SubscriptionPresenter;
import com.r.himalaya.utils.Constants;
import com.r.himalaya.views.ConfirmDialog;
import com.r.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class SubscriptionFragment extends BaseFragment implements ISubscriptionCallback, AlbumListAdapter.OnAlbumItemClickListener, AlbumListAdapter.OnAlbumItemLongClickListener, ConfirmDialog.OnDialogActionClickListener {

    private SubscriptionPresenter mSubscriptionPresenter;
    private RecyclerView mSubListView;
    private AlbumListAdapter mAlbumListAdapter;
    private Album mCurrentClickAlbum = null;
    private UILoader mUiLoader;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_subscription, container, false);
        if (mUiLoader == null) {

            mUiLoader = new UILoader(container.getContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView();
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tipsView=emptyView.findViewById(R.id.empty_view_tips_tv);
                    tipsView.setText("没有订阅内容，赶紧去订阅吧！");
                    return emptyView;
                }
            };
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);
            }
            rootView.addView(mUiLoader);
        }

        return rootView;
    }

    private View createSuccessView() {
        View itemView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.item_subscription, null);
        mSubListView = itemView.findViewById(R.id.sub_list);
        TwinklingRefreshLayout refreshLayout = itemView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        mSubListView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        //
        mAlbumListAdapter = new AlbumListAdapter();
        mAlbumListAdapter.setOnAlbumItemClickListener(this);
        mAlbumListAdapter.setOnAlbumItemLongClickListener(this);
        mSubListView.setAdapter(mAlbumListAdapter);
        mSubListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 5);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 5);
                outRect.left = UIUtil.dip2px(view.getContext(), 5);
                outRect.right = UIUtil.dip2px(view.getContext(), 5);

            }
        });
        mSubscriptionPresenter = SubscriptionPresenter.getInstance();
        mSubscriptionPresenter.registerViewCallback(this);
        mSubscriptionPresenter.getSubscriptionList();
        if (mUiLoader != null) {
            mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        }
        return itemView;
    }

    @Override
    public void onAddResult(boolean isSuccess) {

    }

    @Override
    public void onDeleteResult(boolean isSuccess) {
        //给出取消订阅的提示
        Toast.makeText(BaseApplication.getAppContext(), isSuccess ? "取消订阅成功" : "取消订阅失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscriptionsLoaded(List<Album> albums) {
        if (albums.size()==0) {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
            }
        }else {
            if (mUiLoader != null) {
                mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
            }
        }
        //更新ui
        if (mAlbumListAdapter != null) {
            mAlbumListAdapter.setData(albums);
        }

    }

    @Override
    public void onSubFull() {
        Toast.makeText(getActivity(), "订阅数量不得超过" + Constants.MAX_SUB_COUNT, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消接口注册，避免内存泄漏
        if (mSubscriptionPresenter != null) {
            mSubscriptionPresenter.unRegisterViewCallback(this);
        }
        mAlbumListAdapter.setOnAlbumItemClickListener(null);
    }

    @Override
    public void OnItemClick(int position, Album album) {
        AlbumDetialPresenter.getInstance().setTargetAlbum(album);

        //根据位置拿到数据
        //item被点击了,跳转到详情页面
        Intent intent = new Intent(getContext(), DetailActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Album album) {
        this.mCurrentClickAlbum = album;
        //订阅item被长按
        ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
        confirmDialog.setOnDialogActionClickListener(this);
        confirmDialog.show();
    }

    @Override
    public void onCancelSubClick() {
        //取消订阅
        if (mCurrentClickAlbum != null && mSubscriptionPresenter != null) {
            mSubscriptionPresenter.deleteSubscription(mCurrentClickAlbum);
        }
    }

    @Override
    public void onGiveUpClick() {
        //放弃取消订阅
    }
}
