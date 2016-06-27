package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import fang.util.CloseableInterface;
import fang.util.StringMsgHandler;

public class TCPClient {
	String ipAddress=null;
	int mPORT=0;
	Socket mSocket = null;
	boolean isConnected = false;
	List<CloseableInterface> ThreadList = new LinkedList<CloseableInterface>();
	ReadThread readThread = null;WriteThread writeThread = null;
	
	public  TCPClient(Socket socket) {
		mSocket = socket;
	}
	
	public TCPClient(String ip,int PORT) {
		setIpAddress(ip);setPORT(PORT);
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setPORT(int PORT) {
		mPORT = PORT;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	public void reConnect(){
		if(ipAddress != null || mPORT !=0){
			close();
			connect();
		}
	}
	
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
	public boolean connect(){
		boolean conResult = false;
		if(ipAddress != null){
			//connect
			try {
				mSocket = new Socket(ipAddress, mPORT);
				writeThread = new WriteThread(mSocket.getOutputStream());
				readThread = new ReadThread(mSocket.getInputStream());
				ThreadList.add(readThread);ThreadList.add(writeThread);
				readThread.start();writeThread.start();
				setConnected(true);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return conResult;
	}
	
	/**
	 * connect to server
	 * @param ip
	 * @param PORT
	 * @return
	 */
	public boolean connect(String ip,int PORT){
		setIpAddress(ip);setPORT(PORT);
		return connect();
	}
	
	private void closeAllResource() {
		for (CloseableInterface closeableInterface : ThreadList) {
			closeableInterface.close();
		}
	}
	
	/**
	 * close the connect to the server
	 */
	public void close(){
		//1.kill all the thread associated with TCPClient Object
		closeAllResource();
		//2.close socket
		if(mSocket != null && !mSocket.isClosed())
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		//3.modify the connect status
		setConnected(false);
	}
	
	/**
	 * send data to the server connected
	 * @param data
	 */
	public void send(String data) {
		if(writeThread.isAlive())
			writeThread.send(data);
	}
	
	public void sendLine(String data) {
		String newdata = data+"\r\n";
		send(newdata);
	}
}

class WriteThread extends Thread implements CloseableInterface{
	Queue<String> MsgSendingQueue = new LinkedBlockingQueue<>(255);
	OutputStream outputStream;
	volatile boolean stop=false;
	
	public WriteThread(OutputStream out) {
		outputStream = out;
	}
	
	public void send(String msg) {
		synchronized (MsgSendingQueue) {
			MsgSendingQueue.add(msg);
			MsgSendingQueue.notifyAll();
		}
	}
	
	@Override
	public void close() {
		stop = true;
	}
	
	@Override
	public void run() {
		stop = false;
		while(!stop){
			synchronized (MsgSendingQueue) {
				try{
				if(MsgSendingQueue.isEmpty())
					MsgSendingQueue.wait();
				else {
					outputStream.write(MsgSendingQueue.remove().getBytes());
				}
				}catch(Exception e){
					this.close();
					break;
				}
			}
		}
	}
			
}

/**
 * TCP client thread for receive and display msg
 * @author fang
 *
 */
class ReadThread extends Thread implements CloseableInterface{
	InputStream inputStream=null;
	volatile boolean stop=false;
	StringMsgHandler mMsgHandler = null;
	
	public ReadThread(InputStream in) {
		inputStream = in;
	}
	
	public void setMsgHandler(StringMsgHandler msgHandler) {
		mMsgHandler = msgHandler;
	}
	
	@Override
	public void close() {
		stop = true;
	}
	
	@Override
	public void run() {
		stop = false;
		BufferedReader bf=new BufferedReader(new InputStreamReader(inputStream));
		while(!stop){
			try {
				if(bf == null){
					System.err.println("buffer reader is null");
					break;
				}
				String msg = bf.readLine();
				if(mMsgHandler != null){
					mMsgHandler.HandleStringMsg(msg);
				}else{
					if(msg !=null && !msg.isEmpty())
						System.out.println(msg);//display the msg
				}
			} catch (IOException e) {
				stop = true;
			}
		}
	}
}
