package view.components.heatmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import control.analysisView.AnalysisController;
import view.components.ColorScale;
import view.components.VisualizationComponent;
import view.components.rubberbandselection.ISelectableComponent;
import view.components.rubberbandselection.RubberBandSelection;
import model.LDAConfiguration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;



/**
 * Implements a Canvas-based heatmap.
 * Adapted from http://stackoverflow.com/questions/25214538/draw-a-smooth-color-scale-and-assign-specific-values-to-it.
 */
public class HeatMap extends VisualizationComponent implements ISelectableComponent
{
	/*
	 * UI elements.
	 */
	
	/**
	 * Canvas on which heatmap is drawn.
	 */
	private Canvas canvas;
	
	/**
	 * NumberAxis for x-Axis.
	 */
	private NumberAxis xAxis;
	/**
	 * NumberAxis for y-Axis.
	 */
	private NumberAxis yAxis;
	
	/**
	 * Indicates if granularity is to be adjusted dynamically.
	 */
	private boolean adjustGranularityDynamically;
	/**
	 * Granularity (number of squares/elements on on axis) of heatmap.
	 * Only used when adjustGranularityDynamically == false.
	 */
	private int granularity;
	
	/**
	 * Component enabling rubberband-type selection of points in scatterchart.
	 */
	private RubberBandSelection rubberbandSelection;
	
	/*
	 * Actual data to be binned and drawn. 
	 */
	
	/**
	 * References to all LDA configurations in current workspace.
	 */
	private ArrayList<LDAConfiguration> allLDAConfigurations;
	/**
	 * Store reference to chosen (may be filtered or selected) LDAConfigurations.
	 * These are examined, binned and then plotted. 
	 */
	private ArrayList<LDAConfiguration> chosenLDAConfigurations;

	/**
	 * Reference to coordinates of points in MDS scatterplot.
	 */
	private double[][] coordinates;
	/**
	 * Reference to extrema of .coordinates.
	 */
	private double[] coordinateExtrema;
	
	/**
	 * Binned data.
	 */
	private BinnedOccurenceEntity binnedData;
	
	/*
	 * Metadata.
	 */
	
	/**
	 * Stores index of an cell (key is in form of rowIndex * 10 + columnIndex) and the IDs of the corresponding/contained LDA configurations.
	 */
	private Map<Pair<Integer, Integer>, Set<Integer>> cellsToConfigurationIDs;
	/**
	 * Stores index of an cell (key is in form of rowIndex * 10 + columnIndex) and the coordinates (minX, minY, maxX, maxY) of this cell.
	 */
	private Map<Pair<Integer, Integer>, double[]> cellsToCoordinates;
	/**
	 * Store which cells were selected.
	 */
	Set<Pair<Integer, Integer>> selectedCellsCoordinates;
	
	/**
	 * Type of heatmap data used with this instance.
	 */
	private HeatmapDataType dataType;
	/**
	 * Represents which elements (selected or filtered) the current heatmap is bound to. 
	 */
	private HeatmapDataBinding dataBinding;
			
	/*
	 * Other data.
	 */
	
	/**
	 * Name of x-axis parameter.
	 */
	private String key1;
	/**
	 * Name of y-axis parameter.
	 */
	private String key2;
	
	/**
	 * Signifies whether the ctrl key is down at any given time.
	 */
	private boolean isCtrlDown;
	
	
	/*
	 * Methods.
	 */
	
	public HeatMap(AnalysisController analysisController, Canvas canvas, NumberAxis xAxis, NumberAxis yAxis, HeatmapDataType dataType)
    {
		super(analysisController);
		
		this.canvas							= canvas;
		this.xAxis							= xAxis;
		this.yAxis							= yAxis;
		this.dataType						= dataType;
		
		// Init granularity settings.
		this.adjustGranularityDynamically	= true;
		this.granularity					= -1;
		
		// Init metadata collections.
		cellsToConfigurationIDs				= new HashMap<Pair<Integer,Integer>, Set<Integer>>();
		cellsToCoordinates					= new HashMap<Pair<Integer, Integer>, double[]>();
		selectedCellsCoordinates			= new HashSet<Pair<Integer,Integer>>();
		
		// Init axis settings.
		initAxes();
		
		// Init selection tools.
		initSelection();
    }

	private void initSelection()
	{
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) canvas.getParent(), this);
        
        if (dataType == HeatmapDataType.MDSCoordinates)
        	rubberbandSelection.disable();
	}

	private void initAxes()
	{
		if (xAxis != null && yAxis != null) {
			xAxis.setAutoRanging(false);
			yAxis.setAutoRanging(false);
			
			xAxis.setForceZeroInRange(false);
			yAxis.setForceZeroInRange(false);
			
	    	// Adapt axis options.
	    	xAxis.setAnimated(false);
	    	yAxis.setAnimated(false);
		}
	}

	/**
	 * Method to be used when drawing occurence density matrix of one parameter versus another (HeatmapDataType.LDAConfiguration).
	 * @param allLDAConfigurations
	 * @param selectedLDAConfigurations
	 * @param key1
	 * @param key2
	 * @param relativeViewMode
	 */
	public void refresh(final ArrayList<LDAConfiguration> allLDAConfigurations, final ArrayList<LDAConfiguration> chosenLDAConfigurations, 
						final String key1, final String key2, 
						boolean relativeViewMode, HeatmapDataBinding dataBinding)
    {
		// Update data binding type.
		this.dataBinding				= dataBinding;
		
		// If in relative view mode: allLDAConfigurations -> selectedLDAConfigurations.
		this.allLDAConfigurations		= relativeViewMode ? chosenLDAConfigurations : allLDAConfigurations;
    	this.chosenLDAConfigurations	= chosenLDAConfigurations;
    	
    	this.key1						= key1;
    	this.key2						= key2;
    	
    	if (xAxis != null && yAxis != null) {
	    	// Set label text.
	    	xAxis.setLabel(key1);
	    	yAxis.setLabel(key2);
    	}
    	
    	// Reset metadata collections.
    	cellsToConfigurationIDs.clear();
    	cellsToCoordinates.clear();
    	selectedCellsCoordinates.clear();
    	
    	// Bin LDA configuration parameter frequency data and draw it.
    	binnedData = examineLDAConfigurations();
    	
    	// Draw heatmap.
    	draw(binnedData, false);
    }
	
	/**
	 * Method to be used when drawing heatmap of occurence density in MDS scatterplot (HeatmapDataType.MDSCoordinates).
	 * @param coordinates
	 * @param coordinateExtrema
	 */
	public void refresh(double coordinates[][], double coordinateExtrema[])
	{
		this.coordinates		= coordinates;
		this.coordinateExtrema	= coordinateExtrema;
		
		// Bind coordinate frequency data.
		binnedData = examineMDSCoordinateData();
		
		// Draw heatmap.
		draw(binnedData, true);
	}
    
	/**
	 * Refresh heatmap. Re-examines availble data, if desired.
	 * @param reexamineData Specifies whether available data is to be re-evaluated.
	 */
    public void refresh(boolean reexamineData)
    {
    	// Re-examine LDA configurations, if desired (e.g. after changing the granularity settings).
    	if (reexamineData) {
    		binnedData = dataType == HeatmapDataType.LDAConfiguration ? examineLDAConfigurations() : examineMDSCoordinateData();
    		this.refresh(allLDAConfigurations, chosenLDAConfigurations, key1, key2, false, dataBinding);
    	}
    	
    	else {
	    	// Draw data.
	    	boolean useBorders = dataType == HeatmapDataType.LDAConfiguration ? false : true;
	    	draw(binnedData, useBorders);
    	}
    }
    
    /**
     * Bins LDA configuration parameter data.
     * @param binMatrix Matrix in which data is binned. Delivered matrix is modified in this function.
     * @return Minimal and maximal occurence count in bin matrix.
     */
    private BinnedOccurenceEntity examineLDAConfigurations()
    {
    	// 1. Get maximum and minimum for defined keys (alias parameter values).
    	
    	double max_key1	= Double.MIN_VALUE;
		double min_key1	= Double.MAX_VALUE;
		double max_key2	= Double.MIN_VALUE;
		double min_key2	= Double.MAX_VALUE;
		
		for (LDAConfiguration ldaConfig : allLDAConfigurations) {
			max_key1 = max_key1 >= ldaConfig.getParameter(key1) ? max_key1 : ldaConfig.getParameter(key1);
			max_key2 = max_key2 >= ldaConfig.getParameter(key2) ? max_key2 : ldaConfig.getParameter(key2);
			
			min_key1 = min_key1 <= ldaConfig.getParameter(key1) ? min_key1 : ldaConfig.getParameter(key1);
			min_key2 = min_key2 <= ldaConfig.getParameter(key2) ? min_key2 : ldaConfig.getParameter(key2);
		}
		
		// 2. Bin data based on found minima and maxima.
		
		final int numberOfBins	= adjustGranularityDynamically ? (int) Math.sqrt(allLDAConfigurations.size()) : granularity;
		int[][] binMatrix		= new int[numberOfBins][numberOfBins];
		double binInterval_key1	= (max_key1 - min_key1) / numberOfBins;
		double binInterval_key2	= (max_key2 - min_key2) / numberOfBins;
		
		for (LDAConfiguration ldaConfig : chosenLDAConfigurations) {
			// Calculate bin index.
			int index_key1 = (int) ( (ldaConfig.getParameter(key1) - min_key1) / binInterval_key1);
			int index_key2 = (int) ( (ldaConfig.getParameter(key2) - min_key2) / binInterval_key2);
			
			// Check if value is maximum. If so, it should be binned in the last bin nonetheless.
			index_key1 = index_key1 < numberOfBins ? index_key1 : numberOfBins - 1;
			index_key2 = index_key2 < numberOfBins ? index_key2 : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			binMatrix[index_key1][index_key2]++;
			
			// Store references from cells to actual data.
			final Pair<Integer, Integer> mapCellKey = new Pair<Integer, Integer>(index_key1, index_key2);
			// 	Add entry in map, if it doesn't exist already.
			if (!cellsToConfigurationIDs.containsKey(mapCellKey)) {
				cellsToConfigurationIDs.put(mapCellKey, new HashSet<Integer>());
			}
			// 	Add to collection of datasets in this cell.
			cellsToConfigurationIDs.get(mapCellKey).add(ldaConfig.getConfigurationID());
		}
		
		// 3. Determine minimal and maximal occurence count.
		int maxOccurenceCount = Integer.MIN_VALUE;
		int minOccurenceCount = Integer.MAX_VALUE;
		
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = binMatrix[i][j] < minOccurenceCount ? binMatrix[i][j] : minOccurenceCount;
			}	
		}
 		
		// Return binned data.
		return new BinnedOccurenceEntity(binMatrix, minOccurenceCount, maxOccurenceCount, min_key1, max_key1, min_key2, max_key2);
    }
    
    /**
     * Bins MDS coordinate data.
     * @param coordinateExtrema 
     * @param binMatrix Matrix in which data is binned. Delivered matrix is modified in this function.
     * @return Minimal and maximal occurence count in bin matrix.
     */
    private BinnedOccurenceEntity examineMDSCoordinateData()
    {
    	// 1. Get maximum and minimum for defined keys.
    	
    	double minX	= coordinateExtrema[0] * 1.0;
    	double maxX	= coordinateExtrema[1] * 1.0;
    	double minY	= coordinateExtrema[2] * 1.0;
		double maxY	= coordinateExtrema[3] * 1.0;
		
		// 2. Bin data based on found minima and maxima.
		
		final int numberOfBins	= adjustGranularityDynamically ? (int) Math.sqrt(coordinates[0].length) * 2 : granularity;
		int[][] binMatrix		= new int[numberOfBins][numberOfBins];
		double binIntervalX		= (maxX - minX) / numberOfBins;
		double binIntervalY		= (maxY - minY) / numberOfBins;
		
		for (int i = 0; i < coordinates[0].length; i++) {
			// Calculate bin index.
			int indexX = (int) ( (coordinates[0][i] - minX) / binIntervalX);
			int indexY = (int) ( (coordinates[1][i] - minY) / binIntervalY);
			
			// Check if value is maximum. If so, it should be binned in the last bin nonetheless.
			indexX = indexX < numberOfBins ? indexX : numberOfBins - 1;
			indexY = indexY < numberOfBins ? indexY : numberOfBins - 1;
			
			// Increment content of corresponding bin.
			binMatrix[indexX][indexY]++;			
		}
		
		for (int i = 0; i < numberOfBins; i++) {
			for (int j = 0; j < numberOfBins; j++) {
				binMatrix[i][j] = (int) (Math.sqrt(binMatrix[i][j])  *  100);
			}	
		}
		
		// 3. Determine minimal and maximal occurence count.
		int maxOccurenceCount = Integer.MIN_VALUE;
		int minOccurenceCount = Integer.MAX_VALUE;
		
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				maxOccurenceCount = binMatrix[i][j] > maxOccurenceCount ? binMatrix[i][j] : maxOccurenceCount;
				minOccurenceCount = (binMatrix[i][j] < minOccurenceCount) && (binMatrix[i][j] > 0)  
																		? binMatrix[i][j] : minOccurenceCount;
			}	
		}
		
		// Proofcheck minOccurenceCount (in case no LDA configurations are filtered).
		minOccurenceCount = minOccurenceCount != Integer.MAX_VALUE ? minOccurenceCount : 0;
		
		// Return binned data.
		return new BinnedOccurenceEntity(binMatrix, minOccurenceCount, maxOccurenceCount, minX, maxX, minY, minY);
    }
    
    /**
     * Draw data to canvas.
     * @param binnedData
     * @param useBorders 
     */
    private void draw(BinnedOccurenceEntity binnedData, boolean useBorders)
    {
    	GraphicsContext gc		= canvas.getGraphicsContext2D();
    	int binMatrix[][]		= binnedData.getBinMatrix();
    	int minOccurenceCount	= binnedData.getMinOccurenceCount();
    	int maxOccurenceCount	= binnedData.getMaxOccurenceCount();
    	
    	// Set stroke color.
    	gc.setStroke(Color.BLACK);
    	
    	// Set base colors for heatmap, dependent on data binding type.
    	Color minColor = dataBinding == HeatmapDataBinding.FILTERED ? Color.LIGHTBLUE 	: Color.RED;
    	Color maxColor = dataBinding == HeatmapDataBinding.FILTERED ? Color.DARKBLUE	: Color.DARKRED;
    	
    	// Adjust tick values at both axes.
    	if (xAxis != null && yAxis != null) {
	    	// Set axis label values.
	    	xAxis.setLowerBound(binnedData.getMin_key1());
	    	xAxis.setUpperBound(binnedData.getMax_key1());
	    	yAxis.setLowerBound(binnedData.getMin_key2());
	    	yAxis.setUpperBound(binnedData.getMax_key2());
	    	
	    	// Adjust tick width.
	    	final int numberOfTicks = binMatrix.length;
	    	xAxis.setTickUnit( (binnedData.getMax_key1() - binnedData.getMin_key1()) / numberOfTicks);
	    	yAxis.setTickUnit( (binnedData.getMax_key2() - binnedData.getMin_key2()) / numberOfTicks);
	    	xAxis.setMinorTickCount(4);
	    	yAxis.setMinorTickCount(4);
    	}
    	
    	// Clear canvas.
    	gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    	
		// Calculate cell width and height (a quadratic matrix is assumed).
		final double cellWidth	= canvas.getWidth() / binMatrix.length; 
		final double cellHeight	= canvas.getHeight() / binMatrix.length;
		
		// Draw each cell in its corresponding place.
		for (int i = 0; i < binMatrix.length; i++) {
			for (int j = 0; j < binMatrix[i].length; j++) {
				// Calculate coordinates (minX, minY, maxX, maxY).
				double[] cellCoordinates	= new double[4];
				cellCoordinates[0]			= cellWidth * i + 1;
				cellCoordinates[1]			= cellHeight * (binMatrix.length - 1 - j) - 1;
				cellCoordinates[2] 			= cellCoordinates[0] + cellWidth;
				cellCoordinates[3] 			= cellCoordinates[1] + cellHeight;
				
				// Set color for this cell.
				Color cellColor = ColorScale.getColorForValue(binMatrix[i][j], minOccurenceCount, maxOccurenceCount, minColor, maxColor);
				gc.setFill(cellColor);
				
				// Draw cell.
				gc.fillRect(cellCoordinates[0], cellCoordinates[1], cellWidth, cellHeight);
				
				// Draw borders, if this is desired and cell has content.
				if (useBorders && cellColor != Color.TRANSPARENT)
					gc.strokeRect(cellCoordinates[0], cellCoordinates[1] + 1, cellWidth - 1, cellHeight - 1);
				
				// Add coordinate metadata to collection.
				cellsToCoordinates.put(new Pair<Integer, Integer>(i, j), cellCoordinates);
			}	
		}
    }
    
    public void setGranularityInformation(boolean adjustGranularityDynamically, int granularity, boolean update)
    {
    	this.adjustGranularityDynamically	= adjustGranularityDynamically;
    	this.granularity					= granularity;
    	
    	if (update) {
    		this.refresh(true);
    	}
    }

	@Override
	public void changeViewMode()
	{
		
	}

	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{	
		// Get GraphicsContext for drawing.
		GraphicsContext gc					= canvas.getGraphicsContext2D();
		
		// Get current metadata.
		final int[][] binMatrix				= binnedData.getBinMatrix();
		final int minOccurenceCount			= binnedData.getMinOccurenceCount();
		final int maxOccurenceCount			= binnedData.getMaxOccurenceCount();
    	
		// Define color spectrum.
		// Set highlight color (red for additional selection, blue for subtractive).
		final Color highlightColor 	= isCtrlDown ? new Color(0.0, 0.0, 1.0, 0.5) : new Color(1.0, 0.0, 0.0, 0.5); 
		final Color minColor 		= dataBinding == HeatmapDataBinding.FILTERED ? Color.LIGHTBLUE 	: Color.RED;
		final Color maxColor 		= dataBinding == HeatmapDataBinding.FILTERED ? Color.DARKBLUE	: Color.DARKRED;
    	
		// Check if settings icon was used. Workaround due to problems with selection's mouse event handling. 
		if (minX == maxX && minY == maxY) {
			final Pair<Integer, Integer> offsets = provideOffsets();
			analysisController.checkIfSettingsIconWasClicked(minX + offsets.getKey(), minY + offsets.getValue(), "settings_paramDist_icon");
		}
		
		// Check which cells are in selected area, highlight them.
		for (Map.Entry<Pair<Integer, Integer>, double[]> cellCoordinateEntry : cellsToCoordinates.entrySet()) {
			double cellMinX = cellCoordinateEntry.getValue()[0];
			double cellMinY = cellCoordinateEntry.getValue()[1];
			double cellMaxX = cellCoordinateEntry.getValue()[2];
			double cellMaxY = cellCoordinateEntry.getValue()[3];
			
			// Set color.
			gc.setFill(highlightColor);
			
			// Get cell ID/location.
			final Pair<Integer, Integer> cellCoordinates = cellCoordinateEntry.getKey();
			
			if (	cellMinX > minX && cellMaxX < maxX &&
					cellMinY > minY && cellMaxY < maxY) {
				// Get cell's contents.
				final Set<Integer> cellContentSet	= cellsToConfigurationIDs.get(cellCoordinateEntry.getKey()); 

				// Add cell content to collection (if there is any).
				if (cellContentSet != null) {
					// Check if cell('s content) was already selected. If not: Highlight it, add content to selection.  
					if (!selectedCellsCoordinates.contains(cellCoordinates)) {
						// Highlight cell.
						gc.fillRect(cellMinX, cellMinY, cellMaxX - cellMinX, cellMaxY - cellMinY);
						
						// Add to collection of selected cells.
						selectedCellsCoordinates.add(cellCoordinates);
					}
				}
			}
			
			// If not in selected area anymore: Paint in original color, remove from selection.
			else if (selectedCellsCoordinates.contains(cellCoordinates)) {
				// Get row and column.
				final int cellRow		= cellCoordinateEntry.getKey().getKey();
				final int cellColumn 	= cellCoordinateEntry.getKey().getValue();
				
				// Calculate original color.
				Color cellColor = ColorScale.getColorForValue(binMatrix[cellRow][cellColumn], minOccurenceCount, maxOccurenceCount, minColor, maxColor);
				gc.setFill(cellColor);
				
				// Paint cell in original color.
				gc.fillRect(cellMinX, cellMinY, cellMaxX - cellMinX, cellMaxY - cellMinY);
				
				// Remove from selection.
				selectedCellsCoordinates.remove(cellCoordinates);
			}
		}
	}

	@Override
	public void processEndOfSelectionManipulation()
	{
		// Collect content in all selected cells.
		Set<Integer> selectedLDAConfigIDs = new HashSet<Integer>();
		
		for (Pair<Integer, Integer> cellCoordinates : selectedCellsCoordinates) {
			selectedLDAConfigIDs.addAll(cellsToConfigurationIDs.get(cellCoordinates));
		}
		
		// Pass references to selected data onward to AnalysisController.
		analysisController.integrateHeatmapSelection(selectedLDAConfigIDs, !isCtrlDown);
	}
	
	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(66, 45);
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
}