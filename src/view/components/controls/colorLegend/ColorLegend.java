package view.components.controls.colorLegend;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.RangeSlider;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import model.workspace.TaskType;
import model.workspace.tasks.ITaskListener;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;

/**
 * Displays color scale for given range of values and selected colors.
 * Is embedded in other components.
 * @author RM
 *
 */
public class ColorLegend extends VisualizationComponent
{
	/*
	 * GUI elements.
	 * 
	 */
	
	/**
	 * Image of color legend.
	 */
	private ImageView legend;
	
	/**
	 * Label for min. value.
	 */
	private Label minLabel;
	/**
	 * Label for max. value.
	 */
	private Label maxLabel;
	
	/**
	 * RangeSlider for manually defined cutoff.
	 */
	private RangeSlider slider;
	
	/**
	 * Rectangle representing border of actual legend.
	 */
	private Rectangle legendBorder;
	
	/**
	 * Histogram displaying distribution over value range.
	 */
	private BarChart<Number, String> histogram;
	/**
	 * X-axis for prob. dist. barchart. 
	 */
	private NumberAxis histogram_xAxis;
	/**
	 * Y-Axis for prob. dist. barchart..
	 */
    private CategoryAxis histogram_yAxis;
    
	/*
	 * Data.
	 */
	
	/**
	 * Dataset holding all relevant data.
	 */
	private ColorLegendDataset data;
	
	/**
	 * Listener to notify once slider value has changed.
	 */
	private ITaskListener listener;
	
	/*
	 * Metadata.
	 */
	
	private int legendWidth;
	private int legendHeight;
	private int legendOffsetX;
	
	
	public ColorLegend(ITaskListener listener)
	{
		System.out.println("Creating ColorLegend.");
		
		// Remember listener.
		this.listener = listener;

		// Initialize root node.
		initRootNode();
		
		// Initialize labels.
		initLabels();
		
		// Initialize range slider.
		initRangeSlider();
		
		// Initialize legend border shape.
		initLegendBorder();
		
		// Initialize histogram.
		initHistogram();
		
		// Set preferable width for legend.
		legendWidth		= 7;
		legendOffsetX 	= 3;
	}
	
	private void initHistogram()
	{
        histogram_xAxis = new NumberAxis();
        histogram_yAxis = new CategoryAxis();
        histogram	 	= new BarChart<Number, String>(histogram_xAxis, histogram_yAxis);
        histogram.setId("colorLegend_histogram");
        
        histogram.setAnimated(false);
        histogram.setLegendVisible(false);  
        histogram.setBackground(Background.EMPTY);
        
        histogram_xAxis.setTickMarkVisible(false);
        histogram_yAxis.setTickMarkVisible(false);
        histogram_xAxis.setMinorTickVisible(false);
		
        // Hide ticks.
        histogram_xAxis.setMinorTickVisible(false);
        histogram_xAxis.setTickLabelsVisible(false);
        histogram_yAxis.setTickLabelsVisible(false);
        
        // Set width.
        histogram.setPrefWidth(60);
        histogram.setMaxWidth(60);
        histogram.setMinWidth(60);
        
        // Hide background.
        histogram.setBackground(Background.EMPTY);
        histogram.setBorder(Border.EMPTY);
        histogram.setStyle("-fx-background-color: #ffffff;");
        histogram_xAxis.setStyle("-fx-background-color: #ffffff;");
        histogram_yAxis.setStyle("-fx-background-color: #ffffff;");
        
        // Adjust gaps.
        histogram.setBarGap(0);
        histogram.setCategoryGap(0);
        histogram_yAxis.setGapStartAndEnd(false);
        
        // Hide lines.
        histogram.setHorizontalGridLinesVisible(false);
        histogram.setVerticalGridLinesVisible(false);
        histogram.setHorizontalZeroLineVisible(true);
        histogram.setVerticalZeroLineVisible(false);
        
        // Set offset.
        histogram.setTranslateX(-10);
        histogram.setTranslateY(-15);
        
		// Ensure resizability of barchart.
		AnchorPane.setTopAnchor(histogram, 0.0);
		AnchorPane.setBottomAnchor(histogram, 0.0);
		AnchorPane.setLeftAnchor(histogram, 0.0);
		AnchorPane.setRightAnchor(histogram, 0.0);
	}
	
	private void initLegendBorder()
	{
		legendBorder = new Rectangle();
		legendBorder.setStroke(Color.GREY);
		legendBorder.setFill(Color.TRANSPARENT);
	}

	private void initRootNode()
	{
		// Create root node.
		this.rootNode = new AnchorPane();
	}
	
	private void initRangeSlider()
	{
		slider = new RangeSlider();

		slider.setMajorTickUnit(5);
		slider.setMinorTickCount(0);
		
		slider.setSnapToTicks(false);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(false);
		
		slider.setOrientation(Orientation.VERTICAL);
				
		// Initialize event handler.
		initSliderEventHandler();
	}
	
	/**
	 * Initializes event handler for slider.
	 */
	private void initSliderEventHandler()
	{
		// Add listener to refresh during drag.
		slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	refresh();
            }
        });
		
		// Add listener to notify listener after release.
		slider.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	listener.notifyOfTaskCompleted(TaskType.COLOR_LEGEND_MODIFIED);
            }
        });
	}
	
	private void initLabels()
	{
		minLabel 		= new Label();
		maxLabel 		= new Label();
		
		// Add to parent.
		((AnchorPane) rootNode).getChildren().add(minLabel);
		((AnchorPane) rootNode).getChildren().add(maxLabel);
		
		// Set position.
		minLabel.setLayoutX(25);
		maxLabel.setLayoutX(25);
		
		// Set style.
		minLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 9));
		maxLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 9));
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
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

	/**
	 * Refresh legend using the provided colours and values.
	 * @param data
	 */
	public void refresh(ColorLegendDataset data)
	{
		// Reset slider, if data set is new.
		if (this.data == null || !this.data.equals(data)) {
			// Reset slider to initial values.
			slider.setHighValue(slider.getMax());
			slider.setLowValue(slider.getMin());
		}

		// Update data set.
		this.data = data;

		// Refresh color legend.
		this.refresh();
	}
	
	/**
	 * Update legend after refresh.
	 */
	private void updateLegend()
	{
		AnchorPane parent = (AnchorPane) rootNode;
		
		// Remove old ImageView from root, if one exists.
		if (legend != null && parent.getChildren().contains(legend)) {
			// Remove legend.
			parent.getChildren().remove(legend);
		}
		
		// Add histogram, if not yet added.
		if (histogram != null && !parent.getChildren().contains(histogram)) {
			parent.getChildren().add(histogram);
			histogram.toBack();
		}
		
		// Calculate percentage of legend selected with slider.
		double percentageSelected 	= (slider.getHighValue() - slider.getLowValue()) 	/ (slider.getMax() - slider.getMin());
		double legendOffsetY		= (slider.getMax() - slider.getHighValue()) 		/ (slider.getMax() - slider.getMin()) * legendHeight;
		
		// Create new legend
		legend = ColorScale.createColorScaleImageView(	data.getMin(), data.getMax(), data.getMinColor(), data.getMaxColor(),
														legendWidth, (int)(legendHeight * percentageSelected), Orientation.VERTICAL);
		legend.setLayoutX(legendOffsetX);
		legend.setLayoutY(legendOffsetY);
		
		// Add to root.
		parent.getChildren().add(legend);
	}
	
	/**
	 * Update slider after refresh.
	 */
	private void updateSlider()
	{
		if ( !((AnchorPane) rootNode).getChildren().contains(slider) ) {
			// Add to parent.
			((AnchorPane) rootNode).getChildren().add(slider);
		}
		
		// Set new values.
		slider.setMax(data.getMax());
		slider.setMin(data.getMin());
		
		// Resize.
		slider.setPrefHeight(legendHeight);
		
		// Push to front.
		slider.toFront();
	}
	
	/**
	 * Update labels after refresh.
	 */
	private void updateLabels()
	{
		// Display extrema in lables.
		minLabel.setText( String.valueOf(data.getMin()));
		maxLabel.setText( String.valueOf(data.getMax()).substring(0, 5));
		
		// Reposition labels.
		minLabel.setLayoutY(legendHeight - 15);
		maxLabel.setLayoutY(5);	
	}
	
	@Override
	public void refresh()
	{
		if (this.data != null) {
			// Update legend.
			updateLegend();
			
			// Update labels.
			updateLabels();
			
			// Update legend border.
			updateLegendBorder();
			
			// Update slider.
			updateSlider();
			
			// Update histogram.
			updateHistogram();
		}
	}
	
	/**
	 * Update histogram with new data.
	 */
	private void updateHistogram()
	{
		// Set new height.
		histogram.setMinHeight(legendHeight + 40);
		histogram.setPrefHeight(legendHeight + 40);
		histogram.setMaxHeight(legendHeight + 40);
		
		/*
		 * Insert data.
		 */
		
		// Allocate data series.
		XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
		
		// Bin data.
		int counter = 0;
		for (double binValue : data.getBinList()) {
			XYChart.Data<Number, String> binData = new XYChart.Data<Number, String>(binValue, String.valueOf(counter));
			binData.setExtraValue((double)counter++ / data.getBinList().length);
			series.getData().add(binData);
		}
		
		// Re-add data series.
		histogram.getData().clear();
		histogram.getData().add(series);
		
		colorHistogramBarchart(histogram);
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
		if (height > 0) {
			// Resize root node/container.
			((AnchorPane)this.rootNode).setPrefSize(width, height);
			
			// Remember new height.
			legendHeight = height > 0 ? (int) height : legendHeight;
			
			// Set new histogram width.
			histogram.setPrefWidth(60);
	        histogram.setMaxWidth(60);
	        histogram.setMinWidth(60);
			
			// Redraw legend.
			refresh();
		}
	}

	private void updateLegendBorder()
	{
		if ( !((AnchorPane) rootNode).getChildren().contains(legendBorder) ) {
			// Add to parent.
			((AnchorPane) rootNode).getChildren().add(legendBorder);
		}
		
		legendBorder.setLayoutX(legendOffsetX);
		
		legendBorder.setWidth(legendWidth);
		legendBorder.setHeight(legendHeight);
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	/**
	 * Provide selected extrema.
	 * @return
	 */
	public Pair<Double, Double> getSelectedExtrema()
	{
		return new Pair<Double, Double>(slider.getLowValue(), slider.getHighValue());
	}

	/**
	 * Colours a histogram barchart in the desired colors.
	 * Draws information from node.getUserData() to determine colour of bar.
	 * @param barchart Bar chart to color.
	 */
	private void colorHistogramBarchart(BarChart<Number, String> barchart)
	{
		for (XYChart.Series<Number, String> series : histogram.getData()) {
			for (XYChart.Data<Number, String> dataPoint : series.getData()) {
				// Get corresponding color.
				Color color = Color.LIGHTGREY;
				// Use grey color, if bin is outside of selected range.
				double lowSlider_relativePosition 	= slider.getLowValue() / slider.getMax();
				double highSlider_relativePosition	= slider.getHighValue() / slider.getMax();
				
				if (	(double)dataPoint.getExtraValue() <= highSlider_relativePosition && 
						(double)dataPoint.getExtraValue() >= lowSlider_relativePosition) {
					color = ColorScale.getColorForValue(	(double)dataPoint.getExtraValue(),
															lowSlider_relativePosition, highSlider_relativePosition, 
															data.getMinColor(), data.getMaxColor());
				}
				
				String hex = String.format("#%02x%02x%02x", (int)(color.getRed() * 255), (int)(color.getGreen() * 255), (int)(color.getBlue() * 255));
				// Set color.
				dataPoint.getNode().setStyle("-fx-bar-fill: " + hex + ";");
			}	
		}
	}
}
