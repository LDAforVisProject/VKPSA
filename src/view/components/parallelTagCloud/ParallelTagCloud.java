package view.components.parallelTagCloud;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.workspace.TaskType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

/**
 * Component used for detail view of topic data.
 * Shows n most relevant keyword for all specified topics. 
 * @author RM
 *
 */
public class ParallelTagCloud extends VisualizationComponent
{
	/**
	 * AnchorPane containing actual tag cloud.
	 */
	private @FXML AnchorPane tagcloud_anchorpane;
	/**
	 * AnchorPane containing probability distribution barchart..
	 */
	private @FXML AnchorPane barchart_anchorpane; 
	
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
	
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing ParallelTagCloud component.");
		
		// Initialize prob. dist. barchart.
		initProbabilityDistributionBarchart();
		
		// Add resize listeners.
		addResizeListeners();
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
		
		System.out.println("refreshing after data was loaded");
		
		if (this.options != null && this.data != null) {
		
			
			// Refresh probability distribution barchart.
			refreshBarchart(ptcData, ptcOptions);
		}
	}
	
	/**
	 * Refreshes barchart. Adds all available data to it. 
	 * @param data
	 * @param options
	 */
	private void refreshBarchart(ParallelTagCloudDataset data, ParallelTagCloudOptionset options)
	{
		// Clear data.
		probabilityDistribution_barchart.getData().clear();
		
		// Add new data to chart.
		for (Pair<Integer, Integer> topicConfig : data.getKeywordProbabilities().keySet()) {
			// Allocate series object, set name.
			XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
			series.setName(String.valueOf(topicConfig.getKey()) + "#" + String.valueOf(topicConfig.getValue()));
			
			System.out.println(topicConfig);
			
			// Loop through all n requested keyword/probability pairs.
			
			// Add all keyword/probability pairs to data series.
			// Sort by priority for each topic (as opposed to alphabetically).
			// Important: One bar most probably represents different keywords for different topics.
			// Get data.
			ArrayList<Pair<String, Double>> keywordProbabilityData = data.getKeywordProbabilities().get(topicConfig);
			// Calculate start index.
			final int startIndex = options.getNumberOfKeywordsToDisplay() <= keywordProbabilityData.size() ? options.getNumberOfKeywordsToDisplay() - 1 : keywordProbabilityData.size() - 1; 
			System.out.println("si = " + startIndex);
			// Iterate over keyword/probablity pairs.
			for (int i = startIndex; i >= 0; i--) {
				Pair<String, Double> keywordProbPair = keywordProbabilityData.get(i);
				
				series.getData().add(new XYChart.Data<Number, String>(	keywordProbPair.getValue(), 
																		"#" + String.valueOf( i + 1 ))
				);
				System.out.println("\t" + (keywordProbabilityData.size() - i) + " -> " + keywordProbPair.getValue());
			}
			
			// Add data series to barchart.
			probabilityDistribution_barchart.getData().add(series);
		}
		
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// @todo Implement ParallelTagCloud::processSelectionManipulationRequest(...).
		
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// @todo Implement ParallelTagCloud::processEndOfSelectionManipulation(...).
		
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		// @todo Implement ParallelTagCloud::provideOffsets(...).
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
		// @todo Implement ParallelTagCloud::processKeyPressedEvent(...).
		
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
		// @todo Implement ParallelTagCloud::processKeyReleasedEvent(...).
		
	}

	@Override
	public void initHoverEventListeners()
	{
		// @todo Implement ParallelTagCloud::initHoverEventListener(...).
		
	}

	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		// @todo Implement ParallelTagCloud::highlightHoveredOverDataPoints(...).
		
	}

	@Override
	public void removeHoverHighlighting()
	{
		// @todo Implement ParallelTagCloud::removeHoverHighlighting(...).
		
	}

	@Override
	public void resizeContent(double width, double height)
	{
		probabilityDistribution_barchart.setPrefWidth(barchart_anchorpane.getWidth());
		probabilityDistribution_barchart.setPrefHeight(barchart_anchorpane.getHeight());
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
