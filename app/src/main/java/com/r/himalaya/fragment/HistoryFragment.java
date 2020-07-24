package com.r.himalaya.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.r.himalaya.PlayActivity;
import com.r.himalaya.R;
import com.r.himalaya.adapter.TrackListAdapter;
import com.r.himalaya.base.BaseApplication;
import com.r.himalaya.base.BaseFragment;
import com.r.himalaya.interfaces.IHistoryCallback;
import com.r.himalaya.presents.HistoryPresent;
import com.r.himalaya.presents.PlayPresenter;
import com.r.himalaya.views.ConfirmCheckBoxDialog;
import com.r.himalaya.views.UILoader;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.util.List;

public class HistoryFragment extends BaseFragment implements IHistoryCallback, TrackListAdapter.ItemClickListener, TrackListAdapter.ItemLongClickListener, ConfirmCheckBoxDialog.OnDialogActionClickListener {

    private UILoader mUiLoader;
    private TrackListAdapter mTrackListAdapter;
    private HistoryPresent mHistoryPresent;
    private Track mCurrentClickHistoryItem = null;

    @Override
    protected View onSubViewLoaded(LayoutInflater layoutInflater, ViewGroup container) {
        FrameLayout rootView = (FrameLayout) layoutInflater.inflate(R.layout.fragment_history, container, false);
        if (mUiLoader == null) {
            mUiLoader = new UILoader(BaseApplication.getAppContext()) {
                @Override
                protected View getSuccessView(ViewGroup container) {
                    return createSuccessView(container);
                }

                @Override
                protected View getEmptyView() {
                    View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_empty_view, this, false);
                    TextView tips = emptyView.findViewById(R.id.empty_view_tips_tv);
                    tips.setText("没有历史记录呢！");
                    return emptyView;
                }
            };
        } else {
            if (mUiLoader.getParent() instanceof ViewGroup) {
                ((ViewGroup) mUiLoader.getParent()).removeView(mUiLoader);

            }
        }
        mHistoryPresent = HistoryPresent.getInstance();
        mHistoryPresent.registerViewCallback(this);
        mUiLoader.updateStatus(UILoader.UIStatus.LOADING);
        mHistoryPresent.listHistories();
        rootView.addView(mUiLoader);
        return rootView;
    }

    private View createSuccessView(ViewGroup container) {
        View successView = LayoutInflater.from(container.getContext()).inflate(R.layout.item_history, container, false);
        RecyclerView historyList = successView.findViewById(R.id.history_list);
        TwinklingRefreshLayout refreshLayout = successView.findViewById(R.id.over_scroll_view);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        refreshLayout.setEnableOverScroll(true);
        historyList.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mTrackListAdapter = new TrackListAdapter();
        historyList.setAdapter(mTrackListAdapter);
        mTrackListAdapter.setItemLongClickListener(this);
        mTrackListAdapter.setItemClickListener(this);
        historyList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.top = UIUtil.dip2px(view.getContext(), 2);
                outRect.bottom = UIUtil.dip2px(view.getContext(), 2);
                outRect.left = UIUtil.dip2px(view.getContext(), 2);
                outRect.right = UIUtil.dip2px(view.getContext(), 2);
            }
        });
        return successView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHistoryPresent != null) {
            mHistoryPresent.unRegisterViewCallback(this);
        }
    }

    @Override
    public void onHistoriesLoaded(List<Track> tracks) {
        if (tracks.size()==0||tracks==null) {
            mUiLoader.updateStatus(UILoader.UIStatus.EMPTY);
        }else {
            //更新数据
            mTrackListAdapter.setDate(tracks);
            mUiLoader.updateStatus(UILoader.UIStatus.SUCCESS);
        }
    }

    @Override
    public void onItemClick(List<Track> detailData, int position) {
        //设置播放器的数据
        PlayPresenter playerPresenter = PlayPresenter.getPlayPresenter();
        playerPresenter.setPlayList(detailData, position);
        //跳转到播放器界面
        Intent intent = new Intent(getActivity(), PlayActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(Track track) {
        this.mCurrentClickHistoryItem = track;
        //删除历史
        ConfirmCheckBoxDialog dialog = new ConfirmCheckBoxDialog(getActivity());
        dialog.setOnDialogActionClickListener(this);
        dialog.show();
    }

    @Override
    public void onCancelClick() {
        //取消
    }

    @Override
    public void onConfirmClick(boolean isCheck) {
        //确定，删除历史
        if (mHistoryPresent != null && mCurrentClickHistoryItem != null) {
            if (!isCheck) {
                mHistoryPresent.delHistory(mCurrentClickHistoryItem);
            } else {
                mHistoryPresent.cleanHistory();
            }
        }
    }
}
