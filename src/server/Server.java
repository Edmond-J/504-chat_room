package server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cipher.*;
import dataStructure.User;
import database.DBLogin;

public class Server {
	static ArrayList<User> onlineUsers = new ArrayList<>();
	int port;
	int rsaBit;
	static String configPath;
	DBLogin db;

	public Server() throws IOException {
		configPath = System.getProperty("user.home")+"\\EdmondChatRoomServer\\";
		readConfig();
		checkRSAKeys();
		setupServer();
	}

	public void setupServer() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			while (true) {
				System.out.println("thread started");
				Socket socket = serverSocket.accept();
				ServerThread s = new ServerThread(socket, db);
				s.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkRSAKeys() {
		File file1 = new File(configPath+"rsa\\publicKey");
		File file2 = new File(configPath+"rsa\\privateKey");
		File parentFolder = file1.getParentFile();
		if (parentFolder != null && !parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		if (!file1.exists() || !file2.exists()) {
			RSA rsa = new RSA(rsaBit);
			ReadWrite.writeByteToFile(file1, rsa.getPublicKey().getEncoded());
			ReadWrite.writeByteToFile(file2, rsa.getPrivateKey().getEncoded());
		}
	}

	private void readConfig() {
		File serverConfigFile = new File(configPath+"config.json");
		File parentFolder = serverConfigFile.getParentFile();
		// 检查并创建文件夹（如果不存在）
		if (parentFolder != null && !parentFolder.exists()) {
			parentFolder.mkdirs();
		}
		if (!serverConfigFile.exists()) {
			System.out.println("First time startup, system configuration is required...");
			System.out.println("input database IP address:");
			@SuppressWarnings("resource")
			Scanner scanner1 = new Scanner(System.in);
			String dbAddress = scanner1.nextLine();
			System.out.println("input database user name:");
			@SuppressWarnings("resource")
			Scanner scanner2 = new Scanner(System.in);
			String dbName = scanner2.nextLine();
			System.out.println("input database password:");
			@SuppressWarnings("resource")
			Scanner scanner3 = new Scanner(System.in);
			String dbPw = scanner3.nextLine();
			port = 6069;
			rsaBit = 1024;
			HashMap<String, Object> config = new HashMap<>();
			config.put("port", port);
			config.put("rsa_bit", rsaBit);
			config.put("db_address", dbAddress);
			config.put("db_name", dbName);
			config.put("db_pw", dbPw);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonConfig = gson.toJson(config);
			ReadWrite.writeStringToFile(serverConfigFile, jsonConfig);
			db = new DBLogin(dbAddress, dbName, dbPw);
		} else {
			try (FileReader fileReader = new FileReader(serverConfigFile)) {
				Gson gson = new Gson();
				JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
				port = jsonObject.get("port").getAsInt();
				rsaBit = jsonObject.get("rsa_bit").getAsInt();
				String dbAddress = jsonObject.get("db_address").getAsString();
				String dbName = jsonObject.get("db_name").getAsString();
				String dbPw = jsonObject.get("db_pw").getAsString();
				db = new DBLogin(dbAddress, dbName, dbPw);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new Server();
//		System.out.println(System.getProperty("user.home")+"\\EdmondChatRoom\\rsa");
	}
}
