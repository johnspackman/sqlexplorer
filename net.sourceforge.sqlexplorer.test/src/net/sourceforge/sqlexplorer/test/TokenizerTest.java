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
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.sqlexplorer.parsers.Tokenizer;
import net.sourceforge.sqlexplorer.util.TextUtil;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Test class for Tokenizer.  It reads tok_*.xml files in the current directory (i.e. the
 * project root); each file is expected to be in the form:
 * 
 * 	<tests>
 * 		<test>
 * 			<data/>
 * 			<t line="n"/>
 * 		</test>
 *	</tests>
 * 
 * There are one or more <test/> elements, each of which has a <data/> tag and one or more
 * <t/> tags.  The contents of <data/> is the text to parse, and <t/> is an expected token.
 * <t/> has an attribute called type which is the enum name of the token type, and the tags'
 * text is the expected token text.  For comments, the whitespace is ignored when comparing.
 * 
 * the <t/> tag has a line attribute which is the line number that the tokenizer should report
 * the token starting on.  Note that whitespace at the start of the <q/> tag is ignored, i.e.
 * the line of text inside <q> with a non-whitespace character is line 1.
 * 
 * @author John Spackman
 */
public class TokenizerTest {
	
	public TokenizerTest() throws Exception {
		super();
		File file = new File(".");
		File files[] = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith("tok_") && name.endsWith(".xml");
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
		int testNo = 0;
		for (Element test : (List<Element>)root.elements("test")) {
			testNo++;
			System.out.print("\n   " + testNo + ":: ");
			runTest(test);
		}
		System.out.println("\ndone\n");
	}
	
	private void runTest(Element test) throws Exception {
		Tokenizer tokenizer = new Tokenizer(new StringBuffer(test.element("data").getText().trim()));
		List<Element> expectedResults = test.elements("t");
		Tokenizer.Token token;
		LinkedList<Tokenizer.Token> tokens = new LinkedList<Tokenizer.Token>();
		int tokNo = 0;
		while ((token = tokenizer.nextToken()) != null) {
			tokNo++;
			System.out.print(" " + tokNo);
			tokens.add(token);
			if (expectedResults.isEmpty())
				throw new TestException("Too many tokens found", tokens);
			
			Element expect = expectedResults.remove(0);
			Tokenizer.TokenType expectedType = Tokenizer.TokenType.valueOf(expect.attributeValue("type").toUpperCase());
			int expectedLineNo = Integer.parseInt(expect.attributeValue("line"));
			String expectedValue = expect.getTextTrim();
			
			if (expectedType != token.getTokenType())
				throw new TestException("Wrong token type, expected " + expectedType + ", found " + token.getTokenType(), tokens);
			if (expectedLineNo != token.getLineNo())
				throw new TestException("Wrong line number, expected " + expectedLineNo + " found " + token.getLineNo());
			String tokenValue = token.toString();
			if (expectedType == Tokenizer.TokenType.EOL_COMMENT || expectedType == Tokenizer.TokenType.ML_COMMENT) {
				expectedValue = TextUtil.compressWhitespace(expectedValue);
				tokenValue = TextUtil.compressWhitespace(tokenValue);
			}
			if (!expectedValue.equalsIgnoreCase(tokenValue))
				throw new TestException("Wrong value, expected " + expectedValue + ", found " + token.toString(), tokens);
		}
		if (!expectedResults.isEmpty())
			throw new TestException("Not enough results found", tokens);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(new File(".").getAbsolutePath());
		new TokenizerTest();
	}

}
