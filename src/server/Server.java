package server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cipher.RSA;

public class Server {
	int port;
	int rsaBit;
	String configPath;

	public Server() throws IOException {
		configPath = System.getProperty("user.home")+"\\EdmondChatRoom\\";
		readConfig();
		checkRSAKeys();
//		setupServer();
	}

	public void setupServer() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
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
			try {
				RSA rsa = new RSA(rsaBit);
				rsa.createKeys();
				rsa.writeKeyToFile(configPath+"rsa\\publicKey", rsa.getPublicKey().getEncoded());
				rsa.writeKeyToFile(configPath+"rsa\\privateKey", rsa.getPrivateKey().getEncoded());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void readConfig() {
		File file = new File(configPath+"\\config.txt");
		if (!file.exists()) {
			//需要从文件中读取
			port = 6068;
			rsaBit = 1024;
		}
	}

	public static void main(String[] args) throws IOException {
		new Server();
//		System.out.println(System.getProperty("user.home")+"\\EdmondChatRoom\\rsa");
	}
}
