package view.components.scentedFilter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.AnalysisDataspace;
import model.LDAConfiguration;

import org.controlsfx.control.RangeSlider;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import view.components.VisualizationComponent;
import view.components.rubberbandselection.RubberBandSelection;

public class ScentedFilter extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	private @FXML Label param_label;
	private @FXML TextField min_textfield;
	private @FXML TextField max_textfield;
	
	/**
	 * VBox containing barchart and slider.
	 */
	private @FXML VBox container_vbox;
	
	/**
	 * Barchart displaying histogram.
	 */
	private @FXML StackedBarChart<String, Integer> barchart;
	
	/**
	 * Range slider for value interval control.
	 */
	private RangeSlider rangeSlider;
	/**
	 * Common slider for value control.
	 */
	private Slider slider;
	
	/*
	 * Data.
	 */
	
	/**
	 * Options for this scented filter.
	 */
	private ScentedFilterOptionset options;
	
	/**
	 * Data for this scented filter.
	 */
	private ScentedFilterDataset data;
	
	/**
	 * Auxiliary variable used for check of extrema adjustments.
	 */
	private boolean isAdjustedToExtrema;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing ScentedFilter component.");
		
		// Init data.
		isAdjustedToExtrema	= false;
		rangeSlider			= null;
		slider 				= null;
		
		// Adjust arrangement of barchart.
		barchart.setTranslateY(10);
		barchart.setAnimated(false);
		
		// Init rubberband selection.
		initSelection();
	}
	
	private void initSelection()
	{
		// Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) barchart.getParent(), this);	
	}
	
	/**
	 * Initialize/apply options on this scented filter.
	 * @param options
	 */
	public void applyOptions(ScentedFilterOptionset options)
	{
		this.options = options;
		
		// Update label ID.
		param_label.setText( LDAConfiguration.getSymbolForParameter(options.getParamID()) );
		
		// Configure used slider.
		if (options.useRangeSlider()) {
			rangeSlider = new RangeSlider();
			
			rangeSlider.setMax(options.getMax());
			rangeSlider.setLowValue(0);
			rangeSlider.setHighValue(options.getMax());
			
			rangeSlider.setMajorTickUnit(5);
			rangeSlider.setMinorTickCount(4);
			
			rangeSlider.setSnapToTicks(false);
			rangeSlider.setShowTickLabels(true);
			rangeSlider.setShowTickMarks(true);
			
			// Move upwards.
			rangeSlider.setTranslateY(0);
			rangeSlider.setTranslateX(2);
			
			// Add event handler - trigger update of visualizations (and the
			// data preconditioning necessary for that) if filter settings
			// are changed.
			addEventHandlerToRangeSlider(rangeSlider);
			
			// Add to GUI.
			container_vbox.getChildren().add(rangeSlider);
		}
		
		else {
			slider = new Slider();
			
			slider.setMin(options.getMin());
			slider.setMax(options.getMax());
			slider.setValue(options.getMax());
			
			slider.setMajorTickUnit(5);
			slider.setMinorTickCount(4);
			
			slider.setSnapToTicks(true);
			slider.setShowTickLabels(true);
			slider.setShowTickMarks(true);
			
			// Move upwards.
			slider.setTranslateY(-14);
			slider.setTranslateX(2);
			
			// Add event handler - trigger update of visualizations (and the
			// data preconditioning necessary for that) if filter settings
			// are changed.
			addEventHandlerToSlider(slider);
			
			// Add to GUI.
			container_vbox.getChildren().add(slider);
		}
	}
	
	/**
	 * Adds event handler processing slide and other events to a specified range slider.
	 * @param rs
	 * @param parameter
	 */
	private void addEventHandlerToRangeSlider(RangeSlider rs)
	{
		// Add listener to determine position during after release.
		rs.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs);

            	// Filter by values; refresh visualizations.
            	analysisController.refreshVisualizations(true);
            }
        });
		
		// Add listener to determine position during mouse drag.
		rs.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(rs);
            };
        });
	}
	
	/**
	 * Adds event handler processing slide and other events to a specified range slider.
	 * @param rs
	 * @param parameter
	 */
	private void addEventHandlerToSlider(Slider s)
	{
		// Add listener to determine position during after release.
		s.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(s);

            	// Filter by values; refresh visualizations.
            	analysisController.refreshVisualizations(true);
            }
        });
		
		// Add listener to determine position during mouse drag.
		s.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	// Update control values after slider values where changed.
            	updateControlValues(s);
            };
        });
	}
	
	/**
	 * Updates control values after slide event ended.
	 * @param rs
	 */
	private void updateControlValues(RangeSlider rs)
	{
		double parameterValue_low	= rs.getLowValue() 	>= rs.getMin() ? rs.getLowValue() 	: rs.getMin();
		double parameterValue_high	= rs.getHighValue() <= rs.getMax() ? rs.getHighValue() 	: rs.getMax();
		
		// Update textfield values.
		min_textfield.setText(String.valueOf(parameterValue_low));
    	max_textfield.setText(String.valueOf(parameterValue_high));            		
	}
	
	/**
	 * Updates control values after slide event ended.
	 * @param rs
	 */
	private void updateControlValues(Slider s)
	{
		double parameterValue	= s.getValue() >= s.getMin() && s.getValue() <= s.getMax()? s.getValue() : 0;
		
		// Update textfield values.
		min_textfield.setText(String.valueOf(parameterValue));
    	max_textfield.setText(String.valueOf(parameterValue));            		
	}

	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		// @todo Auto-generated method stub
		return null;
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
		// @todo Auto-generated method stub
		
	}

	@Override
	public void refresh()
	{
		this.refresh(this.data);
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}


	@Override
	public void resizeContent(double width, double height)
	{
		if (width > 0) {
			final double delta	= 140;
			double newWidth 	= width - delta > 0 ? width - delta : 0;
			
			barchart.setPrefWidth(newWidth);
			barchart.setMinWidth(newWidth);
			barchart.setPrefWidth(newWidth);
			
			if (options.useRangeSlider()) {			
				rangeSlider.setPrefWidth(newWidth);
				rangeSlider.setMinWidth(newWidth);
				rangeSlider.setMaxWidth(newWidth);
			}
			else {
				slider.setPrefWidth(newWidth);
				slider.setMinWidth(newWidth);
				slider.setMaxWidth(newWidth);
			}
		}
	}
	
	/**
	 * Refresh parameter filter control histograms.
	 * @param numberOfBins
	 */
	public void refresh(ScentedFilterDataset data)
	{
		this.data = data;

		/*
		 * Bin data.
		 */
		
		ArrayList<int[]> binnedData = data.binData(	options.getParamID(), options.getNumberOfBins(),
													options.useRangeSlider() ? rangeSlider.getMin() : slider.getMin(),
													options.useRangeSlider() ? rangeSlider.getMax() : slider.getMax());
		
		/*
		 * Adjust controls to new extrema.
		 */
		if (!isAdjustedToExtrema) {
			adjustControlExtrema(data.getLDAConfigurations());
			isAdjustedToExtrema = true;
		}
			
		
		/*
		 * Draw data.
		 */
		
		// Clear old data.
		barchart.getData().clear();

		// Add inactive data series to barcharts.
		addParameterHistogramDataSeries(binnedData.get(0), options.getNumberOfBins(), 0);
		
		// Add discarded data series to barcharts.
		addParameterHistogramDataSeries(binnedData.get(2), options.getNumberOfBins(), 1);
		
		// Add active data series to barcharts.
		addParameterHistogramDataSeries(binnedData.get(1), options.getNumberOfBins(), 2);
	}
	
	/**
	 * Adjusts minimal and maximal control values so that they fit the loaded data set.
	 */
	public void adjustControlExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		if (options != null) {
			Map<String, Pair<Double, Double>> ldaParameterExtrema = AnalysisDataspace.identifyLDAParameterExtrema(ldaConfigurations, options.getParamID());
			System.out.println("adjusting");
			// Update values of range slider. 
			String param	= options.getParamID();
			double min		= ldaParameterExtrema.get(param).getKey();
			double max		= ldaParameterExtrema.get(param).getValue();
			
			// Set range slider values.
			if (options.useRangeSlider()) {
				rangeSlider.setMin(min);
				rangeSlider.setMax(max);
			}
			else {
				slider.setMin(min);
				slider.setMax(max);
			}
			
			// Update text values.
			min_textfield.setText(String.valueOf(min));
			max_textfield.setText(String.valueOf(max));
		}
		
		isAdjustedToExtrema = true;
	}
	/**
	 * Generates parameter histogram in/for specified barcharts.
	 * @param paramterBinLists
	 * @param numberOfBins
	 * @param seriesIndex
	 */
	private void addParameterHistogramDataSeries(int[] parameterBinList, final int numberOfBins, final int seriesIndex)
	{
		// Add data series to barcharts.
		barchart.getData().add( generateParameterHistogramDataSeries(parameterBinList, numberOfBins) );
		
		// Color data.
		colorParameterHistogramBarchart(barchart, seriesIndex);
	}
	
	/**
	 * Generates data series for histograms in scented widgets controlling the selected parameter boundaries.
	 * @param key
	 * @param parameterBinLists
	 * @param numberOfBins
	 * @return
	 */
	private XYChart.Series<String, Integer> generateParameterHistogramDataSeries(int[] parameterBinList, final int numberOfBins)
	{
		final XYChart.Series<String, Integer> data_series = new XYChart.Series<String, Integer>();
		
		for (int i = 0; i < numberOfBins; i++) {
			final int binContent = parameterBinList[i];
			data_series.getData().add( new XYChart.Data<String, Integer>(String.valueOf(i), binContent ));
		}
		
		return data_series;
	}

	/**
	 * Colours a parameter histogram barchart in the desired colors (one for filtered, one for discarded).
	 * @param barchart
	 * @param seriesIndex Index of series to be colored. 0 for discarded, 1 for filtered data points.
	 */
	private void colorParameterHistogramBarchart(StackedBarChart<String, Integer> barchart, int seriesIndex)
	{
		// Color bars (todo: color according to defined options).
		for (Node node : barchart.lookupAll(".chart-bar")) {
			switch (seriesIndex)
			{
				// Selectede data.
				case 2:
					if (node.getUserData() == null || node.getUserData().toString() == "active") {
						node.setUserData("active");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
				
				// Discarded data.
				case 1:
					if (node.getUserData() == null || node.getUserData().toString() == "discarded") {
						node.setUserData("discarded");
						node.setStyle("-fx-bar-fill: lightgrey;");
					}
				break;
				
				// Filtered data.
				case 0:
					if (node.getUserData() == null || node.getUserData().toString() == "inactive") {
						node.setUserData("inactive");
						node.setStyle("-fx-bar-fill: darkgrey;");
					}
				break;
			}
		}
	}
	
	/**
	 * Adds current maximum and minimum to specified map.
	 * @param thresholdMap
	 */
	public void addThresholdsToMap(Map<String, Pair<Double, Double>> thresholdMap)
	{
		Pair<Double, Double> values = options.useRangeSlider() ? 	new Pair<Double, Double>(rangeSlider.getLowValue(), rangeSlider.getHighValue()) : 
																	new Pair<Double, Double>(slider.getValue(), slider.getValue());

		// Add to map.
		thresholdMap.put(options.getParamID(), values);
	}
}
