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

/**
 * 1.ReadWriter is a class that contains independent thread for read and write
 * 
 * 2.It's suitable for no-blocking scene when reading and writing
 * 
 * 3.The read and write thread will be blocked when no data to improve CPU usage
 * 
 * 4.You can get single thread for readWriter Object through static function.
 * In this case,it's only for read or write.It'll be a reader or writer.
 * @author fang
 *
 */
public class ReadWriter implements CloseableInterface{
	WriteThread writeThread = null;
	ReadThread readThread = null;
	List<CloseableInterface> ThreadList = new LinkedList<CloseableInterface>();
	StringMsgHandler mReceiveMsgHandler = null;
	
	public ReadWriter(InputStream in) {
		Constructor(in, null, null);
	}
	
	public ReadWriter(InputStream in,StringMsgHandler receiveHandler){
		Constructor(in, null,receiveHandler);
	}
	
	public ReadWriter(OutputStream out) {
		Constructor(null, out, null);
	}
	
	public ReadWriter(InputStream in,OutputStream out) {
		Constructor(in, out, null);
	}
	
	public ReadWriter(InputStream in,OutputStream out,StringMsgHandler recMsgHandler) {
		Constructor(in, out, recMsgHandler);
	}
	
	/**
	 * the real constructor for the class
	 * @param in
	 * @param out
	 * @param recMsgHandler
	 */
	private void Constructor(InputStream in,OutputStream out,StringMsgHandler recMsgHandler) {
		if(null != in){
			readThread = new ReadThread(in);
			if (null != recMsgHandler) {
				this.setReceiveMsgHandler(recMsgHandler);
			}
			ThreadList.add(readThread);
			readThread.start();
		}
		if(null != out){
			writeThread = new WriteThread(out);
			ThreadList.add(writeThread);
			writeThread.start();
		}
	}
	
	public static ReadWriter getReader(InputStream in) {
		return new ReadWriter(in);
	}
	
	public static ReadWriter getReader(InputStream in,StringMsgHandler recMsgHandler) {
		return new ReadWriter(in,recMsgHandler);
	}
	
	public static ReadWriter getWriter(OutputStream out) {
		return new ReadWriter(out);
	}
	
	/**
	 * if you set recMsgHandler to readThread of ReadWriter object,
	 * it will run when read data,handling the string msg
	 * @param recMsgHandler
	 */
	public void setReceiveMsgHandler(StringMsgHandler recMsgHandler) {
		if(null == readThread)//check readThread is not null
			return;
		mReceiveMsgHandler = recMsgHandler;
		readThread.setMsgHandler(recMsgHandler);
	}
	
	/**
	 * write string msg
	 * @param msg
	 */
	public void write(String msg) {
		if(writeThread.isAlive())
			writeThread.write(msg);
	}
	
	/**
	 * write a line of msg
	 * @param msg
	 */
	public void writeLine(String msg){
		String newDate = msg+"\r\n";
		this.write(newDate);
	}
	
	@Override
	public void close() {
		for (CloseableInterface closeableInterface : ThreadList) {
			if(closeableInterface != null)
				closeableInterface.close();
		}
	}
}

/**
 * write thread for writer,contains a SendingMsgQueue
 * if the Queue is empty,block itself,else notify the thread
 * @author fang
 *
 */
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

