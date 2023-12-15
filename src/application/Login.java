package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import cipher.*;
import dataStructure.Friend;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Login implements Initializable {
	static String serverName;
	static int serverPort;
	static String configPath;
	static Cipher cipher;
	static String algorithm;
	static int bit;
	static Socket client;
	static PrintWriter out;
	static BufferedReader in;
	@FXML
	TextField userName, password;
	@FXML
	ImageView userAvatar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		userName.setText("jam");
		password.setText("jam");
		configPath = System.getProperty("user.home")+"\\EdmondChatRoom\\";
		File configFile = new File(configPath+"config.json");
		String lastUser = "jam";
		String lastUserAvatar = "";
		if (!configFile.exists()) {
			serverName = "Localhost";
			serverPort = 6069;
			algorithm = "AES";
			bit = 128;
			HashMap<String, Object> config = new HashMap<>();
			config.put("server_name", serverName);
			config.put("server_port", serverPort);
			config.put("algorithm", algorithm);
			config.put("bit", bit);
			config.put("laster_user", "");
			config.put("avatar", "");
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String jsonConfig = gson.toJson(config);
			ReadWrite.writeStringToFile(configFile, jsonConfig);
		} else {
			try (FileReader fileReader = new FileReader(configFile)) {
				Gson gson = new Gson();
				JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
				serverName = jsonObject.get("server_name").getAsString();
				serverPort = jsonObject.get("server_port").getAsInt();
				algorithm = jsonObject.get("algorithm").getAsString();
				bit = jsonObject.get("bit").getAsInt();
				lastUser = jsonObject.get("laster_user").getAsString();
				lastUserAvatar = jsonObject.get("avatar").getAsString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*Prepare Key*/
		File keyFile = new File(configPath+"symmetric\\"+algorithm+bit);
		if (!keyFile.exists()) {
			Cipher.createKeyFile(algorithm, bit, keyFile);
		}
		cipher = new Cipher(algorithm, bit, configPath);
		/*Setup Login UI*/
		if (lastUser.length() > 0) {
			userName.setText(lastUser);
			if (lastUserAvatar.length() > 0) {
				userAvatar.setImage(new Image(lastUserAvatar));
			}
		}
		setupComm();
	}

	private void setupComm() {
		try {
			client = new Socket(serverName, serverPort);
			out = new PrintWriter(client.getOutputStream(), true);
			InputStreamReader inputStream = new InputStreamReader(client.getInputStream());
			in = new BufferedReader(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void login() {
		if (userName.getText().length() == 0 || password.getText().length() == 0) {
			System.out.println("user name or password can't be empty");
			return;
		}
		System.out.println("Connecting to server："+serverName+" ，port："+serverPort);
		System.out.println("Local socket："+client.getLocalSocketAddress());
		File file = new File(configPath+"server_rsa\\public");
		if (!file.exists()) {
			requireKey(client);
		}
		validate(client);
	}

	private void requireKey(Socket client) {
		try {
			JsonObject messageObject = new JsonObject();
			messageObject.addProperty("req_type", "public_key");
			messageObject.addProperty("user", userName.getText());// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
			Gson gson = new Gson();
			out.println(gson.toJson(messageObject));
			File file = new File(configPath+"rsa_server\\publicKey");
			String response = in.readLine();
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
			String resType = jsonObject.get("res_type").getAsString();
			if (resType.equals("key")) {
				ReadWrite.writeStringToFile(file, jsonObject.get("key").getAsString());
			}
//			String result = in.readLine();
//			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void validate(Socket client) {
		try {
			JsonObject message = new JsonObject();
			message.addProperty("user", userName.getText());
			message.addProperty("password", password.getText());
//			messageObject.addProperty("algorithm", algorithm);
//			messageObject.addProperty("bit", bit);
//			messageObject.addProperty("key", bit);
			Gson gson = new Gson();
			String bodyTx = gson.toJson(message);
			String bodyTxEn = RSA.encrypt(bodyTx, RSA.getPublicFromFile(configPath+"rsa_server\\publicKey"));
			JsonObject jsonToSend = new JsonObject();
			jsonToSend.addProperty("req_type", "validate");
			jsonToSend.addProperty("body", bodyTxEn);
			out.println(gson.toJson(jsonToSend));
			// 发送用户名密码
			String response = in.readLine();
			// 需要用AES解密
			System.out.println(response);
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
			String resType = jsonObject.get("res_type").getAsString();
			if (resType.contains("200")) {
				String bodyRx = jsonObject.get("body").getAsString();
				String bodyRxDe = AES.fakeDecryption(bodyRx);
				JsonObject jsonBodyRx = gson.fromJson(bodyRxDe, JsonObject.class);
				String token = jsonBodyRx.get("token").getAsString();
				String currentUser = jsonBodyRx.get("user").getAsString();
				String friends = jsonBodyRx.get("friends").getAsString();
				Type listType = new TypeToken<ArrayList<Friend>>() {
				}.getType();
				ArrayList<Friend> friendList = gson.fromJson(friends, listType);
				loadChatPage(currentUser, token, friendList);
				System.out.println("login succeed");
			} else if (resType.contains("300")) {
				System.out.println("no such user or user name and password not match");
			} else if (resType.contains("400")) {
				System.out.println("too much attamptions, please try later");
			} else System.out.println("server has no response");
//			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	private void negotiage(Socket client) {
//		JsonObject messageObject = new JsonObject();
//		messageObject.addProperty("req_type", "negotiate");
//		messageObject.addProperty("user", userName.getText());// 可以使用服务器发来的token
//		messageObject.addProperty("algorithm", algorithm);
//		messageObject.addProperty("bit", bit);
//		Gson gson = new Gson();
//		out.println(gson.toJson(messageObject));
//		// 收到token则启动聊天界面
//	}

	private void loadChatPage(String currentUser, String token, ArrayList<Friend> friendList) {
		try {
			Stage stage = (Stage)userName.getScene().getWindow();// 必须从一个节点获取
			stage.close();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoom.fxml"));
			Scene scene = new Scene(loader.load());
			Chat chatController = loader.getController();
			chatController.setCurrentUser(currentUser);
			chatController.setToken(token);
			chatController.buildUI(friendList);
			chatController.setupPortListener();
			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
