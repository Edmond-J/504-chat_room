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
	static CipherBox cipher;
	static String algorithm;
	static int bit;
	static Socket client;
	static PrintWriter out;
	static BufferedReader in;
	@FXML
	TextField userNameTF, passwordTF;
	@FXML
	ImageView userAvatarIV;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		userNameTF.setText("jam");
		passwordTF.setText("jam");
		configPath = System.getProperty("user.home")+"\\EdmondChatRoom\\";
		String lastUser = "jam";
		String lastUserAvatar = "";
		File clientConfigFile = new File(configPath+"config.json");
		if (!clientConfigFile.exists()) {
			serverName = "Localhost";
			serverPort = 6069;
			algorithm = "AES";
			bit = 128;
			saveConfigToFile();
		} else {
			try (FileReader fileReader = new FileReader(clientConfigFile)) {
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
		/*Setup Login UI*/
		if (lastUser.length() > 0) {
			userNameTF.setText(lastUser);
			if (lastUserAvatar.length() > 0) {
				userAvatarIV.setImage(new Image(lastUserAvatar));
			}
		}
		setupComm();
	}

	static public void saveConfigToFile() {
		File clientConfigFile = new File(configPath+"config.json");
		File parentFoler = clientConfigFile.getParentFile();
		if (parentFoler != null && !parentFoler.exists()) {
			parentFoler.mkdirs();
		}
		HashMap<String, Object> config = new HashMap<>();
		config.put("server_name", serverName);
		config.put("server_port", serverPort);
		config.put("algorithm", algorithm);
		config.put("bit", bit);
		config.put("laster_user", "");
		config.put("avatar", "");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonConfig = gson.toJson(config);
		ReadWrite.writeStringToFile(clientConfigFile, jsonConfig);
	}

	public void prepareKey() {
		/*Prepare Key*/
		File keyFile = new File(configPath+"symmetric\\"+userNameTF.getText()+"\\"+algorithm+"-"+bit);
		File parentFoler = keyFile.getParentFile();
		if (parentFoler != null && !parentFoler.exists()) {
			parentFoler.mkdirs();
		}
		if (!keyFile.exists()) {
			CipherBox.createKeyFile(algorithm, bit, keyFile);
		}
		cipher = new CipherBox(algorithm, bit, keyFile);
//		String ss=cipher.encrypt("good anmeng");
//		System.out.println(ss);
//		System.out.println(cipher.decrypt(ss));
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
	private void loadSettingPage() {
		ConnectController connController = new ConnectController();
		connController.show();
	}

	@FXML
	public void login() {
		if (userNameTF.getText().length() == 0 || passwordTF.getText().length() == 0) {
			System.out.println("user name or password can't be empty");
			return;
		}
		System.out.println("Connecting to server："+serverName+" ，port："+serverPort);
		System.out.println("Local socket："+client.getLocalSocketAddress());
		File file = new File(configPath+"server_rsa\\public");
		if (!file.exists()) {
			requireKey();
		}
		prepareKey();
		validate();
	}

	private void requireKey() {
		try {
			JsonObject messageObject = new JsonObject();
			messageObject.addProperty("req_type", "public_key");
			messageObject.addProperty("user", userNameTF.getText());// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
			Gson gson = new Gson();
			out.println(gson.toJson(messageObject));
			File keyFile = new File(configPath+"rsa_server\\publicKey");
			File parentFoler = keyFile.getParentFile();
			if (parentFoler != null && !parentFoler.exists()) {
				parentFoler.mkdirs();
			}
			String response = in.readLine();
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
			String resType = jsonObject.get("res_type").getAsString();
			if (resType.equals("key_delivery")) {
				ReadWrite.writeStringToFile(keyFile, jsonObject.get("key").getAsString());
			}
//			String result = in.readLine();
//			System.out.println(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void validate() {
		try {
			JsonObject message = new JsonObject();
			message.addProperty("user", userNameTF.getText());
			message.addProperty("password", passwordTF.getText());
			message.addProperty("algorithm", algorithm);
			File keyFile = new File(configPath+"symmetric\\"+userNameTF.getText()+"\\"+algorithm+"-"+bit);
			message.addProperty("key", ReadWrite.readStringFromFile(keyFile));
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
				String bodyRxDe = cipher.decrypt(bodyRx);
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
			Stage stage = (Stage)userNameTF.getScene().getWindow();// 必须从一个节点获取
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
