package control.dataView;

import java.net.URL;
import java.util.ResourceBundle;

import control.Controller;

public class DataSubViewController extends Controller
{
	protected DataViewController dataViewController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}
	
	public void setDataViewController(DataViewController dataViewController)
	{
		this.dataViewController = dataViewController;
	}

}
