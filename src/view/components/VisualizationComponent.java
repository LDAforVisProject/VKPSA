package view.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import view.components.rubberbandselection.ISelectableComponent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import control.Controller;
import control.analysisView.AnalysisController;
import model.workspace.Workspace;

public abstract class VisualizationComponent extends Controller implements ISelectableComponent 
{
	/**
	 * Root node of loaded .fxml.
	 */
	protected Node rootNode;
	
	/**
	 * Data set for this component.
	 */
	protected VisualizationComponentDataset data;
	
	/**
	 * Option set for this component.
	 */
	protected VisualizationComponentOptionset options;
	
	/**
	 * Reference to AnalysisController.
	 */
	protected AnalysisController analysisController;
	
	/**
	 * Contains all .fxml paths for the respective visualization components.
	 */
	public static final Map<VisualizationComponentType, String> FXML_PATHS = new HashMap<VisualizationComponentType, String>();
	static {
		FXML_PATHS.put(VisualizationComponentType.HEATMAP, "/view/SII/components/Heatmap.fxml");
	}
	
	/**
	 * Generates new instance of this type of VisualizationComponent. 
	 * @param type
	 * @param workspace
	 * @param logPI
	 * @param logTA
	 * @return
	 */
	public static VisualizationComponent generateInstance(	VisualizationComponentType type, AnalysisController analysisController, 
															Workspace workspace, ProgressIndicator logPI, TextArea logTA)
	{
		FXMLLoader fxmlLoader				= new FXMLLoader();
        Node rootNode						= null;
        VisualizationComponent component	= null;
        String path							= null;
        
        // Determine path to .fxml.
        switch (type)
		{
			case HEATMAP:
				path = FXML_PATHS.get(VisualizationComponentType.HEATMAP);
			break;
			
			default:
				System.out.println("Currently not supported.");
		}
        
        // Load .fxml.
        try {
			rootNode 	= (Node) fxmlLoader.load(new Object().getClass().getResource(path).openStream());
			// Get controller / component.
			component	= (VisualizationComponent)fxmlLoader.getController();
			
			// Set references.
			component.setRoot(rootNode);
			component.setReferences(analysisController, workspace, logPI, logTA);
        }
        
        catch (IOException e) {
			e.printStackTrace();
		}
        
        return component;
	}
	
	/**
	 * Embeds this component in parent node.
	 * @param parent
	 */
	public void embedIn(AnchorPane parent)
	{
		// Add root node to parent.
		parent.getChildren().add(this.rootNode);
		
		// Ensure resizability of content.
		AnchorPane.setTopAnchor(rootNode, 0.0);
		AnchorPane.setBottomAnchor(rootNode, 0.0);
		AnchorPane.setLeftAnchor(rootNode, 0.0);
		AnchorPane.setRightAnchor(rootNode, 0.0);
	}

	/*
	 * Getter and setter.
	 */
	
	/**
	 * Set references, including one to the supervising AnalysisController.
	 * @param analysisController
	 * @param workspace
	 * @param logPI
	 * @param logTA
	 */
	public void setReferences(AnalysisController analysisController, Workspace workspace, ProgressIndicator logPI, TextArea logTA)
	{
		super.setReferences(workspace, logPI, logTA);
		this.analysisController = analysisController;
	}
	
	public Node getRoot()
	{
		return rootNode;
	}

	public void setRoot(Node rootNode)
	{
		this.rootNode = rootNode;
	}
	
	/**
	 * Refreshes visualization, using the same data and option set already provided.
	 */
	public abstract void refresh();
}
