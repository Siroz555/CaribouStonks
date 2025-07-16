package fr.siroz.cariboustonks.util;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * A calculator that evaluates a mathematical expression.
 * <p>
 * NOTE-ME : {@code fr.siroz.cariboustonks.util.CalculatorTest}
 * <p>
 * This file is a “modern version” of NEU's Calculator.
 * (<a href="https://github.com/NotEnoughUpdates/NotEnoughUpdates">GitHub NotEnoughUpdates</a>)
 * <p>
 * Credits to the Skyblocker Team for this reworked version
 * (<a href="https://github.com/SkyblockerMod/Skyblocker">GitHub Skyblocker</a>)
 */
public final class Calculator {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+\\.?\\d*)([sekmbt]?)");
	private static final Object2LongMap<String> MAGNITUDE_VALUES = Object2LongMaps.unmodifiable(new Object2LongOpenHashMap<>(Map.of(
			"s", 64L,
			"e", 160L,
			"k", 1_000L,
			"m", 1_000_000L,
			"b", 1_000_000_000L,
			"t", 1_000_000_000_000L
	)));

	private Calculator() {
	}

	/**
	 * Evaluates a mathematical expression represented as a string and returns the computed result.
	 * <p>
	 * <ul>
	 *     <li>Basic arithmetic operators: <code>+</code>, <code>-</code>, <code>*</code>, <code>x</code>, <code>/</code>, <code>^</code></li>
	 *     <li>Parentheses for grouping: <code>()</code></li>
	 *     <li>Implicit multiplication: <code>a(b + c)</code></li>
	 *     <li>Magnitude suffixes: <code>k</code>, <code>m</code>, <code>b</code></li>
	 *     <li>Case-insensitive parsing and optional spacing</li>
	 * </ul>
	 *
	 * @param expression the string representation of the mathematical expression to evaluate
	 * @return the evaluated result as a double
	 * @throws UnsupportedOperationException if the expression contains:
	 *                                       <ul>
	 *                                           <li>Division by zero</li>
	 *                                           <li>Invalid operator sequences</li>
	 *                                           <li>Unmatched parentheses</li>
	 *                                           <li>Unsupported or invalid characters</li>
	 *                                           <li>Unknown or invalid magnitude suffix</li>
	 *                                       </ul>
	 */
	public static double calculate(@NotNull String expression) throws UnsupportedOperationException {
		expression = expression.toLowerCase();
		return evaluate(shunt(lex(expression)));
	}

	private static @NotNull List<Token> lex(String input) {
		List<Token> tokens = new ArrayList<>();
		input = input.replace(" ", "").toLowerCase().replace("x", "*");
		int i = 0;
		while (i < input.length()) {
			Token token = new Token();
			switch (input.charAt(i)) {
				case '+', '-', '*', '/', '^' -> {
					token.type = TokenType.OPERATOR;
					token.value = String.valueOf(input.charAt(i));
					token.tokenLength = 1;
					if (!tokens.isEmpty() && tokens.getLast().type == TokenType.OPERATOR) {
						throw new UnsupportedOperationException("Duplicate operator");
					}
				}

				case '(' -> {
					token.type = TokenType.L_PARENTHESIS;
					token.value = String.valueOf(input.charAt(i));
					token.tokenLength = 1;
					if (!tokens.isEmpty()) {
						TokenType lastType = tokens.getLast().type;
						if (lastType == TokenType.R_PARENTHESIS || lastType == TokenType.NUMBER) {
							Token mutliplyToken = new Token();
							mutliplyToken.type = TokenType.OPERATOR;
							mutliplyToken.value = "*";
							tokens.add(mutliplyToken);
						}
					}
				}

				case ')' -> {
					token.type = TokenType.R_PARENTHESIS;
					token.value = String.valueOf(input.charAt(i));
					token.tokenLength = 1;
				}

				default -> {
					token.type = TokenType.NUMBER;
					Matcher numberMatcher = NUMBER_PATTERN.matcher(input.substring(i));
					if (!numberMatcher.find()) {
						throw new UnsupportedOperationException("Character is invalid");
					}

					int end = numberMatcher.end();
					token.value = input.substring(i, i + end);
					token.tokenLength = end;
				}
			}

			tokens.add(token);
			i += token.tokenLength;
		}

		return tokens;
	}

	private static List<Token> shunt(@NotNull List<Token> tokens) {
		Deque<Token> operatorStack = new ArrayDeque<>();
		List<Token> outputQueue = new ArrayList<>();

		for (Token shuntingToken : tokens) {
			switch (shuntingToken.type) {
				case NUMBER -> outputQueue.add(shuntingToken);
				case OPERATOR -> {
					int precedence = getPrecedence(shuntingToken.value);
					while (!operatorStack.isEmpty()) {
						Token leftToken = operatorStack.peek();
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break;
						}

						assert (leftToken.type == TokenType.OPERATOR);
						int leftPrecedence = getPrecedence(leftToken.value);
						if (leftPrecedence >= precedence) {
							outputQueue.add(operatorStack.pop());
							continue;
						}

						break;
					}

					operatorStack.push(shuntingToken);
				}
				case L_PARENTHESIS -> operatorStack.push(shuntingToken);
				case R_PARENTHESIS -> {
					while (true) {
						if (operatorStack.isEmpty()) {
							throw new UnsupportedOperationException();
						}

						Token leftToken = operatorStack.pop();
						if (leftToken.type == TokenType.L_PARENTHESIS) {
							break;
						}

						outputQueue.add(leftToken);
					}
				}
				case null, default -> {
				}
			}
		}

		while (!operatorStack.isEmpty()) {
			Token leftToken = operatorStack.pop();
			if (leftToken.type == TokenType.L_PARENTHESIS) {
				continue;
			}

			outputQueue.add(leftToken);
		}

		return outputQueue.stream().toList();
	}

	private static int getPrecedence(@NotNull String operator) {
		switch (operator) {
			case "+", "-" -> {
				return 0;
			}
			case "*", "/" -> {
				return 1;
			}
			case "^" -> {
				return 2;
			}
			default -> throw new UnsupportedOperationException("Invalid operator");
		}
	}

	private static double evaluate(@NotNull List<Token> tokens) {
		Deque<Double> values = new ArrayDeque<>();
		for (Token token : tokens) {
			switch (token.type) {
				case NUMBER -> values.push(calculateValue(token.value));
				case OPERATOR -> {
					Double right = values.pollFirst();
					Double left = values.pollFirst();
					if (left == null || right == null) {
						throw new UnsupportedOperationException();
					}

					switch (token.value) {
						case "+" -> values.push(left + right);
						case "-" -> values.push(left - right);
						case "/" -> {
							if (right == 0) {
								throw new UnsupportedOperationException("Division by Zero");
							}
							values.push(left / right);
						}
						case "*" -> values.push(left * right);
						case "^" -> values.push(Math.pow(left, right));
						case null, default -> {
						}
					}
				}
				case L_PARENTHESIS, R_PARENTHESIS -> throw new UnsupportedOperationException();
				case null, default -> {
				}
			}
		}

		if (values.isEmpty()) {
			throw new UnsupportedOperationException();
		}

		return values.pop();
	}

	private static double calculateValue(@NotNull String value) {
		Matcher numberMatcher = NUMBER_PATTERN.matcher(value.toLowerCase());
		if (!numberMatcher.matches()) {
			throw new UnsupportedOperationException("Not a Number");
		}

		double number = Double.parseDouble(numberMatcher.group(1));
		String magnitude = numberMatcher.group(2);

		if (!magnitude.isEmpty()) {
			if (!MAGNITUDE_VALUES.containsKey(magnitude)) {
				throw new UnsupportedOperationException("Magnitude is invalid");
			}

			number *= MAGNITUDE_VALUES.getLong(magnitude);
		}

		return number;
	}

	private enum TokenType {
		NUMBER, OPERATOR, L_PARENTHESIS, R_PARENTHESIS
	}

	private static class Token {
		TokenType type;
		String value;
		int tokenLength;
	}
}
