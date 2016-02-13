package view.components.legacy;

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
	private double max_key1;
	private double min_key1;
	private double max_key2;
	private double min_key2;
	
	public BinnedOccurenceEntity(int[][] binMatrix, int minOccurenceCount, int maxOccurenceCount, double min_key1, double max_key1, double min_key2, double max_key2)
	{
		this.binMatrix			= binMatrix;
		this.minOccurenceCount	= minOccurenceCount;
		this.maxOccurenceCount	= maxOccurenceCount;
		this.min_key1			= min_key1;
		this.max_key1			= max_key1;
		this.min_key2			= min_key2;
		this.max_key2			= max_key2;
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

	public double getMaxKey1()
	{
		return max_key1;
	}

	public double getMinKey1()
	{
		return min_key1;
	}

	public double getMaxKey2()
	{
		return max_key2;
	}

	public double getMinKey2()
	{
		return min_key2;
	}
}
