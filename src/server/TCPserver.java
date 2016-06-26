package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPserver implements Runnable{
	Socket mSocket = null;
	ServerSocket serverSocket=null;
	int mPort = 0;
	volatile boolean stop = false; 
	
	public TCPserver(int PORT) {
		mPort = PORT;
	}
	
	private void setStop(boolean stop) {
		this.stop = stop;
	}
	
	@Override
	public void run() {
		while(!stop){
			
		}
	}
	
	void startService(){
		setStop(false);
		new Thread(this).start();
	}
	
	void stopService(){
		setStop(true);
	}
	
	void closeService(){
		if(serverSocket!=null && !serverSocket.isClosed()){
			try {
				serverSocket.close();
				serverSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class TCPServerThread extends Thread{
	Socket mSocket = null;
	
	public TCPServerThread(Socket socket) {
		mSocket = socket;
	}
	
	@Override
	public void run() {
		
	}
}
