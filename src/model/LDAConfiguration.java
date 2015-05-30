package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LDAConfiguration
{
	// -----------------------------------------------
	// 				Parameters
	// -----------------------------------------------
	
	private int k;
	private double alpha;
	private double eta;
	
	/**
	 * Contains all LDA parameters currently used in a LDA configuration.
	 */
	public static final Map<String, Integer> PARAMETER_MAP;
	static {
	        Map<String, Integer> tempParameterMap	= new HashMap<String, Integer>();
	        tempParameterMap.put("k", -1);
	        tempParameterMap.put("eta", -1);
	        tempParameterMap.put("alpha", -1);
	        PARAMETER_MAP 							= Collections.unmodifiableMap(tempParameterMap);
	}
	   
	
	// -----------------------------------------------	
	// -----------------------------------------------
	// 					Methods
	// -----------------------------------------------
	// -----------------------------------------------	
	
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
	
	/**
	 * Auto-generated method for generating the hash code of this object.
	 * @return
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result		= 1;
		long temp;
		
		temp	= Double.doubleToLongBits(alpha);
		result	= prime * result + (int) (temp ^ (temp >>> 32));
		temp	= Double.doubleToLongBits(eta);
		result	= prime * result + (int) (temp ^ (temp >>> 32));
		result	= prime * result + k;
		
		return result;
	}

	/**
	 * Auto-generated method for comparing two objects.
	 * @param obj
	 * @return
	 */	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		LDAConfiguration other = (LDAConfiguration) obj;
		
		if (Double.doubleToLongBits(alpha) != Double.doubleToLongBits(other.alpha))
			return false;
		
		if (Double.doubleToLongBits(eta) != Double.doubleToLongBits(other.eta))
			return false;
		
		if (k != other.k)
			return false;
		
		return true;
	}
	
	public static LDAConfiguration generateLDAConfiguration(String ldaConfigString) throws Exception
	{
		Map<String, Integer> paramMapCopy	= new HashMap<String, Integer>(LDAConfiguration.PARAMETER_MAP);
		Map<String, Double> paramValueMap	= new HashMap<String, Double>();
		
		// Check for completeness of configuration string.
		paramMapCopy.put("k", ldaConfigString.indexOf("k="));
		paramMapCopy.put("eta", ldaConfigString.indexOf("eta="));
		paramMapCopy.put("alpha", ldaConfigString.indexOf("alpha="));
		
		for (String key : paramMapCopy.keySet()) {
			if (paramMapCopy.get(key) < 0) {
				throw new Exception("### ERROR ### Metadata incomplete in configuration string @" + key);
			}
		}
		
		// Parse configuration string.
		for (String paramSubString : ldaConfigString.split("\\|")) {
			String[] paramSubSubStrings = paramSubString.split("=");
			if (paramSubSubStrings.length == 2) {
				paramValueMap.put(paramSubSubStrings[0], Double.valueOf(paramSubSubStrings[1]));
			}
		}
		
		// Return newly created LDA configuration.
		return new LDAConfiguration( 	paramValueMap.get("k").intValue(), 
										paramValueMap.get("alpha"),
										paramValueMap.get("eta")
									);
	}
	
	public static ArrayList<LDAConfiguration> generateLDAConfigurations(int numberOfDivisions, int numberOfDatasetsToGenerate, String samplingMode, Map<String, ArrayList<Double>> parameterValueLists)
	{
		ArrayList<LDAConfiguration> configList = new ArrayList<LDAConfiguration>();
		
		switch (samplingMode) {
			// numberOfDivisions == numberOfDatasetsToGenerate.
			// Combine element at index [i] in list of every parameter to new LDAConfiguration.
			case "Random":
				for (int i = 0; i < numberOfDatasetsToGenerate; i++) {
					int kappa		= parameterValueLists.get("kappa").get(i).intValue();
					double alpha	= parameterValueLists.get("alpha").get(i);
					double eta		= parameterValueLists.get("eta").get(i);
					
					configList.add(new LDAConfiguration(kappa, alpha, eta)); 
				}
			break;
			
			default:
				System.out.println("Sampling mode '" + samplingMode + "' currently not supported.");
		}
		
		return configList;
	}
	
	// -----------------------------------------------
	// 				Getter and setter
	// -----------------------------------------------	

	public int getK()
	{
		return k;
	}

	public double getAlpha()
	{
		return alpha;
	}

	public double getEta()
	{
		return eta;
	}
	
	public void setK(int k)
	{
		this.k = k;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

	public void setEta(double eta)
	{
		this.eta = eta;
	}

	@Override
	public String toString()
	{
		return "k=" + k + "|alpha=" + alpha + "|eta=" + eta;
	}
}
