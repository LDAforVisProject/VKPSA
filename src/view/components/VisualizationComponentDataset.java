package view.components;

import java.util.ArrayList;

import model.LDAConfiguration;

public abstract class VisualizationComponentDataset
{
	/**
	 * References to all LDA configurations in current workspace.
	 */
	protected ArrayList<LDAConfiguration> allLDAConfigurations;
	

	public ArrayList<LDAConfiguration> getAllLDAConfigurations()
	{
		return allLDAConfigurations;
	}

	public void setAllLDAConfigurations(ArrayList<LDAConfiguration> allLDAConfigurations)
	{
		this.allLDAConfigurations = allLDAConfigurations;
	}
}
