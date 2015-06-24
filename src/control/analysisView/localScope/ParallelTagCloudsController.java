package control.analysisView.localScope;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ParallelTagCloudsController extends LocalScopeVisualizationController
{
	protected @FXML Canvas canvas;
	
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing SII_ParallelTagCloudsController.");	
	}

	@Override
	public void refresh()
	{
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		gc.setFill(Color.GREEN);
	    gc.setStroke(Color.BLUE);
	    gc.fillRect(5, 5, canvas.getWidth() - 10, canvas.getHeight() - 10);
	}

	@Override
	public void resize(double width, double height)
	{
		if (width > 0) {
			canvas.setWidth(width);
			refresh();
		}
		
		// Adapt height.
		if (height > 0) {
			canvas.setHeight(height - 35);
			refresh();
		}
	}
}
