package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import view.components.ColorScale;
import model.LDAConfiguration;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Pair;

public class ParallelTagCloudsController extends LocalScopeVisualizationController
{
	/*
	 * GUI elements.
	 */
	
	/**
	 * Canvas used to paint connections between words.
	 */
	protected @FXML Canvas canvas;
	
	/**
	 * Label for ID of topic 1.
	 */
	private Label topic1ID_label;
	/**
	 * Label for ID of topic 2.
	 */
	private Label topic2ID_label;
	
	/**
	 * Container holding VBoxes of labels.
	 */
	private ArrayList<VBox> tagCloudContainer; 
	
	/**
	 * Font size used as default - if all lables have this font size, they fit exactly in the space
	 * designated for the local scope visualization.
	 */
	private double defaultFontSize;
	
	/**
	 * Keyword that is currently hovered over.
	 */
	private String selectedKeyword;
	
	/*
	 * Other data.
	 */
	
	/**
	 * Since it's reasonable to display only one dataset at once - given the available
	 * space - currentIndex stores the index of the LDA configuration data currently 
	 * displayed in this local scope. 
	 */
	private int currentIndex;
	
	/**
	 * Collection containing all LDA configurations to display.
	 */
	private ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations;
	
	/**
	 * Collection of selected topic configurations (LDA configuration ID, topic ID).
	 */
	private ArrayList<Pair<Integer, Integer>> selectedTopicConfigurations;
	
	/**
	 * List (for this LDA configuration) of lists (of topics) of keyword/probability pairs. 
	 */
	private ArrayList<ArrayList<Pair<String, Double>>> data;
	
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
	 * Exponent applied to each keyword's probability.
	 * Goal: Improve visibility of (small) differences in keyword probability.
	 */
	private final double keywordProbabilityExponent = 2.5; 
	
	
	// -----------------------------------------------
	// 				Methods
	// -----------------------------------------------
	
	
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_ParallelTagCloudsController.");
		
		// Set visualization type.
		this.visualizationType				= LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS;

		// Init collections.
		keywordProbabilitySumsOverTopics	= new HashMap<String, Double>();
		selectedTopicConfigurations			= new ArrayList<Pair<Integer,Integer>>();
		
		// Set inital scalar values.
		currentIndex						= 0;
		keywordProbabilitySumOverTopicsMax	= 0;
		keywordProbabilitySumOverTopicsMin	= Double.MAX_VALUE;
		selectedKeyword						= "";
		
		// Init GUI elements.
		topic1ID_label						= new Label();
		topic2ID_label						= new Label();
		topic1ID_label.setId("topic1ID_label");
		topic1ID_label.setId("topic2ID_label");
		topic1ID_label.setFont(Font.font("Verdana", FontPosture.ITALIC, 10));
	}
			
	/**
	 * Refreshes PTC using exactly two specified topics. Called after distance chord in
	 * chord diagram was selected.
	 * @param selectedTopicConfigurations
	 * @param maxNumberOfKeywords
	 * @param numberOfKeywords
	 * @param updateData
	 */
	public void refresh(ArrayList<Pair<Integer, Integer>> selectedTopicConfigurations,
						int maxNumberOfKeywords, int numberOfKeywords, 
						boolean updateData)
	{
		// Reset visualization.
		reset(selectedFilteredLDAConfigurations, maxNumberOfTopics, numberOfTopics, maxNumberOfKeywords, numberOfKeywords);
	
		// Get list (for this LDA configuration) of lists (of topics) of keyword/probability pairs.
		if (updateData) {
			// Update configuration collection.
			this.selectedTopicConfigurations = selectedTopicConfigurations;
			
			// Load data from database.
			data = new ArrayList<ArrayList<Pair<String,Double>>>(2);
			data.add( workspace.getDatabaseManagement().getRawDataForTopic(selectedTopicConfigurations.get(0).getKey(), selectedTopicConfigurations.get(0).getValue(), maxNumberOfKeywords) );
			data.add( workspace.getDatabaseManagement().getRawDataForTopic(selectedTopicConfigurations.get(1).getKey(), selectedTopicConfigurations.get(1).getValue(), maxNumberOfKeywords) );
			
			// Refresh control for number of topics.
			localScope.setNumberOfTopicsMaximum(data.size());
		}
		
	    this.numberOfTopics = 2;
		    
	    // Probability sums for each topic.
	    ArrayList<Double> probabilitySums = new ArrayList<Double>(numberOfTopics);
	    
	    /*
	     *	0. Place labels. 
	     */
	    AnchorPane node = (AnchorPane) canvas.getParent(); 
	    // Add, if not already added.
	    if (!node.getChildren().contains(topic1ID_label)) {
	    	node.getChildren().add(topic1ID_label);
	    	node.getChildren().add(topic2ID_label);
	    }
	    
	    /*
		 * 1. Create tag clouds, fill them with data.
		 */
	    
	    createTagClouds(numberOfTopics, probabilitySums);
    	
		/*
		 * 2. Adjust tag font sizes. 
		 */
	    
	    adjustTagFontSizes(numberOfTopics, numberOfKeywords, probabilitySums);
	    
	    /*
	     * 3. Draw connections between tags with same content in different topics. 
	     */
	   
	    drawBridges(data, numberOfTopics, numberOfKeywords);
	    
	    /*
	     * 4. Place labels above tag clouds, set text.
	     */
    	topic1ID_label.setLayoutX(canvas.getWidth() / 2 - 86 / 2 - 10);
    	topic1ID_label.setText(	selectedTopicConfigurations.get(0).getKey() + "#" + selectedTopicConfigurations.get(0).getValue() +
    							" ⇔ " + 
    							selectedTopicConfigurations.get(1).getKey() + "#" + selectedTopicConfigurations.get(1).getValue());
	}
	
	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, 
						int maxNumberOfTopics, int numberOfTopics, 
						int maxNumberOfKeywords, int numberOfKeywords, 
						boolean updateData)
	{
		// Reset visualization.
		reset(selectedFilteredLDAConfigurations, maxNumberOfTopics, numberOfTopics, maxNumberOfKeywords, numberOfKeywords);
	
		// For now: Allow only if exactly two LDA configurations are specified.
		if (selectedFilteredLDAConfigurations.size() == 2)  {
			// Get list (for this LDA configuration) of lists (of topics) of keyword/probability pairs.
			if (updateData) {
				// Load data from database.
				data = workspace.getDatabaseManagement().getKITData(selectedFilteredLDAConfigurations.get(currentIndex), maxNumberOfKeywords);
				
				// Refresh control for number of topics.
				localScope.setNumberOfTopicsMaximum(data.size());
			}
			
		    // Check if the actually available number of topics is smaller than the required one.
		    // If so: Adapt number of topics used.
		    this.numberOfTopics = data.size() < numberOfTopics ? data.size() : numberOfTopics;  
		    numberOfTopics		= this.numberOfTopics;
		    
		    // Probability sums for each topic.
		    ArrayList<Double> probabilitySums = new ArrayList<Double>(numberOfTopics);
		    
		    /*
			 * 1. Create tag clouds, fill them with data.
			 */
		    
		    createTagClouds(numberOfTopics, probabilitySums);

			/*
			 * 2. Adjust tag font sizes. 
			 */
		    
		    adjustTagFontSizes(numberOfTopics, numberOfKeywords, probabilitySums);
		    
		    /*
		     * 3. Draw connections between tags with same content in different topics. 
		     */
		    
		    drawBridges(data, numberOfTopics, numberOfKeywords);
		}
	}

	/**
	 * Creates tag clouds from loaded data.
	 * @param numberOfTopics
	 * @param probabilitySums
	 */
	private void createTagClouds(final int numberOfTopics, ArrayList<Double> probabilitySums)
	{
	    // Init tag cloud container.
	    double intervalX					= canvas.getWidth() / (numberOfTopics);
	    tagCloudContainer					= new ArrayList<VBox>(numberOfTopics);
	    
		// For exit from local scope instance: Reset selected label.
		anchorPane.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	selectedKeyword = "";
            	
            	// Update visualizations.
            	drawBridges();
            }
        });
		
	    // Iterate over all topics; create cloud for each one.
		for (int i = 0; i < numberOfTopics; i++) {
			ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs = data.get(i);
			
			VBox vbox = new VBox();
			vbox.setLayoutX(canvas.getLayoutX() + i * intervalX + intervalX / (numberOfTopics * 2));
			vbox.setLayoutY(canvas.getLayoutY() + 15);
			tagCloudContainer.add(vbox);
			
			// Init labels to container.
			double probabilitySum = 0;
			for (int j = 0; j < numberOfKeywords; j++) {
				String keyword		= topicKeywordProbabilityPairs.get(j).getKey();
				double probability	= topicKeywordProbabilityPairs.get(j).getValue();
				Label label 		= new Label();
				
				/*
				 * Add event listener.
				 */
				
				// For entry into label:
				label.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) 
		            {
		            	Label label = (Label)event.getSource();
		            	
		            	if (!selectedKeyword.equals(label.getText())) {
		            		selectedKeyword	= label.getText();
		            		
			            	// Update visualizations.
			            	drawBridges();
		            	}
		            }
		        });
				// For exit from label:
				label.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
		            public void handle(MouseEvent event) 
		            {
		            }
		        });
				
				//label.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
				label.setText(keyword);
				vbox.getChildren().add(label);
				
				// Update probability sums.
				// Modify this statement to enable different approaches to ranking font sizes.
				probabilitySum += Math.pow(probability, keywordProbabilityExponent);
				
				// Update probability sum for this keyword over all topics.
				if (!keywordProbabilitySumsOverTopics.containsKey(keyword)) {
					keywordProbabilitySumsOverTopics.put(keyword, probability);
				}
				
				else {
					double newSum = keywordProbabilitySumsOverTopics.get(keyword) + probability;
					keywordProbabilitySumsOverTopics.put(keyword, newSum);
				}
				
				// Check if there are new extrema in keyword probability sums over all topics.
				keywordProbabilitySumOverTopicsMax = keywordProbabilitySumsOverTopics.get(keyword) > keywordProbabilitySumOverTopicsMax ? keywordProbabilitySumsOverTopics.get(keyword) : keywordProbabilitySumOverTopicsMax;
				keywordProbabilitySumOverTopicsMin = keywordProbabilitySumsOverTopics.get(keyword) < keywordProbabilitySumOverTopicsMin ? keywordProbabilitySumsOverTopics.get(keyword) : keywordProbabilitySumOverTopicsMin;
			}
			
			// Set probability sum for this topic.
			probabilitySums.add(probabilitySum);
			
			// Add tag clouds to parent.
			anchorPane.getChildren().add(vbox);
		}
	}
	
	/**
	 * Adjusts font sizes so that they reflect the probability values of their respective keywords in their respective topics.
	 * @param numberOfTopics
	 * @param probabilitySums
	 */
	private void adjustTagFontSizes(final int numberOfTopics, final int numberOfKeywords, final ArrayList<Double> probabilitySums)
	{
		if (anchorPane != null && data.size() > 0 && numberOfTopics > 0 && numberOfKeywords > 0) {
			anchorPane.applyCss();
			anchorPane.layout();
			
			// Determine ratio of current tag-cloud height to available (canvas) height.
			
			// Get first tag cloud (covering the first topic), determine how much room we have to spare.
			VBox firstTagCloud	= tagCloudContainer.get(0);
			double ratio		= canvas.getHeight() / (firstTagCloud.getLayoutBounds().getHeight() * 1.2);
			// Get first label, extract current font size.
			double oldFontsize	= ((Label)firstTagCloud.getChildren().get(0)).getFont().getSize();
			
			// Calculate new number of "font size units" that may be spent so that the vertical axis
			// is filled entirely.
			defaultFontSize						= oldFontsize * ratio;
			final double fontSizeUnitsTotal 	= defaultFontSize * numberOfKeywords;
			
			// Set new font size
			for (int i = 0; i < numberOfTopics; i++) {
				// Get tag cloud.
				VBox tagCloud													= tagCloudContainer.get(i);
				// Get keyword/probability pairs for this tag cloud.
				ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs	= data.get(i);
				// Get probability sum for this topic/tag cloud.
				double currProbabilitySum										= probabilitySums.get(i);

				// Maximal tag width, used as reference to tag centering.
				double maxTagWidth												= 0;
				// Iterate over all labels, calculate the appropriate font size.
				for (int j = 0; j < numberOfKeywords; j++) {
					double probability	= topicKeywordProbabilityPairs.get(j).getValue();
					Font newFont		= new Font(fontSizeUnitsTotal * (Math.pow(probability, keywordProbabilityExponent) / currProbabilitySum));
					
					// Update used font in current tag label.
					Label currLabel 	= ((Label)tagCloud.getChildren().get(j));
					currLabel.setFont(newFont);
					currLabel.autosize();
					
					// Check for widest tag.
					double labelWidth	= currLabel.getWidth();//.getLayoutBounds().getWidth();
					maxTagWidth			= labelWidth > maxTagWidth ? labelWidth : maxTagWidth;
				}
				
				// Redraw GUI.
				tagCloud.applyCss();
				tagCloud.layout();
				anchorPane.applyCss();
				anchorPane.layout();
				
				// Center labels so that they are aligned .
				// Iterate over all labels, calculate the appropriate size.
				for (int j = 0; j < numberOfKeywords; j++) {
					// Update used font in current tag label.
					Label currLabel 	= ((Label)tagCloud.getChildren().get(j));
					
					// Center label.
					double labelWidth	= currLabel.getWidth();//getLayoutBounds().getWidth();
					double diffX		= (maxTagWidth - labelWidth) / 2;

					if (labelWidth != maxTagWidth) {
						currLabel.setTranslateX(diffX);
					}
				}
			}
		}
	}
	
	private void drawBridges()
	{
		drawBridges(this.data, this.numberOfTopics, this.numberOfKeywords);
	}
	
	/**
	 * Draws connection between tag clouds.
	 * @param data
	 * @param numberOfTopics
	 * @param numberOfKeywords
	 */
	private void drawBridges(ArrayList<ArrayList<Pair<String, Double>>> data, final int numberOfTopics, final int numberOfKeywords)
	{
		// Redraw GUI.
		anchorPane.applyCss();
		anchorPane.layout();
		
		// Define default line width in pixel.
		double defaultLineWidth = canvas.getHeight() * (3.0 / 219);
		
		// Get GraphicsContext for canvas, draw background.
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setFill(Color.BLACK);
		
		for (int i = 0; i < numberOfTopics - 1; i++) {
			VBox currTagCloud							= tagCloudContainer.get(i);
			VBox nextTagCloud							= tagCloudContainer.get(i + 1);
			ArrayList<Pair<String, Double>> currData	= data.get(i);
			ArrayList<Pair<String, Double>> nextData	= data.get(i + 1);
			
			// Re-render tag cloud.
			currTagCloud.applyCss();
			currTagCloud.layout();
			
			double currBridgeOffsetX					= currTagCloud.getLayoutX();
			double currBridgeOffsetY					= currTagCloud.getLayoutY() - 3 + defaultLineWidth;
			double nextBridgeOffsetX					= nextTagCloud.getLayoutX();
			double nextBridgeOffsetY					= nextTagCloud.getLayoutY() - 3 + defaultLineWidth;
			
			// For all keywords in the current tag cloud: See if they exist in the next tagcloud too.
			for (int keywordIndex = 0; keywordIndex < numberOfKeywords; keywordIndex++) {
				String keyword		= currData.get(keywordIndex).getKey();
				double probability	= currData.get(keywordIndex).getValue();
				Label currLabel		= (Label)currTagCloud.getChildren().get(keywordIndex);
				
				// Proceed iff (1) no keyword is hovered over right now or (2) the current keyword is hovered over.
				boolean noKeywordSelected 		= selectedKeyword.equals("");
				boolean isThisKeywordSelected	= !selectedKeyword.equals("") && keyword.equals(selectedKeyword);
				
				if ( noKeywordSelected || isThisKeywordSelected ) {
					// Calculate positions of connecting line on left side.				
					double currBridgeX	= currBridgeOffsetX + currLabel.getBoundsInParent().getMaxX() + 5; 
					double currBridgeY	= currBridgeOffsetY + (currLabel.getBoundsInParent().getMinY() + currLabel.getBoundsInParent().getMaxY()) / 2;
					
					// Initialize flag to indicate whether a keyword was found in the next tag cloud.
					boolean wasFound		= false;
					// Search in next tag cloud for this word.
					for (int nextKeywordIndex = 0; nextKeywordIndex < numberOfKeywords && !wasFound; nextKeywordIndex++) {
						String nextKeyword		= nextData.get(nextKeywordIndex).getKey();
						double nextProbability	= nextData.get(nextKeywordIndex).getValue();
						Label nextLabel			= (Label)nextTagCloud.getChildren().get(nextKeywordIndex); 
					
						// Calculate positions of connecting line on right side.
						double nextBridgeX	= nextBridgeOffsetX + nextLabel.getBoundsInParent().getMinX() - 10; 
						double nextBridgeY	= nextBridgeOffsetY + (nextLabel.getBoundsInParent().getMinY() + nextLabel.getBoundsInParent().getMaxY()) / 2;
						
						if (nextKeyword.equals(keyword)) {
							wasFound			= true;
							
							// Calculate proportion from current to next probability.
							double proportion	= probability > nextProbability ? nextProbability / probability : probability / nextProbability; 
							
							// Line width is proportional to the difference in probability between these two topics.
							gc.setLineWidth(defaultLineWidth * Math.pow(proportion, 2));
							
							// Line color is dependent on the overall importance of this keyword.
							final int colorExponent					= 3;
							double adjustedKeywordProbabilityValue 	= Math.pow(keywordProbabilitySumsOverTopics.get(keyword), colorExponent);
							Color color								= ColorScale.getColorForValue(	adjustedKeywordProbabilityValue, 
																									Math.pow(keywordProbabilitySumOverTopicsMin, colorExponent), 
																									Math.pow(keywordProbabilitySumOverTopicsMax, colorExponent), 
																									Color.RED, Color.DARKRED);
							
							// Set color.
							gc.setStroke(color); 
							
							// Draw line.
							gc.strokeLine(currBridgeX, currBridgeY, nextBridgeX, nextBridgeY);
						}
					}
				}
			}
		}
	}

	@Override
	public void resize(double width, double height)
	{
		// Adapt width.
		if (width > 0) {
			canvas.setWidth(width - 5 - 5);
			if (selectedTopicConfigurations.size() == 2) {
				refresh(selectedTopicConfigurations, maxNumberOfKeywords, numberOfKeywords, false);
			}
		}
		
		// Adapt height.
		if (height > 0) {
			canvas.setHeight(height - 5);
			if (selectedTopicConfigurations.size() == 2) {
				refresh(selectedTopicConfigurations, maxNumberOfKeywords, numberOfKeywords, false);
			}
		}
	}

	@Override
	public void reset(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords, int numberOfKeywords)
	{
		// Update reference to data collections.
		this.selectedFilteredLDAConfigurations = selectedFilteredLDAConfigurations;
		
		// Clear tag clouds.
		if (tagCloudContainer != null) {
			for (VBox vbox : tagCloudContainer) {
				anchorPane.getChildren().remove(vbox);
			}
			
			tagCloudContainer.clear();
		}
		
		// Clear canvas.
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Update settings.
		this.maxNumberOfTopics		= maxNumberOfTopics;
		this.numberOfTopics			= numberOfTopics;
		// Temporary solution: 
		this.maxNumberOfKeywords	= maxNumberOfKeywords;
		this.numberOfKeywords		= numberOfKeywords;
	}
	
	@Override
	public void clear()
	{
		// Reset LDA configuration collection.
		this.selectedFilteredLDAConfigurations = null;
		
		// Clear tag clouds.
		if (tagCloudContainer != null) {
			for (VBox vbox : tagCloudContainer) {
				anchorPane.getChildren().remove(vbox);
			}
			
			tagCloudContainer.clear();
		}
		
		// Clear canvas.
		canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Update settings.
		this.maxNumberOfTopics		= 0;
		this.numberOfTopics			= 0;
		// Temporary solution: 
		this.maxNumberOfKeywords	= 0;
		this.numberOfKeywords		= 0;
	}

	@Override
	protected void updateData()
	{	
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
}
