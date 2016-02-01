package view.components.scatterchart;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

import model.LDAConfiguration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import view.components.heatmap.HeatmapDataset;
import view.components.heatmap.HeatmapOptionset;
import view.components.legacy.mdsScatterchart.DataPointState;

public class ParameterSpaceScatterchart extends Scatterchart
{
	/*
	 * GUI elements.
	 */
	
	private @FXML ComboBox<String> paramX_combobox;
	private @FXML ComboBox<String> paramY_combobox;
	
	// -----------------------------------------------
	//				Methods
	// -----------------------------------------------
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing ParameterSpaceScatterchart component.");
		
		// Initialize basic data.
		super.initialize();		

		// Init combo boxes.
		initComboboxes();
		
		// Init scatterchart.
		initScatterchart();
	}
	
	private void initComboboxes()
	{
		// Initialize comboboxes.
		paramX_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);
		paramY_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);
		
		// Set values.
		paramX_combobox.setValue("alpha");
		paramY_combobox.setValue("kappa");
		
		// Bring comboboxes to front.
		paramX_combobox.toFront();
		paramY_combobox.toFront();
	}
	
	/**
	 * Applies set of options to this instance.
	 * @param options
	 */
	public void applyOptions(ScatterchartOptionset options)
	{
		this.options = options;
		
		// Update density heatmap visibility.
		densityHeatmap.getRoot().setVisible(options.showDensityHeatmap());
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(50, 15);
	}

	@Override
	public void refresh(ScatterchartDataset data)
	{
		this.data = data;

		// Update information about number of datapoints.
		discardedDataSeries.setName("Discarded (" + data.getDiscardedLDAConfigurations().size() + ")");
		inactiveDataSeries.setName("Inactive (" + data.getInactiveLDAConfigurations().size() + ")");
		activeDataSeries.setName("Active (" + data.getActiveLDAConfigurations().size() + ")");
		
		// Clear scatterchart.
		discardedDataSeries.getData().clear();
		inactiveDataSeries.getData().clear();
		activeDataSeries.getData().clear();
		pointsManipulatedInCurrSelectionStep.clear();
		activePoints.clear();
		inactivePoints.clear();
		
		// If density heatmap is not to be shown: Draw scatterchart.
		if (!options.showDensityHeatmap()) {
			scatterchart.getData().clear();
			
			/*
			 * Add data to scatterchart. 
			 */
			
			// Get parameter chosen for x- respectively y-axis.
			final String xParam = paramX_combobox.getValue();
			final String yParam = paramY_combobox.getValue();
			
	        addDataPoints(discardedDataSeries, data.getDiscardedLDAConfigurations(), xParam, yParam, DataPointState.DISCARDED);
	        // Add filtered points points to scatterchart.
	        addDataPoints(inactiveDataSeries, data.getInactiveLDAConfigurations(), xParam, yParam, DataPointState.INACTIVE);
	        // Add active data points to scatterchart.
	        addDataPoints(activeDataSeries, data.getActiveLDAConfigurations(), xParam, yParam, DataPointState.ACTIVE);
	
	        // Add data in scatterchart.
	        scatterchart.getData().add(0, discardedDataSeries);
	        scatterchart.getData().add(0, inactiveDataSeries);
	        scatterchart.getData().add(0, activeDataSeries);
	        
	        // Redraw scatterchart.
	        scatterchart.layout();
	        scatterchart.applyCss();
	        
	        // Add mouse listeners.
	//        addMouseListenersToMDSScatterchart();
		}
		
		// Otherwise: Draw heatmap.
		else if (this.data != null) {
			
			densityHeatmapOptions							= new HeatmapOptionset(	options.isGranularityDynamic(), options.getGranularity(), 
																					options.getDHMMinColor(), options.getDHMMaxColor(), new Color(0.0, 0.0, 1.0, 0.5), new Color(1.0, 0.0, 0.0, 0.5),
																					paramX_combobox.getValue(), paramY_combobox.getValue(),
																					false, false, false);
			// Create dataset for heatmap.
			ArrayList<LDAConfiguration> ldaDataForHeatmap	= new ArrayList<LDAConfiguration>(this.data.getAllLDAConfigurations().size());
			
			if ( (options.getCategoriesValue() & 1) > 0 ) {
				ldaDataForHeatmap.addAll(this.data.getActiveLDAConfigurations());
			}	
			if ( (options.getCategoriesValue() & 2) > 0 ) {
				ldaDataForHeatmap.addAll(this.data.getInactiveLDAConfigurations());
			}
			if ( (options.getCategoriesValue() & 4) > 0 ) {
				ldaDataForHeatmap.addAll(this.data.getDiscardedLDAConfigurations());
			}
			
			HeatmapDataset densityHeatmapData				= new HeatmapDataset(this.data.getAllLDAConfigurations(), ldaDataForHeatmap, this.densityHeatmapOptions);
			// Refresh heatmap.
			densityHeatmap.refresh(this.densityHeatmapOptions, densityHeatmapData);
		}
		
        // Update scatterchart ranges.
        updateScatterchartRanges();
        
        // Mark reference TM.
        markReferenceTM();
        
        // Init hover event listeners.
        initHoverEventListeners();
	}
	
	
	/**
	 * Adds mouse event listeners handling single-point requests.
	 * @todo Adapt to new selection handling method.
	 */
	private void addMouseListenersToScatterchart()
	{
        // @todo Add single choice mouse event listeners to points in all data series.
		
//        for (XYChart.Data<Number, Number> dataPoint : inactiveDataSeries.getData()) {
//        	addSingleSelectionModeMouseListenerToNode(dataPoint, DataPointState.INACTIVE);
//        }
//		
//		// Add mouse listeners for selected data points.
//        for (XYChart.Data<Number, Number> dataPoint : activeDataSeries.getData()) {
//        	addSingleSelectionModeMouseListenerToNode(dataPoint, DataPointState.ACTIVE);
//        }
	}
	
	/**
	 * Updates x- and y-range of the MDS scatterchart.
	 * @param inactiveCoordinates
	 */
	private void updateScatterchartRanges()
	{
		// Else: Absolute view - set ranges manually.
		double diffX = globalExtrema.get(paramX_combobox.getValue())[1] - globalExtrema.get(paramX_combobox.getValue())[0]; 
		double diffY = globalExtrema.get(paramY_combobox.getValue())[1] - globalExtrema.get(paramY_combobox.getValue())[0];
		
	 	// Set axis label values.
		xAxis_numberaxis.setLowerBound(globalExtrema.get(paramX_combobox.getValue())[0] - diffX / 10);
		xAxis_numberaxis.setUpperBound(globalExtrema.get(paramX_combobox.getValue())[1] + diffX / 10);
		yAxis_numberaxis.setLowerBound(globalExtrema.get(paramY_combobox.getValue())[0] - diffY / 10);
		yAxis_numberaxis.setUpperBound(globalExtrema.get(paramY_combobox.getValue())[1] + diffY / 10);
    	
    	// Adjust tick width.
    	final int numberOfTicks = 4;
    	xAxis_numberaxis.setTickUnit( diffX / numberOfTicks);
    	yAxis_numberaxis.setTickUnit( diffY / numberOfTicks);
    	xAxis_numberaxis.setMinorTickCount(2);
    	yAxis_numberaxis.setMinorTickCount(2);
	}
	
	@Override
	public void refresh()
	{
		this.refresh(this.data);
	}

	@Override
	public void resizeContent(double width, double height)
	{
		final double chartWidth		= (width - 123);
		final double chartHeight 	= (height - 79);
		
		// Reposition comboboxes.
		if (paramX_combobox != null && paramY_combobox != null && xAxis_numberaxis != null && yAxis_numberaxis != null) {
			if (width > 0)
				paramX_combobox.setLayoutX(100 + chartWidth / 2 - paramX_combobox.getWidth() / 2);
			if (height > 0)
				paramY_combobox.setLayoutY(25 + chartHeight / 2 - paramY_combobox.getHeight() / 2);
		}
		
		// Resize scatterchart.
		zoomContainer_anchorpane.setPrefWidth(zoomContainer_scrollpane.getWidth() - 10);
		zoomContainer_anchorpane.setPrefHeight(zoomContainer_scrollpane.getHeight() - 10);
		
		// Resize and -position density heatmap.
		updateHeatmapPosition();
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}
	
	@FXML
	public void updateParamValue(ActionEvent e)
	{
		if (this.data != null)
			this.refresh();
	}

	@Override
	protected void refreshDensityHeatmap()
	{
		// @todo Implement ParameterSpaceScatterchart::refreshDensityHeatmap().
	}
	
	@Override
	protected void updateHeatmapPosition()
	{
		final double xBorderFactor = 0.0675;
		final double yBorderFactor = 0.05;
		
		final double offsetX = 26 + scatterchart.getWidth() * xBorderFactor;
		final double offsetY = -30 - scatterchart.getHeight() * yBorderFactor;
		
		// Set x position and new width.
		densityHeatmap.getRoot().setTranslateX(offsetX);
		final double newWidth = (scatterchart.getWidth() - 14 ) * (1 - xBorderFactor * 1.5);
		
		// Set y positiona and new height.
		densityHeatmap.getRoot().setTranslateY(offsetY);
		final double newHeight = (scatterchart.getHeight() - 14) * (1 - yBorderFactor * 2.0);
		
		densityHeatmap.resizeContent(newWidth, newHeight);
	}
}
