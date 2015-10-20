package view.components;

import java.util.ArrayList;

import model.LDAConfiguration;
import model.workspace.Workspace;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import control.analysisView.AnalysisController;
import control.analysisView.localScope.LocalScopeVisualizationController;
import control.analysisView.localScope.LocalScopeVisualizationType;

/**
 * Administers usage of two visualizations: The chord diagram (for the comparison of multiple topic models)
 * and the parallel tag cloud (for the comparison of multiple topics).
 * @author RM
 *
 */
public class LocalScopeInstance extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	/**
	 * AnchorPane holding parallel tag clouds visualization.
	 */
	private AnchorPane ptc_anchorpane;
	/**
	 * AnchorPane holding chord diagram visualization.
	 */
	private AnchorPane cd_anchorpane;
	
	// Option controls.
	private Slider slider_localScope_numTopicsToUse;
	private TextField textfield_localScope_numTopicsToUse;
	private Slider slider_localScope_numKeywordsToUse;
	private TextField textfield_localScope_numKeywordsToUse;
	
	/**
	 * Path to .fxml for parallel tag clouds.
	 */
	private final String ptcPath 	= "/view/SII/localScope/SII_Content_Analysis_LocalScope_ParallelTagCloud.fxml";
	/**
	 * Path to .fxml for chord diagram.
	 */
	private final String cdPath		= "/view/SII/localScope/SII_Content_Analysis_LocalScope_ChordDiagram.fxml";
	
	/*
	 * Other data.
	 */
	
	/**
	 * Reference to instance of local scope controller manipulating the parallel tag clouds visualization.
	 */
	private LocalScopeVisualizationController ptcController;
	/**
	 * Reference to instance of local scope controller manipulating the chord diagram visualization.
	 */
	private LocalScopeVisualizationController cdController;
	
	/**
	 * Reference to workspace.
	 */
	private Workspace workspace;
	
	/**
	 * Collection storing all LDA configurations relevant for this local scope. 
	 */
	private ArrayList<LDAConfiguration> selectedLDAConfigurations;
	
	// -----------------------------------------------
	// 		Methods.
	// -----------------------------------------------

	
	public LocalScopeInstance(	AnalysisController analysisController,
								AnchorPane ptc_anchorpane, AnchorPane cd_anchorpane, 
								Slider slider_localScope_numTopicsToUse, TextField textfield_localScope_numTopicsToUse,
								Slider slider_localScope_numKeywordsToUse, TextField textfield_localScope_numKeywordsToUse)
	{
		super(analysisController);
		
		this.ptc_anchorpane							= ptc_anchorpane;
		this.cd_anchorpane							= cd_anchorpane;
		this.slider_localScope_numTopicsToUse		= slider_localScope_numTopicsToUse;
		this.textfield_localScope_numTopicsToUse	= textfield_localScope_numTopicsToUse;
		this.slider_localScope_numKeywordsToUse		= slider_localScope_numKeywordsToUse;
		this.textfield_localScope_numKeywordsToUse	= textfield_localScope_numKeywordsToUse;
		
		// Deactive snap-to-tick functionality in sliders.
		slider_localScope_numTopicsToUse.setSnapToTicks(false);
		slider_localScope_numKeywordsToUse.setSnapToTicks(false);
		
		// Set up control listeners.
		initControlListener();
	}
	
	private void initControlListener()
	{
		/*
		 * Declare event handler for slider changes.
		 */
		
		EventHandler<MouseEvent> numTopicsHandler = new EventHandler<MouseEvent>()
		{
            public void handle(MouseEvent event) 
            {
            	// Round value.
            	int roundedNumberOfTopics = (int) Math.round(slider_localScope_numTopicsToUse.getValue());
            	// Update number in textfield.
            	textfield_localScope_numTopicsToUse.setText(String.valueOf(roundedNumberOfTopics));
            	
            	// Update visualization.
            	if (event.getEventType() == MouseEvent.MOUSE_RELEASED && selectedLDAConfigurations != null) {
            		ptcController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), roundedNumberOfTopics, (int)slider_localScope_numKeywordsToUse.getMax(), (int) Math.round(slider_localScope_numKeywordsToUse.getValue()), false);
            		cdController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), roundedNumberOfTopics, (int)slider_localScope_numKeywordsToUse.getMax(), (int) Math.round(slider_localScope_numKeywordsToUse.getValue()), false);
            	}
            }
		};
		
		EventHandler<MouseEvent> numKeywordsHandler = new EventHandler<MouseEvent>()
		{
            public void handle(MouseEvent event) 
            {
            	// Round value.
            	int roundedNumberOfKeywords = (int) Math.round(slider_localScope_numKeywordsToUse.getValue());
            	// Update number in textfield.
            	textfield_localScope_numKeywordsToUse.setText(String.valueOf(roundedNumberOfKeywords));
            	
            	// Update visualization.
            	if (event.getEventType() == MouseEvent.MOUSE_RELEASED && selectedLDAConfigurations != null) {
            		ptcController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int) Math.round(slider_localScope_numTopicsToUse.getValue()), (int)slider_localScope_numKeywordsToUse.getMax(), roundedNumberOfKeywords, false);
            		cdController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int) Math.round(slider_localScope_numTopicsToUse.getValue()), (int)slider_localScope_numKeywordsToUse.getMax(), roundedNumberOfKeywords, false);
            	}
            }
		};		
		
		/*
		 * Add event handler to slider.
		 */
		
		// Slider for number of topics:
		// 	Add listener to determine position during after release.
		slider_localScope_numTopicsToUse.addEventHandler(MouseEvent.MOUSE_RELEASED, numTopicsHandler);
		// 	Add listener to determine position during mouse drag.
		slider_localScope_numTopicsToUse.addEventHandler(MouseEvent.MOUSE_DRAGGED, numTopicsHandler);
		
		// Slider for number of words:
		// 	Add listener to determine position during after release.
		slider_localScope_numKeywordsToUse.addEventHandler(MouseEvent.MOUSE_RELEASED, numKeywordsHandler);
		// 	Add listener to determine position during mouse drag.
		slider_localScope_numKeywordsToUse.addEventHandler(MouseEvent.MOUSE_DRAGGED, numKeywordsHandler);
	}

	/**
	 * Load/create visualizations from .fxml files.
	 */
	public void load()
	{
		// Load parallel tag clouds.
		load(LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS);
		// Load chord diagram.
		load(LocalScopeVisualizationType.CHORD_DIAGRAM);
	}
	
	/**
	 * Load specific visualization from .fxml file.
	 * @param type
	 */
	private void load(LocalScopeVisualizationType type)
	{
		FXMLLoader fxmlLoader							= new FXMLLoader();
        Node contentNode								= null;
        String fxmlPath									= null;
        AnchorPane anchorPane							= null;
        LocalScopeVisualizationController controller 	= null;
        
        // Set corresponding values.
        if (type == LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS) {
        	fxmlPath	= ptcPath;
        	anchorPane	= ptc_anchorpane;
        }
        else if (type == LocalScopeVisualizationType.CHORD_DIAGRAM) {
        	fxmlPath	= cdPath;
        	anchorPane	= cd_anchorpane;
        }
        
        // Load .fxml files.
        try {
	      	// Here: Support for several local scope visualizations, if reasonable.
			contentNode	= (Node) fxmlLoader.load(getClass().getResource(fxmlPath).openStream());
			
			// Init controller.
			controller	= (LocalScopeVisualizationController)fxmlLoader.getController();
			controller.setWorkspace(workspace);
			controller.setAnchorPane(anchorPane);
			
			// Add to parent pane.
			anchorPane.getChildren().add(contentNode);
			
			// Ensure resizability of content.
			AnchorPane.setTopAnchor(contentNode, 0.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
    		
    		// Set reference to component.
    		controller.setLocalScopeInstance(this);
        }
        
        catch (Exception e) {
        	e.printStackTrace();
        }
        
        // Update controller references.
        if (type == LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS) {
        	ptcController = controller;
        }
        else if (type == LocalScopeVisualizationType.CHORD_DIAGRAM) {
        	cdController = controller;
        }
	}
	
	/**
	 * Refreshes this local scope instance.
	 * @param selectedLDAConfigurations
	 */
	public void refresh(ArrayList<LDAConfiguration> selectedLDAConfigurations)
	{
		// Update reference.
		this.selectedLDAConfigurations = selectedLDAConfigurations;
		
		// Refresh parallel tag clouds.
		//ptcController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int)slider_localScope_numTopicsToUse.getValue(), (int)slider_localScope_numKeywordsToUse.getMax(), (int)slider_localScope_numKeywordsToUse.getValue(), true);
		
		// Refresh chord diagram.
		cdController.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int)slider_localScope_numTopicsToUse.getValue(), (int)slider_localScope_numKeywordsToUse.getMax(), (int)slider_localScope_numKeywordsToUse.getValue(), true);
	}

	/**
	 * Resizes visualizations according to given width and height.
	 */
	public void resize(double width, double height, LocalScopeVisualizationType type)
	{
		if (type == LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS && ptcController != null)
			;//ptcController.resize(width, height);
		
		else if (type == LocalScopeVisualizationType.CHORD_DIAGRAM && cdController != null)
			cdController.resize(width, height);;
	}

	@Override
	public void changeViewMode()
	{
		
	}
	
	/**
	 * Sets reference to instance of Workspace.
	 * @param workspace
	 */
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
		
		// Pass reference on to controllers.
		ptcController.setWorkspace(workspace);
		cdController.setWorkspace(workspace);
	}
	
	/**
	 * Updates possible maximum of number of topics to select.
	 * Called from instance of LocalScopeVisualizationController, after
	 * a new dataset was selected/loaded.
	 * @param maxNumberOfTopics
	 */
	public void setNumberOfTopicsMaximum(int maxNumberOfTopics)
	{
		slider_localScope_numTopicsToUse.setMax(maxNumberOfTopics);
		slider_localScope_numTopicsToUse.setMajorTickUnit(maxNumberOfTopics / 5 >= 1 ? maxNumberOfTopics / 5 : 1);
		slider_localScope_numTopicsToUse.setMinorTickCount(4);
	}
}
