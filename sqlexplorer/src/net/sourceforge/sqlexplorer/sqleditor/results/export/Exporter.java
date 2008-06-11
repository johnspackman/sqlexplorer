package net.sourceforge.sqlexplorer.sqleditor.results.export;

/**
 * Implements 
 * @author John Spackman
 *
 */
public interface Exporter {

	/**
	 * Get export format title
	 */
	public String getTitle();

	/**
	 * Get dialog's file filter when choosing input file.
	 * 
	 * @return List of file patterns (already containing '*').
	 */
	public String[] getFileFilter();
}
