package view.components.scatterchart;

import java.util.ArrayList;

import model.LDAConfiguration;
import view.components.VisualizationComponentDataset;

public class ScatterchartDataset extends VisualizationComponentDataset
{
	private ArrayList<LDAConfiguration> discardedLDAConfigurations;
	private ArrayList<LDAConfiguration> inactiveLDAConfigurations;
	private ArrayList<LDAConfiguration> activeLDAConfigurations;
	
	public ScatterchartDataset(	ArrayList<LDAConfiguration> allLDAConfigurations, ArrayList<LDAConfiguration> discardedLDAConfigurations,
									ArrayList<LDAConfiguration> inactiveLDAConfigurations, ArrayList<LDAConfiguration> activeLDAConfigurations)
	{
		super(allLDAConfigurations);
		
		this.discardedLDAConfigurations	= discardedLDAConfigurations;
		this.inactiveLDAConfigurations	= inactiveLDAConfigurations;
		this.activeLDAConfigurations	= activeLDAConfigurations;
	}

	public ArrayList<LDAConfiguration> getDiscardedLDAConfigurations()
	{
		return discardedLDAConfigurations;
	}

	public ArrayList<LDAConfiguration> getInactiveLDAConfigurations()
	{
		return inactiveLDAConfigurations;
	}

	public ArrayList<LDAConfiguration> getActiveLDAConfigurations()
	{
		return activeLDAConfigurations;
	}

}
