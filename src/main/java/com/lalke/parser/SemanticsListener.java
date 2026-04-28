package com.lalke.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token; // New Import
import org.antlr.v4.runtime.tree.TerminalNode;

import com.lalke.LogoKeywords;
import com.lalke.antler.LogoBaseListener;
import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;

public class SemanticsListener extends LogoBaseListener {
    /*as per lsp specification, semantic tokens are serialized into array
    where 5 array fields are dedicated to each token. token i consists of
    following indices:
    at index 5*i - deltaLine: token line number, relative to the start of the previous token
    at index 5*i+1 - deltaStart: token start character, relative to the start of the previous token (relative to 0 or the previous token’s start if they are on the same line)
    at index 5*i+2 - length: the length of the token.
    at index 5*i+3 - tokenType: will be looked up in SemanticTokensLegend.tokenTypes
    at index 5*i+4 - tokenModifiers: each set bit will be looked up in SemanticTokensLegend.tokenModifiers*/
    private final List<Integer> data = new ArrayList<>();
    private final SymbolTable symbolTable;
    private int lastLine = 0;
    private int lastChar = 0;

    public SemanticsListener(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    //will be called by ParseTreeWalker for every terminal token in parse tree
    @Override
    public void visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        String text = token.getText();
        int type = token.getType();
        String lowerText = text.toLowerCase();

        if (type == LogoLexer.COMMENT) {addToken(token, text.length(), 6); return;}

        if (type == LogoLexer.NUMBER) {addToken(token, text.length(), 3); return;}

        if (isInsideDeref(node) || isInsideParameter(node)) {addToken(token, text.length(), 1); return;}

        if (isInsideProcedureDeclaration(node)) {addToken(token, text.length(), 2); return;}

        if (isInsideProcedureInvocation(node)) {
            if (symbolTable.getProcedures().containsKey(lowerText)) {
                addToken(token, text.length(), 2);
            }
            return; 
        }

        if (LogoKeywords.KEYWORDS.contains(lowerText)) {addToken(token, text.length(), 0); return;}

        if (text.equals("[") || text.equals("]")) {addToken(token, 1, 5); return;}
        
        if (type == LogoLexer.STRINGLITERAL) {addToken(token, text.length(), 4);}
    }
    
    private boolean isInsideDeref(TerminalNode node) {
        return node.getParent() instanceof LogoParser.NameContext && node.getParent().getParent() instanceof LogoParser.DerefContext;
    }

    private boolean isInsideParameter(TerminalNode node) {
        return node.getParent() instanceof LogoParser.NameContext && node.getParent().getParent() instanceof LogoParser.ParameterContext;
    }

    private boolean isInsideProcedureInvocation(TerminalNode node) {
        return node.getParent() instanceof LogoParser.NameContext && node.getParent().getParent() instanceof LogoParser.ProcedureInvocationContext;
    }

    private boolean isInsideProcedureDeclaration(TerminalNode node) {
        if (!(node.getParent() instanceof LogoParser.NameContext nameCtx)) return false;
        
        ParserRuleContext parent = (ParserRuleContext) nameCtx.getParent();
        while (parent != null) {
            if (parent.getRuleIndex() == LogoParser.RULE_procedureDeclaration) {
                return true;
            }
            parent = (ParserRuleContext) parent.getParent();
        }
        return false;
    }
    
    private void addToken(Token token, int length, int typeIndex) {
        /*1-based indexing is used for lines, but to make calculating line deltas easier,
        i convert it to 0-based indexing*/
        int line = token.getLine() - 1;
        int charPos = token.getCharPositionInLine();
        int deltaLine = line - lastLine;
        // same line: char delta; new line: absolute char
        int deltaChar = (deltaLine == 0) ? charPos - lastChar : charPos;
        //i have not used tokenModifiers in this project, so i always set it to 0
        data.addAll(Arrays.asList(deltaLine, deltaChar, length, typeIndex, 0));
        lastLine = line; lastChar = charPos;
    }

    public List<Integer> getData() { return data; }
}