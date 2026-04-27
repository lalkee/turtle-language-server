package com.lalke.parser;

import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import com.lalke.antler.LogoBaseListener;
import com.lalke.antler.LogoParser;
import com.lalke.antler.LogoParser.ProcedureDeclarationContext;

public class SymbolListener extends LogoBaseListener {
    private final SymbolTable symbolTable;
    private final String uri;
    private Range currentProcedureScope = null;

    public SymbolListener(SymbolTable symbolTable, String uri) {
        this.symbolTable = symbolTable;
        this.uri = uri;
    }

    @Override
    public void enterProcedureDeclaration(ProcedureDeclarationContext ctx) {
        Range procRange = toFullRange(ctx.getStart(), ctx.getStop());
        currentProcedureScope = procRange;

        if (ctx.name() != null) {
            Token nameToken = ctx.name().getStart();
            symbolTable.addProcedure(nameToken.getText(), uri, toRange(nameToken));
        }

        if (ctx.parameter() != null) {
            for (var paramCtx : ctx.parameter()) {
                Token pToken = paramCtx.name().getStart();
                symbolTable.addVariable(pToken.getText(), uri, toRange(pToken), procRange);
            }
        }
    }

    @Override
    public void exitProcedureDeclaration(ProcedureDeclarationContext ctx) {
        currentProcedureScope = null;
    }

    @Override
    public void enterMake(LogoParser.MakeContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); // removes " or '
            symbolTable.addVariable(name, uri, toRange(token), currentProcedureScope);
        }
    }

    @Override
    public void enterName_cmd(LogoParser.Name_cmdContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); 
            symbolTable.addVariable(name, uri, toRange(token), currentProcedureScope);
        }
    }

    @Override
    public void enterLocalmake(LogoParser.LocalmakeContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1); 
            symbolTable.addVariable(name, uri, toRange(token), currentProcedureScope);
        }
    }

    @Override
    public void enterLocal(LogoParser.LocalContext ctx) {
        if (ctx.STRINGLITERAL() != null) {
            Token token = ctx.STRINGLITERAL().getSymbol();
            String name = token.getText().substring(1);
            symbolTable.addVariable(name, uri, toRange(token), currentProcedureScope);
        }
    }

    @Override
    public void enterFore(LogoParser.ForeContext ctx) {
        if (ctx.name() != null && ctx.block() != null) {
            Token nameToken = ctx.name().getStart();
            
            Range blockScope = toFullRange(ctx.block().getStart(), ctx.block().getStop());
            
            symbolTable.addVariable(nameToken.getText(), uri, toRange(nameToken), blockScope);
        }
    }

    @Override
    public void enterDotimes(LogoParser.DotimesContext ctx) {
        if (ctx.name() != null && ctx.block() != null) {
            Token nameToken = ctx.name().getStart();
            
            Range blockScope = toFullRange(ctx.block().getStart(), ctx.block().getStop());
            
            symbolTable.addVariable(nameToken.getText(), uri, toRange(nameToken), blockScope);
        }
    }

    private Range toRange(Token token) {
        int line = token.getLine() - 1;
        int col = token.getCharPositionInLine();
        return new Range(new Position(line, col), new Position(line, col + token.getText().length()));
    }

    private Range toFullRange(Token start, Token stop) {
        return new Range(
            new Position(start.getLine() - 1, start.getCharPositionInLine()),
            new Position(stop.getLine() - 1, stop.getCharPositionInLine() + stop.getText().length())
        );
    }
}
