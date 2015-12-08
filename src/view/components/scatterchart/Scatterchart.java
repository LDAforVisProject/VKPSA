package view.components.scatterchart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import model.LDAConfiguration;

import com.sun.javafx.charts.Legend;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import view.components.VisualizationComponent;

public abstract class Scatterchart extends VisualizationComponent
{
	/*
	 * GUI elements.
	 */
	
	protected @FXML ScatterChart<Number, Number> scatterchart;
	protected @FXML NumberAxis xAxis_numberaxis;
	protected @FXML NumberAxis yAxis_numberaxis;
	
	/*
	 * Data needed for selection.
	 */
	
	/**
	 * Selection mode (single or group).
	 */
	protected SelectionMode selectionMode;
	
	/**
	 * Set of point indices changed in current selection action. 
	 */
	protected Set<Integer> pointsManipulatedInCurrSelectionStep;
			
	/**
	 * Stores last drag coordinates.
	 */
	protected MouseEvent lastMouseEvent;
	
	/**
	 * Flips when selection was changed. Refresh of visualizations is only
	 * triggered if this is true.
	 */
	protected boolean changeInSelectionDetected;
	/**
	 * Flips when selection was changed. Local scope visualization refresh is
	 * only triggered if this is true.
	 */
	protected boolean changeInSelectionDetected_localScope;
	
	/**
	 * Map storing all active chart points as values; their respective indices as keys.
	 * Used to track changes over time when user manipulates the current selection.
	 */
	protected Map<Integer, XYChart.Data<Number, Number>> activePoints;
	/**
	 * Map storing all chart points whose status was changed as values; their respective indices as keys.
	 * Used to track changes over time when user manipulates the current selection.
	 */
	protected Map<Integer, XYChart.Data<Number, Number>> inactivePoints;
	
	/*
	 * Data series for scatterchart.
	 */
	
	/**
	 * Data series holding all discarded data points.
	 */
	protected Series<Number, Number> discardedDataSeries;
	/**
	 * Data series holding all filtered data points.
	 */
	protected Series<Number, Number> inactiveDataSeries;
	/**
	 * Data series holding all selected data points.
	 */
	protected Series<Number, Number> activeDataSeries;
	
	/*
	 * Data.
	 */
	
	/**
	 * Data set for this instance of ParameterSpaceScatterchart.
	 */
	protected ScatterchartDataset data;
	/**
	 * Option set for this instance of ParameterSpaceScatterchart.
	 */
	protected ScatterchartOptionset options;
	
	/**
	 * Global extrema in provided data. 
	 */
	protected Map<String, double[]> globalExtrema;
	
	
	// -----------------------------------
	//				Methods
	// -----------------------------------
	
	/**
	 * Initializes data series.
	 */
	protected void initDataSeries()
	{
		discardedDataSeries						= new Series<Number, Number>();
		inactiveDataSeries						= new Series<Number, Number>();
		activeDataSeries						= new Series<Number, Number>();
		
		discardedDataSeries.setName("Discarded");
		inactiveDataSeries.setName("Inactive");
		activeDataSeries.setName("Active");
		
		activePoints							= new HashMap<Integer, XYChart.Data<Number, Number>>();
		inactivePoints							= new HashMap<Integer, XYChart.Data<Number, Number>>();
	}

	/**
	 * Initializes scatterchart.
	 */
	protected void initScatterchart()
	{
        // Disable auto ranging.
        xAxis_numberaxis.setAutoRanging(false);
        yAxis_numberaxis.setAutoRanging(false);
		
		// Use minimum and maximum to create range.
		xAxis_numberaxis.setForceZeroInRange(false);
		yAxis_numberaxis.setForceZeroInRange(false);
		
        scatterchart.setVerticalGridLinesVisible(true);
        
        // Init automatic update of references to labels in legend.
        updateLegendLabels();
        
        // Initialize zooming capabiliy.
        initZoom();
	}

	/**
	 * Updates references to lables in scatterchart legend.
	 * Sets color according to data series.
	 */
	@SuppressWarnings("restriction")
	protected void updateLegendLabels()
	{
		for (Node n : scatterchart.getChildrenUnmodifiable()) { 
			if (n instanceof Legend) { 
				final Legend legend = (Legend) n;
				
				for (Node legendNode : legend.getChildren()) {
					if (legendNode instanceof Label) {
						Label label = (Label)legendNode;
						
						String txt = "";
                		if (label.getText().contains("Discarded") && data.getDiscardedLDAConfigurations() != null) {
                			txt = "Discarded (" + data.getDiscardedLDAConfigurations().size() + ")";
	            		}
	            		
	            		else if (label.getText().contains("Inactive") && data.getInactiveLDAConfigurations() != null) {
	            			txt = "Inactive (" + (data.getInactiveLDAConfigurations().size() - activePoints.size()) + ")";
	            		}
	            		
	            		else if (label.getText().contains("Active") && data.getInactiveLDAConfigurations() != null) {
	            			txt = "Active (" + activePoints.size() + ")";
	            		}
                		
            			label.setText(txt);
					}
					
				}
	        }
	    }	
	}
	
	/**
	 * Initializes zoom in scatterchart.
	 */
	protected abstract void initZoom();
	
	/**
	 * Identifies global extrema based on provided data.
	 */
	public abstract void identifyGlobalExtrema(ArrayList<LDAConfiguration> ldaConfigurations);
	
	/**
	 * Generates an list of extrema (x-minimum, x-maximum, y-minimum, y-maximum) for every parameter.
	 * @param paramValues Possible parameter values for which extrema have to be identified.
	 * @return
	 */
	protected Map<String, double[]> identifyExtrema(final String[] paramValues, ArrayList<LDAConfiguration> ldaConfigurations)
	{
		Map<String, double[]> extrema = new LinkedHashMap<String, double[]>();
		
		// Init map.
		for (String paramValue : paramValues) {
			extrema.put(paramValue, new double[2]);
			
			// Set initial values.
			extrema.get(paramValue)[0] = Double.MAX_VALUE;
			extrema.get(paramValue)[1] = Double.MIN_VALUE;
		}
		
		// Identify global coordinate extrema.
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// For each parameter:
			for (Map.Entry<String, double[]> paramSet : extrema.entrySet()) {
				paramSet.getValue()[0] = 	ldaConfig.getParameter(paramSet.getKey()) < paramSet.getValue()[0] ? 
											ldaConfig.getParameter(paramSet.getKey()) : paramSet.getValue()[0];
				paramSet.getValue()[1] = 	ldaConfig.getParameter(paramSet.getKey()) > paramSet.getValue()[1] ? 
											ldaConfig.getParameter(paramSet.getKey()) : paramSet.getValue()[1];
			}	
		}
		
		return extrema;
	}
	
	/**
	 * Checks if node is positioned within specified bounds.
	 * @param node
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @return
	 */
	protected boolean isNodeWithinBounds(Node node, double minX, double minY, double maxX, double maxY)
	{
		boolean nodeXWithinBounds = node.getLayoutX() <= maxX && node.getLayoutX() >= minX;
		boolean nodeYWithinBounds = node.getLayoutY() <= maxY && node.getLayoutY() >= minY;
		
		return nodeXWithinBounds && nodeYWithinBounds;
	}
}
