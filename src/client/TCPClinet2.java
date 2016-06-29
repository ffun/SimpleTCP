package client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import fang.io.ReadWriter;
import fang.util.StringMsgHandler;

public class TCPClinet2 {
	ReadWriter mReadWriter = null;
	String ipAddress;int mPORT = 0;
	Socket mSocket = null;
	StringMsgHandler mRecHandler = null;
	boolean isActive = false;
	
	public TCPClinet2(String ip,int PORT) {
		this.ipAddress = ip;
		mPORT = PORT;
	}
	
	public TCPClinet2(Socket socket) {
		mSocket = socket;
	}
	
	public void setRecHandler(StringMsgHandler recHandler) {
		mRecHandler = recHandler;
		
	}
	
	private void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public boolean connect() {
		if(isActive())
			return true;
		if(mSocket != null){
			try {
				InputStream inputStream = mSocket.getInputStream();
				OutputStream outputStream = mSocket.getOutputStream();
				mReadWriter = new ReadWriter(inputStream, outputStream, mRecHandler);
				setActive(true);
			} catch (Exception e) {
				e.printStackTrace();
				setActive(false);
			}
		}else {
			if(ipAddress != null){
				try {
					mSocket = new Socket(ipAddress, mPORT);
					InputStream inputStream = mSocket.getInputStream();
					OutputStream outputStream = mSocket.getOutputStream();
					mReadWriter = new ReadWriter(inputStream, outputStream, mRecHandler);
					setActive(true);
				} catch (Exception e) {
					e.printStackTrace();
					setActive(false);
				}
			}
		}
		return isActive();
	}
	
	public boolean connect(String ip,int PORT) {
		ipAddress = ip;
		mPORT = PORT;
		return connect();
	}
	
	public boolean reConnect() {
		this.close();
		return connect();
	}
	
	public void close() {
		if(mReadWriter != null){
			mReadWriter.close();
		}
		setActive(false);
	}
	
	public void write(String msg) {
		if(mReadWriter == null)
			return;
		mReadWriter.write(msg);
	}
	
	public void writeLine(String msg) {
		if(mReadWriter == null)
			return;
		mReadWriter.writeLine(msg);
	}
	
	public void sendLine(String msg) {
		writeLine(msg);
	}
}
