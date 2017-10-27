package com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by RuanJian-GuoYong on 2017/6/6.
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private SparseArray<View> mViews = null;
    private View mConvertView;

    public ViewHolder(Context context, View itemView, ViewGroup parent) {
        super(itemView);
        mContext = context;
        mConvertView = itemView;
        mViews = new SparseArray<View>();
    }

    public static ViewHolder get(Context context, ViewGroup parent, int layoutId)
    {

        View itemView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        ViewHolder holder = new ViewHolder(context, itemView, parent);
        return holder;
    }


    /**
     * 通过控件Id获取对应的控件，如果没有则加入view则加入！
     * @param viewId
     * @param <T>
     * @return
     */
    public <T extends View> T getView(int viewId){
        View view = mViews.get(viewId);
        if (view == null){
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    //添加一些常用参考的方法
    public ViewHolder setText(int viewId, String text){
        TextView view = getView(viewId);
        view.setText(text);
        return this;
    }

    public ViewHolder setTextColor(int viewId, int srcColor){
        TextView view = getView(viewId);
        view.setTextColor(srcColor);
        return this;
    }

    //通过drawable去加载图片
    public ViewHolder setImageResource(int viewId, int imageResId){
        ImageView view = getView(viewId);
        view.setImageResource(imageResId);
        return this;
    }
    //通过Bitmap去加载图片(无效？？？)
    public ViewHolder setImageBitmap(int viewId, Bitmap bmp){
        ImageView view = getView(viewId);
        view.setImageBitmap(bmp);
        return this;
    }

    //设置进度progress  //add by hyj 2017.09.26
    public ViewHolder setProgress(int viewId, int progress){
        ProgressBar view = getView(viewId);
        view.setProgress(progress);
        return this;
    }

    //条目中的很多控件中的某一个控件，对其设置点击事件
    public ViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    //对ListView中的一个条目，对其设置点击事件
    public ViewHolder setOnItemClickListener(AdapterView.OnClickListener  listener) {
        View view = mConvertView;//这里是不是需要设置成convertView呢？？？
        view.setOnClickListener (listener);
        return this;
    }

    public ViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }
}
