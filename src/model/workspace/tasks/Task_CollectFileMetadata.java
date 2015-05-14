package model.workspace.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.util.Pair;
import model.LDAConfiguration;
import model.topic.Topic;
import model.topic.TopicKeywordAlignment;
import model.workspace.Dataset;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_CollectFileMetadata extends Task_WorkspaceTask
{
	public Task_CollectFileMetadata(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}

	@Override
	protected Integer call() throws Exception
	{
		int numberOfDatasets		= 0;
		int count					= 0;
		final String[] filenames	= new File(workspace.getDirectory()).list();
		
		// Collected LDA configurations.
		ArrayList<LDAConfiguration> ldaConfigurations = new ArrayList<LDAConfiguration>();
		
		// Iterate through files.
		for (String filename : filenames) {
			// If .csv: Process next file.
			try {
				if (filename.endsWith(".csv")) {
					numberOfDatasets++;
					
					InputStream fis			= new FileInputStream(Paths.get(workspace.getDirectory(), filename).toString());
				    InputStreamReader isr	= new InputStreamReader(fis, Charset.forName("UTF-8"));
				    BufferedReader reader	= new BufferedReader(isr);
				    
					// Read first line in dataset (contains metadata).
					ldaConfigurations.add(LDAConfiguration.generateLDAConfiguration(reader.readLine()));
					
					// Close reader.
					reader.close();
				}
			}
			
			catch (Exception e) {
				e.printStackTrace();
			}
		
			// Update task progress.
			updateProgress(count, filenames.length);
			
			count++;
		}
		
		// Update task progress.
		updateProgress(1, 1);
		
		// Transfer LDA configuration collection.
		workspace.setLDAConfigurations(ldaConfigurations);
		
		// Update number of datasets in workspace.
		workspace.setNumberOfDatasetsInWS(numberOfDatasets);
		
		// Tell workspace that metadata was loaded.
		workspace.setMetadataLoaded(true);
		
		return numberOfDatasets;
	}

}
