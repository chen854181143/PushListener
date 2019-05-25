package com.chenyang.newspushmonitor.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chenyang.newspushmonitor.GlobalConfig;
import com.chenyang.newspushmonitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangmingxing on 18-1-31.
 */
public class NewsInfoAdapter extends RecyclerView.Adapter<NewsInfoAdapter.ViewHolder> {
    public static List<NewsInfoItem> mNewsInfoList = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mNewsInfo;
        public TextView tvMsg;

        public ViewHolder(View v) {
            super(v);
            mNewsInfo = v.findViewById(R.id.tv_title);
            tvMsg = v.findViewById(R.id.tv_msg);
        }
    }

    public void addNewsInfoItem(NewsInfoItem newsInfoItem) {
//        for (NewsInfoItem ni : mNewsInfoList) {
//            if (ni.packageName.equals(newsInfoItem.packageName)) {
//                ni.newsInfo = newsInfoItem.newsInfo;
//                return;
//            }
//        }

        if(GlobalConfig.QQ.equals(newsInfoItem.packageName)){
            if(mNewsInfoList.size()==0){
                mNewsInfoList.add(newsInfoItem);
            }else if(!mNewsInfoList.get(mNewsInfoList.size()-1).time.equals(newsInfoItem.time)){
                mNewsInfoList.add(newsInfoItem);
            }
            notifyDataSetChanged();
        }
    }

    public void updateMsg(String msg) {
        StringBuilder stringBuilder=new StringBuilder(mNewsInfoList.get(mNewsInfoList.size()-1).msg);
        stringBuilder.append(msg);
        mNewsInfoList.get(mNewsInfoList.size()-1).msg=stringBuilder.toString();
        notifyDataSetChanged();
    }



    public String getMsg() {
        return mNewsInfoList.get(mNewsInfoList.size()-1).msg;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_info_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mNewsInfo.setText(mNewsInfoList.get(position).newsInfo);
        holder.tvMsg.setText("备注信息:"+mNewsInfoList.get(position).msg);
    }

    @Override
    public int getItemCount() {
        return mNewsInfoList.size();
    }

}
