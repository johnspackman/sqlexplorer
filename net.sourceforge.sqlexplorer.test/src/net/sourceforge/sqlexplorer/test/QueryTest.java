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

package net.sourceforge.sqlexplorer.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import net.sourceforge.sqlexplorer.oracle.dbproduct.OracleQueryParser;
import net.sourceforge.sqlexplorer.parsers.Query;
import net.sourceforge.sqlexplorer.parsers.QueryParser;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Test class for QueryParser.  It reads qry_*.xml files in the current directory (i.e. the
 * project root); each file is expected to be in the form:
 * 
 * 	<tests>
 * 		<test>
 * 			<data/>
 * 			<q line="n"/>
 * 		</test>
 *	</tests>
 * 
 * There are one or more <test/> elements, each of which has a <data/> tag and one or more
 * <q/> tags.  The contents of <data/> is the text to parse, and <q/> is the query we expect
 * back.  Whitespace is ignored when comparing.
 * 
 * In the <q/> tag, the line="n" is the line number that the query should be reported by the
 * parser as starting on.  Note that whitespace at the start of the <q/> tag is ignored, i.e.
 * the line of text inside <q> with a non-whitespace character is line 1.
 * 
 * @author John Spackman
 */
public class QueryTest {
	
	public QueryTest() throws Exception {
		super();
		File file = new File(".");
		File files[] = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("qry_") && name.endsWith(".xml");
			}
		});
		if (files == null)
			throw new IOException("Cannot find any test files!");
		
		for (File testFile : files) {
			run(testFile);
		}
	}
	
	private void run(File file) throws Exception {
		System.out.print(file);
		SAXReader reader = new SAXReader();
		Element root = reader.read(file).getRootElement();
		int testNo = 1;
		for (Element test : (List<Element>)root.elements("test")) {
			System.out.print(" " + testNo);
			runTest(test);
			testNo++;
		}
		System.out.println("\ndone\n");
	}
	
	private void runTest(Element test) throws Exception {
		List<Element> expectedResults = test.elements("q");
		String sql = test.element("data").getText().trim();
		String strEnabled = test.attributeValue("enable-structured-comments");
		boolean enabled = (strEnabled != null) ? Boolean.parseBoolean(strEnabled) : false;
		QueryParser parser = new OracleQueryParser(sql, enabled);
		parser.parse();
		
		for (Query query : parser) {
			if (expectedResults.isEmpty())
				throw new TestException("Parser returned too many queries");
			Element expect = expectedResults.remove(0);
			int expectedLineNo = Integer.parseInt(expect.attributeValue("line"));
			String expectedValue = TextUtil.compressWhitespace(expect.getTextTrim());
			
			String actualValue = TextUtil.compressWhitespace(query.getQuerySql());
			int lineNo = query.getLineNo();
			lineNo = parser.adjustLineNo(lineNo);
			
			if (!expectedValue.equalsIgnoreCase(actualValue))
				throw new TestException("Differing query; found:\n" + actualValue + "\n============\nExpected:\n" + expectedValue + "\n==============");
			if (expectedLineNo != lineNo)
				throw new TestException("Wrong line number, expected " + expectedLineNo + " found " + lineNo + " (query lineNo=" + query.getLineNo() + ")");
		}
		if (!expectedResults.isEmpty())
			throw new TestException("Parser did not return enough queries");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(new File(".").getAbsolutePath());
		new QueryTest();
	}

}
