package com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kemov.vam.bluetoothtest.R;

import java.util.List;

/**
 * Created by RuanJian-GuoYong on 2017/10/27.
 */

public abstract class RecyclerViewCommonAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;

    public RecyclerViewCommonAdapter(Context context, int resId, List<T> mDatas) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mLayoutId = resId;
        this.mDatas = mDatas;
    }
    public abstract void convert(ViewHolder holder, T t);

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = ViewHolder.get(mContext, parent, mLayoutId);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        convert(holder, mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setData(List<T> mDatas){
        this.mDatas = mDatas;
    }
}
