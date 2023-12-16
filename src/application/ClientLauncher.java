package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class ClientLauncher extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent login = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene scene = new Scene(login, 400, 300);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Edmond ChatRoom");
			primaryStage.getIcons().add(new Image("file:../../img/chat.png"));
			primaryStage.setOnCloseRequest(event->{
				try {
					Login.releaseResource("give_up");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
