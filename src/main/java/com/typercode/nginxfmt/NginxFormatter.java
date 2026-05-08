package com.typercode.nginxfmt;

import java.util.ArrayList;
import java.util.List;

public class NginxFormatter {
    private static final String INDENT = "    ";

    public String format(String source) {
        List<Token> tokens = tokenize(source == null ? "" : source);
        StringBuilder out = new StringBuilder();
        int indent = 0;
        boolean atLineStart = true;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            boolean nextIsInlineComment = i + 1 < tokens.size()
                    && tokens.get(i + 1).type == TokenType.COMMENT
                    && tokens.get(i + 1).inline;
            switch (token.type) {
                case BLANK_LINE:
                    if (!atLineStart) {
                        trimLineEnd(out);
                        out.append('\n');
                    }
                    appendBlankLine(out);
                    atLineStart = true;
                    break;
                case DIRECTIVE:
                    appendIndentIfNeeded(out, indent, atLineStart);
                    out.append(token.text.strip());
                    trimLineEnd(out);
                    if (nextIsInlineComment) {
                        atLineStart = false;
                    } else {
                        out.append('\n');
                        atLineStart = true;
                    }
                    break;
                case BLOCK_OPEN:
                    appendIndentIfNeeded(out, indent, atLineStart);
                    if (!token.text.isBlank()) {
                        out.append(token.text.strip()).append(' ');
                    }
                    out.append('{');
                    trimLineEnd(out);
                    out.append('\n');
                    indent++;
                    atLineStart = true;
                    break;
                case BLOCK_CLOSE:
                    if (!atLineStart) {
                        trimLineEnd(out);
                        out.append('\n');
                    }
                    indent = Math.max(0, indent - 1);
                    appendIndentIfNeeded(out, indent, true);
                    out.append('}');
                    trimLineEnd(out);
                    if (nextIsInlineComment) {
                        atLineStart = false;
                    } else {
                        out.append('\n');
                        atLineStart = true;
                    }
                    break;
                case COMMENT:
                    if (token.inline && !atLineStart) {
                        trimLineEnd(out);
                        out.append(' ').append(token.text.strip());
                        trimLineEnd(out);
                        out.append('\n');
                    } else {
                        appendIndentIfNeeded(out, indent, true);
                        out.append(token.text.strip());
                        trimLineEnd(out);
                        out.append('\n');
                    }
                    atLineStart = true;
                    break;
                default:
                    throw new IllegalStateException("Unknown token type: " + token.type);
            }
        }

        return out.toString();
    }

    private List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;
        int consecutiveNewlines = 0;
        TokenType lastTokenOnLine = null;

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);

            if (ch == '\r') {
                continue;
            }

            if (ch == '\n') {
                if (current.toString().strip().isEmpty()) {
                    current.setLength(0);
                    consecutiveNewlines++;
                    if (consecutiveNewlines > 1) {
                        tokens.add(new Token(TokenType.BLANK_LINE, ""));
                    }
                } else {
                    addDirective(tokens, current);
                    consecutiveNewlines = 1;
                }
                lastTokenOnLine = null;
                escaped = false;
                continue;
            }

            if (!Character.isWhitespace(ch)) {
                consecutiveNewlines = 0;
            }

            if (!inSingleQuote && !inDoubleQuote && ch == '#') {
                addDirective(tokens, current);
                int commentStart = i;
                while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') {
                    i++;
                }
                boolean inline = lastTokenOnLine == TokenType.DIRECTIVE || lastTokenOnLine == TokenType.BLOCK_CLOSE;
                tokens.add(new Token(TokenType.COMMENT, source.substring(commentStart, i), inline));
                lastTokenOnLine = TokenType.COMMENT;
                i--;
                escaped = false;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && ch == '{') {
                addBlockOpen(tokens, current);
                lastTokenOnLine = TokenType.BLOCK_OPEN;
                escaped = false;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && ch == '}') {
                addDirective(tokens, current);
                tokens.add(new Token(TokenType.BLOCK_CLOSE, "}"));
                lastTokenOnLine = TokenType.BLOCK_CLOSE;
                escaped = false;
                continue;
            }

            current.append(ch);

            if (escaped) {
                escaped = false;
            } else if (ch == '\\' && (inSingleQuote || inDoubleQuote)) {
                escaped = true;
            } else if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            if (!inSingleQuote && !inDoubleQuote && ch == ';') {
                addDirective(tokens, current);
                lastTokenOnLine = TokenType.DIRECTIVE;
            }
        }

        addDirective(tokens, current);
        return tokens;
    }

    private static void addDirective(List<Token> tokens, StringBuilder current) {
        String text = current.toString().strip();
        current.setLength(0);
        if (!text.isEmpty()) {
            tokens.add(new Token(TokenType.DIRECTIVE, normalizeInlineWhitespace(text)));
        }
    }

    private static void addBlockOpen(List<Token> tokens, StringBuilder current) {
        String text = current.toString().strip();
        current.setLength(0);
        tokens.add(new Token(TokenType.BLOCK_OPEN, normalizeInlineWhitespace(text)));
    }

    private static String normalizeInlineWhitespace(String text) {
        StringBuilder normalized = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;
        boolean previousWhitespace = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean whitespace = Character.isWhitespace(ch);

            if (!inSingleQuote && !inDoubleQuote && whitespace) {
                previousWhitespace = true;
                continue;
            }

            if (previousWhitespace && normalized.length() > 0 && ch != ';') {
                normalized.append(' ');
            }
            previousWhitespace = false;
            normalized.append(ch);

            if (escaped) {
                escaped = false;
            } else if (ch == '\\' && (inSingleQuote || inDoubleQuote)) {
                escaped = true;
            } else if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }
        }

        return normalized.toString();
    }

    private static void appendIndentIfNeeded(StringBuilder out, int indent, boolean atLineStart) {
        if (!atLineStart) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            out.append(INDENT);
        }
    }

    private static void appendBlankLine(StringBuilder out) {
        int length = out.length();
        if (length == 0 || out.charAt(length - 1) != '\n') {
            out.append('\n');
            return;
        }
        if (length < 2 || out.charAt(length - 2) != '\n') {
            out.append('\n');
        }
    }

    private static void trimLineEnd(StringBuilder out) {
        while (out.length() > 0) {
            char last = out.charAt(out.length() - 1);
            if (last != ' ' && last != '\t') {
                return;
            }
            out.setLength(out.length() - 1);
        }
    }

    private enum TokenType {
        DIRECTIVE,
        BLOCK_OPEN,
        BLOCK_CLOSE,
        COMMENT,
        BLANK_LINE
    }

    private static final class Token {
        private final TokenType type;
        private final String text;
        private final boolean inline;

        private Token(TokenType type, String text) {
            this(type, text, false);
        }

        private Token(TokenType type, String text, boolean inline) {
            this.type = type;
            this.text = text;
            this.inline = inline;
        }
    }
}
