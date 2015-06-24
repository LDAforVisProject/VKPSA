package model.workspace;

public enum WorkspaceAction
{
	/*
	 * Singular/independent actions (involving exactly one - this - WorkspaceAction/task).
	 */
	COLLECT_FILE_METADATA,
	LOAD_RAW_DATA, LOAD_DISTANCES, LOAD_MDS_COORDINATES, LOAD_SPECIFIC_DATASETS,
	CALCULATE_DISTANCES, CALCULATE_MDS_COORDINATES,
	GENERATE_PARAMETER_LIST, GENERATE_DATA,
	/*
	 * Collective/dependent actions (involving one or more - other - WorkspaceActions).
	 */
	REFRESH,
	/*
	 * Maintainance actions. 
	 */
	SWITCH, RESET,
	/*
	 * Other actions.
	 */
	NONE
}
