package model;

public class LDAConfiguration
{
	private int k;
	private double alpha;
	private double eta;
	
	private String filename;
	
	public LDAConfiguration(int k, double alpha, double eta)
	{
		this.k		= k;
		this.alpha	= alpha;
		this.eta	= eta;
	}
	
	public LDAConfiguration(final LDAConfiguration source)
	{
		this.k		= source.k;
		this.alpha	= source.alpha;
		this.eta	= source.eta;		
	}
	
//	@todo Override .hashCode() - http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java.
	@Override
	public boolean equals(Object object)
	{
		if (object instanceof LDAConfiguration) {
			LDAConfiguration set = (LDAConfiguration) object;
			
			if (this.k == set.k && this.alpha == set.alpha && this.eta == set.eta)
				return true;
		}
		
		return false;
	}
}
