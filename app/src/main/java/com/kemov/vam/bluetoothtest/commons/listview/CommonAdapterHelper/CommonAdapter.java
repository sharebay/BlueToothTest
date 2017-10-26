package com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by RuanJian-GuoYong on 2017/6/6.
 */

public abstract class CommonAdapter<T> extends BaseAdapter {

    private LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;
    protected int mItemLayoutId;

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId){
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder,getItem(position));
        return viewHolder.getConvertView();
    }

    /**
     * viewHolder  中包含了很多其他子view，这里对这些子view进行改造，设置属性。
     * @param viewHolder
     * @param itemBean ：为mData的Bean类
     */
    public abstract void convert(ViewHolder viewHolder, T itemBean);


    private ViewHolder getViewHolder(int position, View convertView,
                                     ViewGroup parent){
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }

    //当执行onActivityResult()后，
    // 对adapter中的数据造成改变的时候需要刷新listView的时候，要重新设置mDatas
    public void setData(List<T> mDatas){
        this.mDatas = mDatas;
    }

}
