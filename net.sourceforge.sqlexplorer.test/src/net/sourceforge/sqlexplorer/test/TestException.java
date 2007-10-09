package net.sourceforge.sqlexplorer.test;

import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.sqlexplorer.parsers.Tokenizer;

@SuppressWarnings("serial")
public class TestException extends Exception {

	private LinkedList<Tokenizer.Token> tokens;
	
	public TestException(String message, LinkedList<Tokenizer.Token> tokens) {
		super(message);
		this.tokens = tokens;
	}

	public TestException(String arg0) {
		super(arg0);
	}

	public Collection<Tokenizer.Token> getTokens() {
		return tokens;
	}
}
