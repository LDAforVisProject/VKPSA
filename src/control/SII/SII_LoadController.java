package control.SII;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class SII_LoadController implements Initializable
{
	private @FXML TextField textfield_directory;
	private @FXML Button button_browse;
	private @FXML Button button_load;
	private @FXML ProgressIndicator progressIndicator_load;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		System.out.println("Initializing SII_LoadController.");
	}

}
