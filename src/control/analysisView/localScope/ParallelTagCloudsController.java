package control.analysisView.localScope;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import model.LDAConfiguration;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class ParallelTagCloudsController extends LocalScopeVisualizationController
{
	/**
	 * Canvas used to paint connections between words.
	 */
	protected @FXML Canvas canvas;
	private ArrayList<VBox> tagCloudContainer;
	
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
	public void refresh(ArrayList<LDAConfiguration> selectedFilteredLDAConfigurations, int maxNumberOfTopics, int maxNumberOfKeywords, boolean updateData)
	{
		// Reset visualization.
		clear();
	
		System.out.println("maxTopics = " + maxNumberOfTopics + ", maxKeywords = " + maxNumberOfKeywords);
		
		// Update settings.
		this.maxNumberOfTopics		= maxNumberOfTopics;
		this.maxNumberOfKeywords	= maxNumberOfKeywords;
		
		// Update reference to data collections.
		this.selectedFilteredLDAConfigurations = selectedFilteredLDAConfigurations;
		
		// Update data. This type of visualization is reasonable for one LDA configuration
		// at the same time only, therefore we use only one selected LDAConfiguration.
		
		if (selectedFilteredLDAConfigurations.size() > 0)  {
			// Get list (for this LDA configuration) of lists (of topics) of keyword/probability pairs.
			if (updateData)
				data = workspace.getDatabaseManagement().getLimitedKITData(selectedFilteredLDAConfigurations.get(currentIndex), maxNumberOfTopics, maxNumberOfKeywords);
			
//			int topicID = 0;
//			for (ArrayList<Pair<String, Double>> topicList : data) {
//				System.out.println(topicID++ + " - .size = " + topicList.size());
//				
//				for (Pair<String, Double> keywordPair : topicList) {
//					System.out.println("\t" + keywordPair.getKey() + " -> " + keywordPair.getValue());
//				}
//			}
//			
		    // Init tag cloud container.
		    double intervalX	= canvas.getWidth() / data.size();
		    tagCloudContainer	= new ArrayList<VBox>(data.size());
		    
		    System.out.println("data.size = " + data.size());
		    
		    // Iterate over all topics; create cloud for each one.
			for (int i = 0; i < data.size(); i++) {
				ArrayList<Pair<String, Double>> topicKeywordProbabilityPairs = data.get(i);
				
				VBox vbox = new VBox();
				vbox.setLayoutX(canvas.getLayoutX() + i * intervalX + 10);
				vbox.setLayoutY(canvas.getLayoutY() + 55);
				tagCloudContainer.add(vbox);
				
				// Init labels to container.
				for (int j = 0; j < topicKeywordProbabilityPairs.size(); j++) {
					Label label = new Label();
					label.setText(topicKeywordProbabilityPairs.get(j).getKey());
					vbox.getChildren().add(label);
				}
				
				anchorPane.getChildren().add(vbox);
			}
		}
		
		GraphicsContext gc = canvas.getGraphicsContext2D();
//		
//		gc.setFill(Color.GREEN);
//	    gc.setStroke(Color.BLUE);
//	    gc.fillRect(5, 5, canvas.getWidth() - 10, canvas.getHeight() - 10);
	    

		
	}

	@Override
	public void resize(double width, double height)
	{
		if (width > 0) {
			canvas.setWidth(width);
			refresh(selectedFilteredLDAConfigurations, maxNumberOfTopics, maxNumberOfKeywords, false);
		}
		
		// Adapt height.
		if (height > 0) {
			canvas.setHeight(height - 35);
			refresh(selectedFilteredLDAConfigurations, maxNumberOfTopics, maxNumberOfKeywords, false);
		}
	}

	@Override
	public void clear()
	{
		if (tagCloudContainer != null) {
			for (VBox vbox : tagCloudContainer) {
				anchorPane.getChildren().remove(vbox);
			}
			
			tagCloudContainer.clear();
		}
	}

	@Override
	protected void updateData()
	{
		// @todo Auto-generated method stub
		
	}
}
