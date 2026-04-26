package com.lalke.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.lalke.antler.LogoBaseListener;
import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;

public class LogoSemanticsListener extends LogoBaseListener {
    private final List<Integer> data = new ArrayList<>();
    private int lastLine = 0;
    private int lastChar = 0;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "to", "end", "repeat", "if", "make", "print", "fd", "forward", 
        "bk", "backward", "rt", "right", "lt", "left", "cs", "clearscreen", 
        "pu", "penup", "pd", "pendown", "ht", "hideturtle", "st", "showturtle", 
        "home", "stop", "label", "setxy", "setwidth", "setcolor", "setpencolor", 
        "for", "random", "arc", "local"
    ));

    @Override
    public void enterDeref(LogoParser.DerefContext ctx) {
        int startOffset = ctx.getStart().getStartIndex();
        int endOffset = ctx.getStop().getStopIndex();
        int length = endOffset - startOffset + 1;
        addToken(ctx.getStart(), length, 1);
    }

    @Override 
    public void enterProcedureDeclaration(LogoParser.ProcedureDeclarationContext ctx) {
        Token nameToken = ctx.name().getStart();
        addToken(nameToken, nameToken.getText().length(), 2);
    }

    @Override
    public void enterProcedureInvocation(LogoParser.ProcedureInvocationContext ctx) {
        Token nameToken = ctx.name().getStart();
        addToken(nameToken, nameToken.getText().length(), 2);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        String text = token.getText();
        String lowerText = text.toLowerCase();
        int type = token.getType();

        if (isInsideDeref(node) || isInsideName(node)) return;

        if (text.equals("[") || text.equals("]"))
            addToken(token, 1, 5);
        else if (KEYWORDS.contains(lowerText))
            addToken(token, text.length(), 0); 
        else if (type == LogoLexer.NUMBER)
            addToken(token, text.length(), 3); 
        else if (type == LogoLexer.STRINGLITERAL)
            addToken(token, text.length(), 4); 
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

        lastLine = line;
        lastChar = charPos;
    }

    public List<Integer> getData() { return data; }
}