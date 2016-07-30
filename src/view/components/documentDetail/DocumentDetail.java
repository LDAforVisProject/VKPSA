package view.components.documentDetail;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.Document;
import model.workspace.Workspace;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

/**
 * Component for display of a document's detailed information.
 * @author RM
 *
 */
public class DocumentDetail extends VisualizationComponent
{
	/*
	 * UI elements.
	 */
	
	private @FXML AnchorPane root_anchorpane;
	private @FXML Label title_label;
	private @FXML Label authors_label;
	private @FXML Label date_label;
	private @FXML Label conference_label;
	private @FXML Label keywords_label;
	private @FXML TextArea originalAbstract_textfield;
	private @FXML TextArea processedAbstract_textfield;
	private @FXML ImageView resize_imageview;
	
	/*
	 * Information on pane resizing.
	 */
	
	/**
	 * Size of root pane before modification by drag.
	 */
	private Pair<Double, Double> paneSizeBeforeModification;
	/**
	 * Position of first point used for current resize drag.
	 */
	private Pair<Double, Double> firstResizeDragPosition;
	/**
	 * Position of previous point used for resize drag.
	 */
	private Pair<Double, Double> prevResizeDragPosition;
	
	/**
	 * Currently displayed document.
	 */
	private Document document;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing DocumentDetail.");		
		
		// Initialize resize functionality.
		initResizeButton();
	}
	
	public void initResizeButton()
	{
		/*
		 * 1. Initialize variables.
		 */
		
		// Load resize icon.
		resize_imageview.setImage(new Image(getClass().getResourceAsStream("/icons/resize.png"), 40, 40, true, true));
		
		// Set original pane size.
		paneSizeBeforeModification	= null; 
		// Init first drag position.
		firstResizeDragPosition 	= null;		
		// Init last drag position.
		prevResizeDragPosition 		= null;
		
		/*
		 * 2. Add event listener.
		 */
		
		// Add listener for mouse over.
		resize_imageview.addEventHandler(MouseEvent.MOUSE_ENTERED, (new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) 
            {
            	// Modify cursors.
            	analysisController.getScene().setCursor(Cursor.SE_RESIZE);
            }
		}));
		
		// Add listener for mouse drag.
		resize_imageview.addEventHandler(MouseEvent.MOUSE_DRAGGED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	// Stop event's propagation.
            	me.consume();
            	
            	// Modify cursors.
            	analysisController.getScene().setCursor(Cursor.SE_RESIZE);
            	
            	// Start of drag:
            	if (firstResizeDragPosition == null) {
            		// Store current mose position.
            		firstResizeDragPosition 	= new Pair<Double, Double>(me.getX(), me.getY());
            		prevResizeDragPosition		= new Pair<Double, Double>(me.getX(), me.getY());
            		// Store current pane size.
            		paneSizeBeforeModification	= new Pair<Double, Double>(root_anchorpane.getWidth(), root_anchorpane.getHeight());
            	}
            	
            	// During drag:
            	else {
            	}
            	
            	// Update last position used for resize drag.
            	prevResizeDragPosition = new Pair<Double, Double>(me.getX(), me.getY());
            }
		}));
		
		// Add listener for mouse release.
		resize_imageview.addEventHandler(MouseEvent.MOUSE_RELEASED, (new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) 
            {
            	/*
            	 * Adapt pane size.
            	 * 
            	 */
            	
            	// Calculate delta.
          		final double absoluteDeltaX	= me.getX() - firstResizeDragPosition.getKey();
          		final double absoluteDeltaY	= me.getY() - firstResizeDragPosition.getValue();
          		// Change width.
            	root_anchorpane.setMinWidth(paneSizeBeforeModification.getKey() + absoluteDeltaX);
        		root_anchorpane.setPrefWidth(paneSizeBeforeModification.getKey() + absoluteDeltaX);
        		root_anchorpane.setMaxWidth(paneSizeBeforeModification.getKey() + absoluteDeltaX);
        		// Change height.
        		root_anchorpane.setMinHeight(paneSizeBeforeModification.getValue() + absoluteDeltaY);
        		root_anchorpane.setPrefHeight(paneSizeBeforeModification.getValue() + absoluteDeltaY);
        		root_anchorpane.setMaxHeight(paneSizeBeforeModification.getValue() + absoluteDeltaY);
        		
        		// Force redraw.
        		root_anchorpane.applyCss();
        		root_anchorpane.layout();
        	
        		/*
        		 * Reset variables.
        		 */
        		
            	// Modify cursors.
            	analysisController.getScene().setCursor(Cursor.DEFAULT);
            
            	// Reset first used position.
            	firstResizeDragPosition	= null;            	
            	// Reset previously used position.
            	prevResizeDragPosition	= null;
            }
		}));
	}
	
	/**
	 * Updates UI.
	 * @param document
	 */
	public void update(final Document document)
	{
		// Store displayed document.
		this.document = document;
		
		// Update UI elements.
		title_label.setText(document.getTitle());
		authors_label.setText(document.getAuthors());
		date_label.setText(document.getDate());
		conference_label.setText(document.getConference());
		keywords_label.setText(document.getKeywords());
		originalAbstract_textfield.setText(document.getOriginalAbstract());
		processedAbstract_textfield.setText(document.getProcessedAbstract());
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
