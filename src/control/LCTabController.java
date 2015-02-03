package control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

public class LCTabController extends VisualizationTabController
{
	@FXML private WebView webView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing VisTabController.");
	}
	
	public void loadVisualizationFile(String location)
	{
		webView.getEngine().load(getClass().getResource(location).toExternalForm());
	}

	@Override
	public void draw()
	{
		// @todo Auto-generated method stub
	}

	@Override
	public void updateBounds(double newWidth, double newHeight)
	{
		// @todo Auto-generated method stub
		
	}
}
