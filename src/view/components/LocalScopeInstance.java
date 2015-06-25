package view.components;

import java.util.Set;

import model.workspace.Workspace;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import control.analysisView.AnalysisController;
import control.analysisView.localScope.LocalScopeVisualizationController;

/**
 * Wrapper for local scope instance.
 * May be one of several visualizations (currently supported: Parallel tag clouds).
 * @author RM
 *
 */
public class LocalScopeInstance extends VisualizationComponent
{
	private AnchorPane anchorpane_localScope;
	private Label label_visType;
	
	private LocalScopeVisualizationController controller;
	
	private Workspace workspace;
	
	public LocalScopeInstance(AnalysisController analysisController, Workspace workspace, AnchorPane anchorpane_localScope, Label label_visType)
	{
		super(analysisController);
		
		this.workspace				= workspace;
		this.anchorpane_localScope	= anchorpane_localScope;
		this.label_visType			= label_visType;
	}
	
	public void load(String fxmlPath)
	{
		FXMLLoader fxmlLoader							= new FXMLLoader();
        Node contentNode								= null;
        
        try {
	      	// Here: Support for several local scope visualizations, if reasonable.
			contentNode	= (Node) fxmlLoader.load(getClass().getResource(fxmlPath).openStream());
			
			// Init controller.
			controller	= (LocalScopeVisualizationController)fxmlLoader.getController();
			controller.setWorkspace(workspace);
			controller.setAnchorPane(anchorpane_localScope);
			
			// Add to parent pane.
			anchorpane_localScope.getChildren().add(contentNode);
			
			// Ensure resizability of content.
			AnchorPane.setTopAnchor(contentNode, 35.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
        }
        
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        controller.refresh();
	}

	public void resize(double width, double height)
	{
		controller.resize(width, height);
	}

	@Override
	public void changeViewMode()
	{
		
	}
}
