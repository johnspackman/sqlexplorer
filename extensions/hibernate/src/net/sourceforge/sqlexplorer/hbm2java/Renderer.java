//$Id$
package net.sourceforge.sqlexplorer.hbm2java;


import java.io.PrintWriter;
import java.util.Map;


public interface Renderer {
	public void render(String savedToPackage, String savedToClass, ClassMapping classMapping, Map class2classmap, PrintWriter writer) throws Exception;
}






