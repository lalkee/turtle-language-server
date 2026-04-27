package com.lalke.parser;

import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.lalke.antler.LogoBaseListener;
import com.lalke.antler.LogoParser;
import com.lalke.antler.LogoParser.ProcedureDeclarationContext;

public class LogoSymbolListener extends LogoBaseListener {
    private final LogoSymbolTable symbolTable;
    private final String uri;

    public LogoSymbolListener(LogoSymbolTable symbolTable, String uri) {
        this.symbolTable = symbolTable;
        this.uri = uri;
    }

    @Override
    public void enterProcedureDeclaration(ProcedureDeclarationContext ctx) {
        if (ctx.name() != null) {
            Token nameToken = ctx.name().getStart();
            symbolTable.addProcedure(nameToken.getText(), uri, toRange(nameToken));
        }

        if (ctx.parameter() != null) {
            for (var paramCtx : ctx.parameter()) {
                Token pToken = paramCtx.name().getStart();
                symbolTable.addVariable(pToken.getText(), uri, toRange(pToken));
            }
        }
    }

    @Override
    public void enterMake(LogoParser.MakeContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); // removes " or '
            symbolTable.addVariable(name, uri, toRange(token));
        }
    }

    @Override
    public void enterName_cmd(LogoParser.Name_cmdContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); 
            symbolTable.addVariable(name, uri, toRange(token));
        }
    }

    @Override
    public void enterLocalmake(LogoParser.LocalmakeContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); 
            symbolTable.addVariable(name, uri, toRange(token));
        }
    }

    @Override
    public void enterLocal(LogoParser.LocalContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1);
            symbolTable.addVariable(name, uri, toRange(token));
        }
    }

    @Override
    public void enterFore(LogoParser.ForeContext ctx) {
        if (ctx.name() != null) {
            Token nameToken = ctx.name().getStart();
            symbolTable.addVariable(nameToken.getText(), uri, toRange(nameToken));
        }
    }

    @Override
    public void enterDotimes(LogoParser.DotimesContext ctx) {
        if (ctx.name() != null) {
            Token nameToken = ctx.name().getStart();
            symbolTable.addVariable(nameToken.getText(), uri, toRange(nameToken));
        }
    }

    //converts antlr token properties into lsp range object
    private Range toRange(Token token) {
        //antlr's indexing starts at 1, while lsp4j uses 0-based indexing
        int line = token.getLine() - 1;
        int col = token.getCharPositionInLine();
        return new Range(new Position(line, col), new Position(line, col + token.getText().length()));
    }
}
