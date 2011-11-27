/*
 * GRAL: GRAphing Library for Java(R)
 *
 * (C) Copyright 2009-2011 Erich Seifert <dev[at]erichseifert.de>,
 * Michael Seifert <michael.seifert[at]gmx.net>
 *
 * This file is part of GRAL.
 *
 * GRAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GRAL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GRAL.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.erichseifert.gral.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tokenizing parser that can analyzes a string using different sets of
 * regular expression based parsing rules and produces a list of tokens.
 *
 * The class is intended to be sub-classed to implement new grammars.
 * Different sets of rules can be defined with the method
 * {{@link #addState(String, Rule[])}}, e.g. for string processing. Each rule
 * in a set produces one token with a type name and it can switch to another
 * state or switch back to the previous state with the special state
 * {@code "#pop"}. The is a list of tokens with arbitrary type.
 *
 * The list of produced tokens can be filtered: tokens of same type can be
 * joined by adding the type with {@link #addJoinedType(String)} and tokens can
 * be omitted from the result for easier post-processing by adding with
 * {@link #addIgnoredType(String)}.
 */
public abstract class StatefulTokenizer {
	/** The name of the initial state. */
	protected static final String INITIAL_STATE = "";

	/** Token types that should be joined when adjacent tokens are found. */
	private final Set<Object> joinedTypes;
	/** Token types that shouldn't be added to the output. */
	private final Set<Object> ignoredTypes;
	/** Rules for specific states. */
	private final Map<String, Rule[]> grammar;

	/**
	 * A token that designates a certain section of a text input. The absolute
	 * position within the input stream as well as the relevant text are stored
	 * for later processing.
	 */
	public static class Token {
		/** Absolute position where the token started in the input stream. */
		private final int start;
		/** Absolute position where the token ended in the input stream. */
		private int end;
		/** Type of the token as defined by the corresponding rule. */
		private final Object type;
		/** The relevant content from the input stream. Its lengths can differ
	    from length of the token. */
		private final StringBuilder content;

		/**
		 * Initializes a new token with absolute start and end position, a type
		 * and text content.
		 * @param start Absolute position where the token started in the input stream.
		 * @param end Absolute position where the token ended in the input stream.
		 * @param type Type of the token as defined by the corresponding rule.
		 * @param content The relevant text content from the input stream.
		 */
		public Token(int start, int end, Object type, String content) {
			this.content = new StringBuilder();
			this.start = start;
			this.end = end;
			this.type = type;
			this.content.append(content);
		}

		/**
		 * Joins two tokens by appending the contents of another token to this
		 * token.
		 * @param t Another token that should be appended to this token
		 */
		public void append(Token t) {
			content.append(t.content);
			end = t.end;
		}

		/**
		 * Returns the absolute position where the token starts in the input
		 * stream.
		 * @return Absolute position in the input stream.
		 */
		public int getStart() {
			return start;
		}

		/**
		 * Returns the absolute position where the token ends in the input
		 * stream.
		 * @return Absolute position in the input stream.
		 */
		public int getEnd() {
			return end;
		}

		/**
		 * Returns the type of the token.
		 * @return Type of the token
		 */
		public Object getType() {
			return type;
		}

		/**
		 * Returns the content of the token.
		 * @return Content of the token
		 */
		public String getContent() {
			return content.toString();
		}

		@Override
		public String toString() {
			return String.format("%s[start=%d, end=%d, type=%s, content=\"%s\"]",
				getClass().getSimpleName(), getStart(), getEnd(), getType(), getContent());
		}
	}

	/**
	 * A regular expression based rule for building a parsing grammar.
	 * It stores a regular expression pattern for matching input data, a type
	 * for tokens generated by this rule, and an optional name of a grammar
	 * state that should be activated.
	 *
	 * The method {@link #getToken(String,int)} can be used to process input
	 * data: If the rule matches a token is returned, otherwise {@code null}
	 * will be returned.
	 */
	protected static class Rule {
		/** Compiled regular expression for analyzing the input stream. */
		private final Pattern pattern;
		/** Type of the tokens generated by this rule. */
		private final Object tokenType;
		/** The grammar state that be used next to analyze the input data if
		the rule matched. */
		private final String nextState;

		/**
		 * Initializes a new instance with the specified regular expression
		 * pattern, a type for generated tokens, and a name of a grammar state
		 * that should be triggered.
		 * @param pattern A regular expression pattern string.
		 * @param tokenType The type for the tokens generated by this rule.
		 * @param nextState The grammar state that should be used next to
		 *        analyze the input data if the rule matched.
		 */
		public Rule(String pattern, Object tokenType, String nextState) {
			this.pattern = Pattern.compile(pattern);
			this.tokenType = tokenType;
			this.nextState = nextState;
		}

		/**
		 * Initializes a new instance with the specified regular expression
		 * pattern, a type for generated tokens.
		 * @param pattern A regular expression pattern string.
		 * @param tokenType The type for the tokens generated by this rule.
		 */
		public Rule(String pattern, Object tokenType) {
			this(pattern, tokenType, null);
		}

		/**
		 * Analyzes the specified input data starting at the given position
		 * and returns a token with the defined type, the content matched by
		 * the regular expression if the rule matches. If the rule doesn't
		 * match {@code null} will be returned.
		 * @param data Input data.
		 * @param pos Position to start looking for a match.
		 * @return A token with information about the matched section of the
		 *         input data, or {@code null} if the rule didn't match.
		 */
		public Token getToken(String data, int pos) {
			Matcher m = pattern.matcher(data);
			m.region(pos, data.length());
			if (!m.lookingAt()) {
				return null;
			}
			String content = (m.groupCount() > 0) ? m.group(1) : m.group();
			Token token = new Token(m.start(), m.end(), tokenType, content);
			return token;
		}
	}

	/**
	 * Initializes the internal data structures of a new instance.
	 */
	protected StatefulTokenizer() {
		joinedTypes = new HashSet<Object>();
		ignoredTypes = new HashSet<Object>();
		grammar = new HashMap<String, Rule[]>();
	}

	/**
	 * Adds a token type to the set of tokens that should get joined in the
	 * tokenizer output.
	 * @param tokenType Type of the tokens that should be joined.
	 */
	protected void addJoinedType(Object tokenType) {
		joinedTypes.add(tokenType);
	}

	/**
	 * Adds a token type to the set of tokens that should be ignored in the
	 * tokenizer output.
	 * @param tokenType Type of the tokens that should be ignored.
	 */
	protected void addIngoredType(Object tokenType) {
		ignoredTypes.add(tokenType);
	}

	/**
	 * Sets the rules for the initial state in the grammar.
	 * @param rules A sequence or an array with rules to be added.
	 */
	protected void putRules(Rule... rules) {
		putRules(INITIAL_STATE, rules);
	}

	/**
	 * Sets the rules for the specified state in the grammar.
	 * @param rules A sequence or an array with rules to be added.
	 */
	protected void putRules(String name, Rule... rules) {
		grammar.put(name, rules);
	}

	/**
	 * Analyzes the specified input string using different sets of rules and
	 * returns a list of token objects describing the content structure.
	 * @param data Input string.
	 * @return List of tokens.
	 */
	public List<Token> tokenize(String data) {
		LinkedList<Token> tokens = new LinkedList<Token>();

		Stack<String> states = new Stack<String>();
		states.push(INITIAL_STATE);

		int pos = 0;
		Token tokenCur = null;
		while (pos < data.length() && !states.isEmpty()) {
			String state = states.peek();
			Rule[] rules = grammar.get(state);
			for (Rule rule : rules) {
				Token token = rule.getToken(data, pos);
				if (token == null) {
					continue;
				}

				if (tokenCur != null && tokenCur.type.equals(token.type) &&
						joinedTypes.contains(tokenCur.type)) {
					tokenCur.append(token);
				} else {
					if (tokenCur != null &&
							!ignoredTypes.contains(tokenCur.type)) {
						tokens.add(tokenCur);
					}
					tokenCur = token;
				}

				pos = token.end;

				if ("#pop".equals(rule.nextState)) {
					states.pop();
				} else if (rule.nextState != null) {
					states.push(rule.nextState);
				}
				break;
			}
		}
		if (tokenCur != null && !ignoredTypes.contains(tokenCur.type)) {
			tokens.add(tokenCur);
		}

		return tokens;
	}
}
