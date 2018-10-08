/*
 * Copyright (C) 2007 SQL Explorer Development Team
 * http://sourceforge.net/projects/eclipsesql
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.sqlexplorer.oracle.dbproduct;


import org.dom4j.Element;
import oracle.sql.OPAQUE;
import net.sourceforge.sqlexplorer.ExplorerException;
import net.sourceforge.sqlexplorer.dataset.XmlDataType;

/**
 * 
 * @author John Spackman
 */
public class OracleXmlDataType implements XmlDataType {

//	private OPAQUE opaque;

	public OracleXmlDataType(OPAQUE opaque) {
		super();
/*		
		this.opaque = opaque;
		try {
			XMLType xmlType = XMLType.createXML(opaque);
			InputStreamReader reader = new InputStreamReader(xmlType.getInputStream());
			char[] buffer = new char[4000];
			reader.read(buffer);
		}catch(SQLException e) {
			System.out.println(e.getMessage());
		}catch(IOException e) {
			System.out.println(e.getMessage());
		}
*/
	}

	/* (non-JavaDoc)
	 * @see net.sourceforge.sqlexplorer.dataset.XmlDataType#getRootElement()
	 */
	public Element getRootElement() throws ExplorerException {
		return null;
/*
		try {
			XMLType xmlType = XMLType.createXML(opaque);
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader = new StdXMLReader(xmlType.getInputStream());
			parser.setReader(reader);
			return (IXMLElement) parser.parse();
		}catch(IOException e) {
			throw new ExplorerException(e);
		}catch(SQLException e) {
			throw new ExplorerException(e);
		}catch(XMLException e) {
			throw new ExplorerException(e);
		}catch(InstantiationException e) {
			throw new ExplorerException(e);
		}catch(ClassNotFoundException e) {
			throw new ExplorerException(e);
		}catch(IllegalAccessException e) {
			throw new ExplorerException(e);
		}
*/
	}

	/* (non-JavaDoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return 0; // Not implemented
	}

	/* (non-JavaDoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<xml>";
	}
}
