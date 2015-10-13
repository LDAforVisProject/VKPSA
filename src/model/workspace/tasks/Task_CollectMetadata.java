package model.workspace.tasks;

import java.util.ArrayList;
import java.util.Map;

import database.DBManagement;
import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_CollectMetadata extends WorkspaceTask
{
	public Task_CollectMetadata(Workspace workspace, WorkspaceAction workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}

	@Override
	protected Integer call() throws Exception
	{
		// Open connection to database.
		DBManagement db									= workspace.getDatabaseManagement();
		
		// Collect LDA configurations.
		ArrayList<LDAConfiguration> ldaConfigurations	= db.loadLDAConfigurations(this);
				
		// Update task progress.
		updateProgress(1, 1);
		
		// Transfer LDA configuration collection.
		workspace.setLDAConfigurations(ldaConfigurations);
		
		// Update number of datasets in workspace.
		workspace.setNumberOfDatasetsInWS(ldaConfigurations.size());
		
		// Tell workspace that metadata was loaded.
		workspace.setMetadataLoaded(true);
		
		return ldaConfigurations.size();
	}
}

/*

 // File-based version:
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
					
					// Open file reader.
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
 
*/
