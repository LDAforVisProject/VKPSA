package control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class DataViewController extends Controller
{
	// -----------------------------------------------
	// 				UI elements.
	// -----------------------------------------------
	
	private @FXML AnchorPane loading_anchorpane;
	private @FXML AnchorPane preprocessing_anchorpane;
	private @FXML AnchorPane generation_anchorpane;
	
	
	// -----------------------------------------------
	// 				Methods.
	// -----------------------------------------------
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_DataViewController.");
	}

	public AnchorPane getContainer(String viewID)
	{
		AnchorPane result = null;
		
		switch (viewID) {
			case "load":
				result = loading_anchorpane;
			break;
			
			case "preprocess":
				result = preprocessing_anchorpane;
			break;
				
			case "generate":
				result = generation_anchorpane;
			break;
			
			default:
				System.out.println("viewID '" + viewID + "' unknown.");
		}
		
		return result;
	}
}
