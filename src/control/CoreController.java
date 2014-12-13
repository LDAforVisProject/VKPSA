package control;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
	@FXML private MenuItem menu_help; 
	@FXML private TabPane tabPane;
	
	private Map<String, VisTabController> visTabControllers;
	private VisTabController visTabController;
	
	public CoreController()
	{
		visTabControllers = new HashMap<String, VisTabController>();
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
        System.out.println();
        
        try {
        	// Load .fxml file, get reference to controller.
        	FXMLLoader fxmlLoader = new FXMLLoader();
	        Node tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/visTabContent.fxml").openStream());
	        visTabController = (VisTabController) fxmlLoader.getController();
			
	        // Create new tab.
	        Tab tab = new Tab();
	        
	        switch (item.getId()) {
				// Create new tab, anchor it, then fill it with content.
	        	case "menu_viewMenu_textSimilarity":
	        		// Add settings pane to tab content.
	        		visTabController.addSettingsPane("/view/viewSettings_textBlocks.fxml");

	        		// Ensure resizability of tab content.
	        		AnchorPane tabAnchorPane = new AnchorPane();
	        		tabAnchorPane.getChildren().add(tabContent);
	        		tabAnchorPane.setTopAnchor(tabContent, 0.0);
	        		tabAnchorPane.setBottomAnchor(tabContent, 0.0);
	        		tabAnchorPane.setLeftAnchor(tabContent, 0.0);
	        		tabAnchorPane.setRightAnchor(tabContent, 0.0);

	        		// Add tab to TabPane.
	        		tab.setText("Topical similarity - Text view");
					tab.setContent(tabAnchorPane);
					tabPane.getTabs().add(tab);
					
					// Load visualization file.
					visTabController.loadVisualizationFile("/js/test.html");
	        	break;
	        	
	        	default:
	        		System.out.println("fx:id '" + item.getId() + "' not known.");
	        	break;
	        }
		}
        
        catch (IOException e1) {
			e1.printStackTrace();
		}
    }
}
