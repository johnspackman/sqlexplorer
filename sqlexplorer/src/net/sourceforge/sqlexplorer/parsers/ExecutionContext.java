/**
 * 
 */
package net.sourceforge.sqlexplorer.parsers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Heiko
 *
 */
public class ExecutionContext 
{
	private Map<String,String> options = new HashMap<String, String>();
	
	public void set(String pOption, String pValue)
	{
		this.options.put(pOption, pValue);
	}
	
	public String get(String pOption)
	{
		return this.options.get(pOption);
	}
}
