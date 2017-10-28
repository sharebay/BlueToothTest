package com.kemov.vam.bluetoothtest.BlueTooth.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.kemov.vam.bluetoothtest.BlueTooth.presenter.BlueToothPresenterImpl;
import com.kemov.vam.bluetoothtest.BlueTooth.presenter.IBlueToothPresenter;
import com.kemov.vam.bluetoothtest.BlueTooth.view.IBlueToothView;
import com.kemov.vam.bluetoothtest.R;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.CommonAdapter;
import com.kemov.vam.bluetoothtest.commons.listview.CommonAdapterHelper.ViewHolder;
import com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper.RecyclerViewCommonAdapter;
import com.kemov.vam.bluetoothtest.utils.BlueToothConstants;
import com.kemov.vam.bluetoothtest.utils.BlueToothManager;
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

    Context mCtx;
    LinearLayout bluetooth_rootLayout;
    Button btn_SearchNearby;
    SwitchButton switchButton;
    ListView lv_BlueToothDevices;
    SwipeRefreshLayout mSwipeRefreshWidget;
    RecyclerView recyclerView_BlueToothDevices;

    LinearLayoutManager mLayoutManager;
    CommonAdapter<BluetoothDevice> adpter;
    RecyclerViewCommonAdapter<BluetoothDevice> recyclerAdpter;
    List<BluetoothDevice> devices = new ArrayList<>();

    private IBlueToothPresenter mIBlueToothPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        mCtx = this;
        mIBlueToothPresenter = new BlueToothPresenterImpl(this,this);

        BlueToothManager.getInstance().initBltManager(mCtx);
        ///注册接收蓝牙信息的广播
        initData();
        initView();
    }


    private void initData() {
        blueToothRegister();
    }

    //注册蓝牙回调广播
    private void blueToothRegister() {
        BlueToothManager.getInstance().registerBlueToothReceiver(this,
                new BlueToothManager.OnRegisterBltReceiver() {

                    /*
                    * 当搜索到新的蓝牙设备的时候。
                    * */
                    @Override
                    public void onBluetoothDevice(BluetoothDevice device) {

                        if (devices!= null && !devices.contains(device)){
                            devices.add(device);
                            Toast.makeText(mCtx, "查找到一个蓝牙设备"+device.getName(), Toast.LENGTH_SHORT).show();
                        }
                        if (adpter != null){
                            //adpter.setData(devices);
                            recyclerAdpter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onConnecting(BluetoothDevice device) {
                        Toast.makeText(mCtx, "连接中。。", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnected(BluetoothDevice device) {
                        Toast.makeText(mCtx, "已连接！", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUnConnected(BluetoothDevice device) {

                    }
                });
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

        btn_SearchNearby = (Button) findViewById(R.id.btn_SearchNearby);
        btn_SearchNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mCtx, "1蓝牙搜索状态为："+(BluetoothAdapter.getDefaultAdapter().isDiscovering() ? "搜索中":"未搜索"), Toast.LENGTH_SHORT).show();
                Toast.makeText(mCtx, "开始扫描附近的蓝牙设备。。。", Toast.LENGTH_SHORT).show();
                //开启蓝牙搜索的状态
                BlueToothManager.getInstance().blueToothOperationEvent(mCtx, BlueToothConstants.BLUE_TOOTH_SEARTH);
                //注册蓝牙广播接收器。
                Toast.makeText(mCtx, "2蓝牙搜索状态为："+(BluetoothAdapter.getDefaultAdapter().isDiscovering() ? "搜索中":"未搜索"), Toast.LENGTH_SHORT).show();
            }

        });

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
            public void convert(com.kemov.vam.bluetoothtest.commons.recyclerview.CommonAdapterHelper.ViewHolder holder, final BluetoothDevice device) {
                holder.setText(R.id.tv_bltName, device.getName());
                holder.setText(R.id.tv_bltAddr, device.getAddress());
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BlueToothManager.getInstance().createBond(device, handler);
                            }
                        }).start();
                    }
                });
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
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            sleep(3000);
                            //mSwipeRefreshWidget.setRefreshing(false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1://搜索蓝牙
                    break;
                case 2://蓝牙可以被搜索
                    break;
                case 3://设备已经接入
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    Toast.makeText(BlueToothActivity.this, "设备" + device.getName() + "已经接入", Toast.LENGTH_LONG).show();
                    break;
                case 4://已连接某个设备
                    BluetoothDevice device1 = (BluetoothDevice) msg.obj;
                    Toast.makeText(BlueToothActivity.this, "已连接" + device1.getName() + "设备", Toast.LENGTH_LONG).show();
                    break;
                case 5:

                    break;
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
