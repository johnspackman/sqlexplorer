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

import net.sourceforge.sqlexplorer.parsers.ParserException;
import net.sourceforge.sqlexplorer.parsers.AnnotatedQuery;
import net.sourceforge.sqlexplorer.parsers.AbstractSyntaxQueryParser;
import net.sourceforge.sqlexplorer.parsers.Query.QueryType;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.Token;
import net.sourceforge.sqlexplorer.parsers.Tokenizer.TokenType;
import net.sourceforge.sqlexplorer.util.BackedCharSequence;

/**
 * Implements a query parser for Oracle
 * @author John Spackman
 *
 */
public class OracleQueryParser extends AbstractSyntaxQueryParser {

	/*
	 * Parser state variables
	 */
	// Whether we're in the middle of some PL/SQL
	private boolean inPlSql;
	
	// Whether we've seen at least one BEGIN
	private boolean seenBegin;
	
	// The token at the start of the currently-being-parsed query
	private Token start;
	
	// How deep in begin...end we are currently 
	private int beginEndDepth;
	
	// The type of the query
	private QueryType queryType;
	
	// The type of object being created if this is a "CREATE ... " DDL statement
	private String createType;
	
	// The name of the object being created if this is a "CREATE ... " DDL statement
	private String createName;

	/**
	 * Constructor
	 * @param sql
	 * @param enableStructuredComments
	 */
	public OracleQueryParser(CharSequence sql, boolean enableStructuredComments) {
		super(sql, enableStructuredComments);
	}

	/**
	 * Constructor
	 * @param sql
	 */
	public OracleQueryParser(CharSequence sql, int initialLineNo) {
		super(sql);
		setInitialLineNo(initialLineNo);
	}

	/**
	 * Parses the text into a series of queries
	 */
	protected void parseQueries() throws ParserException {
		
		// Reset our state variables
		reset();
		
		Token token = null;
		Token lastToken = null;
		while ((token = nextToken()) != null) {
			TokenType type = token.getTokenType();
			if (start == null && (type == TokenType.EOL_COMMENT || type == TokenType.ML_COMMENT))
				continue;
			
			lastToken = token;
			
			if (start == null)
				start = token;
			
			if (type == TokenType.PUNCTUATION) {
				String value = token.toString();
				
				// For SQL*PLUS compatability, a / at the start of a line forces a break
				if (token.getCharNo() == 1 && value.equals("/")) {
					if (start == token)
						start = null;
					else
						addQuery(lastToken());
					continue;
				}
				
				// Not in PL/SQL and a semi-colon?  Then it's the end of a SQL query
				if (value.equals(";") && (!inPlSql || (beginEndDepth == 0 && seenBegin))) {
					// If we're at the end of PL/SQL, then we *include* the semi-colon,
					//	otherwise we skip it
					if (inPlSql)
						addQuery(token);
					else if (start == token)
						start = null;
					else
						addQuery(lastToken());
					continue;
				}
			}
			
			if (type == TokenType.WORD) {
				String word = token.toString();
				
				// DDL statement; can either be a straight forward SQL command ("CREATE TABLE ..."), or
				//	it's creating a PL/SQL statement which will have code and theerfore semi-colons and
				//	other SQL in it
				if (word.equalsIgnoreCase("CREATE")) {
					if (queryType == null)
						queryType = QueryType.DDL;
					nextToken();
					
					// Skip optional OR REPLACE
					final String[] OR_REPLACE = new String[] { "OR", "REPLACE" };
					if (matchWordSeq(OR_REPLACE, false))
						nextToken();
					token = getCurrentToken();
					
					// Remember the type of object being created and it's name
					if (token.getTokenType() == TokenType.WORD) {
						createType = token.toString().toUpperCase();
						int tokens = 1;
						if (createType.equalsIgnoreCase("PACKAGE")) {
							Token tmp = nextToken();
							if (tmp.toString().equalsIgnoreCase("BODY")) {
								createType = createType + " BODY";
								tokens++;
							} else
								ungetToken();
						}
						Token name = nextToken();
						createName = name.toString().toUpperCase();
						while (tokens > 0) {
							ungetToken();
							tokens--;
						}
					}
					
					// "CREATE PACKAGE [BODY]..." has an "END" but no "BEGIN", so increase the begin/end
					//	depth
					if (token.getTokenType() == TokenType.WORD && token.toString().equalsIgnoreCase("PACKAGE")) {
						nextToken();
						inPlSql = true;
						beginEndDepth++;
						seenBegin = true;
						
					// "CREATE type name" where type is TRIGGER, PROCEDURE, or FUNCTION *does* have a BEGIN
					} else {
						// PL/SQL DDL?
						final String[] PLSQL_DDL = new String[] { "PROCEDURE", "FUNCTION", "TRIGGER" };
						if (matchAnyWord(PLSQL_DDL))
							inPlSql = true;
					}
				
				// Other DDL keywords
				} else if (word.equalsIgnoreCase("GRANT")) {
					if (queryType == null)
						queryType = QueryType.DDL;
				
				// DECLARE also puts us in PL/SQL mode
				} else if (word.equalsIgnoreCase("DECLARE")) {
					if (queryType == null)
						queryType = QueryType.CODE;
					inPlSql = true;
				
				// BEGIN puts us in PL/SQL mode
				} else if (word.equalsIgnoreCase("BEGIN")) {
					if (queryType == null)
						queryType = QueryType.CODE;
					inPlSql = true;
					seenBegin = true;
					beginEndDepth++;
					
				} else if (word.equalsIgnoreCase("SELECT")) {
					if (queryType == null)
						queryType = QueryType.SELECT;
				
				} else if (word.equalsIgnoreCase("UPDATE")) {
					if (queryType == null)
						queryType = QueryType.DML;
				
				} else if (word.equalsIgnoreCase("DELETE")) {
					if (queryType == null)
						queryType = QueryType.DML;
				
				} else if (word.equalsIgnoreCase("INSERT")) {
					if (queryType == null)
						queryType = QueryType.DML;
				
				// If we're already in PL/SQL, then we have to count BEGIN/END pairs to know when the code
				//	runs out.
				} else if (inPlSql) {
					// END
					if (word.equalsIgnoreCase("END")) {
						beginEndDepth--;
					} else {
						final String[] BEGIN_WORDS = new String[] { "LOOP", "IF", "CASE" };
						boolean followingEnd = lastToken().getTokenType() == TokenType.WORD && lastToken().toString().equalsIgnoreCase("END");
						
						// Some keywords start a begin/end pairing implicitly; their END can also optionally be
						//	followed by the keyword again (eg "WHILE condition LOOP ... END LOOP").
						if (!followingEnd && matchAnyWord(BEGIN_WORDS))
							beginEndDepth++;
					}
				}
				
				continue;
			}
		}
		
		// Whatever's left is the last query
		if (start != null && token == null)
			addQuery(start, lastToken);
	}
	
	/**
	 * Compares the current token to a series of words, returning true if any
	 * of them match; the current token is not changed.
	 * @param words
	 * @return true if current token is a TokenType.WORD and any of words match it
	 */
	private boolean matchAnyWord(final String[] words) {
		if (getCurrentToken().getTokenType() != TokenType.WORD)
			return false;
		String token = getCurrentToken().toString();
		for (String word : words)
			if (word.equalsIgnoreCase(token))
				return true;
		return false;
	}
	
	/**
	 * Looks for a sequence of words in the token stream, starting with the current token.
	 * If rewindAfter is true then the current token is NOT changed, otherwise the current
	 * token will be set to the last token in the sequence.  If the sequence is not fully
	 * matched then the current token will not be changed regardless of the value of 
	 * rewindAfter. 
	 * @param words
	 * @param rewindAfter
	 * @return true if the sequence can be found
	 */
	private boolean matchWordSeq(final String[] words, boolean rewindAfter) throws ParserException {
		int count = 0;
		boolean ok = true;
		for (String word : words) {
			Token token = (count == 0) ? getCurrentToken() : nextToken();
			count++;
			if (token == null || token.getTokenType() != TokenType.WORD || !token.toString().equalsIgnoreCase(word)) {
				ok = false;
				rewindAfter = true;
				break;
			}
		}
		if (rewindAfter)
			for (; count > 1; count--)
				ungetToken();
		return ok;
	}
	
	/* (non-Javadoc)
	 * @see net.sourceforge.sqlexplorer.parsers.AbstractSyntaxQueryParser#newQueryInstance(java.lang.CharSequence, int)
	 */
	@Override
	protected AnnotatedQuery newQueryInstance(BackedCharSequence buffer, int lineNo) {
		OracleQuery query = new OracleQuery(buffer, lineNo, queryType == null ? QueryType.UNKNOWN : queryType);
		query.setCreateObjectName(createName);
		query.setCreateObjectType(createType);
		return query;
	}

	/**
	 * Adds a query, starting at start and ending inclusively (with endToken)  
	 * @param endToken
	 */
	private void addQuery(Token endToken) {
		addQuery(start, endToken);
		reset();
	}
	
	/**
	 * Resets the query parser state
	 */
	private void reset() {
		inPlSql = false;
		seenBegin = false;
		beginEndDepth = 0;
		start = null;
		queryType = null;
		createName = null;
		createType = null;
	}

}
