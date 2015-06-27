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
	
	private int configurationID;
	private int kappa;
	private double alpha;
	private double eta;
	
	/**
	 * Lists all parametes currently fully or partially supported.
	 */
	public static String[] SUPPORTED_PARAMETERS = {"alpha", "eta", "kappa"};
	
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
	
	public LDAConfiguration(int configurationID, int kappa, double alpha, double eta)
	{
		this.configurationID 	= configurationID;
		this.kappa				= kappa;
		this.alpha				= alpha;
		this.eta				= eta;
	}
	
	public LDAConfiguration(final LDAConfiguration source)
	{
		this.configurationID	= source.configurationID;
		this.kappa				= source.kappa;
		this.alpha				= source.alpha;
		this.eta				= source.eta;		
	}
	
	public void copy(final LDAConfiguration source)
	{
		this.configurationID	= source.configurationID;
		this.kappa				= source.kappa;
		this.alpha				= source.alpha;
		this.eta				= source.eta;		
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
		result	= prime * result + kappa;
		
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
		
		if (kappa != other.kappa)
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
		return new LDAConfiguration( 	-1,
										paramValueMap.get("k").intValue(), 
										paramValueMap.get("alpha"),
										paramValueMap.get("eta")
									);
	}
	
	/**
	 * Combines already generated values to new LDAConfiguration instances, dependend on the chosen sampling mode.
	 * @param numberOfDivisions
	 * @param numberOfDatasetsToGenerate
	 * @param samplingMode
	 * @param parameterValueLists
	 * @return
	 */
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
					
					configList.add(new LDAConfiguration(-1, kappa, alpha, eta)); 
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

	public int getKappa()
	{
		return kappa;
	}

	public double getAlpha()
	{
		return alpha;
	}

	public double getEta()
	{
		return eta;
	}
	
	public void setKappa(int kappa)
	{
		this.kappa = kappa;
	}

	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}

	public void setEta(double eta)
	{
		this.eta = eta;
	}

	public double getParameter(String key)
	{
		double value = Double.MAX_VALUE;
		
		switch(key) {
			case "alpha":
				value = alpha;
			break;
			
			case "eta":
				value = eta;
			break;
			
			case "kappa":
				value = (double)kappa;
			break;
		}
		
		return value;
	}
	
	@Override
	public String toString()
	{
		return "k=" + kappa + "|alpha=" + alpha + "|eta=" + eta;
	}
	
	/**
	 * Returns corresponding symbol for parameter.
	 * @param key
	 * @return
	 */
	public static String getSymbolForParameter(String key)
	{
		String symbol;
		
		switch(key) {
			case "alpha":
				symbol = "α";
			break;
			
			case "eta":
				symbol = "η";
			break;
			
			case "kappa":
				symbol = "κ";
			break;
			
			default:
				symbol = "?";
		}
		
		return symbol;
	}

	public int getConfigurationID()
	{
		return configurationID;
	}
}
