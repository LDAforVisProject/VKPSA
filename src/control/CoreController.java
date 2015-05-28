package control;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import model.workspace.Workspace;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
		String directory	= "D:\\Workspace\\Scientific Computing\\VKPSA\\src\\data";
		workspace			= new Workspace(directory);
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
				
				// Is controller already initiated? If so, node was already created.
				if (!viewNodeMap.containsKey("analyze")) {
					initView("analyze", "/view/SII/SII_Content_Analysis.fxml");
					
					// Draw scatterchart.
					((AnalysisController)controllerMap.get("analyze")).refreshVisualizations();
				}
				
				// Controller already exists: Switch to node.
				else {
					enableView("analyze");
				}
			break;
			
			case "icon_load":
				label_title.setText("Load");
				
				if (!viewNodeMap.containsKey("load")) {
					initView("load", "/view/SII/SII_Content_Load.fxml");
				}
				
				else {
					enableView("load");
				}
			break;
			
			case "icon_preprocess":
				label_title.setText("Preprocess");
				
				if (!viewNodeMap.containsKey("preprocess")) {
					initView("preprocess", "/view/SII/SII_Content_Preprocess.fxml");
				}
				
				else {
					enableView("preprocess");
				}
			break;
			
			case "icon_generate":
				label_title.setText("Generate");
				
				if (!viewNodeMap.containsKey("generate")) {
					initView("generate", "/view/SII/SII_Content_Generate.fxml");
				}
				
				else {
					enableView("generate");
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
			
			// Ensure resizability of tab content.
			AnchorPane.setTopAnchor(contentNode, 0.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
		} 
        
        catch (IOException e) {
			e.printStackTrace();
		}
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
}