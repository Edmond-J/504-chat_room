package application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import server.User;

public class Chat implements Initializable{
	String token;
	ArrayList<User> friendList;
	
	@FXML
	private ImageView CurrentAvatar, CurrentAvatar1;
	@FXML
	private Label CurrentName;
	@FXML
	private TextArea Editor, TalkHistory;
	@FXML
	private ListView<User> userList;
	
	

	public Chat(String token, ArrayList<User> friendList) {
		super();
		this.token = token;
		this.friendList = friendList;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 构建用户列表
		
		userList=new ListView<>();
		ObservableList<User> friends = FXCollections.observableArrayList();
		System.out.println(friendList.size());
		
	}
	
	public void fetchFriendsStatus(ArrayList<User> friendList) {
		this.friendList=friendList;
	}
	
	@FXML
	public void sendMessage() {
		
	}
	
	public void show() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatRoom.fxml"));
			loader.setController(this);
			Scene scene = new Scene(loader.load());
			Stage newStage = new Stage();
			newStage.setScene(scene);
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void setToken(String token) {
		this.token = token;
	}


	public void setFriendList(ArrayList<User> friendList) {
		this.friendList = friendList;
	}



	
	
	
}
