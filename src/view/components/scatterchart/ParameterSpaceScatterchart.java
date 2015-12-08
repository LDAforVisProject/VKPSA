package view.components.scatterchart;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.javafx.charts.Legend;

import model.LDAConfiguration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import view.components.VisualizationComponent;
import view.components.legacy.mdsScatterchart.DataPointState;
import view.components.rubberbandselection.RubberBandSelection;

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
		
		// Initialize comboboxes.
		paramX_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);
		paramY_combobox.getItems().addAll(LDAConfiguration.SUPPORTED_PARAMETERS);		
		
		// Init flags.
		changeInSelectionDetected				= false;
		changeInSelectionDetected_localScope	= false;
		isCtrlDown								= false;
		
		// Init other data.
		selectionMode							= SelectionMode.GROUP;
		pointsManipulatedInCurrSelectionStep	= new HashSet<Integer>();
		
		// Init combo boxes.
		initComboboxes();
		
		// Init data series.
		initDataSeries();
		
		// Init scatterchart.
		initScatterchart();
		
		// Init selection mechanism.
		initSelection();
	}
	
	private void initComboboxes()
	{
		paramX_combobox.setValue("alpha");
		paramY_combobox.setValue("kappa");
	}

	@Override
	protected void initZoom()
	{
		// @todo Implement zoom for ParameterSpaceScatterchart.
	}
	
	private void initSelection()
	{
		rubberbandSelection = new RubberBandSelection((Pane) scatterchart.getParent(), this);
	}

	/**
	 * Applies set of options to this instance.
	 * @param options
	 */
	public void applyOptions(ScatterchartOptionset options)
	{
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// Clear selection-step-dependent data collections.
		pointsManipulatedInCurrSelectionStep.clear();
		
		// Update local scope.
		if (changeInSelectionDetected_localScope) {
			// Reset dirty flag.
			changeInSelectionDetected_localScope = false;
			
			// Refresh local scope visualization.
			analysisController.integrateSelection(!isCtrlDown ? activePoints.keySet() : inactivePoints.keySet(), !isCtrlDown);
		}
	}

	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(87, 26);
	}

	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
    	// Remember if CTRL is down.
    	isCtrlDown = ke.isControlDown();
	}

	@Override
	public void processKeyReleasedEvent(KeyEvent ke)
	{
    	// Check if CAPS was released.
		if (ke.getCode() == KeyCode.CAPS) {
			
    		// Switch selection mode.
    		if (selectionMode == SelectionMode.GROUP) {
    			selectionMode = SelectionMode.SINGULAR;
    		
    			// Disable rubber band selection listener.
    			rubberbandSelection.disable();
    		}
    		
    		else {
    			selectionMode = SelectionMode.GROUP;
    			
    			// Enable rubber band selection listener.
    			rubberbandSelection.enable();
    		}
    	}
    	
		// Remember if CTRL is down.
    	isCtrlDown = ke.isControlDown();	
	}

	public void refresh(ScatterchartDataset data)
	{
		this.data = data;
		
		// Update information about number of datapoints.
		discardedDataSeries.setName("Discarded (" + data.getDiscardedLDAConfigurations().size() + ")");
		inactiveDataSeries.setName("Inactive (" + data.getInactiveLDAConfigurations().size() + ")");
		activeDataSeries.setName("Active (" + data.getActiveLDAConfigurations().size() + ")");
		
		// Clear scatterchart.
		scatterchart.getData().clear();
		discardedDataSeries.getData().clear();
		inactiveDataSeries.getData().clear();
		activeDataSeries.getData().clear();
		pointsManipulatedInCurrSelectionStep.clear();
		activePoints.clear();
		inactivePoints.clear();
		
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
        
        // Update scatterchart ranges.
        updateScatterchartRanges();
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
	
	/**
	 * Auxiliary method to add selected data points to an arbitrary data series in scatterchart. 
	 * @param dataSeries
	 * @param ldaConfigurations
	 * @param xParam
	 * @param yParam
	 * @param isActive
	 */
	private void addDataPoints(	XYChart.Series<Number, Number> dataSeries, ArrayList<LDAConfiguration> ldaConfigurations,
										String xParam, String yParam, DataPointState state)
	{
        // Add selected data points.
        for (LDAConfiguration ldaConfig : ldaConfigurations) {
    		XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(ldaConfig.getParameter(xParam), ldaConfig.getParameter(yParam));
        	dataPoint.setExtraValue(ldaConfig.getConfigurationID());
        	
        	dataSeries.getData().add(dataPoint);
        	if (state == DataPointState.ACTIVE)
        		activePoints.put(ldaConfig.getConfigurationID(), dataPoint);
        }
	}
	
	@Override
	public void refresh()
	{
		this.refresh(this.data);
	}

	@Override
	public void resizeContent(double width, double height)
	{
		if (paramX_combobox != null && paramY_combobox != null && xAxis_numberaxis != null && yAxis_numberaxis != null) {
			if (width > 0)
				paramX_combobox.setLayoutX(100 + (width - 123) / 2 - paramX_combobox.getWidth() / 2);
			if (height > 0)
				paramY_combobox.setLayoutY(25 + (height - 94) / 2 - paramY_combobox.getHeight() / 2);
		}
	}

	@Override
	protected Map<String, Integer> prepareOptionSet()
	{
		return null;
	}

	@Override
	public void identifyGlobalExtrema(ArrayList<LDAConfiguration> ldaConfigurations)
	{
		globalExtrema = identifyExtrema(LDAConfiguration.SUPPORTED_PARAMETERS, ldaConfigurations);
	}
	
	@FXML
	public void updateParamValue(ActionEvent e)
	{
		if (this.data != null)
			this.refresh();
	}	
}
