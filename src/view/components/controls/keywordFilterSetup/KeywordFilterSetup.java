package view.components.controls.keywordFilterSetup;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.textfield.TextFields;

import control.analysisView.AnalysisController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

public class KeywordFilterSetup extends VisualizationComponent
{	
	/**
	 * Textfield used to accept input and suggest available entries.
	 */
	private @FXML TextField textfield;
	
	/**
	 * Initializes new instance of KeywordFilterSetup.
	 * @param analysisController
	 */
	public KeywordFilterSetup(AnalysisController analysisController)
	{
		this.analysisController = analysisController;
		
		// Initialize root node.
		initRootNode();
		
		// Initialize text field.
		initTextfield();
	}
	
	/**
	 * Initialize root node to be embedded in GUI.
	 */
	private void initRootNode()
	{
		// Create root node.
		this.rootNode = new AnchorPane();
	}
	
	/**
	 * Initializes auto-suggestion textfield used for selection of keyword to filter. 
	 */
	private void initTextfield()
	{
		// Get root node as parent.
		AnchorPane parent = (AnchorPane) rootNode;
		
		// Use label for hint about textfield.
		Label label = new Label("Keyword to filter by: ");
		
		// Create text field.
		textfield = new TextField();
		
		// Add auto-suggestion to textfield.
		TextFields.bindAutoCompletion(textfield, t-> {
			try {
				return workspace.getDatabaseManagement().getKeywordSuggestions(t.getUserText());
			}
	          
	        catch (Exception e) {
	        	e.printStackTrace();
			}
			
			return null;
	    });
		
		// Add listener.
		textfield.addEventHandler(KeyEvent.KEY_PRESSED, (new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode() == KeyCode.ENTER && textfield.getText() != null) {
					analysisController.createKeywordFilter(textfield.getText());
				}
			}
		}));
		
		/*
		 * Add elements to GUI.
		 */
		
		// Create box used for elements.
		VBox box = new VBox();
		box.setPadding(new Insets(10, 10, 10, 10));
		// Add label and text field.
		box.getChildren().add(label);
		box.getChildren().add(textfield);
		// Add to root node.
		parent.getChildren().add(box);
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{	
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{	
	}

	@Override
	public void refresh()
	{
	}

	@Override
	public void initHoverEventListeners()
	{
	}

	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
	}

	@Override
	public void removeHoverHighlighting()
	{
	}

	@Override
	public void resizeContent(double width, double height)
	{
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

}
