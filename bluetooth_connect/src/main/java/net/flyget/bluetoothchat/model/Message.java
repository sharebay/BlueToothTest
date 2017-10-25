package net.flyget.bluetoothchat.model;

/**
 * 消息实体封装类
 * @author Micheal
 *
 */
public class Message {
	public byte type;
	public long total;
	public int length;
	public int index;
	public byte[] fileBuffer;
	public String msg;
	public String fileName;
	public String remoteDevName;
}
