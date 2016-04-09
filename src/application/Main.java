package application;

import java.math.BigDecimal;

import mdsj.ClassicalScaling;
import mdsj.Data;
import mdsj.MDSJ;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import control.CoreController;
import database.DBManagement;

public class Main extends Application
{
	/**
	 * Holds all command line arguments.
	 */
	private static String args[];
	
	@Override
	public void start(Stage primaryStage)
	{	
		// -----------------------------------------------
		// 					Run VKPSA
		// -----------------------------------------------
		
		try {
			// Load core .fxml file. 
			FXMLLoader fxmlLoader			= new FXMLLoader();
			Pane root 						= (Pane) fxmlLoader.load(getClass().getResource("/view/fxml/SII_Core.fxml").openStream());
			CoreController coreController	= (CoreController) fxmlLoader.getController();
			Scene scene						= new Scene(root);
			
			coreController.setScene(scene);
			coreController.setInitialWorkspaceDirectory(args[0]);
			primaryStage.setTitle("VKPSA");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	        // Add CSS sheet.
			scene.getStylesheets().add(getClass().getResource("/view/css/styles.css").toExternalForm());
			
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
		        coreController.resizeContent(newSceneWidth.doubleValue(), 0);
		    }
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
		    {
		        coreController.resizeContent(0, newSceneHeight.doubleValue());
		    }
		});
	}
	
	public static void main(String[] args)
	{
		// Copy CLI arguments.
		Main.args = args;
		// Launch application.
		launch(args);
	}

}
