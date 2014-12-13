package control.settingControl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public abstract class SettingsController implements Initializable
{
	@FXML private Button browse_button;
	@FXML private TextField filepath_textfield;
}
