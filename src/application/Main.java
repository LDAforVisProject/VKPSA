package application;
	

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try {
			// Load core .fxml file. 
			Pane root = FXMLLoader.load(getClass().getResource("/view/core.fxml"));
			Scene scene = new Scene(root);
	        primaryStage.setTitle("VKPSA");
	        primaryStage.setScene(scene);
	        primaryStage.show();

			scene.getStylesheets().add(getClass().getResource("/view/css/visTabContent.css").toExternalForm());
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void main(String[] args) 
	{
		launch(args);
	}
}
