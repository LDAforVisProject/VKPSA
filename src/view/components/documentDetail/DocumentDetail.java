package view.components.documentDetail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import database.DBManagement;
import model.documents.Document;
import model.documents.DocumentForLookupTable;
import model.workspace.Workspace;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
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
	
	// Metadata (information on doc. source and topic probability distribution) elements.
	private @FXML GridPane metadata_gridpane;
	private @FXML Label topicProbabilities_label;
	private @FXML BarChart<String, Number> topicProbabilities_barchart;
	private @FXML NumberAxis topicProbabilities_barchart_yAxis_numberaxis;
	private @FXML Label highlightInfo_label;
	
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
	 * Currently displayed document.
	 */
	private Document document;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing DocumentDetail.");		
		
		// Initialize resize functionality.
		initResizeButton();
		
		// Init topic probability bar chart.
		initTopicProbabilityBarchart();
	}
	
	/**
	 * Initiailze probabiilty bar chart.
	 */
	private void initTopicProbabilityBarchart()
	{
		// Hide x-axis.
		topicProbabilities_barchart.getXAxis().setTickLabelsVisible(false);
		topicProbabilities_barchart.getXAxis().setOpacity(0);
		
		// Set boundaries for y-axis.
		topicProbabilities_barchart_yAxis_numberaxis.setAutoRanging(false);
		topicProbabilities_barchart_yAxis_numberaxis.setLowerBound(0);
		topicProbabilities_barchart_yAxis_numberaxis.setUpperBound(1);
		topicProbabilities_barchart_yAxis_numberaxis.setTickUnit(0.25);
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
            		// Store current pane size.
            		paneSizeBeforeModification	= new Pair<Double, Double>(root_anchorpane.getWidth(), root_anchorpane.getHeight());
            	}
            	
            	// During drag:
            	else {
            	}
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
            	
            	/*
            	 * Resize content.
            	 */
            	
            	resizeContent(paneSizeBeforeModification.getKey() + absoluteDeltaX, paneSizeBeforeModification.getValue() + absoluteDeltaY);
            }
		}));
	}
	
	/**
	 * Refreshes component.
	 * @param document Document to display.
	 * @param topicProbabilitiesInDoc List of topics for this document, listed descendingly.
	 * @param topicID Currently examined topic's comprehensive ID.
	 */
	public void refresh(final Document document, final ArrayList<Pair<Pair<Integer, Integer>, Float>> topicProbabilitiesInDoc,
						final Pair<Integer, Integer> topicID)
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
		
		// Update bar chart.
		refreshBarchart(topicProbabilitiesInDoc, topicID);
	}
	
	/**
	 * Refreshes bar chart.
	 * @param topicProbabilitiesInDoc
	 * @param topicID Currently examined topic's identification.
	 */
	private void refreshBarchart(ArrayList<Pair<Pair<Integer, Integer>, Float>> topicProbabilitiesInDoc, Pair<Integer, Integer> topicID)
	{
		// Clear bar chart.
		topicProbabilities_barchart.getData().clear();
		
		// Allocate new data series.
		XYChart.Series<String, Number> dataSeries = new XYChart.Series<String, Number>();
		
		// Get probability for all topics; add to data series.
		int count = 0;
		for (Pair<Pair<Integer, Integer>, Float> topicProbabilityEntry : topicProbabilitiesInDoc) {
			// Create new entry.
			Data<String, Number> entry = new XYChart.Data<String, Number>(String.valueOf(count++), topicProbabilityEntry.getValue());
			entry.setExtraValue(topicProbabilityEntry.getKey());
			// Add entry to data series.
			dataSeries.getData().add(entry);
		}
		
		// Add data series to chart.
		topicProbabilities_barchart.getData().add(dataSeries);
	
		// Adjust opacity.
		for (XYChart.Data<String, Number> item : topicProbabilities_barchart.getData().get(0).getData()) {
			if ( !((Pair<Integer, Integer>)item.getExtraValue()).equals(topicID) ) {
				item.getNode().setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
			}
			
			else {
				item.getNode().setOpacity(1);
				
				final double adjustedBarChartWidth = topicProbabilities_barchart.getWidth() > 0 ? topicProbabilities_barchart.getWidth() : 260; 
				final double adjustedScaleFactor = 10 * 2.0 /  ( (adjustedBarChartWidth - 30) / topicProbabilities_barchart.getData().get(0).getData().size() );
				item.getNode().setScaleX(adjustedScaleFactor >= 1 ? adjustedScaleFactor : 1);
			}
		}
		
		// Update info label.
		highlightInfo_label.setText("Highlighted: " + topicID.getKey() + "#" + topicID.getValue());
	}
	
	/**
	 * Refreshes bar chart after resize.
	 * @param topicID Currently examined topic's identification.
	 */
	private void refreshBarchart(Pair<Integer, Integer> topicID)
	{
		
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
		// Resize original abstract text field.
		originalAbstract_textfield.setPrefWidth((width - 35 * 2) / 2 - 5);
		// Resize processed abstract text field.
		processedAbstract_textfield.setPrefWidth((width - 35 * 2) / 2 - 5);
		
		// Resize grid.
		metadata_gridpane.setPrefWidth((width - 35 - 35 - 10) * 0.5);
		// Resize bar chart.
		
		topicProbabilities_barchart.setPrefWidth((width - 35 - 35 - 10) * 0.5);
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
}
