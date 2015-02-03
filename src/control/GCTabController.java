package control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class GCTabController extends VisualizationTabController
{
	// FXML elements.
	@FXML private Canvas canvas;
	@FXML private SplitPane root;
	
	private GraphicsContext graphicsContext;
	
	// Data used for drawing coordinates of datasets.
	private double[][] coordinates;
	private double offsetX;
	private double offsetY;
	private double translationFactorX;
	private double translationFactorY;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing GCTabController.");
		graphicsContext = canvas.getGraphicsContext2D();
		
		// Add listener to split pane's divider actions.
		addDividerListener();
	}
	
	/**
	 * Adds listener to detect any changes in divider positions.
	 */
	private void addDividerListener()
	{	
		root.getDividers().get(0).positionProperty().addListener(new ChangeListener<Number>() {
		    @Override 
		    public void changed(ObservableValue<? extends Number> observableValue, Number oldPosition, Number newPosition) 
		    {
		    	System.out.println("Change! " + oldPosition + ", " + newPosition);
		        updateBounds(-1, -1);
		        draw();
		    }
		});
	}

	public void setCoordinates(double[][] coordinates)
	{
		this.coordinates = coordinates;
	}
	
	// @todo Optimization: Normalize negative values only once; search for maximum with each drawing.
	@Override
	public void draw()
	{
		System.out.println(".width = " + canvas.getWidth() + ", .height = " + canvas.getHeight());
		double maxX						= 0;
		double maxY						= 0;
		double minX						= 0;
		double minY						= 0;
		double[][] adaptedCoordinates	= new double[coordinates.length][coordinates[0].length];
		
		// Clear canvas.
		graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Determine maximal x and y value of cities for scaling.
		for (int i = 0; i < coordinates[0].length; i++) {
			maxX = coordinates[0][i] > maxX ? coordinates[0][i] : maxX;
			maxY = coordinates[1][i] > maxY ? coordinates[1][i] : maxY;
			
			minX = coordinates[0][i] < minX ? coordinates[0][i] : minX;
			minY = coordinates[1][i] < minY ? coordinates[1][i] : minY;
		}
		
		// Translate coordinates to positive values only. 
		if (minX < 0) {
			for (int i = 0; i < coordinates[0].length; i++) {
//				coordinates[0][i] -= minX;
				adaptedCoordinates[0][i] = coordinates[0][i] - minX; 
			}
			
			maxX -= minX;
			minX -= minX;
		}
		if (minY < 0) {
			for (int i = 0; i < coordinates[0].length; i++) {
//				coordinates[1][i] -= minY;
				adaptedCoordinates[1][i] = coordinates[1][i] - minY;
			}
			
			maxY -= minY;
			minY -= minY;
		}
		
//		System.out.println("New: ");
//		for (int i = 0; i < coordinates.length; i++) {
//			for (int j = 0; j < coordinates[i].length; j++) {
//				System.out.print(coordinates[i][j] + " ");
//			}
//			System.out.println("\n");
//		}
		
		offsetX				= canvas.getWidth() * 0.1;
		offsetY				= canvas.getHeight() * 0.1;
		translationFactorX 	= (canvas.getWidth() * 0.8) / maxX;
		translationFactorY 	= (canvas.getHeight() * 0.8) / maxY;
		
		graphicsContext.setLineCap(StrokeLineCap.ROUND);
		graphicsContext.setLineJoin(StrokeLineJoin.ROUND);

		drawDatasetRepresentations(graphicsContext, adaptedCoordinates);
	}
	
	private void drawDatasetRepresentations(GraphicsContext graphicsContext, double[][] coordinates)
	{
		float circleRadius = 5;
		graphicsContext.setFill(Color.RED);
		
		for (int i = 0; i < coordinates[0].length; i++) {
			graphicsContext.fillOval(coordinates[0][i] * translationFactorX + offsetX - circleRadius / 2, coordinates[1][i]  * translationFactorY + offsetY - circleRadius / 2, circleRadius, circleRadius);
		}
		
		graphicsContext.setFill(Color.BLACK);
	}

	@Override
	public void updateBounds(final double newSceneWidth, final double newSceneHeight)
	{
		// Estimated height of menubar. 
		final int menubarHeight = 55;
		
		// New values for scene width and/or height.
		if (newSceneWidth > 0)
			canvas.setWidth(newSceneWidth * root.getDividerPositions()[0]);
		if (newSceneHeight > 0)
			canvas.setHeight(newSceneHeight - menubarHeight);
		
		// Only divider position has changed. 
		if(newSceneWidth == -1 && newSceneHeight == -1) {
			canvas.setWidth(root.getScene().getWidth() * root.getDividerPositions()[0]);
		}
	}
}
