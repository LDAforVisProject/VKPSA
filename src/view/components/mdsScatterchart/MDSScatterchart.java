package view.components.mdsScatterchart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;












import com.sun.javafx.charts.Legend;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Pair;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import control.analysisView.AnalysisController;
import view.components.VisualizationComponent;
import view.components.heatmap.HeatMap;
import view.components.heatmap.HeatmapDataType;
import view.components.rubberbandselection.ISelectableComponent;
import view.components.rubberbandselection.RubberBandSelection;

enum SelectionMode
{
	SINGULAR, GROUP
};

public class MDSScatterchart extends VisualizationComponent implements ISelectableComponent
{
	/*
	 * GUI elements.
	 */
	
	/**
	 * ScatterChart used for visualization.
	 */
	private ScatterChart<Number, Number> scatterchart;
	/**
	 * NumberAxis on x-axis. 
	 */
	private NumberAxis scatterchart_xAxis;
	/**
	 * NumberAxis on y-axis. 
	 */
	private NumberAxis scatterchart_yAxis;
	/**
	 * Contains references to labels in scatterchart legend.
	 */
	private Map<String, Label> scatterchartLegendLabels;
	
	/**
	 * Component enabling rubberband-type selection of points in scatterchart.
	 */
	private RubberBandSelection rubberbandSelection;
	
	/**
	 * Heatmap displaying number of occurences of MDS points, based on coordinates.
	 */
	private HeatMap heatmap;
	/**
	 * Canvas used for drawing of heatmap.
	 */
	private Canvas heatmap_canvas;
	
	// Heatmap controls:
	/**
	 * Slider specifying heatmap's granularity.
	 */
	private Slider heatmapGranularity_slider;
	/**
	 * Checkbox en-/disabling dynamic/customized degree of granularity.
	 */
	private CheckBox heatmap_dynGranularity_checkbox;
	
	/*
	 * Constants.
	 */
	
	/**
	 * Describes zoom factor.
	 */
	private static final float ZOOM_DELTA					= 1.1F;
	/**
	 * Describes speed of navigation via translation.
	 */
	private static final float TRANSLATION_FACTOR_DEFAULT	= 0.84F;
	
	/*
	 * Other data.
	 */
	
	private SelectionMode selectionMode;
	private boolean isCtrlDown;
	private boolean isShiftDown;
	
	/**
	 * Set of point indices changed in current selection action. 
	 */
	private Set<Integer> pointsManipulatedInCurrSelectionStep;
			
	/**
	 * Stores last drag coordinates.
	 */
	private MouseEvent lastMouseEvent;
	
	/**
	 * Flips when selection was changed. Refresh of visualizations is only
	 * triggered if this is true.
	 */
	private boolean changeInSelectionDetected;
	/**
	 * Flips when selection was changed. Local scope visualization refresh is
	 * only triggered if this is true.
	 */
	private boolean changeInSelectionDetected_localScope;
	
	/**
	 * Map storing all selected MDS chart points as values; their respective indices as keys.
	 */
	private Map<Integer, XYChart.Data<Number, Number>> selectedMDSPoints;
	
	/**
	 * Global coordinate extrema on x axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_X;
	/**
	 * Global coordinate extrema on y axis (absolute; not filter- or selection-specific).
	 */
	private Pair<Double, Double> globalCoordinateExtrema_Y;
	/**
	 * Global coordinate extrema in array form. Is to replace pair mode. 
	 */
	private double[] globalCoordinateExtrema;
	
	/**
	 * Data series holding all discarded data points.
	 */
	private Series<Number, Number> discardedDataSeries;
	/**
	 * Data series holding all filtered data points.
	 */
	private Series<Number, Number> filteredDataSeries;
	/**
	 * Data series holding all selected data points.
	 */
	private Series<Number, Number> selectedDataSeries;
	
	/*
	 * Collections storing the domain data. 
	 */
	
	/**
	 * Reference to this workspace's coordinate collection.
	 */
	private double coordinates[][];
	/**
	 * Reference to this workspace's filtered coordinate collection.
	 */
	private double filteredCoordinates[][];
	/**
	 * Reference to this workspace's collection of filtered indices.
	 */
	private Set<Integer> filteredIndices;
	
	/**
	 * Reference to this workspace's selected coordinate collection.
	 */
	private double selectedCoordinates[][];
	/**
	 * Reference to this workspace's collection of selected indices.
	 */
	private Set<Integer> selectedIndices;
	
	/**
	 * Reference to this workspace's discarded coordinate collection.
	 */
	private double discardedCoordinates[][];
	/**
	 * Reference to this workspace's collection of discarded indices.
	 */
	private Set<Integer> discardedIndices;
	
	/*
	 * Event handler (used for single selection mode).
	 */
	/**
	 * EventHandler for click on a filtered point.
	 */
	private EventHandler<MouseEvent> singleSelectionMode_filteredPointHandler;
	/**
	 * EventHandler for click on a selected point.
	 */
	private EventHandler<MouseEvent> singleSelectionMode_selectedPointHandler;
	/**
	 * EventHandler for click on a discarded point.
	 */
	private EventHandler<MouseEvent> singleSelectionMode_discardedPointHandler;
	
	
	// -----------------------------------------------
	// 				Methods.
	// -----------------------------------------------
	
	public MDSScatterchart(	AnalysisController analysisController, ScatterChart<Number, Number> scatterchart,
							Canvas heatmap_canvas, 
							CheckBox heatmap_dynGranularity_checkbox, Slider heatmapGranularity_slider)
	{
		super(analysisController);
		
		this.scatterchart						= scatterchart;
		this.heatmap_canvas						= heatmap_canvas;
		
		this.heatmapGranularity_slider			= heatmapGranularity_slider;
		this.heatmap_dynGranularity_checkbox	= heatmap_dynGranularity_checkbox;
	
		// Init collection of selected data points in the MDS scatterchart.
		selectedMDSPoints						= new HashMap<Integer, XYChart.Data<Number, Number>>();
		globalCoordinateExtrema_X				= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalCoordinateExtrema_Y				= new Pair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE);
		globalCoordinateExtrema					= new double[4];
		discardedDataSeries						= new Series<Number, Number>();
		filteredDataSeries						= new Series<Number, Number>();
		selectedDataSeries						= new Series<Number, Number>();
		
		// Init flags.
		changeInSelectionDetected				= false;
		changeInSelectionDetected_localScope	= false;
		isShiftDown								= false;
		isCtrlDown								= false;
		
		// Init other data.
		selectionMode							= SelectionMode.GROUP;
		pointsManipulatedInCurrSelectionStep	= new HashSet<Integer>();
		
		// Init data series.
		initDataSeries();
		
		// Init scatterchart.
		initScatterchart();
		
		// Init heatmap.
		initHeatmap();
	}

	/**
	 * Updates references to lables in scatterchart legend.
	 * Sets color according to data series.
	 */
	@SuppressWarnings("restriction")
	private void updateLegendLabels()
	{
		for (Node n : scatterchart.getChildrenUnmodifiable()) { 
			if (n instanceof Legend) { 
				final Legend legend = (Legend) n;
				
				for (Node legendNode : legend.getChildren()) {
					if (legendNode instanceof Label) {
						Label label = (Label)legendNode;
						
						String txt = "";
                		if (label.getText().contains("Discarded") && discardedIndices != null) {
                			txt = "Discarded (" + discardedIndices.size() + ")";
	            		}
	            		
	            		else if (label.getText().contains("Filtered") && filteredIndices != null) {
	            			txt = "Filtered (" + (filteredIndices.size() - selectedMDSPoints.size()) + ")";
	            		}
	            		
	            		else if (label.getText().contains("Selected") && selectedIndices != null) {
	            			txt = "Selected (" + selectedMDSPoints.size() + ")";
	            		}
                		
            			label.setText(txt);
					}
					
				}
	        }
	    }	
	}
	
	private void initDataSeries()
	{
		discardedDataSeries.setName("Discarded");
		filteredDataSeries.setName("Filtered");
		selectedDataSeries.setName("Selected");
		
		scatterchartLegendLabels = new HashMap<String, Label>(3);
		scatterchartLegendLabels.put("Discarded", null);
		scatterchartLegendLabels.put("Filtered", null);
		scatterchartLegendLabels.put("Selected", null);
		
		scatterchart.getData().add(discardedDataSeries);
        scatterchart.getData().add(filteredDataSeries);
        scatterchart.getData().add(0, selectedDataSeries);
	}

	private void initScatterchart()
	{
		// Init scatterchart.
		scatterchart_xAxis = (NumberAxis) scatterchart.getXAxis();
        scatterchart_yAxis = (NumberAxis) scatterchart.getYAxis();
        
        // Disable auto ranging.
    	scatterchart_xAxis.setAutoRanging(false);
		scatterchart_yAxis.setAutoRanging(false);
		
		// Use minimum and maximum to create range.
		scatterchart_xAxis.setForceZeroInRange(false);
		scatterchart_yAxis.setForceZeroInRange(false);
		
        scatterchart.setVerticalGridLinesVisible(true);
        
        // Init automatic update of references to labels in legend.
        updateLegendLabels();
        
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) scatterchart.getParent(), this);
        
        // Initialize zooming capabiliy.
        initZoom();
	}
	
	/**
	 * Highlights one particular LDA configuration.
	 * @param index 
	 */
	public void highlightLDAConfiguration(int index)
	{
		for (XYChart.Data<Number, Number> data : selectedDataSeries.getData()) {
			if (index == ((int) data.getExtraValue()) ) {
				 // Setting the uniform variable for the glow width and height
				int depth = 20;
				
				DropShadow borderGlow = new DropShadow();
				borderGlow.setOffsetY(0f);
				borderGlow.setOffsetX(0f);
				
				borderGlow.setRadius(5);
				borderGlow.setColor(Color.RED);
				borderGlow.setWidth(depth);
				borderGlow.setHeight(depth); 
				
				// Apply the borderGlow effect to the JavaFX node.
				data.getNode().setEffect(borderGlow);
			}
			
			else {
				data.getNode().setEffect(null);
			}
		}
	}
	
	private void initHeatmap()
	{
		heatmap = new HeatMap(this.analysisController, heatmap_canvas, (NumberAxis)null, (NumberAxis)null, HeatmapDataType.MDSCoordinates);
		
		/*
		 * Init option controls.
		 */
		
		// Add listener to determine position during after release.
		heatmapGranularity_slider.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	heatmap.setGranularityInformation(heatmap_dynGranularity_checkbox.isSelected(), (int) heatmapGranularity_slider.getValue(), true);
            }
        });
		
		// Add listener to determine position during mouse drag.
		heatmapGranularity_slider.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) 
            {
            	heatmap.setGranularityInformation(heatmap_dynGranularity_checkbox.isSelected(), (int) heatmapGranularity_slider.getValue(), true);
            };
        });
	}
	
	/**
	 * Enables zooming in scatterchart.
	 */
	private void initZoom()
	{
		scatterchart.setOnScroll(new EventHandler<ScrollEvent>() {
		    public void handle(ScrollEvent event) {
		        event.consume();
		        
		        if (event.getDeltaY() == 0) {
		            return;
		        }

		        double scaleFactor = (event.getDeltaY() > 0) ? MDSScatterchart.ZOOM_DELTA : 1 / MDSScatterchart.ZOOM_DELTA;

		        XYChart.Data<Number, Number> firstData = null;
		        
		        for (Series<Number, Number> ds : scatterchart.getData()) {
		        	System.out.println(ds.getName());
		        }
		        
//		        IDEA for pin-point-zoom: Maps seems to zoom to the middle. So:
//		        	- Add to translation the difference between middle of the chart and the desired (mouse event!) Point.
//		        	- Keep track of where the new "middle" is; use for following zooms.
//		        -> Start with first iteration and work that out. All other follows once after the initial task works.
		        
		        // Get first data point.
		        System.out.println(scatterchart.getData().isEmpty());
		        System.out.println(scatterchart.getData().get(2) != null);
		        System.out.println(scatterchart.getData().get(2).getData().isEmpty());

		        if (!scatterchart.getData().isEmpty())
		        	if (scatterchart.getData().get(2) != null)
		        		if (!scatterchart.getData().get(2).getData().isEmpty())
		        			firstData = scatterchart.getData().get(2).getData().get(0);
		        	
		        double x = event.getX();
		        
		        
		        scatterchart.setAnimated(false);
		        if (firstData != null) {
		        System.out.println("--------" + event.getX() + ", " + event.getY());
//		        System.out.println("BEFORE ****** " + scatterchart.getLayoutX() + " / " + scatterchart.getLayoutY());
		        System.out.println("BEFORE ****** " + scatterchart.getWidth() + " / " + scatterchart.getHeight());
		        System.out.println("BEFORE ****** " + firstData.getNode().getBoundsInLocal().getMinX() + " / " + firstData.getNode().getBoundsInLocal().getMinY());
		        
		        // Update chart's scale factor.
		        scatterchart.setScaleX(scatterchart.getScaleX() * scaleFactor);
		        scatterchart.setScaleY(scatterchart.getScaleY() * scaleFactor);
		        
//		        System.out.println("AFTER ****** " + scatterchart.getLayoutX() + " / " + scatterchart.getLayoutY());
		        System.out.println("AFTER ****** " + firstData.getNode().getBoundsInLocal().getMinX() + " / " + firstData.getNode().getBoundsInLocal().getMinY());
		        System.out.println("AFTER ****** " + scatterchart.getWidth() + " / " + scatterchart.getHeight());
		        System.out.println("--------" + event.getX() + ", " + event.getY());
		        
		        double translationX = x - (x - 0) * scaleFactor;
		        }
		    }
		});

		scatterchart.setOnMousePressed(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent event) {
		    	lastMouseEvent = event;
		    	
		    	if (event.isSecondaryButtonDown() == true) {
		            scatterchart.setScaleX(1.0);
		            scatterchart.setScaleY(1.0);
		            
		            scatterchart.setTranslateX(0);
					scatterchart.setTranslateY(0);
		        }
		    }
		});
		
		
		// Enable navigation in zoomed map.
		scatterchart.setOnMouseDragged(new EventHandler<MouseEvent>() {
			 public void handle(MouseEvent event) {
					 if (isShiftDown) {
						 event.consume();
						 
						 double diffX = event.getX() - lastMouseEvent.getX();
						 double diffY = event.getY() - lastMouseEvent.getY();
						 
						 scatterchart.setTranslateX(scatterchart.getTranslateX() + diffX * TRANSLATION_FACTOR_DEFAULT * scatterchart.getScaleX());
						 scatterchart.setTranslateY(scatterchart.getTranslateY() + diffY * TRANSLATION_FACTOR_DEFAULT * scatterchart.getScaleY());
					 }
					 
					 lastMouseEvent = event;
			    }
		});
	}
	
	/**
	 * Initializes mouse click event handler for single selection mode.
	 */
	private void initSingleSelectionModeEventHandler()
	{
//		singleSelectionMode_selectedPointHandler = new EventHandler(<InputEvent>() {
//		    public void handle(InputEvent event) {
//		        System.out.println("Handling event " + event.getEventType()); 
//		        event.consume();
//		    }
 
//				(new EventHandler<MouseEvent>() {
//		    @Override
//		    public void handle(MouseEvent mouseEvent)
//		    {       
//		    	// If control is not down: Add to selection.
//		    	if (!mouseEvent.isControlDown()) {
//			    	if (!selectedMDSPoints.containsKey(dataPoint.getExtraValue())) {
//			    		// Add to map of selected data points.
////				    		selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);
//			    		
//			    		// Refresh scatterchart.
////				    		refresh(coordinates, filteredIndices, selectedCoordinates, selectedIndices, discardedCoordinates, discardedIndices);
////				    		
//			    		// Update collection of selected points.
//						selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);
//
//						// Change data point selection status.
//						changeDataPointSelectionStatus(dataPoint, true);
//						
//						// 
//						
//						// Refresh other charts.
//			    		analysisController.integrateMDSSelection(selectedMDSPoints.keySet(), false);								
//			    	}
//		    	}
//		    });
	}
	
	/**
	 * Processes global (captured by analysis controller in scene) KeyPressedEvent.
	 * @param ke
	 */
	@Override
	public void processKeyPressedEvent(KeyEvent ke)
	{
    	// Remember if CTRL is down.
    	isCtrlDown = ke.isControlDown();
	}
	
	/**
	 * Processes global (captured by analysis controller in scene) KeyReleasedEvent.
	 * @param ke
	 */
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
	
	/**
	 * Identifies global extrema in the provided dataset.
	 * @param coordinates
	 */
	public void identifyGlobalExtrema(double[][] coordinates)
	{
		double[] extrema = identifyExtrema(coordinates);
		
		globalCoordinateExtrema_X 	= new Pair<Double, Double>(extrema[0], extrema[1]);
		globalCoordinateExtrema_Y 	= new Pair<Double, Double>(extrema[2], extrema[3]);
		globalCoordinateExtrema		= extrema; 
	}
	
	/**
	 * Generates an list of extrema (x-minimum, x-maximum, y-minimum, y-maximum) in an 2-dimensional matrix.
	 * @return
	 */
	private double[] identifyExtrema(double[][] coordinates)
	{
		double[] extrema = new double[4];
		
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		
		// Identify global coordinate extrema.
		for (int i = 0; i < coordinates[0].length; i++) {
			// Search for minima.
			minX = coordinates[0][i] < minX ? coordinates[0][i] : minX;
			minY = coordinates[1][i] < minY ? coordinates[1][i] : minY;
			
			// Search for maxima.
			maxX = coordinates[0][i] > maxX ? coordinates[0][i] : maxX;
			maxY = coordinates[1][i] > maxY ? coordinates[1][i] : maxY;			
		}
		
		// Set and return extrema.
		extrema[0] = minX;
		extrema[1] = maxX;
		extrema[2] = minY;
		extrema[3] = maxY;
		
		return extrema;
	}
	
	/**
	 * Refresh scatterchart with data from MDS coordinates. 
	 * @param filteredCoordinates
	 * @param filteredIndices 
	 * @param discardedCoordinates
	 * @param discardedIndices 
	 */
	public void refresh(double coordinates[][],
						double filteredCoordinates[][], Set<Integer> filteredIndices,
						double selectedCoordinates[][], Set<Integer> selectedIndices,
						double discardedCoordinates[][], Set<Integer> discardedIndices)
	{	
		// Store references to data collection.
		this.filteredCoordinates	= filteredCoordinates;
		this.filteredIndices		= filteredIndices;
		this.selectedCoordinates	= selectedCoordinates;
		this.selectedIndices		= selectedIndices;
		this.discardedCoordinates	= discardedCoordinates;
		this.discardedIndices		= discardedIndices;
		
		// Update information about number of datapoints.
		discardedDataSeries.setName("Discarded (" + discardedIndices.size() + ")");
		filteredDataSeries.setName("Filtered (" + filteredIndices.size() + ")");
		selectedDataSeries.setName("Selected (" + selectedIndices.size() + ")");
		
		// Clear scatterchart.
		scatterchart.getData().clear();
		discardedDataSeries.getData().clear();
		filteredDataSeries.getData().clear();
		selectedDataSeries.getData().clear();
		pointsManipulatedInCurrSelectionStep.clear();
		selectedMDSPoints.clear();
		
		// Draw only if heatmap is currently not shown. 
		if (!heatmap_canvas.isVisible()) {
	        // Add filtered points points to scatterchart.
	        addFilteredDataPoints();
	        // Add selected data points to scatterchart.
	        addSelectedDataPoints();
	        // Add discarded data points (greyed out) to scatterchart.
	        addDiscardedDataPoints();
	        
	        // Add data in scatterchart.
	        scatterchart.getData().add(discardedDataSeries);
	        scatterchart.getData().add(filteredDataSeries);
	        scatterchart.getData().add(0, selectedDataSeries);
	        
	        // Redraw scatterchart.
	        scatterchart.layout();
	        scatterchart.applyCss();
	        
	        // Add mouse listeners.
	        addMouseListenersToMDSScatterchart();
		}    
		
        // Update heatmap.
        heatmap.refresh(this.filteredCoordinates, globalCoordinateExtrema);

        // Update scatterchart ranges.
        updateMDSScatterchartRanges();
	
	}
	
	/**
	 * Refreshes chart's heatmap explicitly after resizing.
	 */
	public void refreshHeatmapAfterResize()
	{
		heatmap.refresh(false);
	}
	
	/**
	 * Auxiliary method to add filtered data points to data series in scatterchart.
	 * @param dataSeries
	 * @param selectedDataSeries
	 */
	private void addFilteredDataPoints()
	{
        int count = 0;

        // Add filtered data points.
        for (int index : filteredIndices) {
        	// Add point only if it's not part of the set of selected indices.
        	if (!selectedIndices.contains(index)) {
	        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(filteredCoordinates[0][count], filteredCoordinates[1][count]);
	        	dataPoint.setExtraValue(index);
	        	filteredDataSeries.getData().add(dataPoint);
        	}
        	
        	count++;
        }
	}
	
	/**
	 * Auxiliary method to add selected data points to data series in scatterchart.
	 * @param dataSeries
	 * @param selectedDataSeries
	 */
	private void addSelectedDataPoints()
	{
        int count = 0;
        
        // Add selected data points.
        count = 0;
        for (int index : selectedIndices) {
    		XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(selectedCoordinates[0][count], selectedCoordinates[1][count]);
        	dataPoint.setExtraValue(index);
        	selectedDataSeries.getData().add(dataPoint);
        	selectedMDSPoints.put(index, dataPoint);
    	
        	count++;
        }
	}
	/**
	 * Adds discarded data points to data series.
	 * @param discardedDataSeries
	 */
	private void addDiscardedDataPoints()
	{
        int count = 0;
        for (int index : discardedIndices) {
        	// Add point.
        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(discardedCoordinates[0][count], discardedCoordinates[1][count]);
        	dataPoint.setExtraValue(index);
	        discardedDataSeries.getData().add(dataPoint);
	        
	        count++;
        }
	}
	
	/**
	 * Assigns mouse listener for single selection node to data point's node.
	 * @param dataPoint
	 */
	private void addSingleSelectionModeMouseListenerToNode(XYChart.Data<Number, Number> dataPoint, DataPointState state)
	{
		switch (state)
		{
			case FILTERED:
				dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent mouseEvent)
				    {       
				    	// If control is not down: Add to selection.
				    	if (!mouseEvent.isControlDown()) {
					    	if (!selectedMDSPoints.containsKey(dataPoint.getExtraValue())) {
					    		// Update collection of selected points.
								selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);

								// Change data point selection status.
								changeDataPointSelectionStatus(dataPoint, true);
					    		
								// Refresh other charts.
					    		analysisController.integrateMDSSelection(selectedMDSPoints.keySet(), false);
					    	}
				    	}
				    	else
				    		System.out.println("blub");
				    }
				});
			break;
			
			case SELECTED:
	        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
    			{
    			    @Override
    			    public void handle(MouseEvent mouseEvent)
    			    {       
    			    	System.out.println("in event");
    			    	// If control is down: Remove from selection.
    			    	if (isCtrlDown || mouseEvent.isControlDown()) {
				    		// Update collection of selected points.
				    		selectedMDSPoints.remove(dataPoint.getExtraValue());
							
							// Change data point selection status.
							changeDataPointSelectionStatus(dataPoint, false);
							
							// Refresh other charts.
				    		analysisController.integrateMDSSelection(selectedMDSPoints.keySet(), false);
    			    	}
    			    }
    			});
			break;
		}
	}
	
	/**
	 * Adds mouse event listeners handling single-point requests.
	 * @todo Adapt to new selection handling method.
	 */
	private void addMouseListenersToMDSScatterchart()
	{
        // Add mouse event listeners to points in all data series.
		
        for (XYChart.Data<Number, Number> dataPoint : filteredDataSeries.getData()) {
        	addSingleSelectionModeMouseListenerToNode(dataPoint, DataPointState.FILTERED);
        }
		
		// Add mouse listeners for selected data points.
        for (XYChart.Data<Number, Number> dataPoint : selectedDataSeries.getData()) {
        	addSingleSelectionModeMouseListenerToNode(dataPoint, DataPointState.SELECTED);
        }
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
	private boolean isNodeWithinBounds(Node node, double minX, double minY, double maxX, double maxY)
	{
		boolean nodeXWithinBounds = node.getLayoutX() <= maxX && node.getLayoutX() >= minX;
		boolean nodeYWithinBounds = node.getLayoutY() <= maxY && node.getLayoutY() >= minY;
		
		return nodeXWithinBounds && nodeYWithinBounds;
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// Check if settings icon was used. Workaround due to problems with selection's mouse event handling. 
		if (minX == maxX && minY == maxY) {
			Pair<Integer, Integer> offsets = provideOffsets();
			analysisController.checkIfSettingsIconWasClicked(minX + offsets.getKey(), minY + offsets.getValue(), "settings_mds_icon");
		}
		
		// Set of data points to add to selection. 
		Set<XYChart.Data<Number, Number>> pointsToAddToSelection		= new HashSet<XYChart.Data<Number,Number>>();
		// Set of data points to remove from selection.
		Set<XYChart.Data<Number, Number>> pointsToRemoveFromSelection	= new HashSet<XYChart.Data<Number,Number>>();
		
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
		if (!isCtrlDown) {
			// Iterate over all filtered points.
			for (XYChart.Data<Number, Number> datapoint : filteredDataSeries.getData()) {
				// Point was selected - add to sets of selected indices and selected points.
				if (	isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) &&
						!selectedMDSPoints.containsKey((int)datapoint.getExtraValue())
					) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					selectedMDSPoints.put((int)datapoint.getExtraValue(), datapoint);
					
					// Update set of values added to selection in this action.
					pointsToAddToSelection.add(datapoint);
					
					// Mark data point as manipulated in this step.
					pointsManipulatedInCurrSelectionStep.add((int) datapoint.getExtraValue());
				}
			}

			// Iterate over all selected points.
			for (XYChart.Data<Number, Number> datapoint : selectedDataSeries.getData()) {
				if (	!isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) 	&&
						selectedMDSPoints.containsKey((int)datapoint.getExtraValue())		&&
						pointsManipulatedInCurrSelectionStep.contains((int) datapoint.getExtraValue())
					) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					selectedMDSPoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToRemoveFromSelection.add(datapoint);
				}
			}
		}
		
		// Else if control is down: Remove selected points in chose area from selection (in data series 0).
		else {
			// Iterate over selected points.
			for (XYChart.Data<Number, Number> datapoint : selectedDataSeries.getData()) {
				// Point was deselected - remove from of selected points and selected indices.
				if (isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY) &&
					selectedMDSPoints.containsKey((int)datapoint.getExtraValue())
				) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					selectedMDSPoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToRemoveFromSelection.add(datapoint);			
					
					// Mark data point as manipulated in this step.
					pointsManipulatedInCurrSelectionStep.add((int) datapoint.getExtraValue());
				}
			}
			
			// Iterate over all filtered points.
			for (XYChart.Data<Number, Number> datapoint : filteredDataSeries.getData()) {
				// Point was deselected - remove from of selected points and selected indices.
				if (!isNodeWithinBounds(datapoint.getNode(), minX, minY, maxX, maxY)	&&
					!selectedMDSPoints.containsKey((int)datapoint.getExtraValue())		&&
					pointsManipulatedInCurrSelectionStep.contains((int) datapoint.getExtraValue())
				) {
					// Set dirty flags.
					changeInSelectionDetected				= true;
					changeInSelectionDetected_localScope	= true;
					
					// Update collection of selected points.
					selectedMDSPoints.remove((int)datapoint.getExtraValue());
					
					// Update set of values removed from selection in this action.
					pointsToAddToSelection.add(datapoint);						
				}
			}
		}
		
		// Incorporate identified selection changes into data point's series associations.
		if (changeInSelectionDetected) {
			// Add newly selected data points to selection.
			for (XYChart.Data<Number, Number> newlySelectedDataPoint : pointsToAddToSelection) {
				// Change selection status.
				changeDataPointSelectionStatus(newlySelectedDataPoint, true);
				
				// Update collection of selected points.
				selectedMDSPoints.put((int)newlySelectedDataPoint.getExtraValue(), newlySelectedDataPoint);
			}
			
			// Remove newly de-selected data points to selection.
			for (XYChart.Data<Number, Number> newlyDeselectedDataPoint : pointsToRemoveFromSelection) {
				// Change selection status.
				changeDataPointSelectionStatus(newlyDeselectedDataPoint, false);
			}
			
    		// Refresh other charts.
    		analysisController.integrateMDSSelection(selectedMDSPoints.keySet(), false);
    		
    		// Reset flag.
    		changeInSelectionDetected = false;
    		
    		// Update data series names with number of datasets.
    		updateLegendLabels();
		}
	}
	
	/**
	 * Changes selection status of a point in filtered/selected data series.
	 * @param data
	 * @param selected Indicates that data point should be selected, if this is true.
	 */
	private void changeDataPointSelectionStatus(XYChart.Data<Number, Number> data, boolean selected)
	{
		// Copy data point in question.
		XYChart.Data<Number, Number> dataCopy = new XYChart.Data<Number, Number>(data.getXValue(), data.getYValue());
		dataCopy.setExtraValue(data.getExtraValue());
		
		if (selected) {
			// Remove from filteredDataSeries.
			filteredDataSeries.getData().remove(data);
			
			// Add copy to selectedDataSeries.
			if (!selectedDataSeries.getData().contains(data)) {
				selectedDataSeries.getData().add(dataCopy);

				// Add listener for single selection mode.
	    		addSingleSelectionModeMouseListenerToNode(dataCopy, DataPointState.SELECTED);
			}
		}
		
		else {
			// Remove from selectedDataSeries.
			selectedDataSeries.getData().remove(data);
			
			// Add copy to filteredDataSeries.
			if (!filteredDataSeries.getData().contains(data)) {
				filteredDataSeries.getData().add(dataCopy);
				
				// Add listener for single selection mode.
	    		addSingleSelectionModeMouseListenerToNode(dataCopy, DataPointState.FILTERED);
			}
		}
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
			analysisController.integrateMDSSelection(selectedMDSPoints.keySet(), true);
		}
	}
	
	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(55, 44);
	}
	
	/**
	 * Updates x- and y-range of the MDS scatterchart.
	 * @param filteredCoordinates
	 */
	public void updateMDSScatterchartRanges()
	{
		// Else: Absolute view - set ranges manually.
		double diffX = globalCoordinateExtrema_X.getValue() - globalCoordinateExtrema_X.getKey(); 
		double diffY = globalCoordinateExtrema_Y.getValue() - globalCoordinateExtrema_Y.getKey();
		
	 	// Set axis label values.
		scatterchart_xAxis.setLowerBound(globalCoordinateExtrema_X.getKey() - diffX / 10);
		scatterchart_xAxis.setUpperBound(globalCoordinateExtrema_X.getValue() + diffX / 10);
		scatterchart_yAxis.setLowerBound(globalCoordinateExtrema_Y.getKey() - diffY / 10);
		scatterchart_yAxis.setUpperBound(globalCoordinateExtrema_Y.getValue() + diffY / 10);
    	
    	// Adjust tick width.
    	final int numberOfTicks = 4;
    	scatterchart_xAxis.setTickUnit( diffX / numberOfTicks);
    	scatterchart_yAxis.setTickUnit( diffY / numberOfTicks);
    	scatterchart_xAxis.setMinorTickCount(2);
    	scatterchart_yAxis.setMinorTickCount(2);
	}
	
	@Override
	public void changeViewMode()
	{
		
	}
	
	/**
	 * Get set of selected indices.
	 * @return
	 */
	public Set<Integer> getSelectedIndices()
	{
		return selectedMDSPoints.keySet();
	}

	/**
	 * Updates granularity information in heatmap. 
	 * @param selected
	 * @param value
	 * @param update
	 */
	public void setHeatmapGranularityInformation(boolean selected, int value, boolean update)
	{
		heatmap.setGranularityInformation(selected, value, update);
	}

	/**
	 * Changes heatmap visiblity.
	 * @param selected
	 */
	public void setHeatmapVisiblity(boolean selected)
	{
		heatmap_canvas.setVisible(selected);
		
		// Hide data points.
		for (Series<Number, Number> dataSeries : scatterchart.getData()) {
			for (XYChart.Data<Number, Number> dataPoint : dataSeries.getData()) {
				dataPoint.getNode().setVisible(!selected);
			}
		}
		
		// Update heatmap position/indentation.
		updateHeatmapPosition();
		// Redraw heatmap.
		refreshHeatmapAfterResize();
	}

	/**
	 * Update width of chart's heatmap layer.
	 * @param width
	 */
	public void updateHeatmapPosition()
	{
		final double xBorderFactor = 0.08;
		final double yBorderFactor = 0.05;
		
		// Set x position and new width.
		final double offsetX = 43 + scatterchart.getWidth() * xBorderFactor;
		heatmap_canvas.setLayoutX(offsetX);
		heatmap_canvas.setWidth( (scatterchart.getWidth() - 15 - offsetX) * (1 - xBorderFactor));
		
		// Set y positiona and new height.
		final double offsetY = scatterchart.getHeight() * yBorderFactor;
		heatmap_canvas.setTranslateY(offsetY);
		heatmap_canvas.setHeight((scatterchart.getHeight() - 95 - offsetY) * (1 - yBorderFactor));
	}
}