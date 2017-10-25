package net.flyget.bluetoothchat.task;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import net.flyget.bluetoothchat.model.DataProtocol;
import net.flyget.bluetoothchat.model.Message;
import net.flyget.bluetoothchat.sound.SoundEffect;
import net.flyget.bluetoothchat.view.ChatListViewAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * 任务处理服务
 * @author Administrator
 */
public class TaskService extends Service {
	public static final int BT_STAT_WAIT = 0;
	public static final int BT_STAT_CONN = 1;
	public static final int BT_STAT_ONLINE = 2;
	public static final int BT_STAT_UNKNOWN = 3;

	private final String TAG = "TaskService";
	private TaskThread mThread;

	private BluetoothAdapter mBluetoothAdapter;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;

	private boolean isServerMode = true;

	private static Handler mActivityHandler;

	public static boolean isStart = false;

	// 任务队列
	private static ArrayList<Task> mTaskList = new ArrayList<Task>();


	public static final int SERVICE_CHAT= 1;
	public static final int SERVICE_DM5800= 2;
	private static int service_type = SERVICE_DM5800;

	public static int getService_type() {
		return service_type;
	}

	public static void setService_type(int service_type) {
		TaskService.service_type = service_type;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
		mThread = new TaskThread();
		mThread.start();
	}

	private Handler mServiceHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case Task.TASK_GET_REMOTE_STATE:
					android.os.Message activityMsg = mActivityHandler.obtainMessage();
					activityMsg.what = msg.what;
					if (mAcceptThread != null && mAcceptThread.isAlive()) {
						activityMsg.obj = "等待连接...";
						activityMsg.arg1 = BT_STAT_WAIT;
					} else if (mCommThread != null && mCommThread.isAlive()) {
						activityMsg.obj = mCommThread.getRemoteName() + "[在线]";
						activityMsg.arg1 = BT_STAT_ONLINE;
					} else if (mConnectThread != null && mConnectThread.isAlive()) {
						SoundEffect.getInstance(TaskService.this).play(3);
						activityMsg.obj = "正在连接："
								+ mConnectThread.getDevice().getName();
						activityMsg.arg1 = BT_STAT_CONN;
					} else {
						activityMsg.obj = "未知状态";
						activityMsg.arg1 = BT_STAT_UNKNOWN;
						//SoundEffect.getInstance(TaskService.this).play(2);
						// 重新等待连接
						mAcceptThread = new AcceptThread();
						mAcceptThread.start();
						isServerMode = true;
					}

					mActivityHandler.sendMessage(activityMsg);
					break;

				default:
					break;
			}
			super.handleMessage(msg);
		}
	};

	public static void start(Context c, Handler handler){
		isStart = true;
		mActivityHandler = handler;
		Intent intent = new Intent(c, TaskService.class);
		c.startService(intent);
	}

	public static void stop(Context c){
		isStart = false;
		Intent intent = new Intent(c, TaskService.class);
		c.stopService(intent);

	}



	public static void newTask(Task target) {
		synchronized (mTaskList) {
			mTaskList.add(target);
		}
	}

	private class TaskThread extends Thread {
		private boolean isRun = true;
		private int mCount = 0;

		public void cancel() {
			isRun = false;
		}

		@Override
		public void run() {
			Task task;
			while (isRun) {

				// 有任务
				if (mTaskList.size() > 0) {
					synchronized (mTaskList) {
						// 获得任务
						task = mTaskList.get(0);
						doTask(task);
					}
				} else {
					try {
						Thread.sleep(200);
						mCount++;
					} catch (InterruptedException e) {
					}
					// 每过10秒钟进行一次状态检查
					if (mCount >= 50) {
						mCount = 0;
						// 检查远程设备状态
						android.os.Message handlerMsg = mServiceHandler
								.obtainMessage();
						handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
						mServiceHandler.sendMessage(handlerMsg);
					}
				}
			}
		}

	}

	private void doTask(Task task) {

		boolean success = false;

		switch (task.getTaskID()) {
			case Task.TASK_START_ACCEPT:
				mAcceptThread = new AcceptThread();
				mAcceptThread.start();
				isServerMode = true;
				break;
			case Task.TASK_START_CONN_THREAD:
				if (task.mParams == null || task.mParams.length == 0) {
					break;
				}
				BluetoothDevice remote = (BluetoothDevice) task.mParams[0];
				mConnectThread = new ConnectThread(remote);
				mConnectThread.start();
				isServerMode = false;
				break;
			case Task.TASK_SEND_MSG:
				if (mCommThread == null || !mCommThread.isAlive()
						|| task.mParams == null || task.mParams.length == 0) {
					Log.e(TAG, "mCommThread or task.mParams null");
				}else{
					byte[] msg = null;
					try {
						msg = DataProtocol.packMsg((String) task.mParams[0]);
						success = mCommThread.write(msg);
					} catch (UnsupportedEncodingException e) {
						success = false;
					}
				}
				if (!success) {
					android.os.Message returnMsg = mActivityHandler.obtainMessage();
					returnMsg.what = task.getTaskID();
					returnMsg.obj = "消息发送失败";
					mActivityHandler.sendMessage(returnMsg);
				}
				break;
			case Task.TASK_SEND_FILE://发送文件

				if (mCommThread == null || !mCommThread.isAlive()
						|| task.mParams == null || task.mParams.length == 0) {
					Log.e(TAG, "mCommThread or task.mParams null");
				}else{

					success = mCommThread.writeFile((String) task.mParams[0]);
					if (!success) {
						android.os.Message returnMsg = mActivityHandler.obtainMessage();
						returnMsg.what = task.getTaskID();
						returnMsg.obj = "文件发送失败:文件格式不正确或者对方已下线！";
						mActivityHandler.sendMessage(returnMsg);
						return;
					}
				}
				if (!success) {
					android.os.Message returnMsg = mActivityHandler.obtainMessage();
					returnMsg.what = task.getTaskID();
					returnMsg.obj = "文件发送失败";
					mActivityHandler.sendMessage(returnMsg);
				}
				break;
			case Task.TASK_GET_REMOTE_STATE:
				android.os.Message handlerMsg = mServiceHandler
						.obtainMessage();
				handlerMsg.what = task.getTaskID();
				mServiceHandler.sendMessage(handlerMsg);
				break;
		}

		// 移除任务
		mTaskList.remove(task);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThread.cancel();
	}

	private final String UUID_STR = "00001101-0000-1000-8000-00805F9B34FB";

	/**
	 * 等待客户端连接线程
	 *
	 * @author Administrator
	 */
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;
		private boolean isCancel = false;

		public AcceptThread() {
			Log.d(TAG, "AcceptThread");
			BluetoothServerSocket tmp = null;
			try {
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"MT_Chat_Room", UUID.fromString(UUID_STR));
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			while (true) {
				if (mmServerSocket == null){
					return;
				}


				try {
					// 阻塞等待
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					if (!isCancel) {
						try {
							mmServerSocket.close();
						} catch (IOException e1) {
						}
						mAcceptThread = new AcceptThread();
						mAcceptThread.start();
						isServerMode = true;
					}
					break;
				}
				if (socket != null) {
					manageConnectedSocket(socket);
					try {
						mmServerSocket.close();
					} catch (IOException e) {
					}
					mAcceptThread = null;
					break;
				}
			}
		}

		public void cancel() {
			try {
				Log.d(TAG, "AcceptThread canceled");
				isCancel = true;
				isServerMode = false;
				mmServerSocket.close();
				mAcceptThread = null;
				if (mCommThread != null && mCommThread.isAlive()) {
					mCommThread.cancel();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 作为客户端连接指定的蓝牙设备线程
	 *
	 * @author Administrator
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {

			Log.d(TAG, "ConnectThread");

			if (mAcceptThread != null && mAcceptThread.isAlive()) {
				mAcceptThread.cancel();
			}

			if (mCommThread != null && mCommThread.isAlive()) {
				mCommThread.cancel();
			}

			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;
			try {
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString(UUID_STR));
			} catch (IOException e) {
				Log.d(TAG, "createRfcommSocketToServiceRecord error!");
			}

			mmSocket = tmp;
		}

		public BluetoothDevice getDevice() {
			return mmDevice;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				Log.e(TAG, "Connect server failed");
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				mAcceptThread = new AcceptThread();
				mAcceptThread.start();
				isServerMode = true;
				Log.e(TAG, "[ConnectThread]  to ServerMode...");
				return;
			} // Do work to manage the connection (in a separate thread)
			manageConnectedSocket(mmSocket);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
			mConnectThread = null;
		}
	}

	private ConnectedThread mCommThread;

	private void manageConnectedSocket(BluetoothSocket socket) {
		// 启动子线程来维持连接
		mCommThread = new ConnectedThread(socket);
		mCommThread.start();
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private BufferedOutputStream mmBos;
		private byte[] buffer;

		public ConnectedThread(BluetoothSocket socket) {
			Log.d(TAG, "ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			mmBos = new BufferedOutputStream(mmOutStream);
		}

		public OutputStream getOutputStream() {
			return mmOutStream;
		}

		public boolean write(byte[] msg) {
			if (msg == null)
				return false;
			try {
				mmBos.write(msg);
				mmBos.flush();
				System.out.println("Write:" + msg);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

		public boolean writeFile(String filePath) {
			File file = new File(filePath);

			if (file == null)
				return false;
			try {

				FileInputStream fis = new FileInputStream(file);

				DataProtocol mDataProtocol = new  DataProtocol();

                mDataProtocol.pack_file_detaile(file);
                Log.e(TAG, "writeFile: pack_file_detaile() :"+file.getAbsolutePath() );
                mmBos.write(mDataProtocol.getByteArray());
                mmBos.flush();

                while (fis.available() > 0){
					try {
						mDataProtocol.pack_file(fis );
						mmBos.write(mDataProtocol.getByteArray());
						mmBos.flush();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return false;
					}
				}
                mDataProtocol.pack_file_end( );
                Log.e(TAG, "writeFile: pack_file_end() :"+file.getAbsolutePath() );
                mmBos.write(mDataProtocol.getByteArray());
                mmBos.flush();

				mDataProtocol.clearFile();
				System.out.println("Write:" + "");
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}



		public String getRemoteName() {
			return mmSocket.getRemoteDevice().getName();
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
			mCommThread = null;
		}

		public void run() {
			try {
				write(DataProtocol.packMsg(mBluetoothAdapter.getName()
						+ "已经上线\n"));
			} catch (UnsupportedEncodingException e2) {
			}
			int size;
			Message msg;
			android.os.Message handlerMsg;


			BufferedInputStream bis = new BufferedInputStream(mmInStream);
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(mmInStream));
			HashMap<String, Object> data;
			while (true) {
				try {
					buffer = new byte[980];
					size = bis.read(buffer);

					ByteBuffer bytebuffer = ByteBuffer.wrap(buffer,0,size);
					Log.d(TAG, "unpackData: "+"size = "+size);
					msg = DataProtocol.unpackData(bytebuffer);

					if (msg == null)
						continue;

					if (mActivityHandler == null) {
						return;
					}

					msg.remoteDevName = mmSocket.getRemoteDevice().getName();
					if (msg.type == DataProtocol.TYPE_FILE) {//收到文件
						// 文件接收处理
						handlerMsg = mActivityHandler.obtainMessage();
						handlerMsg.what = Task.TASK_RECV_FILE;
						handlerMsg.obj = msg;
						mActivityHandler.sendMessage(handlerMsg);

					} else if (msg.type == DataProtocol.TYPE_MSG) {//收到文本信息
						handlerMsg = mActivityHandler.obtainMessage();
						handlerMsg.what = Task.TASK_RECV_MSG;
						switch (service_type){
							case SERVICE_CHAT:
								data = new HashMap<String, Object>();
								System.out.println("Read data.");
								data.put(ChatListViewAdapter.KEY_ROLE,
										ChatListViewAdapter.ROLE_TARGET);
								data.put(ChatListViewAdapter.KEY_NAME,
										msg.remoteDevName);
								data.put(ChatListViewAdapter.KEY_TEXT, msg.msg);
								handlerMsg.obj = data;
								break;
							case SERVICE_DM5800:
								handlerMsg.obj = msg.msg;
								break;
						}

						// 通过Activity更新到UI上
						mActivityHandler.sendMessage(handlerMsg);
					}
				} catch (IOException e) {
					try {
						mmSocket.close();
					} catch (IOException e1) {
					}
					mCommThread = null;
					if (isServerMode) {
						// 检查远程设备状态
						handlerMsg = mServiceHandler.obtainMessage();
						handlerMsg.what = Task.TASK_GET_REMOTE_STATE;
						mServiceHandler.sendMessage(handlerMsg);
						//SoundEffect.getInstance(TaskService.this).play(2);
						mAcceptThread = new AcceptThread();
						mAcceptThread.start();
					}
					break;
				}
			}
		}
	}

	// ================================================================

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
