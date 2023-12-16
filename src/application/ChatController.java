package application;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dataStructure.Friend;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ChatController {
	String currentUser;
	String peerUser;
	boolean isEncrypted;
	String token;
	ArrayList<Friend> friendList;
	volatile boolean isrunning = true;
	@FXML
	private ImageView currentAvatar, setting;
	@FXML
	private Label sourceUser, destUser, userCount;
	@FXML
	private TextArea editor, talkHistory;
	@FXML
	private ListView<Friend> userList;
	@FXML
	private Button sendButton;

	public void setFields(String currentUser, boolean isEncrypted, String token, ArrayList<Friend> friendList) {
		this.currentUser = currentUser;
		this.isEncrypted = isEncrypted;
		this.token = token;
		this.friendList = friendList;
	}

	public void buildUI(ArrayList<Friend> friendList) {
		Stage stage = (Stage)sendButton.getScene().getWindow();
		stage.setOnCloseRequest(e -> {
			try {
				exit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("close chat");
		});
		sourceUser.setText(currentUser);
		this.friendList = friendList;
		ObservableList<Friend> friends = FXCollections.observableArrayList();
		int activeUser = 0;
		for (Friend user : friendList) {
			if (!user.getName().equals(currentUser)) {
				friends.add(user);
				if (user.isOnline()) {
					activeUser++;
				}
			}
		}
		userCount.setText("Active Friends:  ("+activeUser+"/"+(friendList.size()-1)+")");
		userList.setCellFactory(lv -> new ListCell<Friend>() {
			@Override
			protected void updateItem(Friend friend, boolean empty) {
				super.updateItem(friend, empty);
				if (empty || friend == null) {
					setText(null);
					setStyle("");
				} else {
					setText(friend.getName());
					if (!friend.isOnline()) {
						setStyle("-fx-text-fill: gray;");
					} else {
						setStyle("-fx-text-fill: blue;");
					}
				}
			}
		});
		userList.setItems(friends);
		userList.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends Friend> observable, Friend oldValue, Friend newValue) -> {
//					System.out.println("Selected item: "+newValue.getUsername());
					peerUser = newValue.getName();
					destUser.setText(peerUser);
					if (newValue.isOnline())
						sendButton.setDisable(false);
					else sendButton.setDisable(true);
				});
	}

	public void setupPortListener() {
		new Thread(() -> {
			while (isrunning) {
				portListener();
			}
		}).start();
	}

	public void portListener() {
		try {
			Gson gson = new Gson();
			String response = Login.in.readLine();
			if (response == null)
				return;
			// 处理收到的消息
			JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
			String resType = jsonObject.get("res_type").getAsString();
			if (resType.contains("online_broadcast")) {
				System.out.println("new user");
				String newUser = jsonObject.get("new_user").getAsString();
				for (Friend friend : friendList) {
					if (friend.getName().equals(newUser)) {
						friend.setOnline(true);
					}
				}
				buildUI(friendList);
			}
			if (resType.contains("paging")) {
				String bodyRx = jsonObject.get("body").getAsString();
				String bodyRxDe = Login.cipher.decrypt(bodyRx);
				JsonObject jsonBodyRx = gson.fromJson(bodyRxDe, JsonObject.class);
				String time = jsonBodyRx.get("time").getAsString();
				String source = jsonBodyRx.get("source").getAsString();
				String dest = jsonBodyRx.get("dest").getAsString();
				String message = jsonBodyRx.get("message_body").getAsString();
				talkHistory.appendText(source+": "+time+"\n");
				talkHistory.appendText("<-"+message+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void showAccountSetting() {
		AccountController ac = new AccountController(isEncrypted);
		ac.show();
	}

	@FXML
	public void sendMessage() {
		if (editor.getText().length() == 0 || peerUser == null) {
			return;
		}
		JsonObject messageObject = new JsonObject();
//		messageObject.addProperty("req_type", "message");
		messageObject.addProperty("token", token);
		messageObject.addProperty("dest", peerUser);
		messageObject.addProperty("message_body", editor.getText());
		Gson gson = new Gson();
		String bodyTxEn = Login.cipher.encrypt(gson.toJson(messageObject));
		JsonObject jsonToSend = new JsonObject();
		jsonToSend.addProperty("req_type", "message");
		jsonToSend.addProperty("source", currentUser);// 加入user为了防止恶意攻击,需要用一个合理的user身份证明，使用用户输入值不可靠
		jsonToSend.addProperty("body", bodyTxEn);
		PrintWriter out;
		try {
			out = new PrintWriter(Login.client.getOutputStream(), true);
			out.println(gson.toJson(jsonToSend));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 可以加收到确认
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm");
		String currentTime = dtf.format(LocalTime.now());
		talkHistory.appendText(currentUser+": "+currentTime+"\n");
		talkHistory.appendText("->"+editor.getText()+"\n");
		editor.clear();
	}

	@FXML
	public void exit() throws IOException {
		isrunning = false;
		System.out.println("167 client logout");
		Login.releaseResource();
		Stage stage = (Stage)userList.getScene().getWindow();
		stage.close();
	}
}
