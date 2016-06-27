package test;

import java.util.Scanner;

import client.TCPClient;
import client.TCPClinet2;

public class test {
	
	private static String mLogin = "MobileLogin,userID";
	private static String mdCharge1 = "MobileClient,hdu123456";
	private static String mdCharge2 = "ChargeTime,1";
	
	public static void main(String[] args) {
		//for TCP client
//		TCPClient client = new TCPClient("192.168.1.101", 8989);
		TCPClinet2 client = new TCPClinet2("192.168.1.101", 8989);
		client.connect();
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("connected successful\r\ninput:");
		String data = scanner.nextLine();
		while(!"exit".equals(data)){
			if("help".equals(data)){
				System.out.println("\r\nexit--stop the program\r\n"
						+ "input--send data what you input"+"\r\n"
						+"mobile--test mobile device");
			}
			else if("input".equals(data)){
				data = scanner.nextLine();
				client.sendLine(data);
			}
			else if ("charge".equals(data)) {
				client.sendLine(mdCharge1);
				client.sendLine(mdCharge2);
			}else if("login".equals(data)) {
				client.sendLine(mLogin);
			}
			data = scanner.nextLine();
			
		}
		scanner.close();
	}
}
