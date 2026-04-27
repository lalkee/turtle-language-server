package com.lalke.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext; // New Import
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.lalke.LogoKeywords;
import com.lalke.antler.LogoBaseListener;
import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;

public class SemanticsListener extends LogoBaseListener {
    private final List<Integer> data = new ArrayList<>();
    private int lastLine = 0;
    private int lastChar = 0;

    @Override
    public void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        String text = token.getText();
        int type = token.getType();
        String lowerText = text.toLowerCase();

        if (type == LogoLexer.COMMENT) {
            addToken(token, text.length(), 6);
            return;
        }
        if (type == LogoLexer.NUMBER) {
            addToken(token, text.length(), 3);
            return;
        }
        if (LogoKeywords.KEYWORDS.contains(lowerText)) {
            addToken(token, text.length(), 0);
            return;
        }

        if (isInsideDeref(node) || isInsideName(node)) return;

        if (text.equals("[") || text.equals("]")) {
            addToken(token, 1, 5);
        } else if (type == LogoLexer.STRINGLITERAL) {
            addToken(token, text.length(), 4);
        }
    }

    private boolean isInsideDeref(TerminalNode node) {
        if (node.getParent() instanceof ParserRuleContext parent)
            return parent.getRuleIndex() == LogoParser.RULE_deref;
        return false;
    }

    private boolean isInsideName(TerminalNode node) {
        return node.getParent() instanceof LogoParser.NameContext;
    }

    private void addToken(Token token, int length, int typeIndex) {
        int line = token.getLine() - 1;
        int charPos = token.getCharPositionInLine();
        int deltaLine = line - lastLine;
        int deltaChar = (deltaLine == 0) ? charPos - lastChar : charPos;
        data.addAll(Arrays.asList(deltaLine, deltaChar, length, typeIndex, 0));
        lastLine = line; lastChar = charPos;
    }

    public List<Integer> getData() { return data; }
}