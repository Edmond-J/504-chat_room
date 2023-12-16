package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cipher.MD5;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AccountController implements Initializable {
	private ChatController chatController;
	private Scene scene;
	@FXML
	private Button applyButton, cancelButton;
	@FXML
	private PasswordField pwConPF;
	@FXML
	private Label pwLabel, feedbackLabel;
	@FXML
	private TextField pwTF;
	@FXML
	private CheckBox encryptedCB;

	public AccountController(ChatController cc) {
		this.chatController = cc;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cancelButton.setOnAction(event -> {
			close();
		});
		applyButton.setOnAction(event -> {
			apply();
			close();
		});
		encryptedCB.setSelected(chatController.isEncrypted());
		pwLabel.setDisable(!chatController.isEncrypted());
		pwTF.setDisable(!chatController.isEncrypted());
		pwConPF.setDisable(!chatController.isEncrypted());
		encryptedCB.setOnAction(event -> {
			pwLabel.setDisable(!encryptedCB.isSelected());
			pwTF.setDisable(!encryptedCB.isSelected());
			pwConPF.setDisable(!encryptedCB.isSelected());
		});
	}

	public void show() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AccountSetting.fxml"));
		try {
			loader.setController(this);
			Parent parent = loader.load();
			scene = new Scene(parent);
			Stage newStage = new Stage();
			newStage.setResizable(false);
			newStage.setScene(scene);
			newStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void apply() {
		feedbackLabel.setText(null);
		if (encryptedCB.isSelected()) {
			if (pwTF.getText().length() < 3) {
				feedbackLabel.setText("password has to be longer than 3");
			} else if (!pwTF.getText().equals(pwConPF.getText())) {
				feedbackLabel.setText("password is not match");
			} else {
				chatController.setEncrypted(true);
				feedbackLabel.setText("password updated");
				Gson gson = new Gson();
				JsonObject jsonToSend = new JsonObject();
				String bodyTxEn = Login.cipher.encrypt(MD5.toMd5(pwTF.getText()));
				jsonToSend.addProperty("message_pw", bodyTxEn);
				jsonToSend.addProperty("req_type", "enable_encrypt_message");
				Login.out.println(gson.toJson(jsonToSend));
			}
		} else {
			chatController.setEncrypted(false);
			Gson gson = new Gson();
			JsonObject jsonToSend = new JsonObject();
			jsonToSend.addProperty("req_type", "disable_encrypt_message");
			Login.out.println(gson.toJson(jsonToSend));
			feedbackLabel.setText("password canceled");
		}
	}

	@FXML
	private void close() {
		Stage stage = (Stage)scene.getWindow();
		stage.close();
	}
}
