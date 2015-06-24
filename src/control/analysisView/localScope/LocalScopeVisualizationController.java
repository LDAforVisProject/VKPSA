package control.analysisView.localScope;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import control.Controller;

public abstract class LocalScopeVisualizationController extends Controller
{
	protected AnchorPane anchorPane;
	
	@Override
	public abstract void initialize(URL arg0, ResourceBundle arg1);

	@Override
	public void resizeContent(double width, double height)
	{
		System.out.println("Resizing local scope");
	}
	
	public void setAnchorPane(AnchorPane anchorPane)
	{
		this.anchorPane = anchorPane;
	}

	/**
	 * Redraw visualization.
	 */
	public abstract void refresh();

	/**
	 * Resize visualization.
	 * @param width
	 * @param height
	 */
	public abstract void resize(double width, double height);
}
