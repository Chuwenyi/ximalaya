package com.r.himalaya.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.r.himalaya.R;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.InnerHolder> {
    private List<Album> mData=new ArrayList<>();
    private OnAlbumItemClickListener mItemClickListener=null;
    private OnAlbumItemLongClickListener mLongClickListener=null;

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //载入view
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommend,parent,false);

        return new InnerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, final int position) {
        //设置数据
        holder.itemView.setTag(position);
        holder.setData(mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener!=null) {
                int ClickPosition=    (int) v.getTag();
                    mItemClickListener.OnItemClick(ClickPosition,mData.get(ClickPosition));
                }

            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener != null) {
                    int ClickPosition=    (int) v.getTag();
                    mLongClickListener.onItemLongClick(mData.get(ClickPosition));
                }
                //true表示消费该事件
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        //返回要显示的个数
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public void setData(List<Album> albumList) {
        if (mData != null) {
            mData.clear();
            mData.addAll(albumList);

        }
        //更新ui
        notifyDataSetChanged();
    }


    public class InnerHolder extends RecyclerView.ViewHolder {
        public InnerHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setData(Album album) {
            //找到各个控件，设置数据
            //专辑封面
            ImageView albumCoverIv=itemView.findViewById(R.id.album_cover);
            //title
            TextView albumTitleTv=itemView.findViewById(R.id.album_title_tv);
            //描述
            TextView albumDesTv=itemView.findViewById(R.id.album_description_tv);
            //播放数量
            TextView albumPlayCountTv=itemView.findViewById(R.id.album_play_count);
            //专辑内容数量
            TextView albumContentCountTv=itemView.findViewById(R.id.album_content_size);

            albumTitleTv.setText(album.getAlbumTitle());
            albumDesTv.setText(album.getAlbumIntro());
            albumPlayCountTv.setText(album.getPlayCount()+"");
            albumContentCountTv.setText(album.getIncludeTrackCount()+"");

            Glide.with(itemView.getContext()).load(album.getCoverUrlLarge()).into(albumCoverIv);

        }
    }
    public void setOnAlbumItemClickListener(OnAlbumItemClickListener listener){
        this.mItemClickListener=listener;

    }

    public interface OnAlbumItemClickListener {
        void OnItemClick(int position, Album album);
    }

    public void setOnAlbumItemLongClickListener(OnAlbumItemLongClickListener listener){
        this.mLongClickListener=listener;
    }

    //item长按接口
    public interface OnAlbumItemLongClickListener{
        void onItemLongClick(Album album);
    }
}
