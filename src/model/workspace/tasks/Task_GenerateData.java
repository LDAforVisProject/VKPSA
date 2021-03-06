package model.workspace.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import database.DBManagement;
import model.LDAConfiguration;
import model.workspace.Workspace;
import model.workspace.TaskType;

public class Task_GenerateData extends WorkspaceTask
{
	public Task_GenerateData(Workspace workspace, TaskType workspaceAction, final Map<String, Integer> additionalOptionSet)
	{
		super(workspace, workspaceAction, additionalOptionSet);
	}
	
	@Override
	protected Integer call() throws Exception
	{
		System.out.println("Generating data.");
		
		try {
			// Init task progress.
			updateProgress(0, 1);
			
			// Get configuration options.
			Map<String, String> configurationOptions				= workspace.getConfigurationOptions();
			ArrayList<LDAConfiguration> configurationsToGenerate	= workspace.getConfigurationsToGenerate();
			
			/*
			 * Build Python/LDA script command.
			 * 
			 * Pattern:
			 *  	PYTHON_PATH LDA_SCRIPT_PATH -p PASS_COUNT -m MODE -i INPUT_PATH -o OUTPUT_PATH
			 * Example:
			 * 		"D:\Programme\Python27\python.exe" "D:\Workspace\LDA\analysis\Nexus.py" -p 15 -m sample -i "D:\Workspace\Scientific Computing\VKPSA_data\toGenerate.lda" -o "D:\Workspace\Scientific Computing\VKPSA_data\vkpsa.db"

			 */ 

			String python_path	= configurationOptions.get("python_path");
			String lda_path		= configurationOptions.get("lda_path");
			int pass_count		= Integer.parseInt(configurationOptions.get("pass_count"));
			String input_path	= Paths.get(workspace.getDirectory(), Workspace.FILENAME_TOGENERATE).toString();
			String output_path	= workspace.getDirectory() + "\\" + workspace.DBNAME;
			
			/*
			 * Start multiple threads each processing a part of the parameter file list.
			 */
			
			// Close DB connection before Python script is using it.
			workspace.closeDB();

			// Put command in quotation marks to account for space-containing paths.
			String command = "\"" + python_path + "\" \"" + lda_path + "\" -p " + pass_count + " -m sample -i \"" + input_path + "\" -o \"" + output_path + "\"";
			System.out.println("Executing LDA with command\n\t" + command);
			
			// Execute system call with command.
			// Be careful, hic sunt dragones: Only tested on a Windows 7 / 64-bit environment.
			Process process = Runtime.getRuntime().exec(command);
			
			// Consume messages from input stream.
			consumeProcessOutputStreams(process, configurationsToGenerate.size());
				
	      	// Finish only after child process(es) has/have finished.
	      	//process.waitFor();

			// Clear workspace collection containing parameters to generate.
			configurationsToGenerate.clear();
			
			// Reopen database.
			workspace.reopenDB();
			
			// Update task progress.
			updateProgress(1, 1);
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Consumes lines outputted by the LDA Python script. Each line represents  
	 * one finished (e.g. generated and written in database) dataset.
	 * @param process
	 * @param numberOfDatasetsToGenerate
	 */
	private void consumeProcessOutputStreams(Process process, int numberOfDatasetsToGenerate)
	{
		// Count lines output
		int messageCount = 0;
		
		try {
			String line			= null;
			BufferedReader bri	= new BufferedReader(new InputStreamReader(process.getInputStream()));
	      	BufferedReader bre	= new BufferedReader(new InputStreamReader(process.getErrorStream()));
	     
	      	// Read/consume stdout.
//	      	while ((line = bri.readLine()) != null) {
//	      		System.out.println(line);
//	      	}
	      	
	      	// Read/consume stderr.
	      	while ((line = bre.readLine()) != null) {
	      		System.out.println(line);
	      		
	      		// Update progress.
	      		updateProgress(++messageCount, numberOfDatasetsToGenerate);
	      	}
	      	
	      	// Close readers and streams.
	      	bri.close();
	      	bre.close();
	      	process.getInputStream().close();
	      	process.getErrorStream().close();
	      	
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}