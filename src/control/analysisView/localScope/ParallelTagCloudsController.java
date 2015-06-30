package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import model.LDAConfiguration;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
	private ArrayList<VBox> tagCloudContainer;
	
	private double defaultFontSize;
	
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
	 * List (for this LDA configuration) of lists (of topics) of keyword/probability pairs.
	 * To be adapted for multiple LDA configurations (? - how?. 
	 */
	private ArrayList<ArrayList<Pair<String, Double>>> data;
	
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_ParallelTagCloudsController.");
		
		// Set visualization type.
		this.visualizationType	= LocalScopeVisualizationType.PARALLEL_TAG_CLOUDS;
		
		// Set inital index value.
		currentIndex			= 0;
	}

	@Override
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords, int numberOfKeywords, boolean updateData)
	{
		// Reset visualization.
		reset(maxNumberOfTopics, numberOfTopics, maxNumberOfKeywords, numberOfKeywords);
	
		System.out.println("\nmaxTopics = " + maxNumberOfTopics + ", maxKeywords = " + maxNumberOfKeywords);
		System.out.println("topics = " + numberOfTopics + ", keywords = " + numberOfKeywords);
		

		
		// Update reference to data collections.
		this.selectedFilteredLDAConfigurations = selectedFilteredLDAConfigurations;
		
		// This type of visualization is reasonable for one LDA configuration
		// at the same time only, therefore we use only one selected LDAConfiguration.
		if (selectedFilteredLDAConfigurations.size() > 0)  {
			// Get list (for this LDA configuration) of lists (of topics) of keyword/probability pairs.
			if (updateData)
				data = workspace.getDatabaseManagement().getLimitedKITData(selectedFilteredLDAConfigurations.get(currentIndex), maxNumberOfTopics, maxNumberOfKeywords);
			
			/*
			 * Create tag clouds, fill them with data.
			 */
			
		    // Check if the actually available number of topics is smaller than the required one.
		    // If so: Adapt number of topics used.
		    System.out.println("data.size = " + data.size());
		    this.numberOfTopics = data.size() < numberOfTopics ? data.size() : numberOfTopics;  
		    numberOfTopics		= this.numberOfTopics;
		    
		    System.out.println("numberOfTopics = " + numberOfTopics);
		    
		    // Init tag cloud container.
		    double intervalX					= canvas.getWidth() / numberOfTopics;
		    // Probability sums for each topic.
		    ArrayList<Double> probabilitySums	= new ArrayList<Double>(numberOfTopics);
		    tagCloudContainer					= new ArrayList<VBox>(numberOfTopics);
		    
		    // Iterate over all topics; create cloud for each one.
			for (int i = 0; i < numberOfTopics; i++) {
				ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs = data.get(i);
				
				VBox vbox = new VBox();
				vbox.setLayoutX(canvas.getLayoutX() + i * intervalX + 10);
				vbox.setLayoutY(canvas.getLayoutY() + 55);
				tagCloudContainer.add(vbox);
				
				// Init labels to container.
				double probabilitySum = 0;
				for (int j = 0; j < numberOfKeywords; j++) {
					Label label = new Label();
					label.setText(topicKeywordProbabilityPairs.get(j).getKey());
					vbox.getChildren().add(label);
					
					// Update probability sums.
					probabilitySum += topicKeywordProbabilityPairs.get(j).getValue();
				}
				
				// Set probability sum for this topic.
				probabilitySums.add(probabilitySum);
				
				// Add tag clouds to parent.
				anchorPane.getChildren().add(vbox);
			}
			
			/*
			 * Adjust tag font sizes. 
			 */

			if (anchorPane != null && data.size() > 0 && numberOfTopics > 0) {
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
				defaultFontSize				= oldFontsize * ratio;
				double fontSizeUnitsTotal 	= defaultFontSize * numberOfKeywords;
				
				// Set new font size
				for (int i = 0; i < numberOfTopics; i++) {
					// Get tag cloud.
					VBox tagCloud													= tagCloudContainer.get(i);
					// Get keyword/probability pairs for this tag cloud.
					ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs	= data.get(i);
					// Get probability sum for this topic/tag cloud.
					double currProbabilitySum										= probabilitySums.get(i);

					// Iterate over all labels, calculate the appropriate size.
					for (int j = 0; j < numberOfKeywords; j++) {
						double probability	= topicKeywordProbabilityPairs.get(j).getValue();
						Font newFont		= new Font(fontSizeUnitsTotal * (probability / currProbabilitySum));
						
						// Update used font in current tag label.
						Label currLabel = ((Label)tagCloud.getChildren().get(j));
						currLabel.setFont(newFont);
					}
				}
				
				System.out.println("ratio = " + ratio);
			}
		}
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
//		gc.setFill(Color.GREEN);
//	    gc.setStroke(Color.BLUE);
//	    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	    

		
	}

	@Override
	public void resize(double width, double height)
	{
		System.out.println(width + " / " + height);
		if (width > 0) {
			canvas.setWidth(width - 5 - 5);
			refresh(selectedFilteredLDAConfigurations, maxNumberOfTopics, numberOfTopics, maxNumberOfKeywords, numberOfKeywords, false);
		}
		
		// Adapt height.
		if (height > 0) {
			canvas.setHeight(height - 40 - 5);
			refresh(selectedFilteredLDAConfigurations, maxNumberOfTopics, numberOfTopics, maxNumberOfKeywords, numberOfKeywords, false);
		}
	}

	@Override
	public void reset(int maxNumberOfTopics, int numberOfTopics, int maxNumberOfKeywords, int numberOfKeywords)
	{
		if (tagCloudContainer != null) {
			for (VBox vbox : tagCloudContainer) {
				anchorPane.getChildren().remove(vbox);
			}
			
			tagCloudContainer.clear();
		}
		
		// Update settings.
		this.maxNumberOfTopics		= maxNumberOfTopics;
		this.numberOfTopics			= numberOfTopics;
		// Temporary solution: 
		this.maxNumberOfKeywords	= maxNumberOfKeywords;
		this.numberOfKeywords		= numberOfKeywords;
	}

	@Override
	protected void updateData()
	{
		// @todo Auto-generated method stub
		
	}
}
