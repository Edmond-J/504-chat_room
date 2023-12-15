package application;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cipher.AES;
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

public class Chat {
	String currentUser;
	String peerUser;
//	Socket client;
//	PrintWriter out;
//	BufferedReader in;
	String token;
	ArrayList<Friend> friendList;
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

	public void buildUI(ArrayList<Friend> friendList) {
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
					if(newValue.isOnline())
					sendButton.setDisable(false);
					else sendButton.setDisable(true);
				});
	}

	public void setupPortListener() {
		new Thread(() -> {
			while (true) {
				try {
					Gson gson = new Gson();
					String response = Login.in.readLine();
					System.out.println(response);
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
						String bodyRxDe = AES.fakeDecryption(bodyRx);
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
		}).start();
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
		String bodyTxEn = AES.fakeEncryption(gson.toJson(messageObject));
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

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
//	public void setClient(Socket client) {
//		this.client = client;
//	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setFriendList(ArrayList<Friend> friendList) {
		this.friendList = friendList;
	}
}
