package model.documents;

/**
 * Contains (meta-)data for a scientific paper. 
 * @author RM
 *
 */
public class Document
{
	private int id;
	private float probability;
	private String title;
	private String authors;
	private String keywords;
	private String originalAbstract;
	private String refinedAbstract;
	private String date;
	private String conference;
	
	public Document(int id, float probability, String title, String authors, String keywords, String originalAbstract, String refinedAbstract, String date, String conference)
	{
		this.id 				= id;
		this.probability		= probability;
		this.title				= title;
		this.authors			= authors;
		this.keywords			= keywords;
		this.originalAbstract	= originalAbstract;
		this.refinedAbstract	= refinedAbstract;
		this.date				= date;
		this.conference			= conference;
	}
	
	public DocumentForLookupTable generateDocumentForLookup()
	{
		return new DocumentForLookupTable(id, probability, title, authors, date, conference);
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getAuthors()
	{
		return authors;
	}
	
	public String getKeywords()
	{
		return keywords;
	}
	
	public String getOriginalAbstract()
	{
		return originalAbstract;
	}
	
	public String getProcessedAbstract()
	{
		return refinedAbstract;
	}
	
	public String getDate()
	{
		return date;
	}
	
	public String getConference()
	{
		return conference;
	}

	public float getProbability()
	{
		return probability;
	}
	
	/**
	 * Determines whether instance contains specified string in one of it's attributes.
	 * @param term
	 * @return
	 */
	public boolean containsTerm(final String term)
	{
		return 	term == null								||
				String.valueOf(probability).contains(term) 	||
				title.contains(term)						||
				authors.contains(term) 						||
				keywords.contains(term) 					||
				originalAbstract.contains(term) 			||
				refinedAbstract.contains(term) 				||
				date.contains(term) 						||
				conference.contains(term);			
	}
}
