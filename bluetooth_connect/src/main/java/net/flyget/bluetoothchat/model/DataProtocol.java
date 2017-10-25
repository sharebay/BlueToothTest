package net.flyget.bluetoothchat.model;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 *
 * @author Micheal
 *
 */
public class DataProtocol {
    private final static String TAG = "DataProtocol";
    public final static byte HEAD = 0xA;
    public final static byte TYPE_MSG = 0xC;
    public final static byte TYPE_FILE = 0xF;

    public static byte[] packMsg(String msg) throws UnsupportedEncodingException{
        byte[] msgbytes = msg.getBytes("UTF-8");
        byte lowLen = (byte)(msgbytes.length & 0xFF);
        byte hiLen = (byte)(msgbytes.length >> 8 & 0xFF);
        byte[] buf = new byte[msgbytes.length + 4];
        buf[0] = HEAD;
        buf[1] = TYPE_MSG;
        buf[2] = hiLen;
        buf[3] = lowLen;
        System.arraycopy(msgbytes, 0, buf, 4, msgbytes.length);
        return buf;
    }

    //	public static byte[] packFileDetaile(File file) throws UnsupportedEncodingException{
    //		byte total0 = (byte)(file.length() & 0xFF);
    //		byte total1 = (byte)(file.length() >> 8  & 0xFF);
    //		byte total2 = (byte)(file.length() >> 16 & 0xFF);
    //		byte total3 = (byte)(file.length() >> 24 & 0xFF);
    //
    //		byte[] fnamebytes = file.getName().getBytes("UTF-8");
    //
    //		byte lowLen = (byte)(fnamebytes.length & 0xFF);
    //		byte hiLen = (byte)(fnamebytes.length >> 8 & 0xFF);
    //
    //		byte[] buf = new byte[fnamebytes.length + 8];
    //		buf[0] = HEAD;
    //		buf[1] = TYPE_FILE;
    //		buf[2] = total3;
    //		buf[3] = total2;
    //		buf[4] = total1;
    //		buf[5] = total0;
    //		buf[6] = hiLen;
    //		buf[7] = lowLen;
    //		System.arraycopy(fnamebytes, 0, buf, 8, fnamebytes.length);
    //		return buf;
    //	}

    private int file_offset = 0;
    private short nWriteFileIndex = 1;// 传输文件索引:   0：文件传输完毕；1:传输文件详细内容，>1:传输文件具体内容。
    ByteBuffer BF = ByteBuffer.allocate(1024);
    /*
     * 返回数据
     */
    public void clearFile(){
        file_offset = 0;
        nWriteFileIndex = 0;
    }
    public byte[] getByteArray()
    {
        int len = BF.capacity()-BF.remaining();
        BF.flip();
        byte[] byteArray = new byte[len];
        for(int i = 0;i < len;i++)
        {
            byteArray[i] = BF.get();
            //System.out.println("BF--i:"+i+"值:"+byteArray[i]);
        }
        return byteArray;
    }

    //文件传输结束帧组包
    public void pack_file_end( )throws UnsupportedEncodingException{
        int bufferSize = 4;
        BF.clear();
        BF = ByteBuffer.allocate(bufferSize);

        nWriteFileIndex = 0;

        BF.put(HEAD);
        BF.put(TYPE_FILE);
        BF.putShort(nWriteFileIndex);

    }
    //文件详细信息帧组包
    public void pack_file_detaile(File file) throws UnsupportedEncodingException{
        nWriteFileIndex = 1;
        int bufferSize = 255;
        BF.clear();
        BF = ByteBuffer.allocate(bufferSize);
        BF.put(HEAD);
        BF.put(TYPE_FILE);
        BF.putShort(nWriteFileIndex++);
        BF.putLong(file.length());
        byte[] fnamebytes = file.getName().getBytes("UTF-8");
        BF.putShort((short) fnamebytes.length);
        BF.put(fnamebytes);
    }
    //文件内容组包
    public void pack_file(FileInputStream inputstream )throws UnsupportedEncodingException{

		/* 设定每次写入1024bytes */
        int bufferSize = 980;
        BF.clear();
        BF = ByteBuffer.allocate(bufferSize);
        BF.put(HEAD);
        BF.put(TYPE_FILE);
        BF.putShort(nWriteFileIndex++);

        int len = bufferSize-BF.position()-2;
        byte[] buffer = new byte[len];
        try {
            int length  = inputstream.read(buffer);
            if (length != -1)
            {
                BF.putShort((short) length);
                file_offset += length;
                BF.put(buffer, 0, length);
                Log.v(TAG, "pack_file: "+(nWriteFileIndex-1)+"/"+length);
                Log.v(TAG, "pack_file: "+"/"+BF.limit());
            }
            else {
                BF.putShort((short) 0);
            }
        } catch (IOException e) {
            //nWriteFileIndex--;
            e.printStackTrace();
            Log.e(TAG, "pack_file: "+e.toString() );
        }
    }

    //    public static Message unpackData(byte[] data) throws UnsupportedEncodingException{
    //        if(data[0] != HEAD)
    //            return null;
    //        Message msg = new Message();
    //        int index = 0;
    //        switch(data[1]){
    //            case TYPE_FILE:
    //                msg.type = data[1];
    //                msg.index = data[2] << 8 | data[3];
    //                index = 4;
    //                if(msg.index == 0){//end
    //                    break;
    //                }else if (msg.index == 1){//文件信息
    //                    msg.total = data[index] << 24 | data[index+1] << 16 | data[index+2] << 8 | data[index+3];
    //                    msg.total = msg.total <<32;
    //                    index += 4;//8
    //                    msg.total = data[index] << 24 | data[index+1] << 16 | data[index+2] << 8 | data[index+3];
    //
    //                    index += 4;//12
    //                    msg.length = data[index] << 8 | data[index+1];
    //
    //                    index += 2;//14
    //                    msg.fileName = new String(data, index, msg.length, "UTF-8");
    //                }else {//文件内容
    //                    msg.length = data[index] << 8 | data[index+1];
    //                    index += 2;
    //                    msg.msg = new String(data, index, msg.length, "UTF-8");
    //                }
    //                break;
    //            case TYPE_MSG:
    //                msg.type = TYPE_MSG;
    //                msg.length = data[2] << 8 | data[3];
    //                msg.msg = new String(data, 4, msg.length, "UTF-8");
    //                break;
    //        }
    //        return msg;
    //    }

    public static Message unpackData(ByteBuffer bytebuffer) throws UnsupportedEncodingException{
        if(bytebuffer.get() != HEAD)
            return null;
        Message msg = new Message();
        msg.type = bytebuffer.get();

        switch(msg.type ){
            case TYPE_FILE:
                msg.index = bytebuffer.getShort();
                if(msg.index == 0){//end
                    break;
                }else if (msg.index == 1){//文件信息
                    msg.total = bytebuffer.getLong();
                    msg.length = bytebuffer.getShort();
                    msg.fileName = getString(bytebuffer, msg.length);
                }else {//文件内容

                    msg.length = bytebuffer.getShort();
                    int len2 = bytebuffer.limit()-bytebuffer.position();
                    msg.fileBuffer = new byte[msg.length];
                    Log.v(TAG, "unpackData: "+(msg.index)+"/"+msg.length);
                    Log.v(TAG, "unpackData: "+"/len2"+len2);
                    for (int i = 0;i < len2;i++){
                        msg.fileBuffer[i]  = bytebuffer.get();
                    }

                    for (int i = len2;i < msg.length;i++){
                        msg.fileBuffer[i]  = 0;
                    }
                    //Log.e(TAG, "unpackData: "+msg.index +"/"+msg.length +", 文件内容 = "+msg.msg);
                }
                break;
            case TYPE_MSG:
                msg.type = TYPE_MSG;
                msg.length =  bytebuffer.getShort();
                msg.msg = getString(bytebuffer, msg.length);
                break;
        }
        return msg;
    }

    //	public static Message unpackData(byte[] data) throws UnsupportedEncodingException{
    //		if(data[0] != HEAD)
    //			return null;
    //		Message msg = new Message();
    //		switch(data[1]){
    //			case TYPE_FILE:
    //				msg.type = TYPE_FILE;
    //				msg.total = data[2] << 24 | data[3] << 16 | data[4] << 8 | data[5];
    //				msg.total = msg.total<<32;
    //				msg.total = data[6] << 24 | data[7] << 16 | data[8] << 8 | data[9];
    //				//msg.msg = new String(data, 8, data[6] << 8 | data[7], "UTF-8");
    //				msg.index = data[10] << 8 | data[11];
    //				msg.msg = new String(data, 12, msg.length, "UTF-8");
    //				break;
    //			case TYPE_MSG:
    //				msg.type = TYPE_MSG;
    //				msg.length = data[2] << 8 | data[3];
    //				msg.msg = new String(data, 4, msg.length, "UTF-8");
    //				break;
    //		}
    //		return msg;
    //	}

    //ByteBuffer 转换 String：
    private static String getString(ByteBuffer buf,int nlen) throws UnsupportedEncodingException{

        if (nlen <= 0)return "";
        byte[] data = new byte[nlen];
        int i = 0;
        try{
            for (i = 0; i < nlen; i++) {
                data[i] = buf.get();
            }
        }catch (Exception e){
            Log.e(TAG, "getString: i = "+i );
        }


        String text = new String(data, 0, nlen, "UTF-8");
        return text;
    }
    //ByteBuffer 转换 String：
    private static String getString(ByteBuffer buf,int nlen, int index) throws UnsupportedEncodingException{

        if (nlen <= 0)return "";
        byte[] data = new byte[nlen];
        int i = 0;
        for (i = 0; i < nlen; i++) {
            data[i] = buf.get();
        }

        String text = new String(data, 0, nlen, "UTF-8");
        return text;

        //        String text = "";
        //        for (int i = 0; i < nlen; i++) {
        //            text +=  String.format("%c", buf.get());
        //        }
        //        return text;
    }
    //    public static String getString(ByteBuffer buffer) {
    //        Charset charset = null;
    //        CharsetDecoder decoder = null;
    //        CharBuffer charBuffer = null;
    //        try {
    //            charset = Charset.forName("UTF-8");
    //            decoder = charset.newDecoder();
    //            // charBuffer = decoder.decode(buffer); //用这个的话，只能输出来一次结果，第二次显示为空
    //            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
    //            return charBuffer.toString();
    //        } catch (Exception ex) {
    //            ex.printStackTrace();
    //            return "error";
    //        }
    //    }
    //    public static String getVarStrFromBuf(ByteBuffer buf, int MaxLen) {
    //        byte[] btemp = new byte[MaxLen];
    //        byte j = -1;
    //        int avaibleBytes = buf.array().length - buf.position();
    //        if (! (avaibleBytes > 0)) {
    //            return "";
    //        }
    //        int len = 0;
    //        for(int i=0;i<MaxLen;i++){
    //            btemp[i] = buf.get();
    //            len = i;
    //            if(btemp[i]==0){
    //                break;
    //            }
    //        }
    //        return new String(btemp, 0, len);
    //    }
    //String 转换 ByteBuffer：
    public static ByteBuffer getByteBuffer(String str) {
        return ByteBuffer.wrap(str.getBytes());
    }

}
