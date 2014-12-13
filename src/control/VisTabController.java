package control;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;

public class VisTabController implements Initializable
{
	@FXML private WebView webView;
	@FXML private Label contextHeader_label;
	@FXML private TextArea contextText_textArea;
	@FXML private Pane settings_pane;
	@FXML private ProgressIndicator progIndicator_progressIndicator;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing VisTabController.");
	}
	
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
	
	public void loadVisualizationFile(String location)
	{
		webView.getEngine().load(getClass().getResource(location).toExternalForm());
	}
}
