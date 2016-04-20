package model.misc;

/**
 * POJO used to store information about rank of a keyword in a topic.
 * Due to performance reasons {@link KeywordRankObject} is not context-sensitive, i.e.
 * it does not contain information about which keyword this instance refers to (to 
 * avoid handling redundant information).
 * The context is generated via storing these objects in a specifically identified
 * collection.
 * 
 * @author RM
 *
 */
public class KeywordRankObject
{
	private int topicID;
	private int ldaConfigID;
	/**
	 * Rank of keyword in topic.
	 */
	private int rank;
	
	/**
	 * Initialize new KeywordRankObject.
	 * @param topicID
	 * @param ldaConfigID
	 * @param rank
	 */
	public KeywordRankObject(final int topicID, final int ldaConfigID, final int rank)
	{
		this.topicID		= topicID;
		this.ldaConfigID	= ldaConfigID;
		this.rank			= rank;
	}

	public int getTopicID()
	{
		return topicID;
	}

	public int getLDAConfigID()
	{
		return ldaConfigID;
	}

	public int getRank()
	{
		return rank;
	}
}
