package fang.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import fang.util.CloseableInterface;
import fang.util.StringMsgHandler;

public class ReadWriter implements CloseableInterface{
	WriteThread writeThread = null;
	ReadThread readThread = null;
	List<CloseableInterface> ThreadList = new LinkedList<CloseableInterface>();
	StringMsgHandler mReceiveMsgHandler = null;
	
	volatile boolean isActive = false;
	
	public ReadWriter(InputStream in) {
		readThread = new ReadThread(in);
		ThreadList.add(readThread);
		readThread.start();
	}
	
	public ReadWriter(InputStream in,StringMsgHandler receiveHandler){
		readThread = new ReadThread(in);
		readThread.setMsgHandler(receiveHandler);
	}
	
	public void setReceiveMsgHandler(StringMsgHandler recMsgHandler) {
		mReceiveMsgHandler = recMsgHandler;
		readThread.setMsgHandler(recMsgHandler);
	}
	
	public ReadWriter(OutputStream out) {
		writeThread = new WriteThread(out);
		ThreadList.add(writeThread);
		writeThread.start();
	}
	
	public ReadWriter(InputStream in,OutputStream out) {
		readThread = new ReadThread(in);
		writeThread = new WriteThread(out);
		ThreadList.add(readThread);ThreadList.add(writeThread);
		readThread.start();writeThread.start();
	}
	
	public ReadWriter(InputStream in,OutputStream out,StringMsgHandler recMsgHandler) {
		readThread = new ReadThread(in);
		if(null != recMsgHandler)
			this.setReceiveMsgHandler(recMsgHandler);
		writeThread = new WriteThread(out);
		ThreadList.add(readThread);ThreadList.add(writeThread);
		readThread.start();writeThread.start();
	}
	
	public void write(String msg) {
		if(writeThread.isAlive())
			writeThread.write(msg);
	}
	
	public void writeLine(String msg){
		String newDate = msg+"\r\n";
		write(newDate);
	}
	
	@Override
	public void close() {
		for (CloseableInterface closeableInterface : ThreadList) {
			if(closeableInterface != null)
				closeableInterface.close();
		}
	}
}

class WriteThread extends Thread implements CloseableInterface{
	Queue<String> MsgSendingQueue = new LinkedBlockingQueue<>(255);
	OutputStream outputStream;
	volatile boolean stop=false;
	
	public WriteThread(OutputStream out) {
		outputStream = out;
	}
	
	public void write(String msg) {
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
	volatile StringMsgHandler mMsgHandler = null;
	
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

