package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import fang.io.ReadWriter;

public class TCPserver{
	ServerSocket mServerSocket  = null;
	int mPORT = 0;
	ReaderList list = new ReaderList();
	
	public TCPserver(int PORT) {
		mPORT = PORT;
		try {
			mServerSocket = new ServerSocket(mPORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startService() {
		boolean stop =false;
		while(!stop){
			try {
				Socket socket = mServerSocket.accept();
				System.out.println(socket.getInetAddress()+" connected");
				new serverThread(socket).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				stop = true;
			}
			
		}
	}
}

class serverThread extends Thread{
	ReadWriter reader = null;
	Socket mSocket = null;
	public serverThread(Socket socket) {
		mSocket = socket;
	}
	
	@Override
	public void run() {
		InputStream inputStream =null;
		try {
			inputStream = mSocket.getInputStream();
			reader = new ReadWriter(inputStream);
			ReaderList.list.add(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ReaderList{
	public static List<ReadWriter> list = new LinkedList<ReadWriter>();
}