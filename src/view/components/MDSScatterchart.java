package view.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import com.sun.javafx.scene.paint.GradientUtils.Point;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Pair;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import control.analysisView.AnalysisController;
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
	private NumberAxis scatterchart_xAxis;
	private NumberAxis scatterchart_yAxis;
	
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
	private boolean isSpaceDown;
	
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
	
	/*
	 * Collections storing the domain data. 
	 */
	
	/**
	 * Reference to this workspace's coordinate collection.
	 */
	private double coordinates[][];
	/**
	 * Reference to this workspace's collection of filtered indices.
	 */
	private Set<Integer> indices;
	
	/**
	 * Reference to this workspace's discarded coordinate collection.
	 */
	private double discardedCoordinates[][];
	/**
	 * Reference to this workspace's collection of discarded indices.
	 */
	private Set<Integer> discardedIndices;
	
	
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
		
		// Init flags.
		changeInSelectionDetected				= false;
		changeInSelectionDetected_localScope	= false;
		isSpaceDown								= false;
		
		// Init scatterchart.
		initScatterchart();
		// Init heatmap.
		initHeatmap();
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
        
        // Add rubberband selection tool.
        rubberbandSelection = new RubberBandSelection((Pane) scatterchart.getParent(), this);
        
        // Initialize zooming capabiliy.
        initZoom();
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
					 if (isSpaceDown) {
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
	 * Adds listener processing keyboard events (like toggling from group to single selection mode).
	 * @param scene
	 */
	public void addKeyListener(Scene scene)
	{
		selectionMode	= SelectionMode.GROUP;
		isCtrlDown		= false;
		
		// Ensure that CAPS LOCK is off.
		//Toolkit.getDefaultToolkit().setLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK, false);
	
		// Add key listener for selection mode.
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
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

                // Check if space key is down.
            	else if (ke.getCode() == KeyCode.SHIFT) {
        			rubberbandSelection.disable();
        			isSpaceDown = true;
        		}
                
            	// Remember if CTRL is down.
            	isCtrlDown = ke.isControlDown();
            }
        });
        
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) 
            {
            	// Remember if CTRL is down.
            	isCtrlDown = ke.isControlDown();
            	
            	// Check if space key is up.
            	if (ke.getCode() == KeyCode.SHIFT) {
            		
            		rubberbandSelection.enable();
            		isSpaceDown = false;
            	}
            }
        });
	}
	
	/**
	 * Identifies global extrema in the provided dataset.
	 * @param coordinates
	 */
	public void identifyGlobalExtrema(double[][] coordinates)
	{
		double[] extrema = identifyExtrema(coordinates);
		
		globalCoordinateExtrema_X = new Pair<Double, Double>(extrema[0], extrema[1]);
		globalCoordinateExtrema_Y = new Pair<Double, Double>(extrema[2], extrema[3]);
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
	 * @param coordinates
	 * @param indices 
	 * @param discardedCoordinates
	 * @param discardedIndices 
	 */
	public void refresh(double coordinates[][], Set<Integer> indices, double discardedCoordinates[][], Set<Integer> discardedIndices)
	{	
		// Store references to data collection.
		this.coordinates			= coordinates;
		this.indices				= indices;
		this.discardedCoordinates	= discardedCoordinates;
		this.discardedIndices		= discardedIndices;
		
		// Clear scatterchart.
		scatterchart.getData().clear();
		
		// Init data series for filtered, discarded and selected values.
        final Series<Number, Number> dataSeries				= new XYChart.Series<>();
        final Series<Number, Number> discardedDataSeries	= new XYChart.Series<>();
        final Series<Number, Number> selectedDataSeries		= new XYChart.Series<>();
        
        dataSeries.setName("Filtered");
        selectedDataSeries.setName("Selected");
        discardedDataSeries.setName("Discarded");
        
        // Add filtered points as well as filtered and selected points to scatterchart.
        addActiveDataPoints(dataSeries, selectedDataSeries);
        
        // Add discarded data points (greyed out) to scatterchart.
        addDiscardedDataPoints(discardedDataSeries);
        
        // Add data in scatterchart.
        scatterchart.getData().add(discardedDataSeries);
        scatterchart.getData().add(dataSeries);
        scatterchart.getData().add(0, selectedDataSeries);
        
        // Redraw scatterchart.
        scatterchart.layout();
        scatterchart.applyCss();
        
        // Add mouse listeners.
        addMouseListenersToMDSScatterchart(this.coordinates, this.indices);
        
        // Update heatmap.
        heatmap.refresh(coordinates, identifyExtrema(coordinates));
        
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
	 * Auxiliary method to add data points to data series in scatterchart.
	 * @param dataSeries
	 * @param selectedDataSeries
	 */
	private void addActiveDataPoints(final Series<Number, Number> dataSeries, final Series<Number, Number> selectedDataSeries)
	{
        int count = 0;
        for (int index : indices) {
        	// Add point only if it's not part of the set of manually selected indices.
        	if (!selectedMDSPoints.containsKey(index)) {
	        	XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(coordinates[0][count], coordinates[1][count]);
	        	dataPoint.setExtraValue(index);
	        	dataSeries.getData().add(dataPoint);
        	}
        	
        	else {
        		XYChart.Data<Number, Number> selectedPoint	= selectedMDSPoints.get(index);
        		XYChart.Data<Number, Number> dataPoint		= new XYChart.Data<Number, Number>(selectedPoint.getXValue(), selectedPoint.getYValue());
	        	dataPoint.setExtraValue(selectedPoint.getExtraValue());
	        	selectedDataSeries.getData().add(dataPoint);
        	}
        	
        	count++;
        }
	}
	
	/**
	 * Adds discarded data points to data series.
	 * @param discardedDataSeries
	 */
	private void addDiscardedDataPoints(final Series<Number, Number> discardedDataSeries)
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
	 * Adds mouse event listeners handling single-point requests.
	 */
	private void addMouseListenersToMDSScatterchart(double coordinates[][], Set<Integer> filteredIndices)
	{
		// Add listeners to all series of non-selected data points.
		for (int i = 1; i < scatterchart.getData().size(); i++) {
	        // Add mouse event listeners to points in this data series.
	        for (XYChart.Data<Number, Number> dataPoint : scatterchart.getData().get(i).getData()) {
	        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
				{
				    @Override
				    public void handle(MouseEvent mouseEvent)
				    {       
				    	// If control is not down: Add to selection.
				    	if (!mouseEvent.isControlDown()) {
					    	if (!selectedMDSPoints.containsKey(dataPoint.getExtraValue())) {
					    		// Add to map of selected data points.
					    		selectedMDSPoints.put((int)dataPoint.getExtraValue(), dataPoint);
					    		
					    		// Refresh scatterchart.
					    		refresh(coordinates, filteredIndices, discardedCoordinates, discardedIndices);
					    		// Refresh other charts.
					    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
					    	}
				    	}
				    }
				});
	        }
		}
		
		// Add mouse listeners for selected data points.
        for (XYChart.Data<Number, Number> dataPoint : scatterchart.getData().get(0).getData()) {
        	dataPoint.getNode().setOnMouseClicked(new EventHandler<MouseEvent>()
			{
			    @Override
			    public void handle(MouseEvent mouseEvent)
			    {       
			    	// If control is down: Remove from selection.
			    	if (mouseEvent.isControlDown()) {
				    	// Remove from set of selected data points.
			    		selectedMDSPoints.remove(dataPoint.getExtraValue());
			    		
			    		// Refresh scatterchart.
			    		refresh(coordinates, filteredIndices, discardedCoordinates, discardedIndices);
			    		// Refresh other charts.
			    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
			    	}
			    }
			});
        }
	}
	
	@Override
	public void processSelectionManipulationRequest(double minX, double minY, double maxX, double maxY)
	{
		// If control is not down: Ignore selected points, add all non-selected in chosen area.
		if (!isCtrlDown) {
			for (int i = 1; i < scatterchart.getData().size(); i++) {
				// Get data series.
				XYChart.Series<Number, Number> dataSeries = scatterchart.getData().get(i);
				// Iterate over all data points.
				for (XYChart.Data<Number, Number> datapoint : dataSeries.getData()) {
					Node datapointNode			= datapoint.getNode();
					boolean nodeXWithinBounds	= datapointNode.getLayoutX() <= maxX && datapointNode.getLayoutX() >= minX;
					boolean nodeYWithinBounds	= datapointNode.getLayoutY() <= maxY && datapointNode.getLayoutY() >= minY;
					
					// Point was selected - add to sets of selected indices and selected points.
					if (nodeXWithinBounds && nodeYWithinBounds) {
						if (!selectedMDSPoints.containsKey((int)datapoint.getExtraValue())) {
							// Set dirty flags.
							changeInSelectionDetected				= true;
							changeInSelectionDetected_localScope	= true;
							
							// Update collection of selected points.
							selectedMDSPoints.put((int)datapoint.getExtraValue(), datapoint);
						}
					}
				}
			}
			
			if (changeInSelectionDetected) {
	    		// Refresh scatterchart.
	    		refresh(coordinates, indices, discardedCoordinates, discardedIndices);
	    		// Refresh other charts.
	    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
	    		
	    		// Reset flag.
	    		changeInSelectionDetected = false;
			}
		}
		
		// Else if control is down: Remove selected points in chose area from selection (in data series 0).
		else {
			// Get data series.
			XYChart.Series<Number, Number> dataSeries = scatterchart.getData().get(0);
			// Iterate over all data points.
			for (XYChart.Data<Number, Number> datapoint : dataSeries.getData()) {
				Node datapointNode			= datapoint.getNode();
				boolean nodeXWithinBounds	= datapointNode.getLayoutX() <= maxX && datapointNode.getLayoutX() >= minX;
				boolean nodeYWithinBounds	= datapointNode.getLayoutY() <= maxY && datapointNode.getLayoutY() >= minY;
				
				// Point was deselected - remove from of selected points and selected indices.
				if (nodeXWithinBounds && nodeYWithinBounds) {
					if (selectedMDSPoints.containsKey((int)datapoint.getExtraValue())) {
						// Set dirty flags.
						changeInSelectionDetected				= true;
						changeInSelectionDetected_localScope	= true;
						
						// Update collection of selected points.
						selectedMDSPoints.remove((int)datapoint.getExtraValue());
					}
				}
			}
			
			if (changeInSelectionDetected) {
	    		// Refresh scatterchart.
	    		refresh(coordinates, indices, discardedCoordinates, discardedIndices);
	    		// Refresh other charts.
	    		analysisController.refreshVisualizationsAfterGlobalSelection(selectedMDSPoints.keySet(), false);
	    		
	    		// Reset flag.
	    		changeInSelectionDetected = false;
			}
		}
	}
	
	@Override
	public void processEndOfSelectionManipulation()
	{
		// Update local scope.
		if (changeInSelectionDetected_localScope) {
			// Refresh local scope visualization.
			analysisController.refreshLocalScopeAfterGlobalSelection();
			// Reset dirty flag.
			changeInSelectionDetected_localScope = false;
		}
	}
	
	@Override
	public Pair<Integer, Integer> provideOffsets()
	{
		return new Pair<Integer, Integer>(55, 44);
	}
	
	/**
	 * Updates x- and y-range of the MDS scatterchart.
	 * @param coordinates
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
		
		// Show grid lines, if canvas is not visible. Hide them, if canvas is visible.
//		scatterchart.setHorizontalGridLinesVisible(!selected);
//		scatterchart.setVerticalGridLinesVisible(!selected);
//		scatterchart.setHorizontalZeroLineVisible(!selected);
//		scatterchart.setVerticalZeroLineVisible(!selected);
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
		// Set y position
		final double offsetY = scatterchart.getHeight() * yBorderFactor;
		heatmap_canvas.setTranslateY(offsetY);
		heatmap_canvas.setHeight((scatterchart.getHeight() - 95 - offsetY) * (1 - yBorderFactor));
	}
}