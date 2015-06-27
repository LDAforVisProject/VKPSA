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
	
	/*
	 * Other data.
	 */
	
	private LocalScopeVisualizationController controller;

	private Workspace workspace;
	
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
		
		initControlListener();
	}
	
	private void initControlListener()
	{
		// Add listener to determine position during after release.
		slider_localScope_numTopicsToUse.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	textfield_localScope_numTopicsToUse.setText(String.valueOf(slider_localScope_numTopicsToUse.getValue()));
            	System.out.println(slider_localScope_numTopicsToUse.getValue());

            	// Get data
            	
            	// Filter by values; refresh visualizations.
//            	controller.
            }
        });
		
		// Add listener to determine position during mouse drag.
		slider_localScope_numTopicsToUse.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	textfield_localScope_numTopicsToUse.setText(String.valueOf(slider_localScope_numTopicsToUse.getValue()));
            };
        });
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
			AnchorPane.setTopAnchor(contentNode, 35.0);
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
	
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations)
	{
		System.out.println("\n### REFRESHING LOCAL SCOPE - " + selectedFilteredLDAConfigurations.size());
		
		// Refresh visualization.
		controller.refresh(selectedFilteredLDAConfigurations);
	}

	public void resize(double width, double height)
	{
		controller.resize(width, height);
	}

	@Override
	public void changeViewMode()
	{
		
	}
	
	public void setWorkspace(Workspace workspace)
	{
		this.workspace = workspace;
		
		// Pass reference on to controller.
		controller.setWorkspace(workspace);
	}
}
