package cn.hangsman.operationlog.spel;

import org.springframework.expression.ParseException;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by 2022/1/12 13:17
 *
 * @author hangsman
 * @since 1.0
 */
public class SpelUtil {

    private static boolean isSuffixHere(String expressionString, int pos, String suffix) {
        int suffixPosition = 0;
        for (int i = 0; i < suffix.length() && pos < expressionString.length(); i++) {
            if (expressionString.charAt(pos++) != suffix.charAt(suffixPosition++)) {
                return false;
            }
        }
        // the expressionString ran out before the suffix could entirely be found
        return suffixPosition == suffix.length();
    }

    public static int skipToCorrectEndSuffix(String suffix, String expressionString, int afterPrefixIndex)
            throws ParseException {

        // Chew on the expression text - relying on the rules:
        // brackets must be in pairs: () [] {}
        // string literals are "..." or '...' and these may contain unmatched brackets
        int pos = afterPrefixIndex;
        int maxlen = expressionString.length();
        int nextSuffix = expressionString.indexOf(suffix, afterPrefixIndex);
        if (nextSuffix == -1) {
            return -1; // the suffix is missing
        }
        Deque<Bracket> stack = new ArrayDeque<>();
        while (pos < maxlen) {
            if (isSuffixHere(expressionString, pos, suffix) && stack.isEmpty()) {
                break;
            }
            char ch = expressionString.charAt(pos);
            switch (ch) {
                case '{':
                case '[':
                case '(':
                    stack.push(new Bracket(ch, pos));
                    break;
                case '}':
                case ']':
                case ')':
                    if (stack.isEmpty()) {
                        throw new ParseException(expressionString, pos, "Found closing '" + ch +
                                "' at position " + pos + " without an opening '" +
                                Bracket.theOpenBracketFor(ch) + "'");
                    }
                    Bracket p = stack.pop();
                    if (!p.compatibleWithCloseBracket(ch)) {
                        throw new ParseException(expressionString, pos, "Found closing '" + ch +
                                "' at position " + pos + " but most recent opening is '" + p.bracket +
                                "' at position " + p.pos);
                    }
                    break;
                case '\'':
                case '"':
                    // jump to the end of the literal
                    int endLiteral = expressionString.indexOf(ch, pos + 1);
                    if (endLiteral == -1) {
                        throw new ParseException(expressionString, pos,
                                "Found non terminating string literal starting at position " + pos);
                    }
                    pos = endLiteral;
                    break;
            }
            pos++;
        }
        if (!stack.isEmpty()) {
            Bracket p = stack.pop();
            throw new ParseException(expressionString, p.pos, "Missing closing '" +
                    Bracket.theCloseBracketFor(p.bracket) + "' for '" + p.bracket + "' at position " + p.pos);
        }
        if (!isSuffixHere(expressionString, pos, suffix)) {
            return -1;
        }
        return pos;
    }

    private static class Bracket {

        char bracket;

        int pos;

        Bracket(char bracket, int pos) {
            this.bracket = bracket;
            this.pos = pos;
        }

        static char theOpenBracketFor(char closeBracket) {
            if (closeBracket == '}') {
                return '{';
            } else if (closeBracket == ']') {
                return '[';
            }
            return '(';
        }

        static char theCloseBracketFor(char openBracket) {
            if (openBracket == '{') {
                return '}';
            } else if (openBracket == '[') {
                return ']';
            }
            return ')';
        }

        boolean compatibleWithCloseBracket(char closeBracket) {
            if (this.bracket == '{') {
                return closeBracket == '}';
            } else if (this.bracket == '[') {
                return closeBracket == ']';
            }
            return closeBracket == ')';
        }
    }
}
