package control;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import model.Dataset;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class CoreController implements Initializable
{
	@FXML private Node root;
	@FXML private Menu menu;
	@FXML private Menu menu_viewMenu;
	@FXML private MenuItem menu_viewMenu_textSimilarity;
	@FXML private MenuItem menu_viewMenu_globalComparison;
	@FXML private MenuItem menu_help; 
	@FXML private TabPane tabPane;
	
	private Scene scene;
	
	private Map<String, TabController> visTabControllers;
	
	public CoreController()
	{
		visTabControllers = new HashMap<String, TabController>();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		System.out.println("Initializing Controller.");
	}
	
	@FXML
	public void menu_viewMenu_itemSelected(ActionEvent e) 
	{
		MenuItem item = (MenuItem) e.getSource();
        
        try {
        	// Load .fxml file, get reference to controller.
        	FXMLLoader fxmlLoader		= new FXMLLoader();
	        Node tabContent				= null;
	        AnchorPane tabAnchorPane	= new AnchorPane();
	        // Create new tab.
	        Tab tab						= new Tab();
	        
	        System.out.println(item.getId());
	        
			// Create new tab, anchor it, then fill it with content.
	        switch (item.getId()) {
	        	case "menu_viewMenu_globalComparison":
	        		// Load FXML file containing tab content.
	        		tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/GlobalComparisonTabContent.fxml").openStream());
	        		
	        		// Get controlle for tab pane.
	        		// @todo Ensure that current tab is not duplicated.
	        		visTabControllers.put(item.getId(), (GCTabController) fxmlLoader.getController());
	        		
	        		// Add settings pane to tab content.
	        		visTabControllers.get(item.getId()).addSettingsPane("/view/ViewSettings_textBlocks.fxml");
	        		
	        		// Ensure resizability of tab content.	        		
	        		tabAnchorPane.getChildren().add(tabContent);
	        		AnchorPane.setTopAnchor(tabContent, 0.0);
	        		AnchorPane.setBottomAnchor(tabContent, 0.0);
	        		AnchorPane.setLeftAnchor(tabContent, 0.0);
	        		AnchorPane.setRightAnchor(tabContent, 0.0);
	 		
	        		// Set title text.
	        		tab.setText("Topical similarity - Global Comparison");
	        		
					// Load visualization file.
	        		GCTabController controller_gc = (GCTabController) visTabControllers.get(item.getId());
	        		controller_gc.setCoordinates(Dataset.sampleTestData(true));
	        		controller_gc.draw();
	        	break;
	        	
	        	case "menu_viewMenu_textSimilarity":
	        		// Load FXML file containing tab content.
	        		tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/VisTabContent.fxml").openStream());
	        		
	        		// Get controlle for tab pane.
	        		// @todo Ensure that current tab is not duplicated.
	        		visTabControllers.put(item.getId(), (VisTabController) fxmlLoader.getController());
	        		
	        		// Add settings pane to tab content.
	        		visTabControllers.get(item.getId()).addSettingsPane("/view/ViewSettings_textBlocks.fxml");

	        		// Ensure resizability of tab content.
	        		tabAnchorPane.getChildren().add(tabContent);
	        		AnchorPane.setTopAnchor(tabContent, 0.0);
	        		AnchorPane.setBottomAnchor(tabContent, 0.0);
	        		AnchorPane.setLeftAnchor(tabContent, 0.0);
	        		AnchorPane.setRightAnchor(tabContent, 0.0);

	        		// Set title text.
	        		tab.setText("Topical similarity - Text view");
					
					// Load visualization file.
	        		VisTabController controller_tb = (VisTabController) visTabControllers.get(item.getId()); 
	        		controller_tb.loadVisualizationFile("/js/test.html");
	        	break;
	        	
	        	default:
	        		System.out.println("fx:id '" + item.getId() + "' not known.");
	        	break;
	        }
	        
	        // Add tab to GUI.
			tab.setContent(tabAnchorPane);
			tabPane.getTabs().add(tab);
		}
        
        catch (IOException e1) {
			e1.printStackTrace();
		}
    }
	
	/**
	 * Induces redraw of visualizations after resizing of stage.
	 * @param widthDelta
	 * @param heightDelta
	 */
	public void redrawVisualizations(double newWidth, double newHeight)
	{
		for (Map.Entry<String, TabController> item : visTabControllers.entrySet()) {
			item.getValue().updateBounds(newWidth, newHeight);
			item.getValue().draw();
		}
	}
	
	public void setScene(Scene scene)
	{
		this.scene = scene;
	}
}
