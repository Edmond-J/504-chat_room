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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectController implements Initializable {
	Scene scene;
	@FXML
	private ComboBox<String> encryptCB;
	@FXML
	private TextField ipTF, portTF;
	@FXML
	private Button applyButton, cancelButton;

	public ConnectController() {
		System.out.println("constructor");
	}

	public void show() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ConnectivitySetting.fxml"));
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
		if (ipTF.getText().length() > 0)
			Login.serverName = ipTF.getText();
		if (portTF.getText().length() > 0)
			Login.serverPort = Integer.valueOf(portTF.getText());
		if (encryptCB.getValue().equals("AES-128")) {
			Login.algorithm = "AES";
			Login.bit = 128;
		} else if (encryptCB.getValue().equals("AES-192")) {
			Login.algorithm = "AES";
			Login.bit = 192;
		} else if (encryptCB.getValue().equals("AES-256")) {
			Login.algorithm = "AES";
			Login.bit = 256;
		} else if (encryptCB.getValue().equals("DES")) {
			Login.algorithm = "DES";
			Login.bit = 0;
		}
		Login.saveConfigToFile();
		Login.prepareKey();
		close();
	}
	
	@FXML
	private void close() {
		Stage stage = (Stage)scene.getWindow();
		stage.close();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ipTF.setText(Login.serverName);
		portTF.setText(""+Login.serverPort);
		encryptCB.getItems().addAll("AES-128", "AES-192", "AES-256", "DES");
		encryptCB.setPromptText(Login.algorithm+"-"+Login.bit);
		cancelButton.setOnAction(e -> {
			close();
		});
		applyButton.setOnAction(e -> {
			apply();
		});
	}
}
