package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cipher.*;
import dataStructure.User;

public class Server {
	static ArrayList<User> onlineUsers = new ArrayList<>();
	int port;
	int rsaBit;
	static String configPath;

	public Server() throws IOException {
		readConfig();
		checkRSAKeys();
		setupServer();
	}

	public void setupServer() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				System.out.println("thread started");
				Socket socket = serverSocket.accept();
				ServerThread s = new ServerThread(socket);
				s.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkRSAKeys() {
		File file1 = new File(configPath+"rsa\\publicKey");
		File file2 = new File(configPath+"rsa\\privateKey");
		if (!file1.exists() || !file2.exists()) {
			RSA rsa = new RSA(rsaBit);
			ReadWrite.writeByteToFile(file1, rsa.getPublicKey().getEncoded());
			ReadWrite.writeByteToFile(file2, rsa.getPrivateKey().getEncoded());
		}
	}

	private void readConfig() {
		configPath = System.getProperty("user.home")+"\\EdmondChatRoomServer\\";
		File file = new File(configPath+"config.txt");
		if (!file.exists()) {
			// 需要从文件中读取
			port = 6069;
			rsaBit = 1024;
		}
	}

	public static void main(String[] args) throws IOException {
		new Server();
//		System.out.println(System.getProperty("user.home")+"\\EdmondChatRoom\\rsa");
	}
}
