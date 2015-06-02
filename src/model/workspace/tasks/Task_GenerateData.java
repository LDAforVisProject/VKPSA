package model.workspace.tasks;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import model.workspace.Workspace;
import model.workspace.WorkspaceAction;

public class Task_GenerateData extends Task_WorkspaceTask
{
	public Task_GenerateData(Workspace workspace, WorkspaceAction workspaceAction)
	{
		super(workspace, workspaceAction);
	}
	
	@Override
	protected Integer call() throws Exception
	{
		System.out.println("Generating data.");
		
		try {
			// Init task progress.
			updateProgress(0, 1);
			
			// Get configuration options.
			Map<String, String> configurationOptions = workspace.getConfigurationOptions();

			/*
			 * Build Python/LDA script command.
			 * 
			 * Pattern:
			 *  	PYTHON_PATH LDA_SCRIPT_PATH -p PASS_COUNT -m MODE -i INPUT_PATH -o OUTPUT_PATH
			 * Example:
			 * 		D:\Programme\Python27\python.exe D:\Workspace\LDA\analysis\Nexus.py -p 1 -m 'sample' -i 'D:\Workspace\Scientific Computing\VKPSA\src\data\toGenerate.lda' -o 'D:\Workspace\Scientific Computing\VKPSA\src\data'
			 */ 
			
			String python_path	= configurationOptions.get("python_path");
			String lda_path		= configurationOptions.get("lda_path");
			// @todo Add pass count as option to interface (?).
			int pass_count		= Integer.parseInt(configurationOptions.get("pass_count"));
			String input_path	= Paths.get(workspace.getDirectory(), Workspace.FILENAME_TOGENERATE).toString();
			String output_path	= workspace.getDirectory();
			
			// @todo Idea for optimization: Truncate parameter file list / instruct Python script to process only a defined part of it.
			// Start multiple threads each processing a part of the parameter file list.
			
			// Put command in quotation marks to account for space-containing paths.
			String command = "\"" + python_path + "\" \"" + lda_path + "\" -p " + pass_count + " -m sample -i \"" + input_path + "\" -o \"" + output_path + "\"";
			System.out.println("command = " + command);
			
			// Execute system call with command.
			// Be careful: Only tested on a Windows 7 64-bit environment.
			Process process = Runtime.getRuntime().exec(command);

			/**
			 * BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	    StringBuilder out = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	        out.append(line);
	    }
			 */

			System.out.println("finished");
			
			// Update task progress.
			updateProgress(1, 1);
		}
		
		catch (Exception e) {
			
		}
		
		return null;
	}

}
