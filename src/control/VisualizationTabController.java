package control;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

public abstract class VisualizationTabController implements Initializable
{
	@FXML protected Label contextHeader_label;
	@FXML protected TextArea contextText_textArea;
	@FXML protected Pane settings_pane;
	@FXML protected ProgressIndicator progIndicator_progressIndicator;
	
	/**
	 * Add GUI element for settings of a specific view/visualization.
	 * @param fxmlLocation FXML file describing structure of settings panel.
	 */
	public void addSettingsPane(String fxmlLocation)
	{
		try {
			Node settings = FXMLLoader.load(getClass().getResource(fxmlLocation));
			// Add settings pane.
			settings_pane.getChildren().add(settings);
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Draws visualization of this view tab.
	 */
	public abstract void draw();
		
	/**
	 * Updates bounds of visualization element (WebView, Canvas, ...) to scene so that changes in 
	 * scene size are reflected accurately by the GUI.
	 * @param newWidth
	 * @param newHeight
	 */
	public abstract void updateBounds(double newWidth, double newHeight);
}
