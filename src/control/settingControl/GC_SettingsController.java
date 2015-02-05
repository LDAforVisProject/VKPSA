package control.settingControl;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

public class GC_SettingsController extends SettingsController
{
	@FXML ScrollPane root;
	@FXML AnchorPane contentRoot;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		System.out.println("Initializing GCSC");
		root.setFitToWidth(true);
		root.setFitToHeight(true);
	}

	public void resize(double newSettingsPanelWidth, double newSettingsPanelHeight)
	{
		System.out.println("resize");
		System.out.println("nSPW = " + newSettingsPanelWidth);
//		root.setMinSize(newSettingsPanelWidth, root.getMinHeight());
//		root.setPrefSize(newSettingsPanelWidth, root.getPrefHeight());
//		root.setPrefWidth(10);
//		root.setPrefViewportWidth(10);
		
//		contentRoot.setPrefSize(newSettingsPanelWidth, contentRoot.getPrefHeight());
	}
}
