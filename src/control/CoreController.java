package control;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import control.dataView.LoadController;
import control.analysisView.AnalysisController;
import control.dataView.DataSubViewController;
import control.dataView.DataViewController;
import model.workspace.Workspace;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * Base controller for the entire application. 
 * @author RM
 *
 */
public class CoreController extends Controller
{
	private @FXML Node root;
	
	private @FXML ImageView icon_help;
	private @FXML ImageView icon_lock;
	
	private @FXML Label label_title;
	
	private @FXML ImageView icon_current;
	private @FXML ImageView icon_load;
	private @FXML ImageView icon_generate;
	private @FXML ImageView icon_preprocess;
	private @FXML ImageView icon_analyze;
	private @FXML ImageView icon_dataview;
	
	private @FXML AnchorPane pane_content;
	
	/**
	 * Protocol pane's ProgressIndicator.
	 */
	private @FXML ProgressIndicator protocol_progressindicator;
	/**
	 * Protocol pane's TextArea.
	 */
	private @FXML TextArea protocol_textarea;
	
	/**
	 *  Reference to main scene.
	 */
	private Scene scene;
	
	/**
	 *  References to instances of content pane's controllers.
	 */
	private Map<String, Controller> controllerMap;
	
	/**
	 *  References to loaded UI pane views.
	 */
	private Map<String, Node> viewNodeMap;
	
	/**
	 * Inital workspace directory (suggestion) in file chooser dialogue.
	 */
	private String initialWorkspaceDirectory;
	
	// ----------------------------------------------------
	//					Methods
	// ----------------------------------------------------
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_CoreController.");
		
		// Init map of UI views.
		viewNodeMap		= new HashMap<String, Node>();
		controllerMap	= new HashMap<String, Controller>();
		
		// Init workspace.
		workspace		= new Workspace("");
		
		blockAnalysisView();
		blockDataView();
		blockHelpView();
		
		// Enable logging in workspace.
		workspace.setProtocolElements(protocol_progressindicator, protocol_textarea);
	}
	
	public void setScene(Scene scene)
	{
		this.scene = scene;
	}
	
	/**
	 * Initialize introduction screen.
	 */
	private void initIntroScreen()
	{
		label_title.setText("Choose Workspace");
		
		// Is controller already initiated? If so, node was already created.
		if (!viewNodeMap.containsKey("load")) {
			initView("load", "/view/fxml/content/Load.fxml");
			
			// Adjust header icon's image.
			icon_current.setImage(new Image(getClass().getResourceAsStream("../icons/folder_small.jpg"), 30, 22, true, true));
			
			// Set scene and draw visualizations.
			((LoadController)controllerMap.get("load")).setCoreController(this);
		}
	}
	
	/**
	 * Handles processing of icons's action events.
	 * @param e
	 */
	@FXML
	public void actionButtonClicked(MouseEvent e) 
	{
		// Do nothing if ImageView is not source of the event.
		if (!(e.getSource() instanceof ImageView))
			return;
		
        // Get event source.
        ImageView source = (ImageView) e.getSource();
        
		switch (source.getId()) {
			case "icon_analyze":
				label_title.setText("Analyze");
				
				// Adjust header icon's image.
				icon_current.setImage(new Image(getClass().getResourceAsStream("../icons/eye_small.jpg"), 30, 20, true, true));
				
				// Is controller already initiated? If so, node was already created.
				if (!viewNodeMap.containsKey("analyze")) {
					// Change cursor.
					scene.setCursor(Cursor.WAIT);
					
					// Initialize analysis view.
					initView("analyze", "/view/fxml/content/Analysis.fxml");
					
					// Set scene and draw visualizations.
					((AnalysisController)controllerMap.get("analyze")).setScene(scene);
					((AnalysisController)controllerMap.get("analyze")).refreshVisualizations(true);
					
					// Change cursor.
					scene.setCursor(Cursor.DEFAULT);
				}
				
				// Controller already exists: Switch to node.
				else {
					scene.setCursor(Cursor.WAIT);
					// Show analysis view.
					enableView("analyze");
					scene.setCursor(Cursor.DEFAULT);
				}
			break;
			
			case "icon_dataview":
				label_title.setText("Generate Data");
				
				// Adjust header icon's image.
				icon_current.setImage(new Image(getClass().getResourceAsStream("../icons/data.png"), 50, 50, true, true));
				
				if (!viewNodeMap.containsKey("dataview")) {
					scene.setCursor(Cursor.WAIT);
					initDataView();
					scene.setCursor(Cursor.DEFAULT);
				}
				
				else {
					scene.setCursor(Cursor.WAIT);
					enableView("dataview");
					scene.setCursor(Cursor.DEFAULT);
				}
			break;
		}
	}
	
	/**
	 * Creates view (called at first action with this view).
	 * @param viewID
	 * @param path
	 */
	private void initView(String viewID, String path)
	{
    	FXMLLoader fxmlLoader	= new FXMLLoader();
        Node contentNode		= null;
        Controller controller	= null;
        
        try {
        	// Hide all other views.
        	hideAllViews();
        	
        	// Store node of this view.
			viewNodeMap.put( viewID, (Node) fxmlLoader.load(getClass().getResource(path).openStream()) );
			contentNode = viewNodeMap.get(viewID);
			
			// Get controller.
			controllerMap.put(viewID, (Controller)fxmlLoader.getController());
			controller = controllerMap.get(viewID);
			
			// Add to content pane.
			pane_content.getChildren().add(contentNode);
			
			// Set current workspace and protocol elements in analysis controller.
			controller.setReferences(workspace, protocol_progressindicator, protocol_textarea);
			
			// Ensure resizability of content.
			AnchorPane.setTopAnchor(contentNode, 0.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
		} 
        
        catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates data view (called at first action with this view).
	 */
	private void initDataView()
	{
		/*
		 * Init actual data view.
		 */
		
		initView("dataview", "/view/fxml/content/Data.fxml");
		DataViewController dvController	= (DataViewController)controllerMap.get("dataview");
		// Set reference to CoreController.
		dvController.setCoreController(this);
		
		/*
		 * Init sub data views.
		 */
		  
        String[] viewIDs				= {"preprocess", "generate"};
        String[] fxmlFilePaths			= {"/view/fxml/content/Postprocess.fxml", "/view/fxml/content/Generate.fxml"};
        
        try {
        	// Add all relevant sub-views to DataView.
        	for (int i = 0; i < viewIDs.length; i++) {
            	FXMLLoader fxmlLoader	= new FXMLLoader();
                Node contentNode		= null;
                
        		String viewID			= viewIDs[i];
        		String filepath			= fxmlFilePaths[i];
        		
	        	// Store node of this view.
	        	viewNodeMap.put( viewID, (Node) fxmlLoader.load(getClass().getResource(filepath).openStream()) );
				contentNode = viewNodeMap.get(viewID);
				
	        	// Get and store controller.
	        	controllerMap.put(viewID, (Controller)fxmlLoader.getController());
				
	        	// Set current workspace in analysis controller.
	        	controllerMap.get(viewID).setReferences(workspace, protocol_progressindicator, protocol_textarea);				
				
				// Set dataViewController in respective sub-controller.
				((DataSubViewController)controllerMap.get(viewID)).setDataViewController(dvController);
				
				// Add to container.
				((AnchorPane) dvController.getContainer(viewID)).getChildren().add(contentNode);
				
				// Ensure resizability of tab content.
				AnchorPane.setTopAnchor(contentNode, 0.0);
	    		AnchorPane.setBottomAnchor(contentNode, 0.0);
	    		AnchorPane.setLeftAnchor(contentNode, 0.0);
	    		AnchorPane.setRightAnchor(contentNode, 0.0);
        	}
		} 
        
        catch (IOException e) {
			e.printStackTrace();
		}
        
        /*
         * Set references between DataViewController and SubDataViewControllers. 
         */
        
        Map<String, Node> dataSubViewNodes				= new HashMap<String, Node>(viewNodeMap);
		Map<String, Controller> dataSubViewControllers	= new HashMap<String, Controller>(controllerMap);
		
		// Remove analysis and data view (management) from cloned collections.
		dataSubViewNodes.remove("analyze");
		dataSubViewControllers.remove("analyze");
		dataSubViewNodes.remove("dataview");
		dataSubViewControllers.remove("dataview");
		
		// Assign data sub views to DataViewController instance.
		dvController.setDataSubViews(dataSubViewNodes, dataSubViewControllers);
	}
	
	private void hideAllViews()
	{
		for (Node viewNode : viewNodeMap.values()) {
			viewNode.setDisable(true);
			viewNode.setVisible(false);
		}
	}
	
	private void enableView(String viewID)
	{
		hideAllViews();
		
		viewNodeMap.get(viewID).setDisable(false);
		viewNodeMap.get(viewID).setVisible(true);
		
		// Show subview of dataview, if viewID is "dataview".
		if (viewID == "dataview") {
			viewNodeMap.get("load").setDisable(false);
			viewNodeMap.get("load").setVisible(true);
			
			viewNodeMap.get("preprocess").setDisable(false);
			viewNodeMap.get("preprocess").setVisible(true);
			
			viewNodeMap.get("generate").setDisable(false);
			viewNodeMap.get("generate").setVisible(true);
		}
	}
	
	/**
	 * Unblock data and analysis views once data has been loaded.
	 */
	public void unblockViews()
	{
		unblockDataView();
		unblockAnalysisView();
	}
	
	public void unblockAnalysisView()
	{
		icon_analyze.setDisable(false);
		icon_analyze.setEffect(null);
	}
	
	public void blockDataView()
	{
		icon_dataview.setDisable(true);
		icon_dataview.setEffect(new ColorAdjust(0, 0, 0.75, 0));
	}
	
	public void unblockDataView()
	{
		icon_dataview.setDisable(false);
		icon_dataview.setEffect(null);
	}
	
	public void blockAnalysisView()
	{
		icon_analyze.setDisable(true);
		icon_analyze.setEffect(new ColorAdjust(0, 0, 0.75, 0));
	}
	
	public void blockHelpView()
	{
		icon_help.setDisable(true);
		icon_help.setEffect(new ColorAdjust(0, 0, 0.75, 0));
	}
	
	/**
	 * Enable active / "button" cursor if it hovers over an active ImageView.
	 * @param e
	 */
	@FXML
	public void enableActiveCursor(MouseEvent e)
	{
		scene.setCursor(Cursor.HAND);
	}
	
	/**
	 * Disable active / "button" cursor if it hovers over an active ImageView.
	 * @param e
	 */
	@FXML
	public void disableActiveCursor(MouseEvent e)
	{
		scene.setCursor(Cursor.DEFAULT);
	}
	
	@Override
	public void resizeContent(double width, double height)
	{
		// Propagate event to other controllers.
		for (Controller controller : controllerMap.values()) {
			controller.resizeContent(width, height);
		}
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

	/**
	 * Sets intial workspace directory when file browser is used.
	 * @param string
	 */
	public void setInitialWorkspaceDirectory(String initialWorkspaceDirectory)
	{
		this.initialWorkspaceDirectory = initialWorkspaceDirectory;
		
		// Intialize loading/introduction view.
		initIntroScreen();
	}
	
	/**
	 * Fetches initial workspace directory.
	 * @return
	 */
	public String getInitialWorkspaceDirectory()
	{
		return initialWorkspaceDirectory;
	}
}