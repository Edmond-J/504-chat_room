package application;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
	boolean encryptStatus;
	Scene scene;
	@FXML
	private Button applyButton, cancelButton;
	@FXML
	private PasswordField pwConPF;
	@FXML
	private Label pwLabel;
	@FXML
	private TextField pwTF;
    @FXML
    private CheckBox encryptedCB;

	public AccountController(boolean isEncrypted) {
		this.encryptStatus = isEncrypted;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cancelButton.setOnAction(event -> {
			close();
		});
		applyButton.setOnAction(event -> {
			close();
		});
		encryptedCB.setSelected(encryptStatus);
		encryptedCB.setOnAction(event->{
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
	private void close() {
		Stage stage = (Stage)scene.getWindow();
		stage.close();
	}
}
