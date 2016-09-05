package view.components.documentDetail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.documents.Document;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
	private @FXML Label searchedForKeyword_label;
	private @FXML Label authors_label;
	private @FXML Label date_label;
	private @FXML Label conference_label;
	private @FXML ImageView resize_imageview;
	
	// Metadata (information on doc. source and topic probability distribution) elements.
	private @FXML GridPane metadata_gridpane;
	private @FXML Label topicProbabilities_label;
	private @FXML BarChart<String, Number> topicProbabilities_barchart;
	private @FXML NumberAxis topicProbabilities_barchart_yAxis_numberaxis;
	private @FXML Label examinedTopicID_label;
	
	/*
	 * TextFlows and their containers.
	 */
	
	// AnchorPane containing TextFlow for title.
	private @FXML AnchorPane title_anchorpane;
	// Original abstract: TextFlow for title.
	private TextFlow title_textflow;
	
	// AnchorPane containing TextFlow for keywords.
	private @FXML AnchorPane keywords_anchorpane;
	// Original abstract: TextFlows for keywords.
	private TextFlow keywords_textflow;
	
	// Original abstract: ScrollPane containing AnchorPane containing TextFlow.
	private @FXML ScrollPane originalAbstract_scrollpane;
	// Original abstract: AnchorPane containing TextFlow.
	private @FXML AnchorPane originalAbstract_anchorpane;
	// Original abstract: TextFlows for abstracts.
	private TextFlow originalAbstract_textflow;
	
	// Processed abstract: ScrollPane containing TextFlow.
	private @FXML ScrollPane processedAbstract_scrollpane;
	// Processed AnchorPane containing TextFlow.
	private @FXML AnchorPane processedAbstract_anchorpane;
	// Processed TextFlows for abstracts.
	private TextFlow processedAbstract_textflow;
	
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
		
		// Init text fields.
		initTextfields();
	}
	
	/**
	 * Prepare text fields.
	 */
	private void initTextfields()
	{
		/*
		 * Create instances of TextFlow.
		 */
		
		// For title.
		title_textflow				= new TextFlow();
		title_anchorpane.getChildren().add(title_textflow);
		
		// For keywords.
		keywords_textflow			= new TextFlow();
		keywords_anchorpane.getChildren().add(keywords_textflow);
		
		keywords_anchorpane.setTopAnchor(keywords_textflow, 0.0);
		keywords_anchorpane.setBottomAnchor(keywords_textflow, 0.0);
		keywords_anchorpane.setLeftAnchor(keywords_textflow, 0.0);
		keywords_anchorpane.setRightAnchor(keywords_textflow, 0.0);
		
		// For original abstract.
		originalAbstract_textflow 	= new TextFlow();
		originalAbstract_anchorpane.getChildren().add(originalAbstract_textflow);
		// Important: Caching is needed, since rendering TextFlow in ScrollPane doesn't work otherwise.
		originalAbstract_textflow.setCache(true);
		
		// For processed abstract.
		processedAbstract_textflow = new TextFlow();
		processedAbstract_anchorpane.getChildren().add(processedAbstract_textflow);
		// Important: Caching is needed, since rendering TextFlow in ScrollPane doesn't work otherwise.
		processedAbstract_textflow.setCache(true);
		
		// Resize components.
		resizeContent(600, 550);
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
	 * @param keyword Currently selected keyword.
	 */
	public void refresh(final Document document, final ArrayList<Pair<Pair<Integer, Integer>, Float>> topicProbabilitiesInDoc,
						final Pair<Integer, Integer> topicID, final String keyword)
	{
		// Store displayed document.
		this.document = document;
		
		/*
		 * Update UI elements.
		 */
		
		// Update title TextFlow..
		updateTextFlow(title_textflow, document.getTitle(), keyword, FontWeight.BOLD);
		searchedForKeyword_label.setText(keyword);
		authors_label.setText(document.getAuthors());
		date_label.setText(document.getDate());
		conference_label.setText(document.getConference());
		updateTextFlow(keywords_textflow, document.getKeywords(), keyword, FontWeight.NORMAL);
		
		// Update text fields for abstracts. Highlight searched for keywords.
		updateAbstracts(this.document, keyword);
		
		// Update bar chart.
		refreshBarchart(topicProbabilitiesInDoc, topicID);
	}
	
	private void updateAbstracts(final Document document, final String term)
	{
		// Update field for original abstract.
		updateTextFlow(originalAbstract_textflow, document.getOriginalAbstract(), term, FontWeight.NORMAL);
		
		// Update field for processed abstract.
		updateTextFlow(processedAbstract_textflow, document.getProcessedAbstract(), term, FontWeight.NORMAL);
	}

	/**
	 * Highlights keyword's every occurence in specified TextArea.
	 * See http://stackoverflow.com/questions/9128535/highlighting-strings-in-javafx-textarea 
	 * @param textFlow
	 * @param keyword
	 * @param term
	 * @param defaultFontWeight FontWeight value to use for non-highlighted text.
	 */
	private void updateTextFlow(TextFlow textFlow, final String content, final String term, final FontWeight defaultFontWeight)
	{
		// Clear text field.
		textFlow.getChildren().clear();
		
		// Arrange text flow.
		if (textFlow != null && content != null) {
			String tmpContent 	= "";
			// Get all words.
			String[] words		= content.split(" |;");
			
			// Look in all words for term.
			for (int i = 0; i < words.length; i++) {
				// Get current word.
				String word = words[i];
				
				// If word doesn't contain term: Append to tmpContent.
				if (term == null || !word.toLowerCase().contains(term.toLowerCase())) {
					tmpContent += word;
					// Append space if this isn't the last word.
					if(i < words.length - 1)
						tmpContent += " ";
					// If this is the last word: Append to TextFlow.
					else {
						Text textWithoutTerm	= new Text(tmpContent);
						textWithoutTerm.setFill(new Color(0, 0, 0, 0.75));
						textWithoutTerm.setFont(Font.font(textWithoutTerm.getFont().getName(), defaultFontWeight, textWithoutTerm.getFont().getSize()));
						
						// Add node to text field.
						textFlow.getChildren().add(textWithoutTerm);
					}
				}
				
				// If word contains term: Push new Text instances into TextFlow.
				else {
					// Text instance for part not containing term.
					Text textWithoutTerm	= new Text(tmpContent);
					textWithoutTerm.setFill(new Color(0, 0, 0, 0.75));
					textWithoutTerm.setFont(Font.font(textWithoutTerm.getFont().getName(), defaultFontWeight, textWithoutTerm.getFont().getSize()));
					
					// Text instance for key term.
					Text textWithTerm		= new Text(word + " ");
					textWithTerm.setFill(Color.RED);
					textWithTerm.setFont(Font.font(textWithTerm.getFont().getName(), FontWeight.BOLD, textWithTerm.getFont().getSize() - 1));
					
					// Add nodes to text field.
					textFlow.getChildren().add(textWithoutTerm);
					textFlow.getChildren().add(textWithTerm);
					
					// Reset tmpContent.
					tmpContent = "";
				}
			}
		}
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
		examinedTopicID_label.setText(topicID.getKey() + "#" + topicID.getValue());
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
		final double adjustedTextfieldWidth = (width - 35 * 2) / 2 - 5;
		final double adjustedGridpaneWidth	= (width - 35 - 35 - 10) * 0.5;
		final double adjustedBarchartWidth	= (width - 35 - 35 - 10) * 0.5;
		
		// Resize keywords text field.
		keywords_anchorpane.setPrefWidth(adjustedBarchartWidth * 0.5);
		keywords_textflow.setPrefWidth(adjustedBarchartWidth * 0.5);
//		keywords_textflow.setMinHeight(30);
		keywords_textflow.setMaxHeight(30);
//		keywords_textflow.setPrefHeight(30);
//		keywords_anchorpane.setMinHeight(30);
		keywords_anchorpane.setMaxHeight(30);
//		keywords_anchorpane.setPrefHeight(30);
		
		// Resize processed abstract text field.
		processedAbstract_scrollpane.setPrefWidth(adjustedTextfieldWidth);
		processedAbstract_anchorpane.setPrefWidth(adjustedTextfieldWidth - 25);
		processedAbstract_textflow.setPrefWidth(adjustedTextfieldWidth - 25);
		
		// Resize original abstract text field.
		originalAbstract_scrollpane.setPrefWidth(adjustedTextfieldWidth);
		originalAbstract_anchorpane.setPrefWidth(adjustedTextfieldWidth - 25);
		originalAbstract_textflow.setPrefWidth(adjustedTextfieldWidth - 25);
		
		// Resize grid.
		metadata_gridpane.setPrefWidth(adjustedGridpaneWidth);
		// Resize bar chart.
		topicProbabilities_barchart.setPrefWidth(adjustedBarchartWidth);
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
}
