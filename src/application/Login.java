package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login implements Initializable {
	static final String SERVER_NAME = "Localhost";
	static final int PORT = 6069;
	String configPath;
	int serverPort;
	String algorithm;
	int bit;
	@FXML
	TextField userName, password;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configPath = System.getProperty("user.home")+"\\EdmondChatRoom\\";
		readConfig();
	}

	private void readConfig() {
		File file = new File(configPath+"\\config.txt");
		if (!file.exists()) {
			serverPort = 6068;
			algorithm = "AES";
			bit = 1024;
		}
	}

	@FXML
	public void login() {
		if (userName.getText().length() == 0 || password.getText().length() == 0) {
			System.out.println("user name or password can't be empty");
			return;
		}
		try {
			System.out.println("Connecting to server："+SERVER_NAME+" ，port："+PORT);
			Socket client = new Socket(SERVER_NAME, PORT);
			System.out.println("Local socket："+client.getLocalSocketAddress());
			OutputStream outputStream = client.getOutputStream();
			PrintWriter out = new PrintWriter(outputStream, true);
			if (!checkPublicKeys()) {
				JsonObject messageObject = new JsonObject();
				messageObject.addProperty("req_type", "publicKey");
				messageObject.addProperty("user", userName.getText());// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
				Gson gson = new Gson();
				out.println(gson.toJson(messageObject));
				// 请求获取公钥
			} else {
				JsonObject messageObject = new JsonObject();
				messageObject.addProperty("req_type", "validate");
				messageObject.addProperty("user", userName.getText());
				messageObject.addProperty("password", password.getText());
				Gson gson = new Gson();
				out.println(gson.toJson(messageObject));
				// 发送用户名密码
			}
			InputStreamReader inputStream = new InputStreamReader(client.getInputStream());
			BufferedReader in = new BufferedReader(inputStream);
			String result = in.readLine();
			System.out.println(result);
			if (result.contains("200")) {
				JsonObject messageObject = new JsonObject();
				messageObject.addProperty("req_type", "negotiate");
				messageObject.addProperty("user", userName.getText());//可以使用服务器发来的token
				messageObject.addProperty("algorithm", algorithm);
				messageObject.addProperty("bit", bit);
				Gson gson = new Gson();
				out.println(gson.toJson(messageObject));
				// 发送协商信息（加密类型、密钥）
				System.out.println("login succeed");
			} else if (result.contains("300")) {
				System.out.println("no such user or user name and password not match");
			} else if (result.contains("400")) {
				System.out.println("too much attamptions, please try later");
			} else System.out.println("server has no response");
			in.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkPublicKeys() {
		return false;
	}

	@FXML
	private void loadChatPage() {
		Parent chatBox;
		try {
			Stage stage=(Stage)userName.getScene().getWindow();// 必须从一个节点获取
			stage.close();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoom.fxml"));
			chatBox = loader.load();
			Chat chatCon = loader.getController();
			Scene scene=new Scene(chatBox);
			Stage newStage=new Stage();
			newStage.setScene(scene);
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
