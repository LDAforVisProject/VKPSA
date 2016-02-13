package control.analysisView.localScope;

/**
 * Monitors callbacks from javascript to Java.
 * Adapted from http://stackoverflow.com/questions/30390257/how-to-catch-return-value-from-javascript-in-javafx/30390913#30390913.
 * @author RM
 *
 */
public class JSCallbackMonitor
{
	/**
	 * Generating controller.
	 */
	private ChordDiagramController controller;
	
	
	public JSCallbackMonitor(ChordDiagramController controller)
	{
		this.controller = controller;
	}
	
	/**
	 * Processes data fetched after user clicked on a distance chord (identified
	 * by the respective LDA configuration and topic IDs).
	 * @param fromID
	 * @param toID
	 */
    public void processChordData(Object fromID, Object toID)
    {
    	final String[] fromValues	= ((String) fromID).split("#");
    	final String[] toValues		= ((String) toID).split("#");
    	
    	final int ldaID1	= Integer.parseInt(fromValues[0]);
    	final int topicID1	= Integer.parseInt(fromValues[1]);
    	final int ldaID2	= Integer.parseInt(toValues[0]);
    	final int topicID2	= Integer.parseInt(toValues[1]);
    	
    	// Propagate information.
    	controller.propagateSelectedDistanceInformation(ldaID1, ldaID2, topicID1, topicID2);
    }
    
    /**
     * Processes hovering over group/LDA config info.
     * @param groupName
     */
    public void processLDAConfigHover(Object groupName)
    {
    	final String[] values	= ((String) groupName).split("#");
    	
//    	controller.propagateLDAHoverInformation(Integer.parseInt(values[0]));
    }
    
    /**
     * Process information that no group is hovered anymore.
     */
    public void processLDAConfigHoverExited()
    {
    	controller.propagateLDAHoverExitedInformation();
    }
}
