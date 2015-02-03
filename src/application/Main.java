package application;
	

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import control.CoreController;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
			// @todo Write metadata in Python generated topic files. 
	        
			// Load core .fxml file. 
			FXMLLoader fxmlLoader			= new FXMLLoader();
			Pane root 						= (Pane) fxmlLoader.load(getClass().getResource("/view/Core.fxml").openStream());
			CoreController coreController	= (CoreController) fxmlLoader.getController();
			Scene scene						= new Scene(root);
			
			coreController.setScene(scene);
	        primaryStage.setTitle("VKPSA");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	        // Add CSS sheet.
			scene.getStylesheets().add(getClass().getResource("/view/css/visTabContent.css").toExternalForm());
			
			// Add resize listeners.
			addResizeListeners(scene, coreController);
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addResizeListeners(Scene scene, final CoreController coreController)
	{
		scene.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth)
		    {
		        coreController.redrawVisualizations(newSceneWidth.doubleValue(), 0);
		    }
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
		    {
		        coreController.redrawVisualizations(0, newSceneHeight.doubleValue());
		    }
		});
	}
		
	public static void main(String[] args) 
	{
		launch(args);
	}
}
