package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPClient {
	String ipAddress=null;
	int mPORT=0;
	Socket mSocket = null;
	Queue<String> MsgSendingQueue = null,MsgReceiveQueue =null;
	boolean isConnected = false;
	TCPClientReceiveThread receiveThread = null;
	TCPClientSendThread sendThread=null;
	
	public TCPClient() {
		MsgSendingQueue = new LinkedBlockingQueue<String>();
	}
	
	public TCPClient(String ip,int PORT) {
		setIpAddress(ip);setPORT(PORT);
		MsgSendingQueue = new LinkedBlockingQueue<String>();
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setPORT(int PORT) {
		mPORT = PORT;
	}
	
	public boolean connect(){
		boolean conResult = false;
		if(ipAddress != null){
			//connect
			try {
				mSocket = new Socket(ipAddress, mPORT);
				sendThread = new TCPClientSendThread(MsgSendingQueue, mSocket.getOutputStream());
				receiveThread = new TCPClientReceiveThread(mSocket.getInputStream());
				sendThread.start();receiveThread.start();
				isConnected = true;
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
	
	/**
	 * close the connect to the server
	 */
	public void close(){
		if(mSocket != null && mSocket.isClosed())
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		isConnected = false;
	}
	
	/**
	 * send data to the server connected
	 * @param data
	 */
	public void send(String data) {
		if(!isConnected){
			System.out.println("client hasn't connect to server");
			return;
		}
		synchronized (MsgSendingQueue) {
			MsgSendingQueue.add(data);
			MsgSendingQueue.notifyAll();
		}
	}
	
	public void sendLine(String data) {
		String newdata = data+"\r\n";
		send(newdata);
	}
	
}

class TCPClientSendThread extends Thread{
	Queue<String> MsgSendingQueue = null;
	OutputStream outputStream;
	volatile boolean stop=false;
	
	public TCPClientSendThread(Queue<String>sending,OutputStream out) {
		MsgSendingQueue = sending;
		outputStream = out;
	}
	
	public void killSelf() {
		stop = true;
	}
	
	@Override
	public void run() {
		while(!stop){
			synchronized (MsgSendingQueue) {
				try{
				if(MsgSendingQueue.isEmpty())
					MsgSendingQueue.wait();
				else {
					outputStream.write(MsgSendingQueue.remove().getBytes());
				}
				}catch(Exception e){
					e.printStackTrace();
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
class TCPClientReceiveThread extends Thread{
	InputStream inputStream=null;
	volatile boolean stop=false;
	
	public TCPClientReceiveThread(InputStream in) {
		inputStream = in;
	}
	
	public void killSelf() {
		stop = true;
	}
	
	@Override
	public void run() {
		BufferedReader bf=new BufferedReader(new InputStreamReader(inputStream));
		while(!stop){
			try {
				String msg = bf.readLine();
				System.out.println(msg);//display the msg
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
