package control;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class CanvasController implements Initializable
{
	@FXML private Canvas canvas;
	
	private double[][] coordinates;
	
	private double offset;
	private double translationFactor;
	
	public CanvasController()
	{
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
	}
	
	public void setCoordinates(double[][] coordinates)
	{
		this.coordinates = coordinates;
	}
	
	public void draw()
	{
		GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
		double maxX						= 0;
		double maxY						= 0;
		double minX						= 0;
		double minY						= 0;
		
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
				coordinates[0][i] -= minX; 
			}
			
			maxX -= minX;
			minX -= minX;
		}
		if (minY < 0) {
			for (int i = 0; i < coordinates[0].length; i++) {
				coordinates[1][i] -= minY; 
			}
			
			maxY -= minY;
			minY -= minY;
		}
		
		System.out.println("New: ");
		for (int i = 0; i < coordinates.length; i++) {
			for (int j = 0; j < coordinates[i].length; j++) {
				System.out.print(coordinates[i][j] + " ");
			}
			System.out.println("\n");
		}
		
		offset				= canvas.getWidth() * 0.05;
		translationFactor 	= (canvas.getWidth() * 0.9) / maxX;

		graphicsContext.setLineCap(StrokeLineCap.ROUND);
		graphicsContext.setLineJoin(StrokeLineJoin.ROUND);

		drawDatasetRepresentations(graphicsContext, coordinates);
	}
	
	private void drawDatasetRepresentations(GraphicsContext graphicsContext, double[][] coordinates)
	{
		float circleRadius = 5;
		graphicsContext.setFill(Color.RED);
		
		for (int i = 0; i < coordinates[0].length; i++) {
			System.out.println("Drawing #" + i + ": " + (coordinates[0][i] * translationFactor + offset - circleRadius / 2) + " to " + (coordinates[1][i]  * translationFactor + offset - circleRadius / 2) );
			graphicsContext.fillOval(coordinates[0][i] * translationFactor + offset - circleRadius / 2, coordinates[1][i]  * translationFactor + offset - circleRadius / 2, circleRadius, circleRadius);
		}
		
		graphicsContext.setFill(Color.BLACK);
	}
}
