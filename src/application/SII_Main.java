package application;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import control.CoreController;
import control.SII.SII_CoreController;

public class SII_Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try {
			// Load core .fxml file. 
			FXMLLoader fxmlLoader				= new FXMLLoader();
			Pane root 							= (Pane) fxmlLoader.load(getClass().getResource("/view/SII/SII_Core.fxml").openStream());
			SII_CoreController coreController	= (SII_CoreController) fxmlLoader.getController();
			Scene scene							= new Scene(root);
			
			coreController.setScene(scene);
	        primaryStage.setTitle("VKPSA");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        
	        // Add CSS sheet.
			scene.getStylesheets().add(getClass().getResource("/view/css/visTabContent.css").toExternalForm());
			
			// Add resize listeners.
//			addResizeListeners(scene, coreController);
		}
		
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void addResizeListeners(Scene scene, final SII_CoreController coreController)
	{
		scene.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth)
		    {
//		        coreController.resizeContent(newSceneWidth.doubleValue(), 0);
		    }
		});
		
		scene.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
		    {
//		        coreController.resizeContent(0, newSceneHeight.doubleValue());
		    }
		});
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}

}
