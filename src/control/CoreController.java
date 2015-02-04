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
import javafx.scene.control.SingleSelectionModel;
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
	@FXML private MenuItem menu_viewMenu_database;
	@FXML private MenuItem menu_help; 
	@FXML private TabPane tabPane;
	
	private Scene scene;
	
	/**
	 * Stores tab controller by tab names.
	 */
	private Map<String, Initializable> visTabControllers;
	/**
	 * Stores references to tabs by tab names.
	 */
	private Map<String, Tab> tabs;
	
	public CoreController()
	{
		visTabControllers	= new HashMap<String, Initializable>();
		tabs				= new HashMap<String, Tab>();
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
	        boolean isTabUnique			= !visTabControllers.containsKey(item.getId()); 
	        
	        System.out.println(item.getId());
	        
			// Create new tab, anchor it, then fill it with content.
	        switch (item.getId()) {
	        	case "menu_viewMenu_globalComparison":
	        		// Load FXML file containing tab content.
	        		tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/GCTabContent.fxml").openStream());
	        		
	        		// Get controlle for tab pane.
	        		if (isTabUnique) {
	        			// Add reference to controller to map.
	        			visTabControllers.put(item.getId(), (GCTabController) fxmlLoader.getController());

	        			// Add reference to tab to map.
	        			tabs.put(item.getId(), tab);
	        			
		        		// Add settings pane to tab content.
	        			GCTabController controller_gc = (GCTabController) visTabControllers.get(item.getId()); 
	        			controller_gc.addSettingsPane("/view/ViewSettings_textBlocks.fxml");
		        
		        		// Set title text.
		        		tab.setText("Topical similarity - Global Comparison");
		        		
						// Load visualization file.
		        		controller_gc.setCoordinates(Dataset.sampleTestData(true));
		        		controller_gc.draw();
	        		}
	        		
	        		else {
	        			System.out.println("### Tab already opened. Switching to tab.");
	        			tabPane.getSelectionModel().select(tabs.get(item.getId()));
	        		}
	        	break;
	        	
	        	case "menu_viewMenu_database":
	        		// Load FXML file containing tab content.
	        		tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/DBTabContent.fxml").openStream());
	        		
	        		// Get controlle for tab pane.
	        		if (isTabUnique) {
	        			// Add reference to controller to map.
	        			visTabControllers.put(item.getId(), (DBTabController) fxmlLoader.getController());

	        			// Add reference to tab to map.
	        			tabs.put(item.getId(), tab);
	        			
		        		// Get controller
	        			DBTabController controller_db = (DBTabController) visTabControllers.get(item.getId());
	        			
		        		// Set title text.
		        		tab.setText("Database");
	        		}
	        		
	        		else {
	        			System.out.println("### Tab already opened. Switching to tab.");
	        			tabPane.getSelectionModel().select(tabs.get(item.getId()));	
	        		}
	        	break;
	        	
	        	case "menu_viewMenu_textSimilarity":
	        		// Load FXML file containing tab content.
	        		tabContent = (Node) fxmlLoader.load(getClass().getResource("/view/LCTabContent.fxml").openStream());
	        		
	        		// Get controlle for tab pane.
	        		visTabControllers.put(item.getId(), (LCTabController) fxmlLoader.getController());
	        		
	        		// Add settings pane to tab content.
	        		LCTabController controller_lc = (LCTabController) visTabControllers.get(item.getId()); 
	        		controller_lc.addSettingsPane("/view/ViewSettings_textBlocks.fxml");

	        		// Set title text.
	        		tab.setText("Topical similarity - Text view");
					
					// Load visualization file. 
	        		controller_lc.loadVisualizationFile("/js/test.html");
	        	break;
	        	
	        	default:
	        		System.out.println("fx:id '" + item.getId() + "' not known.");
	        	break;
	        }
	        
	        // Add tab to GUI.
	        if (isTabUnique) {
	        	// Add content to container.
        		tabAnchorPane.getChildren().add(tabContent);
        		
        		// Ensure resizability of tab content.
        		AnchorPane.setTopAnchor(tabContent, 0.0);
        		AnchorPane.setBottomAnchor(tabContent, 0.0);
        		AnchorPane.setLeftAnchor(tabContent, 0.0);
        		AnchorPane.setRightAnchor(tabContent, 0.0);
        		
        		// Move loaded content into newly created tab.
				tab.setContent(tabAnchorPane);
				// Add newly created tab to existing tab pane. 
				tabPane.getTabs().add(tab);
	        }
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
		for (Map.Entry<String, Initializable> item : visTabControllers.entrySet()) {
			if (item.getValue() instanceof VisualizationTabController) {
				VisualizationTabController tabController = (VisualizationTabController) item.getValue(); 
				tabController.updateBounds(newWidth, newHeight);
				tabController.draw();
			}
		}
	}
	
	public void setScene(Scene scene)
	{
		this.scene = scene;
	}
}
