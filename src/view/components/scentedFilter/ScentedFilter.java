package view.components.scentedFilter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import model.AnalysisDataspace;
import model.LDAConfiguration;

import org.controlsfx.control.RangeSlider;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import view.components.DatapointIDMode;
import view.components.VisualizationComponent;
import view.components.VisualizationComponentType;
import view.components.controls.spinner.ISpinnerListener;
import view.components.controls.spinner.NumericSpinner;
import view.components.rubberbandselection.RubberBandSelection;
import view.components.scentedFilter.scentedKeywordFilter.ScentedKeywordFilterDataset;

public class ScentedFilter extends VisualizationComponent implements ISpinnerListener
{
	/*
	 * GUI elements.
	 */
	
	protected @FXML AnchorPane root_anchorPane;
	
	protected @FXML Label param_label;
	protected NumericSpinner min_spinner;
	protected NumericSpinner max_spinner;
	
	/**
	 * VBox containing barchart and slider.
	 */
	protected @FXML VBox container_vbox;
	
	/**
	 * Barchart displaying histogram.
	 */
	protected @FXML StackedBarChart<String, Number> barchart;
	
	/**
	 * Range slider for value interval control.
	 */
	protected RangeSlider rangeSlider;
	/**
	 * Common slider for value control.
	 */
	protected Slider slider;
	
	/**
	 * Series containing all data.
	 */
	protected XYChart.Series<String, Number> discardedDataSeries;
	/**
	 * Series containing filtered data.
	 */
	protected XYChart.Series<String, Number> inactiveDataSeries;
	/**
	 * Series containing active data.
	 */
	protected XYChart.Series<String, Number> activeDataSeries;

	
	/*
	 * Data.
	 */
	
	/**
	 * Options for this scented filter.
	 */
	protected ScentedFilterOptionset options;
	
	/**
	 * Data for this scented filter.
	 */
	protected ScentedFilterDataset data;
	
	/**
	 * Auxiliary variable used for check of extrema adjustments.
	 */
	protected boolean isAdjustedToExtrema;
	
	/*
	 * Data for selection mechanism. 
	 */
	
	/**
	 * Collection of the descriptions of selected bars.
	 */
	protected Set<String> selectedBars;
	
	
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
		
		// Initialize spinner.
		initSpinners();
		
		// Init rubberband selection.
		initSelection();
	}
	
	/**
	 * Initialize numeric spinners.
	 */
	protected void initSpinners()
	{
		min_spinner = new NumericSpinner(new BigDecimal(0), new BigDecimal(1));
		max_spinner = new NumericSpinner(new BigDecimal(0), new BigDecimal(1));
		
		// Set IDs.
		min_spinner.setId("min_spinner");
		max_spinner.setId("max_spinner");
		
		// Set properties.
		root_anchorPane.getChildren().add(min_spinner);
		root_anchorPane.getChildren().add(max_spinner);
		min_spinner.setPrefWidth(55);
		min_spinner.setLayoutX(25);
		min_spinner.setLayoutY(30);
		max_spinner.setPrefWidth(55);
		max_spinner.setLayoutX(100);
		max_spinner.setLayoutY(30);
		
		// Register listener.
		min_spinner.registerListener(this);
		max_spinner.registerListener(this);
	}
	
	protected void initSelection()
	{
		// Add rubberband selection tool.
        rubberbandSelection 	= new RubberBandSelection((Pane) barchart.getParent().getParent(), this);
        // Init data collections needed for cross-component interaction.
        selectedBars			= new HashSet<String>();
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
			
			rangeSlider.setMinorTickCount(0);
			
			rangeSlider.setSnapToTicks(false);
			rangeSlider.setShowTickLabels(true);
			rangeSlider.setShowTickMarks(true);
			
			// Move upwards.
			rangeSlider.setTranslateY(0);
			rangeSlider.setTranslateX(8);
			
			min_spinner.setStepWidth(new BigDecimal(options.getStepSize()));
			max_spinner.setStepWidth(new BigDecimal(options.getStepSize()));
			
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
			
			slider.setMinorTickCount(4);
			
			slider.setSnapToTicks(true);
			slider.setShowTickLabels(true);
			slider.setShowTickMarks(true);
			
			// Move upwards.
			slider.setTranslateY(-14);
			slider.setTranslateX(7);
			
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
	protected void addEventHandlerToRangeSlider(RangeSlider rs)
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
	protected void addEventHandlerToSlider(Slider s)
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
	protected void updateControlValues(RangeSlider rs)
	{
		double parameterValue_low	= rs.getLowValue() 	>= rs.getMin() ? rs.getLowValue() 	: rs.getMin();
		double parameterValue_high	= rs.getHighValue() <= rs.getMax() ? rs.getHighValue() 	: rs.getMax();
		
		// Update textfield values.
		if (options.getParamID() != "kappa") {
			min_spinner.setNumber(new BigDecimal(parameterValue_low));
			max_spinner.setNumber(new BigDecimal(parameterValue_high));	
		}
		
		else {
			min_spinner.setNumber(new BigDecimal(Math.round(parameterValue_low)));
			max_spinner.setNumber(new BigDecimal(Math.round(parameterValue_high)));
		}
	}
	
	/**
	 * Updates control values after slide event ended.
	 * @param rs
	 */
	protected void updateControlValues(Slider s)
	{
		double parameterValue	= s.getValue() >= s.getMin() && s.getValue() <= s.getMax()? s.getValue() : 0;
		
		// Update textfield values.       	
		if (options.getParamID() != "kappa") {
			min_spinner.setNumber(new BigDecimal(parameterValue));
			max_spinner.setNumber(new BigDecimal(parameterValue));	
		}
		
		else {
			min_spinner.setNumber(new BigDecimal(Math.round(parameterValue)));
			max_spinner.setNumber(new BigDecimal(Math.round(parameterValue)));
		}
	}
	
	public void processSpinnerValue(BigDecimal value, String id)
	{	
		if (options.useRangeSlider()) {
			if (id.equals("min_spinner")) {
				if (value.doubleValue() <= max_spinner.getNumber().doubleValue()) {
					if (value.doubleValue() >= rangeSlider.getMin()) {
						rangeSlider.setLowValue(value.doubleValue());
						// Filter by values; refresh visualizations.
				    	analysisController.refreshVisualizations(true);
					}
					else
						min_spinner.setNumber( new BigDecimal(rangeSlider.getMin()) );
				}
				
				else
					min_spinner.setNumber(max_spinner.getNumber());
			}
			
			else if (id.equals("max_spinner")) {
				if (value.doubleValue() >= min_spinner.getNumber().doubleValue()) {
					if (value.doubleValue() <= rangeSlider.getMax()) {
						rangeSlider.setHighValue(value.doubleValue());
						// Filter by values; refresh visualizations.
				    	analysisController.refreshVisualizations(true);
					}
					else
						max_spinner.setNumber( new BigDecimal(rangeSlider.getMax()) );
				}
				
				else
					max_spinner.setNumber(min_spinner.getNumber());
			}
		}
		
		else {
			min_spinner.setNumber(value);
			max_spinner.setNumber(value);
			
			slider.setValue(value.doubleValue());
			// Filter by values; refresh visualizations.
	    	analysisController.refreshVisualizations(true);
		}
	}

	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
//		if (isCtrlDown) {
			// Process filtered, non-selected data.
			for (int i = 0; i < inactiveDataSeries.getData().size(); i++) {
				Data<String, Number> barData 	= inactiveDataSeries.getData().get(i);
				Node dataNode 					= barData.getNode();
				String barKey					= "inactive_" + i;
				
				if (	data.getBarToDataAssociations().containsKey(barKey) &&
						data.getBarToDataAssociations().get(barKey).size() > 0 &&
						dataNode.getLayoutX() >= minX && dataNode.getLayoutX() + dataNode.getBoundsInLocal().getWidth() <= maxX &&
						dataNode.getLayoutY() >= minY && dataNode.getLayoutY() + dataNode.getBoundsInLocal().getHeight() <= maxY ) {
					// Highlight bar.
					setBarHighlighting(dataNode, true, Color.BLUE);
				
					// Add to collection.
					if (!selectedBars.contains(barData.getXValue())) {
						selectedBars.add(String.valueOf(i));
					}
				}
				
				else {
					// Remove bar highlighting.
					setBarHighlighting(dataNode, false, null);
					
					// Remove from collection.
					selectedBars.remove(i);
				}
			}
//		}
		
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		Set<Integer> selectedLocalIndices = new HashSet<Integer>();
		
		for (String description : selectedBars) {
			// Add to collection.
			selectedLocalIndices.addAll(data.getBarToDataAssociations().get("inactive_" + description));
			
			// Remove glow from all bars.
			for (Data<String, Number> data : inactiveDataSeries.getData()) {
				setBarHighlighting(data.getNode(), false, null);	
			}
			for (Data<String, Number> data : activeDataSeries.getData()) {
				setBarHighlighting(data.getNode(), false, null);	
			}
		}
		
		// Clear collection of selected bars.
		selectedBars.clear();
		
		// Pass selection data on to controller.
		analysisController.integrateSelectionOfDataPoints(selectedLocalIndices, isCtrlDown, DatapointIDMode.INDEX, false);
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(95, 26);
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
		isCtrlDown = ke.isControlDown();
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
		isCtrlDown = ke.isControlDown();
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

	/**
	 * Toggles border glow (indicating the bar having been selected) on/off.
	 * @param node
	 * @param color 
	 */
	protected void setBarHighlighting(Node node, boolean on, Color color)
	{
		if (!on) {
			node.setEffect(null);
		}
		
		else {
			 // Setting the uniform variable for the glow width and height
			int depth = 20;
			
			DropShadow borderGlow= new DropShadow();
			borderGlow.setOffsetY(0f);
			borderGlow.setOffsetX(0f);
			
			borderGlow.setRadius(3);
			borderGlow.setColor(color);
			borderGlow.setWidth(depth);
			borderGlow.setHeight(depth); 
			
			// Apply the borderGlow effect to the JavaFX node.
			node.setEffect(borderGlow); 
		}
	}

	@Override
	public void resizeContent(double width, double height)
	{
		if (width > 0) {
			// Delta between entire width and width of scented widget.
			final double delta	= 150;
			double newWidth 	= width - delta > 0 ? width - delta : 0;
			
			barchart.setPrefWidth(newWidth);
			barchart.setMinWidth(newWidth);
			barchart.setPrefWidth(newWidth);
			
			if (options.useRangeSlider()) {			
				rangeSlider.setPrefWidth(newWidth + 10);
				rangeSlider.setMinWidth(newWidth + 10);
				rangeSlider.setMaxWidth(newWidth + 10);
			}
			else {
				slider.setPrefWidth(newWidth - 5);
				slider.setMinWidth(newWidth - 5);
				slider.setMaxWidth(newWidth - 5);
			}
			
			// Place max_stepper accordingly.
			max_spinner.setLayoutX(width - max_spinner.getWidth() + 10);
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
		
		ArrayList<double[]> binnedData = data.binData(	options.getParamID(), options.getNumberOfBins(),
														options.useRangeSlider() ? rangeSlider.getMin() : slider.getMin(),
														options.useRangeSlider() ? rangeSlider.getMax() : slider.getMax());
		
		/*
		 * Adjust controls to new extrema.
		 */
		if (!isAdjustedToExtrema) {
			if (!data.isDerived())
				adjustControlExtrema(data.getLDAConfigurations());
			else
				adjustDerivedControlExtrema(data.getDerivedData());
			
			isAdjustedToExtrema = true;
		}
			
		
		/*
		 * Draw data.
		 */
		
		// Clear old data.
		barchart.getData().clear();
		selectedBars.clear();

		// Add active data series to barcharts.
		activeDataSeries	= addParameterHistogramDataSeries(binnedData.get(1), options.getNumberOfBins(), 0);
		// Add inactive data series to barcharts.
		inactiveDataSeries	= addParameterHistogramDataSeries(binnedData.get(0), options.getNumberOfBins(), 1);
		// Add discarded data series to barcharts.
		discardedDataSeries = addParameterHistogramDataSeries(binnedData.get(2), options.getNumberOfBins(), 2);
		
		// Add hover event listeners.
		initHoverEventListeners();
		
		// Lower opacity for all data points.
		removeHoverHighlighting();
	}
	
	/**
	 * Adjusts minimal and maximal control values so that they fit the loaded data set.
	 * Operates on primitive parameters.
	 * @param ldaConfigurations
	 */
	public void adjustControlExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		if (options != null) {
			Map<String, Pair<Double, Double>> ldaParameterExtrema = AnalysisDataspace.identifyLDAParameterExtrema(ldaConfigurations, options.getParamID());
			
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
			
			// Adjust major tick unit.
			rangeSlider.setMajorTickUnit( (max - min) / (options.getMajorTickCount() - 1));
			
			// Update text values.
			min_spinner.setNumber(new BigDecimal(min));
			max_spinner.setNumber(new BigDecimal(max));
		}
		
		isAdjustedToExtrema = true;
	}
	
	/**
	 * Adjusts minimal and maximal control values so that they fit the loaded data set.
	 * Operates on derived parameters. 
	 * @param derivedData
	 */
	public void adjustDerivedControlExtrema(double[] derivedData)
	{
		if (options != null) {
			// Update values of range slider.
			double min		= Double.MAX_VALUE;
			double max		= Double.MIN_VALUE;
						
			for (int i = 0; i < derivedData.length; i++) {
				min = min > derivedData[i] ? derivedData[i] : min;
				max = max < derivedData[i] ? derivedData[i] : max;
			}
			
			// Set range slider values.
			if (options.useRangeSlider()) {
				rangeSlider.setMin(min);
				rangeSlider.setMax(max);
			}
			else {
				slider.setMin(min);
				slider.setMax(max);
			}
			
			// Adjust major tick unit.
			rangeSlider.setMajorTickUnit( (max - min) / (options.getMajorTickCount() - 1));
			
			// Update text values.
			min_spinner.setNumber(new BigDecimal(min));
			max_spinner.setNumber(new BigDecimal(max));
		}
		
		isAdjustedToExtrema = true;
	}
	
	/**
	 * Generates parameter histogram in/for specified barcharts.
	 * @param paramterBinLists
	 * @param numberOfBins
	 * @param seriesIndex
	 * @return Newly added data series.
	 */
	protected XYChart.Series<String, Number> addParameterHistogramDataSeries(double[] parameterBinList, final int numberOfBins, final int seriesIndex)
	{
		// Add data series to barcharts.
		XYChart.Series<String, Number> dataSeries = generateParameterHistogramDataSeries(parameterBinList, numberOfBins);
		barchart.getData().add(dataSeries);
		
		// Color data.
		colorParameterHistogramBarchart(barchart, seriesIndex);
		
		return dataSeries;
	}
	
	/**
	 * Generates data series for histograms in scented widgets controlling the selected parameter boundaries.
	 * @param key
	 * @param parameterBinLists
	 * @param numberOfBins
	 * @return
	 */
	protected XYChart.Series<String, Number> generateParameterHistogramDataSeries(double[] parameterBinList, final int numberOfBins)
	{
		final XYChart.Series<String, Number> data_series = new XYChart.Series<String, Number>();
		
		for (int i = 0; i < numberOfBins; i++) {
			final double binContent = parameterBinList[i];
			data_series.getData().add( new XYChart.Data<String, Number>(String.valueOf(i), binContent ));
		}
		
		return data_series;
	}

	/**
	 * Colours a parameter histogram barchart in the desired colors (one for filtered, one for discarded).
	 * @param barchart
	 * @param seriesIndex Index of series to be colored. 0 for discarded, 1 for filtered data points.
	 */
	protected void colorParameterHistogramBarchart(StackedBarChart<String, Number> barchart, int seriesIndex)
	{
		// Color bars (todo: color according to defined options).
		for (Node node : barchart.lookupAll(".chart-bar")) {
			switch (seriesIndex)
			{
				// Selectede data.
				case 0:
					if (node.getUserData() == null || node.getUserData().toString() == "active") {
						node.setUserData("active");
						node.setStyle("-fx-bar-fill: blue;");
					}
				break;
				
				// Discarded data.
				case 1:
					if (node.getUserData() == null || node.getUserData().toString() == "inactive") {
						node.setUserData("inactive");
						node.setStyle("-fx-bar-fill: darkgrey;");
					}
				break;
				
				// Filtered data.
				case 2:
					if (node.getUserData() == null || node.getUserData().toString() == "discarded") {
						node.setUserData("discarded");
						node.setStyle("-fx-bar-fill: lightgrey;");
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
	
	@Override
	public void initHoverEventListeners()
	{
		if (barchart != null) {
			for (XYChart.Series<String, Number> dataSeries : barchart.getData()) {
				for (XYChart.Data<String, Number> bar : dataSeries.getData()) {
					addHoverEventListenersToNode(bar);
				}
			}
		}
	}
	
	@Override
	public void highlightHoveredOverDataPoints(Set<Integer> dataPointIDs, DatapointIDMode idMode)
	{
		if (idMode == DatapointIDMode.INDEX) {
			Set<String> barsToHighlight = new HashSet<String>();
			
			// Find bars to highlight.
			for (int i = 0; i < options.getNumberOfBins(); i++) {
				// Search in data available for this bar.
				if (isBarToHighlight(i, "discarded_", dataPointIDs) ||
					isBarToHighlight(i, "inactive_", dataPointIDs) 	||
					isBarToHighlight(i, "active_", dataPointIDs)) {
					barsToHighlight.add(Integer.toString(i));
				}
			}
		
			// Highlight bars.
			if (barchart != null) {
				for (XYChart.Series<String, Number> dataSeries : barchart.getData()) {
					for (XYChart.Data<String, Number> bar : dataSeries.getData()) {
						if (barsToHighlight.contains(bar.getXValue()))
							bar.getNode().setOpacity(1);
					}
				}
			}
		}
		
		else {
			System.out.println("### ERROR ### DatapointIDMode.CONFIG_ID not supported for ScentedFilter.");
			log("### ERROR ### DatapointIDMode.CONFIG_ID not supported for ScentedFilter.");
		}
	}
	
	/**
	 * Checks if bar should be highlighted (because there is an overlap between data points looked for
	 * and the data represented in this bar).
	 * @param barIndex
	 * @param seriesPraefix
	 * @param dataPointIDs
	 * @return
	 */
	protected boolean isBarToHighlight(int barIndex, String seriesPraefix, Set<Integer> dataPointIDs)
	{
		for ( int i : data.getBarToDataAssociations().get(seriesPraefix + Integer.toString(barIndex)) ) {
			if (dataPointIDs.contains(i)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void removeHoverHighlighting()
	{
		if (barchart != null) {
			for (XYChart.Series<String, Number> dataSeries : barchart.getData()) {
				for (XYChart.Data<String, Number> bar : dataSeries.getData()) {
					bar.getNode().setOpacity(VisualizationComponent.DEFAULT_OPACITY_FACTOR);
				}
			}
		}
	}
	
	/**
	 * Add hover event listener to single node.
	 * @param dataPoint
	 */
	protected void addHoverEventListenersToNode(XYChart.Data<String, Number> bar)
	{
		bar.getNode().setOnMouseEntered(new EventHandler<MouseEvent>()
		{
		    @Override
		    public void handle(MouseEvent event)
		    {   
		    	// Collect indices of data points in this bar.
		    	Set<Integer> indices = new HashSet<Integer>();
		    	indices.addAll(data.getBarToDataAssociations().get("discarded_" + bar.getXValue()));
		    	indices.addAll(data.getBarToDataAssociations().get("inactive_" + bar.getXValue()));
		    	indices.addAll(data.getBarToDataAssociations().get("active_" + bar.getXValue()));
		    	
	        	// Highlight data point.
	        	highlightHoveredOverDataPoints(indices, DatapointIDMode.INDEX);
	        	
	        	// Notify AnalysisController about hover action.
	        	analysisController.highlightDataPoints(indices, DatapointIDMode.INDEX, VisualizationComponentType.SCENTED_FILTER, param_label.getText());
		    }
		});
		
		bar.getNode().setOnMouseExited(new EventHandler<MouseEvent>()
		{
		    @Override
		    public void handle(MouseEvent event)
		    {       
		    	// Remove highlighting.
	        	removeHoverHighlighting();
	        	
	        	// Notify AnalysisController about end of hover action.
	        	analysisController.removeHighlighting(VisualizationComponentType.SCENTED_FILTER);
		    }
		});
	}
	
	@Override
	public String getComponentIdentification()
	{
		return options.getParamID();
	}
}
