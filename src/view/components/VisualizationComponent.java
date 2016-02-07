package view.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import view.components.rubberbandselection.ISelectableComponent;
import view.components.rubberbandselection.RubberBandSelection;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
	 * Component enabling rubberband-type selection of points in scatterchart.
	 */
	protected RubberBandSelection rubberbandSelection;
	
	/*
	 * Other data.
	 */
	
	/**
	 * Determines factor opacity of points not hovered over should be multiplied with if other points are hovered over. 
	 */
	public static final double HOVER_OPACITY_FACTOR = 1 / 25.0;
	
	/**
	 * Signifies whether the ctrl key is down at any given time.
	 */
	protected boolean isCtrlDown;
	
	/**
	 * Contains all .fxml paths for the respective visualization components.
	 */
	public static final Map<VisualizationComponentType, String> FXML_PATHS = new HashMap<VisualizationComponentType, String>();
	static {
		FXML_PATHS.put(VisualizationComponentType.NUMERICAL_HEATMAP, "/view/fxml/components/NumericalHeatmap.fxml");
		FXML_PATHS.put(VisualizationComponentType.CATEGORICAL_HEATMAP, "/view/fxml/components/CategoricalHeatmap.fxml");
		FXML_PATHS.put(VisualizationComponentType.SCENTED_FILTER, "/view/fxml/components/ScentedFilter.fxml");
		FXML_PATHS.put(VisualizationComponentType.PARAMSPACE_SCATTERCHART, "/view/fxml/components/ParameterSpaceScatterchart.fxml");
		FXML_PATHS.put(VisualizationComponentType.SETTINGS_PANEL, "/view/fxml/components/SettingsPanel.fxml");
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
        String path							= FXML_PATHS.get(type);
        
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
	 * Embeds this component in parent AnchorPane.
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
	
	/**
	 * Embeds this component in parent AnchorPane.
	 * @param parent
	 */
	public void addTo(AnchorPane parent)
	{
		// Add root node to parent.
		parent.getChildren().add(this.rootNode);
	}	
	
	/**
	 * Embeds this component in parent VBox.
	 * @param parent
	 */
	public void embedIn(VBox parent)
	{
		// Add root node to parent.
		parent.getChildren().add(this.rootNode);
		
		// Ensure resizability of content.
		AnchorPane.setTopAnchor(rootNode, 0.0);
		AnchorPane.setBottomAnchor(rootNode, 0.0);
		AnchorPane.setLeftAnchor(rootNode, 0.0);
		AnchorPane.setRightAnchor(rootNode, 0.0);
		
		// Set alignment within parent.
		parent.setAlignment(Pos.CENTER);
		
		// Set VgrowPriority in VBox.
		VBox.setVgrow(this.rootNode, Priority.NEVER);
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
	
	/**
	 * Initializes hover listener responsible for processing and propagation of hover event information.
	 */
	public abstract void initHoverEventListeners();
	
	/**
	 * Highlights all atomic visual entities containing the delivered data point IDs. 
	 * @param dataPointIDs
	 * @param idMode
	 */
	public abstract void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode);
	
	/**
	 * Wrapper for highlightHoveredOverDataPoints() - used for single data points.
	 * @param dataPointIDs
	 * @param idMode
	 */
	public void highlightHoveredOverDataPoint(int dataPointID, DatapointIDMode idMode)
	{
		Set<Integer> dataPointIDs = new HashSet<Integer>();
		dataPointIDs.add(dataPointID);
		
		highlightHoveredOverDataPoints(dataPointIDs, idMode);
	}
	
	/**
	 * Removes highlighting / repaints with default settings.
	 */
	public abstract void removeHoverHighlighting();
}
