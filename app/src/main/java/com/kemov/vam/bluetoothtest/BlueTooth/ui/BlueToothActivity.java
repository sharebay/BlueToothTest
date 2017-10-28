package com.kemov.vam.bluetoothtest.BlueTooth.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kemov.vam.bluetoothtest.BlueTooth.presenter.BlueToothPresenterImpl;
import com.kemov.vam.bluetoothtest.BlueTooth.presenter.IBlueToothPresenter;
import com.kemov.vam.bluetoothtest.BlueTooth.view.IBlueToothView;
import com.kemov.vam.bluetoothtest.R;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.CommonAdapter;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.ViewHolder;
import com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper.RecyclerViewCommonAdapter;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
* 实现如下功能;
* 1.开启或关闭蓝牙
* 2.搜索附近的蓝牙
* 3.设置自己为可被搜索
* 4.长按列表item弹出选项(连接、创建配对、删除配对、从缓存中删除)
* */
public class BlueToothActivity extends AppCompatActivity implements IBlueToothView{
    private static final String TAG = "BlueToothActivity";
    LinearLayout bluetooth_rootLayout;
    SwitchButton switchButton;
    ListView lv_BlueToothDevices;
    SwipeRefreshLayout mSwipeRefreshWidget;
    RecyclerView recyclerView_BlueToothDevices;

    LinearLayoutManager mLayoutManager;
    CommonAdapter<BluetoothDevice> adpter;
    RecyclerViewCommonAdapter<BluetoothDevice> recyclerAdpter;
    List<BluetoothDevice> devices;

    private IBlueToothPresenter mIBlueToothPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);

        mIBlueToothPresenter = new BlueToothPresenterImpl(this,this);
        initView();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "gy.onResume: ");
        mIBlueToothPresenter.initBlueToothStatus();
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "gy.onStart: ");
        mIBlueToothPresenter.initBlueToothStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "gy.onPause: ");
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus)
            mIBlueToothPresenter.initBlueToothStatus();
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "gy.onRestart: ");
        super.onRestart();
    }

    private void initView() {
        bluetooth_rootLayout = (LinearLayout) findViewById(R.id.bluetooth_rootLayout);

        //初始化蓝牙列表
        recyclerView_BlueToothDevices = (RecyclerView) findViewById(R.id.recyclerView_BlueToothDevices);
        lv_BlueToothDevices = (ListView) findViewById(R.id.lv_BlueToothDevices);
        devices = mIBlueToothPresenter.getBlueToothDevices();
        adpter = new CommonAdapter<BluetoothDevice>(this,devices,R.layout.device_item) {
            @Override
            public void convert(ViewHolder viewHolder, BluetoothDevice itemBean) {
                viewHolder.setText(R.id.tv_bltName,itemBean.getName());
                viewHolder.setText(R.id.tv_bltAddr,itemBean.getAddress());
            }
        };
        lv_BlueToothDevices.setAdapter(adpter);

        //RecyclerView
        mSwipeRefreshWidget = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        mSwipeRefreshWidget.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark, R.color.colorPrimaryLight,
                R.color.colorAccent);
        mSwipeRefreshWidget.setOnRefreshListener(null);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView_BlueToothDevices.setLayoutManager(mLayoutManager);
        recyclerView_BlueToothDevices.addOnScrollListener(mOnScrollListener);
        recyclerAdpter = new RecyclerViewCommonAdapter<BluetoothDevice>(this,R.layout.device_item,devices) {

            @Override
            public void convert(com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper.ViewHolder holder, BluetoothDevice device) {
                holder.setText(R.id.tv_bltName, device.getName());
                holder.setText(R.id.tv_bltAddr, device.getAddress());
            }
        };
        recyclerView_BlueToothDevices.setAdapter(recyclerAdpter);


        switchButton = (SwitchButton) findViewById(R.id.switchBlueTooth);
        //switchButton.setChecked(mIBlueToothPresenter.isBlueToothEnabled);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchButton.isChecked()){
                    turnOnBlueTooth();
                } else {
                    turnOffBlueTooth();
                }
            }
        });
        mIBlueToothPresenter.initBlueToothStatus();
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        private int lastVisibleItem;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == recyclerAdpter.getItemCount()
                    /*&& recyclerAdpter.isShowFooter()*/) {
                //加载更多
                //扫描蓝牙
            }
        }
    };

    @Override
    public void turnOnBlueTooth() {
        if (mIBlueToothPresenter.turnOnBlueTooth()){
            switchButton.setChecked(true);
            showSnackBar("已开启蓝牙");
        }
    }

    @Override
    public void turnOffBlueTooth() {
        if (mIBlueToothPresenter.turnOffBlueTooth()){
            switchButton.setChecked(false);
            showSnackBar("已关闭蓝牙");
        }
    }

    @Override
    public void showSnackBar(String msg) {
        Snackbar.make(bluetooth_rootLayout,msg,Snackbar.LENGTH_SHORT).show();
    }
}
