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

/**
 * Wrapper for local scope instance.
 * May be one of several visualizations (currently supported: Parallel tag clouds).
 * @author RM
 *
 */
public class LocalScopeInstance extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private AnchorPane anchorpane_localScope;
	private Label label_visType;

	// Options.
	private Slider slider_localScope_numTopicsToUse;
	private TextField textfield_localScope_numTopicsToUse;
	private Slider slider_localScope_numKeywordsToUse;
	private TextField textfield_localScope_numKeywordsToUse;
	
	/*
	 * Other data.
	 */
	
	/**
	 * Reference to instance of local scope controller actually manipulating the local scope visualization.
	 */
	private LocalScopeVisualizationController controller;

	private Workspace workspace;
	
	/**
	 * Collection storing all LDA configurations relevant for this local scope. 
	 */
	private ArrayList<LDAConfiguration> selectedLDAConfigurations;
	
	// -----------------------------------------------
	// 		Methods.
	// -----------------------------------------------

	
	public LocalScopeInstance(	AnalysisController analysisController, AnchorPane anchorpane_localScope, 
								Label label_visType, Slider slider_localScope_numTopicsToUse, TextField textfield_localScope_numTopicsToUse,
								Slider slider_localScope_numKeywordsToUse, TextField textfield_localScope_numKeywordsToUse)
	{
		super(analysisController);
		
		this.anchorpane_localScope					= anchorpane_localScope;
		this.label_visType							= label_visType;
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
            	if (event.getEventType() == MouseEvent.MOUSE_RELEASED && selectedLDAConfigurations != null)
            		controller.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), roundedNumberOfTopics, (int)slider_localScope_numKeywordsToUse.getMax(), (int) Math.round(slider_localScope_numKeywordsToUse.getValue()), false);
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
            	if (event.getEventType() == MouseEvent.MOUSE_RELEASED && selectedLDAConfigurations != null)
            		controller.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int) Math.round(slider_localScope_numTopicsToUse.getValue()), (int)slider_localScope_numKeywordsToUse.getMax(), roundedNumberOfKeywords, false);
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

	public void load(String fxmlPath)
	{
		FXMLLoader fxmlLoader	= new FXMLLoader();
        Node contentNode		= null;
        
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
			AnchorPane.setTopAnchor(contentNode, 0.0);
    		AnchorPane.setBottomAnchor(contentNode, 0.0);
    		AnchorPane.setLeftAnchor(contentNode, 0.0);
    		AnchorPane.setRightAnchor(contentNode, 0.0);
    		
    		// Set visualization type labe.
    		controller.updateLabelWithVisualizationType(label_visType);
    		
    		// Set reference to component.
    		controller.setLocalScopeInstance(this);
        }
        
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void refresh(ArrayList<LDAConfiguration> selectedLDAConfigurations)
	{
		// Update reference.
		this.selectedLDAConfigurations = selectedLDAConfigurations;
		
		// Refresh visualization.
		controller.refresh(selectedLDAConfigurations, (int)slider_localScope_numTopicsToUse.getMax(), (int)slider_localScope_numTopicsToUse.getValue(), (int)slider_localScope_numKeywordsToUse.getMax(), (int)slider_localScope_numKeywordsToUse.getValue(), true);
	}

	public void resize(double width, double height)
	{
		controller.resize(width, height);
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
		
		// Pass reference on to controller.
		controller.setWorkspace(workspace);
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
