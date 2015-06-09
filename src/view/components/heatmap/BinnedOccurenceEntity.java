package view.components.heatmap;

/**
 * Auxiliary class for readability purposes.
 * @author RM
 *
 */
public class BinnedOccurenceEntity
{
	private int[][] binMatrix;
	private int minOccurenceCount;
	private int maxOccurenceCount;
	
	public BinnedOccurenceEntity(int[][] binMatrix, int minOccurenceCount, int maxOccurenceCount)
	{
		this.binMatrix			= binMatrix;
		this.minOccurenceCount	= minOccurenceCount;
		this.maxOccurenceCount	= maxOccurenceCount;
	}

	public int[][] getBinMatrix()
	{
		return binMatrix;
	}

	public int getMinOccurenceCount()
	{
		return minOccurenceCount;
	}

	public int getMaxOccurenceCount()
	{
		return maxOccurenceCount;
	}
}
