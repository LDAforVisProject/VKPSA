package view.components.parallelTagCloud;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.workspace.TaskType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.scatterchart.Scatterchart;

/**
 * Component used for detail view of topic data.
 * Shows n most relevant keyword for all specified topics. 
 * @author RM
 *
 */
public class ParallelTagCloud extends VisualizationComponent
{
	/*
	 * GUI elements.
	 * 
	 */
	
	/**
	 * Containing SplitPane.
	 */
	private @FXML SplitPane splitpane;
	
	/**
	 * ScrollPane containing AnchorPane containing actual tag cloud.
	 */
	private @FXML ScrollPane tagcloud_scrollpane;
	
	/**
	 * AnchorPane containing actual tag cloud.
	 */
	private @FXML AnchorPane tagcloud_anchorpane;
	
	/**
	 * AnchorPane containing probability distribution barchart..
	 */
	private @FXML AnchorPane barchart_anchorpane; 
	
	/**
	 * Canvas used for drawing.
	 */
    private @FXML Canvas canvas; 
    
	/**
	 * Barchart displaying distribution of probability over words in topic.
	 */
	private BarChart<Number, String> probabilityDistribution_barchart;
	/**
	 * X-axis for prob. dist. barchart. 
	 */
	private NumberAxis probDistBarchart_xAxis;
	/**
	 * Y-Axis for prob. dist. barchart..
	 */
    private CategoryAxis probDistBarchart_yAxis;
    
    /*
     * Collections for PTC vis. elements.
     */
    
	/**
	 * List of labels for topics.
	 */
	private ArrayList<Label> topicIDLabels;
	
	/**
	 * Container holding VBoxes of labels.
	 */
	private ArrayList<VBox> tagCloudContainer; 
	
    /*
     * Other data.
     */
	
	/**
	 * Keyword requested for semi-persistent/non-transient external action (e.g. selection for context search component). 
	 */
	private String requestedKeyword;
	/**
	 * Store reference to specific instance of requested label.
	 */
	private Label requestedKeywordLabel;
	/**
	 * Reference to labels containing the currently selected keyword.
	 */
	private List<Label> labelsWithRequestedKeywords;
	
	/**
	 * Keyword that is currently selected, e.g. by hovering over it.
	 */
	private String selectedKeyword;
	/**
	 * Name of topic the selected keyword belongs to.
	 */
	private String selectedTopicName;
	/**
	 * Reference to labels containing the currently selected keyword.
	 */
	private List<Label> labelsWithSelectedKeyword;
	
	/**
	 * Zoom factor currently used for PTC panel.
	 */
	private double zoomFactor;
	/**
	 * Delta for each mouse wheel action.
	 */
	private static final double ZOOM_DELTA = 0.1F;
	
	/**
	 * Stores probability sums for a keyword over all topics.
	 */
	private Map<String, Double> keywordProbabilitySumsOverTopics;
	/**
	 * Maximum of all keyword probability sum over all topics.
	 */
	private double keywordProbabilitySumOverTopicsMax;
	/**
	 * Minimum of all keyword probability sum over all topics.
	 */
	private double keywordProbabilitySumOverTopicsMin;
	
	/**
	 * Auxiliary variable serving as storage for collection of data point IDs to highlight.
	 */
	private Set<Integer> dataPointConfigIDsToHighlight;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing ParallelTagCloud component.");
		
		// Initialize prob. dist. barchart.
		initProbabilityDistributionBarchart();
		
		// Initialize PTC visualization.
		initPTC();
		
		// Add resize listeners.
		addResizeListeners();
	}
	
	/**
	 * Initialize values and collections needed for PTC visualization.
	 */
	private void initPTC()
	{
		/*
		 *  Allocate collections.
		 */
		
		topicIDLabels 						= new ArrayList<Label>();
		tagCloudContainer 					= new ArrayList<VBox>();
		labelsWithSelectedKeyword			= new ArrayList<Label>();
		labelsWithRequestedKeywords			= new ArrayList<Label>();
		
		keywordProbabilitySumsOverTopics 	= new LinkedHashMap<String, Double>(); 
		
		/*
		 *  Set inital scalar values.
		 */
		
		keywordProbabilitySumOverTopicsMax	= 0;
		keywordProbabilitySumOverTopicsMin	= Double.MAX_VALUE;
		selectedKeyword						= "";
		selectedTopicName					= "";
		requestedKeyword					= "";
		zoomFactor							= 1;
		
	}
	
	private void addResizeListeners()
	{
		// Define list of anchor panes / other elements to add resize listeners to.
		ArrayList<Pane> anchorPanesToResize = new ArrayList<Pane>();

		anchorPanesToResize.add(tagcloud_anchorpane);
		anchorPanesToResize.add(barchart_anchorpane);
		
		// Add width resize listener to all panes.
		for (Pane ap : anchorPanesToResize) {
			// Add listener to width property.
			ap.widthProperty().addListener(new ChangeListener<Number>() {
			    @Override 
			    public void changed(ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth)
			    {
			        resizeElement(ap, newWidth.doubleValue(), 0);
			    }
			});
		}
		
		// Add zoom listener on mouse wheel action.
		tagcloud_anchorpane.setOnScroll(new EventHandler<ScrollEvent>() {
		    public void handle(ScrollEvent event) {
		        event.consume();
		        
		        if (event.getDeltaY() == 0) {
		            return;
		        }

		        // Calculate scale factor.
		        final double deltaZoomFactor 	= event.getDeltaY() > 0 ? ParallelTagCloud.ZOOM_DELTA : - ParallelTagCloud.ZOOM_DELTA;
		        
		        if (event.getDeltaY() > 0)
		        	zoomFactor					= zoomFactor + deltaZoomFactor <= 1 ? zoomFactor + deltaZoomFactor : 1;	
		        
		        else
		        	zoomFactor					= zoomFactor + deltaZoomFactor > 0 ? zoomFactor + deltaZoomFactor : 0;
		        	
		        // Refresh PTC.
		        refreshParallelTagCloud((ParallelTagCloudDataset)data, (ParallelTagCloudOptionset)options, false);
		    }
		});
	}
	
	private void initProbabilityDistributionBarchart()
	{
        probDistBarchart_xAxis 				= new NumberAxis();
        probDistBarchart_yAxis 				= new CategoryAxis();
        probabilityDistribution_barchart 	= new BarChart<Number, String>(probDistBarchart_xAxis, probDistBarchart_yAxis);
        
        probabilityDistribution_barchart.setTitle("Probability Distribution over Keywords");
        probabilityDistribution_barchart.setAnimated(false);
        probDistBarchart_xAxis.setLabel("Probability");  
        probDistBarchart_xAxis.setTickLabelRotation(90);
        probDistBarchart_yAxis.setLabel("n-th most relevant keyword for each topic");        
 
        // Add to pane.
        barchart_anchorpane.getChildren().add(probabilityDistribution_barchart);
        
		// Ensure resizability of barchart.
		AnchorPane.setTopAnchor(barchart_anchorpane, 0.0);
		AnchorPane.setBottomAnchor(barchart_anchorpane, 0.0);
		AnchorPane.setLeftAnchor(barchart_anchorpane, 0.0);
		AnchorPane.setRightAnchor(barchart_anchorpane, 0.0);
	}
	
	@Override
	public void notifyOfTaskCompleted(final TaskType taskType)
	{
		if (taskType == TaskType.LOAD_SPECIFIC_RAW_DATA) {
			log("Loaded keyword/probability data.");
			
			// Update dataset.
			((ParallelTagCloudDataset)this.data).updateKeywordProbabilityData();
			
			// Refresh.
			this.refresh();
		}
	}
	
	/**
	 * Refresh visualization using existing data and a new option set.
	 * @param options
	 */
	public void refresh(ParallelTagCloudOptionset options)
	{
		this.options = options;
		
		// Refresh visualizations.
		refresh();
	}
	
	/**
	 * Refresh visualization, given a generic, preprocessed dataset and a set of options.
	 * @param options
	 * @param data
	 */
	public void refresh(ParallelTagCloudOptionset options, ParallelTagCloudDataset data)
	{
		this.options 		= options;
		this.data			= data;
    	
		// Fetch keyword/probability data from database.
		((ParallelTagCloudDataset)this.data).fetchTopicProbabilityData(this, workspace);
	}
	
	/**
	 * Refreshes heatmap 
	 */
	@Override
	public void refresh()
	{
		ParallelTagCloudDataset ptcData 		= (ParallelTagCloudDataset)this.data;
		ParallelTagCloudOptionset ptcOptions	= (ParallelTagCloudOptionset)this.options;
		
		if (this.options != null && this.data != null) {
			// Refresh actual parallel tag cloud.
			refreshParallelTagCloud(ptcData, ptcOptions, true);
		}
		
		// Add event listener for hover & selection.
		initHoverEventListeners();
	}
	
	/**
	 * Refreshes parallel tag cloud visualization (as opposed to parallel tag cloud component, which
	 * includes the probability distribution barchart).
	 * @param data
	 * @param options
	 * @param hasDataChanged Indicates whether new  data has loaded (or if only bridges are redrawn).
	 */
	private void refreshParallelTagCloud(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options, final boolean hasDataChanged)
	{
	    // Probability sums for each topic.
		final int numberOfTopics			= data.getTopicConfigurations().size();
		ArrayList<Double> probabilitySums 	= new ArrayList<Double>(numberOfTopics);
	 
		// Reset visualization.
		clear();
		
		// Show barchart.
		probabilityDistribution_barchart.setVisible(true);
		
	    /*
	     *	0. Create labels. 
	     */

		if (hasDataChanged || topicIDLabels.size() == 0) {
	    	//System.out.println("	creating topic labels");
	    	createTopicIDLabels(data.getTopicConfigurations());
	    }
	    
	    /*
		 * 1. Create tag clouds, fill them with data.
		 */
	    
	    createTagClouds(data, options, probabilitySums);

		/*
		 * 2. Adjust tag font sizes. 
		 */
	    
	    adjustTagFontSizes(data, options, probabilitySums);
	    
	    /*
	     * 3. Correct positioning of tag clouds.
	     */
	    
	    adjustTagcloudPlacement();
	    
	    /*
	     * 4. Draw connections between tags with same content in different topics. .
	     */
	   
	    drawBridges(data, options);
	   
	    /*
	     * 5. Lower opacity.
	     */
	    
	    removeHoverHighlighting();
	    
	    /*
	     * 6. Move canvas to background / tag clouds to foreground.
	     */
	    
		// Move canvas to background.
		canvas.toBack();
		for (VBox vbox : tagCloudContainer) {
			vbox.toFront();
		}
	}
	
	/**
	 * Refreshes barchart. Adds all available data to it. 
	 * @param data
	 * @param options
	 */
	private void refreshBarchart(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options)
	{
		// Clear data.
		probabilityDistribution_barchart.getData().clear();
		
		// Add new data to chart.
		for (Pair<Integer, Integer> topicConfig : data.getKeywordProbabilities().keySet()) {
			// Allocate series object, set name.
			XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
			series.setName(String.valueOf(topicConfig.getKey()) + "#" + String.valueOf(topicConfig.getValue()));
			
			/*
			 *  Process all n requested keyword/probability pairs.
			 */
			
			// Add all keyword/probability pairs to data series.
			// Sort by priority for each topic (as opposed to alphabetically).
			// Important: One bar most probably represents different keywords for different topics.
			// Get data.
			ArrayList<Pair<String, Double>> keywordProbabilityData = data.getKeywordProbabilities().get(topicConfig);
			
			// Calculate start index.
			final int startIndex = options.getNumberOfKeywordsToDisplay() <= keywordProbabilityData.size() ? options.getNumberOfKeywordsToDisplay() - 1 : keywordProbabilityData.size() - 1; 
			
			// Iterate over keyword/probablity pairs.
			for (int i = startIndex; i >= 0; i--) {
				Pair<String, Double> keywordProbPair = keywordProbabilityData.get(i);				
				series.getData().add(new XYChart.Data<Number, String>(	keywordProbPair.getValue(), "#" + String.valueOf( i + 1 )) );
			}
			
			// Add data series to barchart.
			probabilityDistribution_barchart.getData().add(series);
		}
	}
	
	/**
	 * Refreshes barchart. Adds data from allowed topics to it.
	 * @param data
	 * @param options
	 * @param allowedTopicIDs
	 */
	private void refreshBarchart(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options, final Set<Pair<Integer, Integer>> allowedTopicIDs)
	{
		// Clear data.
		probabilityDistribution_barchart.getData().clear();
		
		// Add new data to chart.
		if (data != null && data.getKeywordProbabilities() != null && allowedTopicIDs != null && allowedTopicIDs.size() > 0) {
			for (Pair<Integer, Integer> topicConfig : data.getKeywordProbabilities().keySet()) {
				// Display only topic configurations existing in provided set.
				if (allowedTopicIDs.contains(topicConfig)) {
					// Allocate series object, set name.
					XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
					series.setName(String.valueOf(topicConfig.getKey()) + "#" + String.valueOf(topicConfig.getValue()));
					
					/*
					 *  Process all n requested keyword/probability pairs.
					 */
					
					// Add all keyword/probability pairs to data series.
					// Sort by priority for each topic (as opposed to alphabetically).
					// Important: One bar most probably represents different keywords for different topics.
					// Get data.
					ArrayList<Pair<String, Double>> keywordProbabilityData = data.getKeywordProbabilities().get(topicConfig);
					
					// Calculate start index.
					final int startIndex = options.getNumberOfKeywordsToDisplay() <= keywordProbabilityData.size() ? options.getNumberOfKeywordsToDisplay() - 1 : keywordProbabilityData.size() - 1; 
					
					// Iterate over keyword/probablity pairs.
					for (int i = startIndex; i >= 0; i--) {
						Pair<String, Double> keywordProbPair = keywordProbabilityData.get(i);
						
						series.getData().add( new XYChart.Data<Number, String>(	keywordProbPair.getValue(), "#" + String.valueOf( i + 1 )) );
					}
					
					// Add data series to barchart.
					probabilityDistribution_barchart.getData().add(series);
				}
			}
		}
	}
	
	/**
	 * Refreshes barchart. Adds data for all specified keywords and all topics to it.
	 * @param data
	 * @param options
	 * @param allowedKeywords
	 */
	private void refreshBarchartByKeywords(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options, final Set<String> allowedKeywords)
	{
		// Clear data.
		probabilityDistribution_barchart.getData().clear();
		
		// Add new data to chart.
		if (data != null && data.getKeywordProbabilities() != null && allowedKeywords != null && allowedKeywords.size() > 0) {
			for (Pair<Integer, Integer> topicConfig : data.getKeywordProbabilities().keySet()) {
				// Allocate series object, set name.
				XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
				series.setName(String.valueOf(topicConfig.getKey()) + "#" + String.valueOf(topicConfig.getValue()));
				
				/*
				 *  Process requested keyword/probability pairs.
				 */
				
				// Add all keyword/probability pairs to data series.
				// Sort by priority for each topic (as opposed to alphabetically).
				// Important: One bar most probably represents different keywords for different topics.
				// Get data.
				ArrayList<Pair<String, Double>> keywordProbabilityData = data.getKeywordProbabilities().get(topicConfig);
				
				// Calculate start index.
				final int startIndex = options.getNumberOfKeywordsToDisplay() <= keywordProbabilityData.size() ? options.getNumberOfKeywordsToDisplay() - 1 : keywordProbabilityData.size() - 1; 
				
				// Iterate over keyword/probablity pairs.
				boolean found = false; 
				for (int i = startIndex; i >= 0; i--) {
					Pair<String, Double> keywordProbPair = keywordProbabilityData.get(i);
					
					// Allowed keyword found: Add probability to chart.
					if ( allowedKeywords.contains(keywordProbPair.getKey()) ) {
						series.getData().add( new XYChart.Data<Number, String>(	keywordProbPair.getValue(), "#" + String.valueOf( i + 1 )) );
						
						// Mark keyword as found, stop loop for this topic.
						found = true;
					}
					
					else
						series.getData().add( new XYChart.Data<Number, String>(0, "#" + String.valueOf( i + 1 )) );
				}
				
				// Add data series to barchart, if keyword was found in data.
				if (found) {
					probabilityDistribution_barchart.getData().add(series);
				}
			}
			
			// Bolden selected keyword.
			for (XYChart.Series<Number, String> dataSeries : probabilityDistribution_barchart.getData()) {
				// Bolden bar reflecting keyword in hovered over topic.
				if (dataSeries.getName().equals(selectedTopicName)) {
					for (XYChart.Data<Number, String> chartData : dataSeries.getData()) {
						chartData.getNode().setScaleY(3);
					}
				}
			}
		}
	}
	
	/**
	 * Creates labels for topic IDs.
	 * @param selectedTopicConfigurations
	 */
	private void createTopicIDLabels(ArrayList<Pair<Integer, Integer>> selectedTopicConfigurations)
	{
	    AnchorPane node = tagcloud_anchorpane;//(AnchorPane) canvas.getParent().getParent();
	    
	    // Remove old labels.
	    for (Label label : topicIDLabels) {
	    	node.getChildren().remove(label);
	    }
	    topicIDLabels.clear();
	    
	    // Add new labels.
	    for (int i = 0; i < selectedTopicConfigurations.size(); i++) {
	    	Pair<Integer, Integer> topicConfig = selectedTopicConfigurations.get(i);
	    	
	    	Label label = new Label();
	    	label.setText(topicConfig.getKey() + "#" + topicConfig.getValue());
	    	label.setFont(Font.font("Verdana", FontPosture.ITALIC, 10));
	    	
	    	// Add label to collections and GUI.
	    	topicIDLabels.add(label);
	    	node.getChildren().add(label);
	    }
	}
	
	/**
	 * Creates tag clouds from loaded data.
	 * @param data
	 * @param options
	 * @param probabilitySums
	 */
	private void createTagClouds(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options, ArrayList<Double> probabilitySums)
	{
	    // Init tag cloud container.
		final int numberOfTopics			= data.getTopicConfigurations().size();
	    tagCloudContainer					= new ArrayList<VBox>(numberOfTopics);
	    
		// For exit from local scope instance: Reset selected label.
	    tagcloud_anchorpane.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	selectedKeyword 	= "";
            	selectedTopicName	= "";
            	
            	// Reset opacity of labels containing selected keyword.
            	removeHoverHighlighting(labelsWithSelectedKeyword);
            	
            	// Update visualizations.
            	drawBridges();
            }
        });
		
	    // Iterate over all topics; create cloud for each one.
	    int cloudCount = 0;
	    
	    for (Map.Entry<Pair<Integer, Integer>, ArrayList<Pair<String, Double>>> keywordProbabilitySet : data.getKeywordProbabilities().entrySet()) {
			// Get keyword probability data.
	    	ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs = keywordProbabilitySet.getValue();

	    	// Get corresponding topic ID label.
	    	final Label topicIDLabel = topicIDLabels.get(cloudCount++);
	    	
	    	// Initialize VBox.
			VBox vbox = new VBox();
			tagCloudContainer.add(vbox);

	    	// Set background and border color.
			//vbox.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 255, 0.9), CornerRadii.EMPTY, Insets.EMPTY)) );
			vbox.setStyle("-fx-border-color: white; -fx-padding: 5px;");
		
			// Init labels and add them to container.
			double probabilitySum = 0;
			for (int j = 0; j < options.getNumberOfKeywordsToDisplay(); j++) {
				/*
				 * Init label.
				 */
				
				String keyword		= topicKeywordProbabilityPairs.get(j).getKey();
				Label label 		= new Label();
		    	
				/*
				 * Add event listener for labels.
				 */
				
				// For entry into label:
				label.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) 
		            {
		            	Label label = (Label)event.getSource();
		            	
		            	if (!selectedKeyword.equals(label.getText())) {
		            		// Store selected keyword.
		            		selectedKeyword		= label.getText();
		            		// Store name of selected topic.
		            		selectedTopicName	= topicIDLabel.getText();
		            		
		            		// Add label to collection of labels containing the selected keyword.
		            		labelsWithSelectedKeyword.add(label);
		            		
			            	// Update connections between words in neighbouring tag clouds.
			            	drawBridges();
			            	
			            	// Increase opacity of hovered over label, bolden text.
			            	label.setOpacity(1);
			            	label.setFont(Font.font(label.getFont().getName(), FontWeight.BOLD, label.getFont().getSize()));
			            	
			            	// Show hovered over keyword in prob. dist. barchart.
			            	Set<String> allowedKeywords = new HashSet<String>();
			            	allowedKeywords.add(selectedKeyword);
			            	refreshBarchartByKeywords((ParallelTagCloudDataset)data, (ParallelTagCloudOptionset)options, allowedKeywords);
		            	}
		            }
		        });
				
				// For exit from label:
				label.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) 
		            {
		            	probabilityDistribution_barchart.getData().clear();
		            }
		        });
				
				// At click in label: Show context for this keyword.
				label.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) 
		            {
			        	final int ldaConfigID 	= Integer.valueOf(topicIDLabel.getText().substring(0, topicIDLabel.getText().indexOf("#")));
			        	final int topicID		= Integer.valueOf(topicIDLabel.getText().substring(topicIDLabel.getText().indexOf("#") + 1));
			        	
			        	// Remove highlighting in other labels.
//			        	removeHoverHighlighting(labelsWithKeywordInContextSearch);
			        	// Clear collection of labels with keywords in context search.
//			        	labelsWithKeywordInContextSearch.clear();
			        	
			        	// Update requested keyword.
			        	requestedKeyword 		= label.getText();
			        	// Update requested label.
			        	requestedKeywordLabel	= label; 
			        	
			        	// Clear previously requested labels.
			        	removeHoverHighlighting(labelsWithRequestedKeywords);
			        	labelsWithRequestedKeywords.clear();

			        	// Add all labels containing the requested keyword.
			        	for (int i = 0; i < tagCloudContainer.size(); i++) {
			    			for (Node node : tagCloudContainer.get(i).getChildren()) {
			    				Label label = (Label)node;
			    				
			    				if (label.getText().equals(requestedKeyword))
			    					labelsWithRequestedKeywords.add(label);
			    			}
			        	}
			        	
			        	// List relevant documents, fetch results.
		            	Map<Integer, Integer> documentRanksByID = analysisController.listRelevantDocuments(new Pair<Integer, Integer>(ldaConfigID, topicID));
		            	// Show keyword context (provide rank results to keyword context component).
		            	analysisController.showKeywordContext(label.getText(), documentRanksByID);
		            	
		            	// Stop event's propagation.
		            	event.consume();
		            }
		        });
				
				// Update label, add it to containing VBox.
				label.setText(keyword);
				vbox.getChildren().add(label);
			}
			
			// Set probability sum for this topic.
			probabilitySums.add(probabilitySum);
			
			// Add tag clouds to parent.
			tagcloud_anchorpane.getChildren().add(vbox);
			
			
			/*
			 * Add event listener for VBoxes and topic ID labels.
			 */
			
			initHoverEventListeners();
		}
	}
	
	/**
	 * Updates keyword/probability data. 
	 * @param keyword
	 * @param probability
	 */
	private void updateKeywordProbabilitySums(final String keyword, final double probability)
	{
		// Update probability sum for this keyword over all topics.
		if (!keywordProbabilitySumsOverTopics.containsKey(keyword)) {
			keywordProbabilitySumsOverTopics.put(keyword, probability);
		}
		
		else {
			keywordProbabilitySumsOverTopics.put(keyword, keywordProbabilitySumsOverTopics.get(keyword) + probability);
		}
		
		// Check if there are new extrema in keyword probability sums over all topics.
		keywordProbabilitySumOverTopicsMax = keywordProbabilitySumsOverTopics.get(keyword) > keywordProbabilitySumOverTopicsMax ? keywordProbabilitySumsOverTopics.get(keyword) : keywordProbabilitySumOverTopicsMax;
		keywordProbabilitySumOverTopicsMin = keywordProbabilitySumsOverTopics.get(keyword) < keywordProbabilitySumOverTopicsMin ? keywordProbabilitySumsOverTopics.get(keyword) : keywordProbabilitySumOverTopicsMin;
	}
	
	/**
	 * Place tag clouds.
	 */
	private void adjustTagcloudPlacement()
	{
	    this.canvas.getParent().applyCss();
	    this.canvas.getParent().layout();
	    
	    // Gap between two tag clouds.
	    final double gap = 5;
	    
	    // Calculate total width of all tag clouds.
	    double tagCloudWidthSum = 0;
	    for (VBox vbox : tagCloudContainer) {
	    	tagCloudWidthSum += vbox.getBoundsInParent().getWidth();
	    }
	    
	    /*
	     * Place tag clouds according to total width.
	     */
	    
	    // Keep track of current position on x-axis.
	    double currXPos = 0;
	    for (int i = 0; i < tagCloudContainer.size(); i++) {
	    	// Get current tag cloud.
	    	VBox vbox = tagCloudContainer.get(i);
	    	
	    	// Get width of tag cloud.
	    	double tagCloudWidth			= vbox.getBoundsInParent().getWidth();
	    	// Calculate segment of total width this tag cloud needs.
	    	double percentageOfWidth		= tagCloudWidth / (tagCloudWidthSum);
	    	// Alloted width in canvas.
			double adjustedTagCloudWidth	= (canvas.getWidth() - tagCloudContainer.size() * gap) * percentageOfWidth;
			
			vbox.setLayoutX(currXPos + (adjustedTagCloudWidth - tagCloudWidth) / 2);
			vbox.setLayoutY(canvas.getLayoutY() + 15);
			
			// Place label.
			topicIDLabels.get(i).setLayoutX(currXPos + (adjustedTagCloudWidth - tagCloudWidth));
			topicIDLabels.get(i).setLayoutY(5);
			
			// Keep track of current position on x-axis.
			currXPos += adjustedTagCloudWidth + gap;
	    }
	}
	
	/**
	 * Adjusts font sizes so that they reflect the probability values of their respective keywords in their respective topics.
	 * @param data
	 * @param options
	 * @param probabilitySums
	 */
	private void adjustTagFontSizes(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options, final ArrayList<Double> probabilitySums)
	{
		this.canvas.getParent().applyCss();
	    this.canvas.getParent().layout();
	    
		final int numberOfTopics 	= data.getTopicConfigurations().size();
		final int numberOfKeywords 	= options.getNumberOfKeywordsToDisplay();
		
		if (tagcloud_anchorpane != null && data.getKeywordProbabilities().size() > 0 && numberOfTopics > 0 && numberOfKeywords > 0) {
			tagcloud_anchorpane.applyCss();
			tagcloud_anchorpane.layout();
			
			/*
			 * Resize labels in current tag-cloud width according to available (canvas) width.			
			 */
			
			// Find max. label widths for all topics.
		    double maxLabelWidth 	= 0;
		    // Determine width of all clouds.
		    double cloudWidthSum	= 0;
		    // Determine max. height of cloud.
		    double maxCloudHeight 	= 0;
		    
		    // Iterate over all clouds.
		    for (VBox vbox : tagCloudContainer) {
		    	double maxTopicLabelWidth 	= 0;
		    	
		    	// Find max. label width for this topic.
		    	for (Node node : vbox.getChildren()) {
		    		Label label			= (Label)node;
		    		maxTopicLabelWidth	= maxTopicLabelWidth >= label.getWidth() ? maxTopicLabelWidth : label.getWidth();
		    	}
		    	
		    	// Center labels.
		    	for (Node node : vbox.getChildren()) {
		    		Label label			= (Label)node;
		    		
		    		if (label.getWidth() < maxTopicLabelWidth)
		    			label.setTranslateX( (maxTopicLabelWidth - label.getWidth()) / 2);
		    	}
		    	
		    	// Update overall width of all clouds.
		    	cloudWidthSum += maxLabelWidth;
		    	// Update max. label width over all topics.
		    	maxLabelWidth = maxLabelWidth >= maxTopicLabelWidth ? maxLabelWidth : maxTopicLabelWidth;
		    	// Update max. cloud height.
		    	maxCloudHeight = maxCloudHeight >= vbox.getHeight() ? maxCloudHeight : vbox.getHeight();
		    }
		    
		    // Calculate ratio to multiply current font size with.
		    final double gap					= 5;
		    final double widthRatio 			= zoomFactor * (canvas.getWidth() - tagCloudContainer.size() * gap) / cloudWidthSum;
		    final double heightRatio			= zoomFactor * (canvas.getHeight() - 35) / maxCloudHeight;
		    final double ratio					= widthRatio < heightRatio ? widthRatio : heightRatio;
		    
		    // Determine new width of PTC container.
		    final double newPTCWidth			= (cloudWidthSum + tagCloudContainer.size() * gap) * heightRatio;
		    final double newPTCContainerWidth	= newPTCWidth > tagcloud_scrollpane.getWidth() ? newPTCWidth : tagcloud_scrollpane.getWidth();
		    // Set new container width in dependence of PTC width.
		    tagcloud_anchorpane.setMinWidth(newPTCContainerWidth);
		    tagcloud_anchorpane.setPrefWidth(newPTCContainerWidth);
		    tagcloud_anchorpane.setMaxWidth(newPTCContainerWidth);
		    
		    
		    // Scale all labels with determined ratio.
		    for (VBox vbox : tagCloudContainer) {
		    	// Adjust font sizes.
		    	for (Node node : vbox.getChildren()) {
		    		Label label	= (Label)node;
		    		label.setFont(new Font(label.getFont().getSize() * ratio));
		    	}
		    }
		}
	}
	
	/**
	 * Draw connections between tag clouds using available data.
	 */
	private void drawBridges()
	{
		drawBridges((ParallelTagCloudDataset)this.data, (ParallelTagCloudOptionset)this.options);
	}
	
	/**
	 * Draws connection between tag clouds.
	 * @param data
	 * @param options
	 */
	private void drawBridges(final ParallelTagCloudDataset data, final ParallelTagCloudOptionset options)
	{
		final int numberOfTopics	= tagCloudContainer.size();//data.getTopicConfigurations().size() < tagCloudContainer.size() ? data.getTopicConfigurations().size() : tagCloudContainer.size(); 
		final int numberOfKeywords 	= options.getNumberOfKeywordsToDisplay();
		
		// Redraw GUI.
		tagcloud_anchorpane.applyCss();
		tagcloud_anchorpane.layout();
		
		// Define default line width in pixel.
		double defaultLineWidth = canvas.getHeight() * (1.0 / 219);
		
		// Get GraphicsContext for canvas, draw background.
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setFill(Color.BLACK);
		
		// Loop over all tagclouds.
		for (int i = 0; i < numberOfTopics ; i++) {
			VBox currTagCloud							= tagCloudContainer.get(i);
			ArrayList<Pair<String, Double>> currData	= data.getNthKeywordProbabilityArray(i).getValue();
			
			// Re-render tag cloud.
			currTagCloud.applyCss();
			currTagCloud.layout();
			
			// Calculate bridge offset for bridges starting at current tag cloud.
			double currBridgeOffsetX					= currTagCloud.getLayoutX();
			double currBridgeOffsetY					= currTagCloud.getLayoutY() - 3 + defaultLineWidth;
			
			// For all keywords in the current tag cloud: See if they exist in the next tagcloud too.
			for (int keywordIndex = 0; keywordIndex < numberOfKeywords; keywordIndex++) {
				String keyword		= currData.get(keywordIndex).getKey();
				Label currLabel		= (Label)currTagCloud.getChildren().get(keywordIndex);
				
				// Proceed iff (1) no keyword is hovered over right now or (2) the current keyword is hovered over.
				boolean noKeywordSelected 		= selectedKeyword.equals("");
				boolean isThisKeywordSelected	= !selectedKeyword.equals("") && keyword.equals(selectedKeyword);
				
				// Change opacity if this keyword is selected.
				if (isThisKeywordSelected) {
					// Change opacity of label.
					currLabel.setOpacity(1);
					// Add to collection of labels containing the selected keyword.
					labelsWithSelectedKeyword.add(currLabel);
				}
				
				// Draw bridges for this keyword in this cloud. Don't do that for last cloud.
				if (i < numberOfTopics - 1) {
					drawBridgesForKeywordInTagCloud(	data, numberOfTopics, numberOfKeywords, i,
														keyword, noKeywordSelected, isThisKeywordSelected,
														currBridgeOffsetX, currBridgeOffsetY,
														currLabel, defaultLineWidth,
														gc);
				}
			}
		}
		
	}
	
	/**
	 * Auxiliary method used to draw bridges for one keyword from one tag cloud to all other (relevant) clouds. 
	 * @param data
	 * @param numberOfTopics
	 * @param numberOfKeywords
	 * @param i
	 * @param keyword
	 * @param noKeywordSelected
	 * @param isThisKeywordSelected
	 * @param currBridgeOffsetX
	 * @param currBridgeOffsetY
	 * @param currLabel
	 * @param defaultLineWidth
	 * @param gc
	 */
	private void drawBridgesForKeywordInTagCloud(	final ParallelTagCloudDataset data, final int numberOfTopics, final int numberOfKeywords, final int i,
													final String keyword, final boolean noKeywordSelected, final boolean isThisKeywordSelected,
													final double currBridgeOffsetX, final double currBridgeOffsetY,
													final Label currLabel, final double defaultLineWidth,
													final GraphicsContext gc)
	{
		if ( noKeywordSelected || isThisKeywordSelected ) {
			// Initialize flag to indicate whether a keyword was found in the next tag cloud.
			boolean wasFound		= false;
			
			// Calculate positions of connecting line on left side.				
			double currBridgeX	= currBridgeOffsetX + currLabel.getBoundsInParent().getMaxX() + 5; 
			double currBridgeY	= currBridgeOffsetY + (currLabel.getBoundsInParent().getMinY() + currLabel.getBoundsInParent().getMaxY()) / 2;
			
			// Search 'til next appeareance of keyword.
			for (int j = i + 1; j < (i + 2) && !wasFound; j++) {
				VBox nextTagCloud							= tagCloudContainer.get(j);
				ArrayList<Pair<String, Double>> nextData	= data.getNthKeywordProbabilityArray(j).getValue();
				
				// Calculate offset from bridges of 
				double nextBridgeOffsetX					= nextTagCloud.getLayoutX();
				double nextBridgeOffsetY					= nextTagCloud.getLayoutY() - 3 + defaultLineWidth;
				
				// Search in next tag cloud for this word.
				for (int nextKeywordIndex = 0; nextKeywordIndex < numberOfKeywords && !wasFound; nextKeywordIndex++) {
					String nextKeyword		= nextData.get(nextKeywordIndex).getKey();
					Label nextLabel			= (Label)nextTagCloud.getChildren().get(nextKeywordIndex); 
				
					// Calculate positions of connecting line on right side.
					double nextBridgeX	= nextBridgeOffsetX + nextLabel.getBoundsInParent().getMinX() - 10; 
					double nextBridgeY	= nextBridgeOffsetY + (nextLabel.getBoundsInParent().getMinY() + nextLabel.getBoundsInParent().getMaxY()) / 2;
					
					if (nextKeyword.equals(keyword)) {
						wasFound			= true;
						
						// Line width is proportional to the difference in probability between these two topics.
						gc.setLineWidth(defaultLineWidth);
						
						// Set color.
						gc.setStroke(Color.RED); 
						
						// Draw line.
						gc.strokeLine(currBridgeX, currBridgeY, nextBridgeX, nextBridgeY);
					}
				}
			}
		}
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
	public void initHoverEventListeners()
	{
		int cloudCount = 0;
		
		// Check that labels exist before hooking up event listener.
		if (topicIDLabels != null 		&& 	tagCloudContainer != null 		&&
			topicIDLabels.size() > 0	&&	tagCloudContainer.size() > 0	&&
			topicIDLabels.size() == tagCloudContainer.size()) {
			for (Label label : topicIDLabels) {
				// Get relevant tag cloud.
				VBox vbox = tagCloudContainer.get(cloudCount);
				
				// Create event listener for hovering over tagcloud.
				EventHandler<MouseEvent> onMouseEnteredEventHandler = new EventHandler<MouseEvent>() 
			    {
			        @Override
			        public void handle(MouseEvent event) 
			        {
			        	// Draw border for current tag cloud.
		            	vbox.setStyle("-fx-border-color: red; -fx-padding: 5px;");
		            	
		            	// Change cursor type.
		            	analysisController.getScene().setCursor(Cursor.HAND);
		            	
		            	/*
		            	 *  Display topic in barchart.
		            	 */
		            	
		            	// 	Prepare set containing topics to display in distribution barchart.
		    			Set<Pair<Integer, Integer>> topicsToDisplayInDistBarchart 	= new HashSet<Pair<Integer,Integer>>();
		    			// Extract topic model and topic IDs from label text.
		    			String[] idParts 											= label.getText().split("#");
		    			// Add topic ID to collection.
		    			topicsToDisplayInDistBarchart.add( new Pair<Integer, Integer>(Integer.parseInt(idParts[0]), Integer.parseInt(idParts[1])) );
		    			
		    			// Refresh barchart.
		    			refreshBarchart((ParallelTagCloudDataset)data, (ParallelTagCloudOptionset)options, topicsToDisplayInDistBarchart);
		    			
		    			/*
		    			 * Highlight topic model assoiated with hovered over topic in other visualizations.
		    			 */
		    			
		    			analysisController.highlightDataPoints(Integer.parseInt(idParts[0]), DatapointIDMode.CONFIG_ID, VisualizationComponentType.PARALLEL_TAG_CLOUD);
		            }
			    };
			    
			    // Create event listener for exiting tagcloud.
			    EventHandler<MouseEvent> onMouseExitEventHandler = new EventHandler<MouseEvent>() 
			    {
			        @Override
			        public void handle(MouseEvent event) 
			        {
			        	// Hide border for current tag cloud.
		            	vbox.setStyle("-fx-border-color: white; -fx-padding: 5px;");
		            	
		            	// Change cursor type.
		            	analysisController.getScene().setCursor(Cursor.DEFAULT);
		            	
		            	// Clear barchart.
		            	probabilityDistribution_barchart.getData().clear();
		            	
		            	// Remove highlighting in other panels.
		            	analysisController.removeHighlighting(VisualizationComponentType.PARALLEL_TAG_CLOUD);
		            }
			    };
			    
			    // Create event listener for clicking tag cloud.
			    EventHandler<MouseEvent> onMouseClickEventHandler = new EventHandler<MouseEvent>() 
			    {
			        @Override
			        public void handle(MouseEvent event) 
			        {
			        	final int ldaConfigID 	= Integer.valueOf(label.getText().substring(0, label.getText().indexOf("#")));
			        	final int topicID		= Integer.valueOf(label.getText().substring(label.getText().indexOf("#") + 1));
			        			
			        	// Get document data, list it in table.
		            	analysisController.listRelevantDocuments(new Pair<Integer, Integer>(ldaConfigID, topicID));
		            }
			    };
	
				// For entry into tagcloud / label:
				label.addEventHandler(MouseEvent.MOUSE_ENTERED, onMouseEnteredEventHandler);
				vbox.addEventHandler(MouseEvent.MOUSE_ENTERED, onMouseEnteredEventHandler);
				// For exit from tagcloud / label:
				label.addEventHandler(MouseEvent.MOUSE_EXITED, onMouseExitEventHandler);
				vbox.addEventHandler(MouseEvent.MOUSE_EXITED, onMouseExitEventHandler);
				
				// For click in tagcloud / label:
				label.addEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClickEventHandler);
				vbox.addEventHandler(MouseEvent.MOUSE_CLICKED, onMouseClickEventHandler);
				
				// Keep track of how many clouds are already processed.
				cloudCount++;
			}
		}
	}

	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		if (idMode == DatapointIDMode.CONFIG_ID) {
			// Cast data to usable format.
			ParallelTagCloudDataset ptcData 							= ((ParallelTagCloudDataset)data);
			// Prepare set containing topics to display in distribution barchart.
			Set<Pair<Integer, Integer>> topicsToDisplayInDistBarchart	= new HashSet<Pair<Integer,Integer>>();
			
			// Check which topic configurations fulfill requirements.
			if (data != null) {
				for (int i = 0; i < ptcData.getTopicConfigurations().size() && i < tagCloudContainer.size(); i++) {
					Pair<Integer, Integer> topicConfig = ptcData.getTopicConfigurations().get(i);
					// If set of topic models to highlight contains topic model ID of current tag cloud:
					// Increase opacity of current tag cloud.
					if (dataPointIDs.contains(topicConfig.getKey())) {
						topicsToDisplayInDistBarchart.add(topicConfig);
					}
				}
				
				// Update barchart.
				refreshBarchart(ptcData, (ParallelTagCloudOptionset)options, topicsToDisplayInDistBarchart);
			}
		}
	}

	@Override
	public void removeHoverHighlighting()
	{
		for (int i = 0; i < tagCloudContainer.size(); i++) {
			for (Node node : tagCloudContainer.get(i).getChildren()) {
				Label label = (Label)node;
				// Reset opacity if label does not contain requested keyword.
				if (!label.getText().equals(requestedKeyword))
					node.setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
			}
		}
	}
	
	/**
	 * Removes hover highlighting from set of defined labels.
	 * @param labels
	 */
	public void removeHoverHighlighting(List<Label> labels)
	{
		// Reset opacity of labels containing selected keyword, reset text weight.
    	for (Label label : labels) {
    		// If label is not requested keyword label:
    		if (!label.equals(requestedKeywordLabel)) {
	    		// Remove boldening (except keyword is keyword in context search component).
	    		label.setFont(Font.font(label.getFont().getName(), FontWeight.NORMAL, label.getFont().getSize()));
	    		
	    		// If labels don't carry requested keyword: Reset opacity.
	    		if (!label.getText().equals(requestedKeyword)) {
					// Set opacity.
					label.setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
	    		}
    		}
    	}
    	
    	// Reset collection.
    	labels.clear();
	}

	@Override
	public void resizeContent(double width, double height)
	{
		/*
		 * Resize elements.
		 */
		
		if (width > 0) 
			// Resize barchart.
			probabilityDistribution_barchart.setPrefWidth(splitpane.getWidth() * (1 - splitpane.getDividerPositions()[0]));
		
		if (height > 0)
			// Resize barchart.
			probabilityDistribution_barchart.setPrefHeight(height - 5);

		// Adjust width and height to initialization procedure.
		final double tagcloud_anchorpane_width	= tagcloud_anchorpane.getWidth() == 0 ? 542 : tagcloud_scrollpane.getWidth();
		final double tagcloud_anchorpane_height = tagcloud_anchorpane.getWidth() == 0 ? 284 : tagcloud_scrollpane.getHeight() - 13;
				
		tagcloud_anchorpane.setMinWidth(tagcloud_anchorpane_width);
		tagcloud_anchorpane.setPrefWidth(tagcloud_anchorpane_width);
		tagcloud_anchorpane.setMaxWidth(tagcloud_anchorpane_width);
		tagcloud_anchorpane.setMinHeight(tagcloud_anchorpane_height);
		tagcloud_anchorpane.setPrefHeight(tagcloud_anchorpane_height);
		tagcloud_anchorpane.setMaxHeight(tagcloud_anchorpane_height);
		
		// Resize canvas.
		canvas.setWidth(tagcloud_anchorpane.getWidth() > 0 ? tagcloud_anchorpane.getWidth() : splitpane.getWidth() * splitpane.getDividerPositions()[0]);
		canvas.setHeight(splitpane.getHeight() - 5);
		
		/*
		 * Clear canvas and redraw PTC.
		 */
		ParallelTagCloudDataset ptcData = (ParallelTagCloudDataset)this.data;
		if (	this.data != null && this.options != null	&& 
				ptcData.getTopicConfigurations() != null 	&& 
				ptcData.getTopicConfigurations().size() > 0) {
			// Redraw.
			refreshParallelTagCloud((ParallelTagCloudDataset)this.data, (ParallelTagCloudOptionset)this.options, false);
		}
	}

	public void clear()
	{
		// Clear tag clouds.
		if (tagCloudContainer != null) {
			tagcloud_anchorpane.getChildren().removeAll(tagCloudContainer);
			tagCloudContainer.clear();
		}
		
		// Remove topic ID labels.
		tagcloud_anchorpane.getChildren().removeAll(topicIDLabels);
		topicIDLabels.clear();
		
		// Clear canvas.
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Clear barchart.
		probabilityDistribution_barchart.getData().clear();
		// Hide barchart.
		probabilityDistribution_barchart.setVisible(false);
	}
	
	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

	@Override
	protected void resizeElement(Node node, double width, double height)
	{
		resizeContent(width, height);
	}
}