package com.r.himalaya.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.r.himalaya.R;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TrackListAdapter extends RecyclerView.Adapter<TrackListAdapter.InnerHolder> {
    private List<Track> mDetailData= new  ArrayList<>();
    //格式化时间
    private SimpleDateFormat mSimpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mDurationFormat=new SimpleDateFormat("mm:ss");
    private ItemClickListener mItemClickListener=null;
    private ItemLongClickListener mItemLongClickListener=null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_detail, parent, false);
        return  new InnerHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        //找到控件，设置数据
        View itemView = holder.itemView;
        //顺序id
        TextView orderTv=itemView.findViewById(R.id.order_text);
        //标题
        TextView titleTv=itemView.findViewById(R.id.detail_item_title);
        //播放次数
        TextView playCountTv=itemView.findViewById(R.id.detail_item_play_count);
        //时长
        TextView durationTv=itemView.findViewById(R.id.detail_item_duration);
        //更新日期
        TextView updateDataTv=itemView.findViewById(R.id.detail_item_update_time);
        //设置数据
        final Track track = mDetailData.get(position);
        orderTv.setText((position+1)+"");
        titleTv.setText(track.getTrackTitle());
        playCountTv.setText(track.getPlayCount()+"");
        int durationMil= track.getDuration () * 1000;
        String duration=  mDurationFormat.format(durationMil);
        durationTv.setText(duration);
        String updateTimeText = mSimpleDateFormat.format(track.getUpdatedAt());
        updateDataTv.setText(updateTimeText);

        //设置item的点击事件
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mDetailData,position);

                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemLongClickListener != null) {
                    mItemLongClickListener.onItemLongClick(track);
                }
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return mDetailData.size();
    }

    public void setDate(List<Track> tracks) {
        //清除原来数据
        mDetailData.clear();
        //添加数据
        mDetailData.addAll(tracks);
        //更新ui
        notifyDataSetChanged();

    }

    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
    public void setItemClickListener(ItemClickListener listener){
        this.mItemClickListener=listener;
    }

    public interface ItemClickListener{
        void onItemClick(List<Track> detailData, int position);
    }
    public void setItemLongClickListener(ItemLongClickListener listener){
        this.mItemLongClickListener=listener;
    }

    public interface ItemLongClickListener{
        void onItemLongClick(Track track);
    }
}
