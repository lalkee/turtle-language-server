package com.lalke.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;
import com.lalke.parser.LogoSemanticsListener;
import com.lalke.parser.LogoSymbolListener;
import com.lalke.parser.LogoSymbolTable;
import com.lalke.parser.TreeUtil;

public class LogoTextDocumentService implements TextDocumentService {
    private LogoLanguageServer server;
    private LanguageClient client;
    private final Map<String, String> documentContentMap = new ConcurrentHashMap<>();
    private final Map<String, LogoSymbolTable> symbolTableMap = new ConcurrentHashMap<>();

    public LogoTextDocumentService(LogoLanguageServer server) {
        this.server = server;
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String content = params.getTextDocument().getText();
        documentContentMap.put(uri, content);
        updateSymbolTable(uri, content);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String content = params.getContentChanges().get(0).getText();
        
        documentContentMap.put(uri, content);
        updateSymbolTable(uri, content);

        try {
            CharStream stream = CharStreams.fromString(content);
            LogoLexer lexer = new LogoLexer(stream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LogoParser parser = new LogoParser(tokens);
            ParseTree tree = parser.prog();
            client.logMessage(new MessageParams(MessageType.Info, "Parse Tree: " + TreeUtil.toPrettyTree(tree, parser)));
        } catch (RecognitionException e) {
            client.logMessage(new MessageParams(MessageType.Error, e.toString()));
        }
    }

    private void updateSymbolTable(String uri, String content) {
        LogoSymbolTable symbolTable = new LogoSymbolTable();
        CharStream stream = CharStreams.fromString(content);
        LogoLexer lexer = new LogoLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LogoParser parser = new LogoParser(tokens);
        ParseTree tree = parser.prog();

        LogoSymbolListener listener = new LogoSymbolListener(symbolTable, uri);
        ParseTreeWalker.DEFAULT.walk(listener, tree);
        symbolTableMap.put(uri, symbolTable);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documentContentMap.remove(uri);
        symbolTableMap.remove(uri);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        Position pos = params.getPosition();
        LogoSymbolTable symbolTable = symbolTableMap.get(uri);
        if (symbolTable == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        String content = documentContentMap.get(uri);
        if (content == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        String word = getWordAt(content, pos);
        if (word == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        List<Location> locations = new ArrayList<>();
        Location procLoc = symbolTable.getProcedureDefinition(word);
        if (procLoc != null) {
            locations.add(procLoc);
        }
        Location varLoc = symbolTable.getVariableDefinition(word);
        if (varLoc != null) {
            locations.add(varLoc);
        }

        return CompletableFuture.completedFuture(Either.forLeft(locations));
    }

    private String getWordAt(String content, Position pos) {
        String[] lines = content.split("\\r?\\n", -1);
        if (pos.getLine() >= lines.length) return null;
        String line = lines[pos.getLine()];
        int col = pos.getCharacter();
        if (col >= line.length()) return null;

        int start = col;
        while (start > 0 && isLogoNamePart(line.charAt(start - 1))) {
            start--;
        }
        int end = col;
        while (end < line.length() && isLogoNamePart(line.charAt(end))) {
            end++;
        }

        if (start == end) return null;
        return line.substring(start, end);
    }

    private boolean isLogoNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        String code = documentContentMap.get(params.getTextDocument().getUri());
        
        LogoLexer lexer = new LogoLexer(CharStreams.fromString(code));
        LogoParser parser = new LogoParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.prog();

        LogoSemanticsListener listener = new LogoSemanticsListener();
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return CompletableFuture.completedFuture(new SemanticTokens(listener.getData()));
    }
    
}
