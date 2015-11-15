package view.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.LDAConfiguration;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import control.analysisView.AnalysisController;
import view.components.HoveredThresholdNode;

/**
 * 
 * @author RM
 * @deprecated
 */
public class DistanceDifferenceCorrelationLinechart extends	VisualizationComponent
{
	/*
	 * UI elements.
	 */
	
	private LineChart<Float, Double> linechart;
	private NumberAxis xAxis_numberAxis;
	private NumberAxis yAxis_numberAxis;
	
	private VBox hoverInformation_vbox;
	private ToggleButton relativeView_button;
	
	private CheckBox alpha_checkbox;
	private CheckBox eta_checkbox;
	private CheckBox kappa_checkbox;
	
	/*
	 * Other data.
	 */
	
	private ArrayList<LDAConfiguration> ldaConfigurations;
	private double distances[][];
	
	/**
	 * Determines number of steps to be considered in chart.
	 */
	private int numberOfSteps;
			
	/**
	 * Indicates wheteher or not the maximal value in the distance line chart was yet determined.
	 */
	private boolean valueMaximumDetermined;
	
	/**
	 * Holds information about what's the highest value associated with a parameter in 
	 * current environment (in the distance line/distance correlation chart).
	 */
	private Pair<Double, Double> valueExtrema;
	
	
	public DistanceDifferenceCorrelationLinechart(	AnalysisController analysisController, LineChart<Float, Double> linechart_distanceCorrelation, 
														NumberAxis numberAxis_distanceCorrelation_xAxis, NumberAxis numberAxis_distanceCorrelation_yAxis, 
														VBox hoverInformation_vbox, ToggleButton button_relativeView_paramDC,
														CheckBox alpha_checkbox, CheckBox eta_checkbox, CheckBox kappa_checkbox)
	{
		super(analysisController);
		
		// Init GUI elements.
		this.linechart				= linechart_distanceCorrelation; 
		this.xAxis_numberAxis 		= numberAxis_distanceCorrelation_xAxis;
		this.yAxis_numberAxis 		= numberAxis_distanceCorrelation_yAxis;
		this.hoverInformation_vbox	= hoverInformation_vbox;
		this.relativeView_button	= button_relativeView_paramDC;
		this.alpha_checkbox			= alpha_checkbox;
		this.eta_checkbox			= eta_checkbox;
		this.kappa_checkbox			= kappa_checkbox;
		
		// Init linechart options.
		linechart_distanceCorrelation.setAnimated(false);
		linechart_distanceCorrelation.setCreateSymbols(false);
		
		// Init hoverbox settings.
		hoverInformation_vbox.setVisible(false);
		hoverInformation_vbox.getStyleClass().addAll("chart-line-symbol");
		
		// Init other data.
		valueMaximumDetermined		= false;
		valueExtrema				= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		
		// Init axis options.
		xAxis_numberAxis.setAutoRanging(false);
		yAxis_numberAxis.setAutoRanging(false);
				
		resetXAxisOptions();
	}

	/**
	 * Default view mode is absolute - disable auto-ranging on axes, configure ticks.
	 */
	private void resetXAxisOptions()
	{
		// Set upper and lower bound for x axis.
		xAxis_numberAxis.setLowerBound(-0.2);
		xAxis_numberAxis.setUpperBound(1.2);
		
    	// Adjust tick width.
    	final int numberOfTicks = 4;
    	xAxis_numberAxis.setTickUnit( 1.2 / numberOfTicks);
    	xAxis_numberAxis.setMinorTickCount(4);
	}
	
	/**
	 * Updates y-axis boundaries and ticks dependent on the extreama
	 * in the sampled dataset.
	 */
	private void updateYAxis()
	{
		// Set y-axis range.
		yAxis_numberAxis.setLowerBound(valueExtrema.getKey() * 3);
		yAxis_numberAxis.setUpperBound(valueExtrema.getValue() * 1.2);

		// Configure tick options.
    	final int numberOfTicks = 4;
    	double diff				= valueExtrema.getValue() * 1.2 - valueExtrema.getKey() * 1.2;
    	yAxis_numberAxis.setTickUnit( diff / numberOfTicks);
    	yAxis_numberAxis.setMinorTickCount(4);
	}

	@Override
	public void changeViewMode()
	{
		// Toggle auto-ranging.
		linechart.getYAxis().setAutoRanging(relativeView_button.isSelected());
		linechart.getXAxis().setAutoRanging(relativeView_button.isSelected());
		
		// If in absolute mode: Manually set axis range options.
		if (!relativeView_button.isSelected())
			resetXAxisOptions();
		
		// Refresh chart.
		refresh(ldaConfigurations, distances, false);
	}
	
	/**
	 * Refreshes the line chart.
	 * @param filteredLDAConfigurations
	 * @param filteredDistances
	 * @param haveFilterSettingsChanged
	 */
	public void refresh(ArrayList<LDAConfiguration> filteredLDAConfigurations, double filteredDistances[][], boolean haveFilterSettingsChanged)
	{
		// Update data references.
		this.distances			= filteredDistances;
		this.ldaConfigurations	= filteredLDAConfigurations;
		
		// Clear data.
		linechart.getData().clear();
		
		if (ldaConfigurations.size() > 0) {
			// Store chart data.
			Map<String, XYChart.Series<Float, Double>> diffDataPointSeries	= new HashMap<String, XYChart.Series<Float, Double>>();
			
			// Get information which parameters are to be coupled.
			ArrayList<String> coupledParameters = new ArrayList<String>();
			
			if (alpha_checkbox.isSelected())
				coupledParameters.add("alpha");
			
			if (eta_checkbox.isSelected())
				coupledParameters.add("eta");
			
			if (kappa_checkbox.isSelected())
				coupledParameters.add("kappa");
			
			
			// Init data series.
			for (String param : coupledParameters) {
				final XYChart.Series<Float, Double> dataSeries = new XYChart.Series<Float, Double>();
				dataSeries.setName(LDAConfiguration.getSymbolForParameter(param));
				diffDataPointSeries.put(param, dataSeries);
			}
				
			
			/*
			 * Determine metadata (step size, thresholds for all parameters).
			 */
			
			numberOfSteps									= distances.length;
			Map<String, Double> stepSizes					= new HashMap<String, Double>();
			// Depending on whether relative view is enabled: Use entire loaded dataset or only filtered/selected componenents.
			Map<String, Pair<Double, Double>> thresholds	= determineThresholds(numberOfSteps, stepSizes, relativeView_button.isSelected() ? ldaConfigurations : this.ldaConfigurations, coupledParameters);
			double prevValue								= 0;
			
			
			/*
			 * Calculate distances for each step.
			 */
			
			for (int step = 0; step <= numberOfSteps; step++) {
				// Calculate thresholds at current step for each coupled parameter.			
				Map<String, Pair<Double, Double>> thresholdsForCurrentStep	= calculateThresholdsForStep(step, coupledParameters, stepSizes, thresholds);
				
				// Assign each LDAConfiguration to one of two sets for each step / set of parameter values.
				Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>> ldaConfigurationSets = segregateLDAConfigurations(ldaConfigurations, coupledParameters, thresholdsForCurrentStep);
				Map<String, Set<Integer>> fixedLDAConfigIndiceSets								= ldaConfigurationSets.getKey();

				// Group distances; separated to distances between datasets where fixed parameters fall within the boundaries
				// of the current step and those between all other datasets.
				Map<String, Pair<Double, Double>> segregatedDistanceValues	= averageSegregatedDistances(distances, fixedLDAConfigIndiceSets);
				
				// Calculate current step values for parameters.
				Map<String, Double> stepValues = new HashMap<String, Double>();
				for (String stepParam : LDAConfiguration.SUPPORTED_PARAMETERS) {
					stepValues.put(stepParam, thresholds.get(stepParam).getKey() + step * stepSizes.get(stepParam));
				}
				
				// Add new data point for every parameter's data series.
				for (String param : coupledParameters) {
					// Add tooltip on hover to new data point.
					// segregatedDistanceValues.getKey(): Datasets with fixed parameters within boundaries, .getValue(): datasets with free parameters.
					// I.e. a positive value means the fixed parameters induce a (on average) greater distance, a negative value means they induce
					// a lesser distance.
					double currValue							= segregatedDistanceValues.get(param).getKey() - segregatedDistanceValues.get(param).getValue();
					// If there were either no fixed (within boundaries) or/nor free (not within boundaries) for current step and parameter:
					// Don't plot point, since it wouldn't contain any sensible information.  
					double currCheckedValue						= (segregatedDistanceValues.get(param).getKey() == -Double.MAX_VALUE || segregatedDistanceValues.get(param).getValue() == -Double.MAX_VALUE) ? 0 : currValue;
					// If fixedDatasetsDistances == -Double.MAX_VALUE: No datasets in boundaries for this parameter and step. Use 0 as actual value, mark as non-existent.
					XYChart.Data<Float, Double> diffDataPoint	= new XYChart.Data<Float, Double>((float)step / numberOfSteps, currCheckedValue);

					// Set hover node.
					diffDataPoint.setNode(new HoveredThresholdNode(analysisController, prevValue, currValue, stepValues, coupledParameters));
					
					// Store prev value.
					prevValue = currValue;
					
					// Add (normalized) data points to data series.
					diffDataPointSeries.get(param).getData().add(diffDataPoint);
				}

			}
			
			// Add data series to chart.
			for (String param : coupledParameters) {
				linechart.getData().add(diffDataPointSeries.get(param));
			}
			
			// Set y-axis range, if in absolute mode.
			if (!relativeView_button.isSelected()) {
				// If in absolute mode for the first time (important: The first draw always happens in absolute
				// view mode!): Get maxima and minima.
				if (!valueMaximumDetermined) {
					double max = valueExtrema.getValue();
					double min = valueExtrema.getKey();
					
					// Seek for extrema.
					for (String param : coupledParameters) {
						for (XYChart.Data<Float, Double> dataPoint : diffDataPointSeries.get(param).getData()) {
							double dataPointYValue = dataPoint.getYValue(); 
							max = dataPointYValue > max ? dataPointYValue : max;
							min = dataPointYValue < min ? dataPointYValue : min;
						}
					}
					
					// Remember minimum and maximum.
					valueExtrema				= new Pair<Double, Double>(min, max);
					// Set flag.
					//valueMaximumDetermined	= true;
				}
				
				// Update y-axis range.
				updateYAxis();
			}
			
		}
	}
	
	/**
	 * Determines thresholds (minima and maxima) for each parameter in the currently selected collection of datasets. 
	 * @param numberOfSteps
	 * @param stepSizes Collection designed to contain the values for each step and parameter. Is modified during this function.  
	 * @param ldaConfigurations
	 * @param coupledParameters
	 * @return A map containing minima and maxima for all supported parameters.
	 */
	private Map<String, Pair<Double, Double>> determineThresholds(final int numberOfSteps, Map<String, Double> stepSizes, final ArrayList<LDAConfiguration> ldaConfigurations, final ArrayList<String> coupledParameters)
	{
		Map<String, Pair<Double, Double>> thresholds = new HashMap<String, Pair<Double, Double>>();
		
		for (LDAConfiguration ldaConfig : ldaConfigurations) {
			// Examine if there is a new maximum or minimum for each parameter in this LDAConfiguration.
			for (String param : LDAConfiguration.SUPPORTED_PARAMETERS) {
				double currMin = thresholds.containsKey(param) ? thresholds.get(param).getKey() : Double.MAX_VALUE;
				double currMax = thresholds.containsKey(param) ? thresholds.get(param).getValue() : Double.MIN_VALUE;
				
				currMin = currMin > ldaConfig.getParameter(param) ? ldaConfig.getParameter(param) : currMin;
				currMax = currMax < ldaConfig.getParameter(param) ? ldaConfig.getParameter(param) : currMax;
				
				// Update thresholds.
				thresholds.put(param, new Pair<Double, Double>(currMin, currMax));
				
				// Update step sizes.
				stepSizes.put(param, (currMax - currMin) / numberOfSteps);
			}
		}
		
		return thresholds;
	}

	/**
	 * Auxiliary function calculating the thresholds for a specific step.
	 * @param coupledParameters
	 * @param stepSizes
	 * @param thresholds
	 * @return
	 */
	private Map<String, Pair<Double, Double>> calculateThresholdsForStep(final int step, final ArrayList<String> coupledParameters, final Map<String, Double> stepSizes, final Map<String, Pair<Double, Double>> thresholds)
	{
		Map<String, Pair<Double, Double>> thresholdsForCurrentStep = new HashMap<String, Pair<Double, Double>>(); 
		
		for (String param : coupledParameters) {
			double stepSize	= stepSizes.get(param);
			// Current data point: Minimum for this parameter + #step * step size for this parameter.
			double point	= thresholds.get(param).getKey() + step * stepSize; 
			// Minimum and maximum: Current data point +- 0.5 * step size.
			double min		= point - stepSize / 2;
			double max		= point + stepSize / 2;
			
			thresholdsForCurrentStep.put(param, new Pair<Double, Double>(min, max));
		}
		
		return thresholdsForCurrentStep;
	}

	/**
	 * Segregates currently filtered/selected LDA configurations based on if they pass the boundaries
	 * for the current step. 
	 * @param ldaConfigurations
	 * @param coupledParameters
	 * @param thresholdsForCurrentStep
	 * @return A pair of two maps (each one containing parameter/set pairs); the first one for configurations
	 * fitting the boundaries (fixed indices), the second one those which do not (free indices). 
	 */
	private Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>> segregateLDAConfigurations(final ArrayList<LDAConfiguration> ldaConfigurations, final ArrayList<String> coupledParameters, final Map<String, Pair<Double, Double>> thresholdsForCurrentStep)
	{
		Map<String, Set<Integer>> fixedLDAConfigIndiceSets	= new HashMap<String, Set<Integer>>();
		Map<String, Set<Integer>> freeLDAConfigIndiceSets	= new HashMap<String, Set<Integer>>();
		
		// Init sets.
		for (String param : coupledParameters) {
			fixedLDAConfigIndiceSets.put(param, new HashSet<Integer>());
			freeLDAConfigIndiceSets.put(param, new HashSet<Integer>());
		}
		
		// Assign LDA configurations to sets.
		for (int i = 0; i < ldaConfigurations.size(); i++) {
			// Check each of the coupled parameters: Is current LDA configuration w.r.t. to all coupled parameters within the boundaries?
			for (String param : coupledParameters) {
				// Get value of current parameter in currently examined LDA configuration.
				double paramValue = ldaConfigurations.get(i).getParameter(param);
				// If one of the selected/coupled parameters of the current LDA configuration exceeds the thresholds of the current step:
				// Important: .getkey() ~ minimum, .getValue() ~ maximum. 
				if (paramValue >= thresholdsForCurrentStep.get(param).getKey() && paramValue <= thresholdsForCurrentStep.get(param).getValue()) {
					fixedLDAConfigIndiceSets.get(param).add(i);
				}
				
				else {
					freeLDAConfigIndiceSets.get(param).add(i);
				}
			}
		}
		
		return new Pair<Map<String, Set<Integer>>, Map<String, Set<Integer>>>(fixedLDAConfigIndiceSets, freeLDAConfigIndiceSets);
	}
	
	/**
	 * Examines distances and calculates two sums: One all specified fixed datasets and one for all other.
	 * Does this seperately for each configured fixed parameter.
	 * @param distances
	 * @param fixedLDAConfigIndices
	 * @return Map of pairs containing the sum for (1) all fixed and (2) all other datasets.
	 */
	private Map<String, Pair<Double, Double>> averageSegregatedDistances(double[][] distances, Map<String, Set<Integer>> fixedLDAConfigIndiceSets)
	{
		Map<String, Double> fixedDistancesSums		= new HashMap<String, Double>();
		Map<String, Double> otherDistancesSums		= new HashMap<String, Double>();
		Map<String, Integer> fixedDistancesCounts	= new HashMap<String, Integer>();
		Map<String, Integer> otherDistancesCounts	= new HashMap<String, Integer>();
		
		// Init collections storing distance counts.
		for (String param : fixedLDAConfigIndiceSets.keySet()) {
			fixedDistancesCounts.put(param, 0);
			otherDistancesCounts.put(param, 0);
			fixedDistancesSums.put(param, 0.0);
			otherDistancesSums.put(param, 0.0);
		}
		
		// Sum up distances.
		for (int i = 0; i < distances.length; i++) {
			for (int j = i + 1; j < distances.length; j++) {
				// Are datasets at both indices i and j fixed datasets? 
				// Add current distance (i to j) to corresponding sum variable.
				for (String param : fixedLDAConfigIndiceSets.keySet()) {
					if (fixedLDAConfigIndiceSets.get(param).contains(i) && fixedLDAConfigIndiceSets.get(param).contains(j)) {
						// Add current distance to fixed distances sum for this parameter.
						fixedDistancesSums.put(param, fixedDistancesSums.get(param) + distances[i][j]);
						// Increase count of fixed distances for this parameter by 1.
						fixedDistancesCounts.put(param, fixedDistancesCounts.get(param) + 1);
					}
					
					else {
						// Add current distance to other distances sum for this parameter.
						otherDistancesSums.put(param, otherDistancesSums.get(param) + distances[i][j]);
						// Increase count of other distances for this parameter by 1.
						otherDistancesCounts.put(param, otherDistancesCounts.get(param) + 1);
					}
				}
			}
		}
		
		// Use "common" distance value as default if there are no fixed datasets in the current range.
		// otherDistancesCount should never be 0 with n > 0.
		Map<String, Pair<Double, Double>> normalizedDistanceAverages = new HashMap<String, Pair<Double, Double>>();
		
		for (String param : fixedLDAConfigIndiceSets.keySet()) {
			double normalizedOtherDistanceAverage = otherDistancesCounts.get(param) > 0 ? otherDistancesSums.get(param) / otherDistancesCounts.get(param) : -Double.MAX_VALUE;
			// If no fixed datasets for this parameter and step: Use -Double.MAX_VALUE to indicate that.
			double normalizedFixedDistanceAverage = fixedDistancesCounts.get(param) > 0 ? fixedDistancesSums.get(param) / fixedDistancesCounts.get(param) : -Double.MAX_VALUE;
			
			normalizedDistanceAverages.put(param, new Pair<Double, Double>(normalizedFixedDistanceAverage, normalizedOtherDistanceAverage));
		}
		
		
		return normalizedDistanceAverages;
	}
}
