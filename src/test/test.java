package test;

import java.util.Scanner;

import client.TCPClient;

public class test {
	public static void main(String[] args) {
		//for TCP client
		TCPClient client = new TCPClient("192.168.1.101", 8989);
		client.connect();
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("connected successful\r\ninput:");
		String data = scanner.nextLine();
		while(!"exit".equals(data)){
			if("help".equals(data)){
				System.out.println("\r\nexit--stop the program\r\n");
			}
			client.sendLine(data);
			System.out.print("input:");
			data = scanner.nextLine();
		}
		scanner.close();
	}
}
