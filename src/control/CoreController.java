package control;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import control.analysisView.AnalysisController;
import control.dataView.DataSubViewController;
import control.dataView.DataViewController;
import model.workspace.Workspace;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class CoreController extends Controller
{
	private @FXML Node root;
	
	private @FXML ImageView icon_settings;
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
	
	// Reference to main scene.
	private Scene scene;
	
	// References to instances of content pane's controllers.
	private Map<String, Controller> controllerMap;
	
	// References to loaded UI pane views.
	private Map<String, Node> viewNodeMap;
	
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
		
//		// Display data view at startup.
//		Timer displayDataViewTimer = new Timer(true);
//		displayDataViewTimer.scheduleAtFixedRate(
//		    new TimerTask() {
//		    	public void run() 
//		    	{ 
//		    		// Wait until CoreController is initialized, then display data view and cancel timer.
//		    		if (!root.getStyleClass().isEmpty()) {
//		    			initDataView();
//		    			cancel();
//		    		}
//		    	}
//		    }, 0, 10
//		);
	}
	
	public void setScene(Scene scene)
	{
		this.scene = scene;
	}
	
	/**
	 * 
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
				
				// Hide workspace chooser, if it is still open.
				((DataViewController)controllerMap.get("dataview")).hideWorkspaceChooser(true);
				
				// Is controller already initiated? If so, node was already created.
				if (!viewNodeMap.containsKey("analyze")) {
					initView("analyze", "/view/SII/SII_Content_Analysis.fxml");
					
					// Set scene and draw visualizations.
					((AnalysisController)controllerMap.get("analyze")).setScene(scene);;
					((AnalysisController)controllerMap.get("analyze")).refreshVisualizations(true);
				}
				
				// Controller already exists: Switch to node.
				else {					
					// Show analysis view.
					enableView("analyze");
				}
			break;
			
			case "icon_dataview":
				label_title.setText("Provide Data");
				
				if (!viewNodeMap.containsKey("dataview")) {
					initDataView();
				}
				
				else {
					enableView("dataview");
					
					// Check if a workspace was already chosen. If not: Show workspace chooser.
					if (workspace.getDirectory().isEmpty() || workspace.getDirectory() == "") {
						((DataViewController)controllerMap.get("dataview")).showWorkspaceChooser(true);
					}
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
			
			// Set current workspace in analysis controller.
			controller.setWorkspace(workspace);
			
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
		
		initView("dataview", "/view/SII/SII_Content_Data.fxml");
		DataViewController dvController	= (DataViewController)controllerMap.get("dataview");
		// Set reference to CoreController.
		dvController.setCoreController(this);
		
		/*
		 * Init sub data views.
		 */
		
        String[] viewIDs				= {"load", "preprocess", "generate"};
        String[] fxmlFilePaths			= {"/view/SII/SII_Content_Load.fxml", "/view/SII/SII_Content_Preprocess.fxml", "/view/SII/SII_Content_Generate.fxml"};
        
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
				controllerMap.get(viewID).setWorkspace(workspace);
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

        // Show workspace chooser.
		dvController.initWorkspaceChooser(viewNodeMap.get("load"), root, scene);
		dvController.showWorkspaceChooser(true);
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
	
	public void unblockAnalysisView()
	{
		icon_analyze.setDisable(false);
	}
	
	public void blockAnalysisView()
	{
		icon_analyze.setDisable(true);
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
		// Adapt width.
		if (width > 0) {
			
		}
		
		// Adapt height.
		if (height > 0) {
			
		}
		
		// Propagate event to other controllers.
		for (Controller controller : controllerMap.values()) {
			controller.resizeContent(width, height);
		}
	}
}