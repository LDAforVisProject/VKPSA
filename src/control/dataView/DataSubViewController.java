package control.dataView;

import java.net.URL;
import java.util.ResourceBundle;

import control.Controller;

public abstract class DataSubViewController extends Controller
{
	protected DataViewController dataViewController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}
	
	/**
	 * Stores reference to supervising DataViewController.
	 * @param dataViewController
	 */
	public void setDataViewController(DataViewController dataViewController)
	{
		this.dataViewController = dataViewController;
	}
	
	/**
	 * Freezes option controls.
	 */
	abstract public void freezeOptionControls();
	/**
	 * Unfreezes option controls.
	 */
	abstract public void unfreezeOptionControls();
}
