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
		// 		Actual (raw and (pre-)processed) data
		// -----------------------------------------------
		
		int n						= 400;
		int numberOfTopics			= 15;
		int numberOfKeywords		= 14000;
		int keywordStringSize		= 15;
		int primitiveSizeInBytes	= 8;
		
		// Calculated sizes.
		float size_MDS				= n * 2 * primitiveSizeInBytes;
		BigDecimal size_distances	= new BigDecimal(1);
		BigDecimal size_topicDist	= new BigDecimal(1);
		BigDecimal size_rawData		= new BigDecimal(1);
		
		// Topic model size_distances: n * n * primitiveSizeInBytes / 2
		size_distances = size_distances.multiply(new BigDecimal(n));
		size_distances = size_distances.multiply(new BigDecimal(n));
		size_distances = size_distances.multiply(new BigDecimal(primitiveSizeInBytes * 2));
		
		size_distances = size_distances.divide(new BigDecimal(2));
		size_distances = size_distances.divide(new BigDecimal(1024));
		size_distances = size_distances.divide(new BigDecimal(1024));
		size_distances = size_distances.divide(new BigDecimal(1024));
		
		// size_topicDist: n * numberOfTopics * n * numberOfTopics * primitiveSizeInBytes / 2
		size_topicDist = size_topicDist.multiply(new BigDecimal(n));
		size_topicDist = size_topicDist.multiply(new BigDecimal(n));
		size_topicDist = size_topicDist.multiply(new BigDecimal(numberOfTopics));
		size_topicDist = size_topicDist.multiply(new BigDecimal(numberOfTopics));
		size_topicDist = size_topicDist.multiply(new BigDecimal(primitiveSizeInBytes * 2));
		
		size_topicDist = size_topicDist.divide(new BigDecimal(2));
		size_topicDist = size_topicDist.divide(new BigDecimal(1024));
		size_topicDist = size_topicDist.divide(new BigDecimal(1024));
		size_topicDist = size_topicDist.divide(new BigDecimal(1024));
		
		// size_rawData: n * numberOfTopics * numberOfKeywords * (keywordStringSize + primitiveSizeInBytes)
		size_rawData = size_rawData.multiply(new BigDecimal(n));
		size_rawData = size_rawData.multiply(new BigDecimal(numberOfTopics));
		size_rawData = size_rawData.multiply(new BigDecimal(numberOfKeywords));
		size_rawData = size_rawData.multiply(new BigDecimal(keywordStringSize + primitiveSizeInBytes));
		
		size_rawData = size_rawData.divide(new BigDecimal(1024));
		size_rawData = size_rawData.divide(new BigDecimal(1024));
		size_rawData = size_rawData.divide(new BigDecimal(1024));
		
		System.out.println("# Rough estimate of amount of data in or out of memory:");
		System.out.println("# \tEstimated for (n = " + n + ", primitive size in bytes = " + primitiveSizeInBytes + "), without optimizations. ");
		System.out.println("# \tMDS data (in MB)\t\t\t= " + size_MDS / (1024 * 1024));
		System.out.println("# \tTopic model distance data (in GB)\t= " + size_distances);
		System.out.println("# \tTopic distance data (in GB)\t\t= " + size_topicDist);
		System.out.println("# \tRaw data (in GB)\t\t\t= " + size_rawData);
		System.out.println("\n\n");
		
		
		// -----------------------------------------------
		// 					Run VKPSA
		// -----------------------------------------------
		
//		DBManagement dbm = new DBManagement("D:\\Workspace\\Scientific Computing\\VKPSA_data\\vkpsa_newKW.db");
//		dbm.importReferenceTopicModel("D:\\Workspace\\Scientific Computing\\VKPSA_data\\referenceTopicModel.csv");
		
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
