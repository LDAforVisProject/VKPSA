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
	
	final static String austria = "Austria";
    final static String brazil = "Brazil";
    final static String france = "France";
    final static String italy = "Italy";
    final static String usa = "USA";
    
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
        probDistBarchart_yAxis.setLabel("Keyword");        
 
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("2003");       
        series1.getData().add(new XYChart.Data(25601.34, austria));
        series1.getData().add(new XYChart.Data(20148.82, brazil));
        series1.getData().add(new XYChart.Data(10000, france));
        series1.getData().add(new XYChart.Data(35407.15, italy));
        series1.getData().add(new XYChart.Data(12000, usa));      
        
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("2004");
        series2.getData().add(new XYChart.Data(57401.85, austria));
        series2.getData().add(new XYChart.Data(41941.19, brazil));
        series2.getData().add(new XYChart.Data(45263.37, france));
        series2.getData().add(new XYChart.Data(117320.16, italy));
        series2.getData().add(new XYChart.Data(14845.27, usa));  
        
        XYChart.Series series3 = new XYChart.Series();
        series3.setName("2005");
        series3.getData().add(new XYChart.Data(45000.65, austria));
        series3.getData().add(new XYChart.Data(44835.76, brazil));
        series3.getData().add(new XYChart.Data(18722.18, france));
        series3.getData().add(new XYChart.Data(17557.31, italy));
        series3.getData().add(new XYChart.Data(92633.68, usa));  
        
        probabilityDistribution_barchart.getData().addAll(series1, series2, series3);
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
	 * Refresh visualization, given a generic, preprocessed dataset.
	 * @param options
	 * @param data
	 */
	public void refresh(ParallelTagCloudOptionset options, ParallelTagCloudDataset data)
	{
		this.options 		= options;
		this.data			= data;
    	
		System.out.println("loading data");
		// Fetch keyword/probability data from database.
		data.fetchTopicProbabilityData(this, workspace);
	}
	
	/**
	 * Refreshes heatmap 
	 */
	@Override
	public void refresh()
	{
		System.out.println("refreshing after data was loaded");
		
		if (this.options != null && this.data != null) {
			
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
