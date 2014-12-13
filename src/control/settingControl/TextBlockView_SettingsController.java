package control.settingControl;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TextBlockView_SettingsController extends SettingsController
{
	@FXML private Label numberOfTopics_label;
	@FXML private TextField keywordCount_textfield;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing TextBlockView_SettingsController.");
		
	}

}
