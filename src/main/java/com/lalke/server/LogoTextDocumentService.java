package com.lalke.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;
import com.lalke.parser.CaseChangingCharStream;
import com.lalke.parser.SemanticsListener;
import com.lalke.parser.SymbolListener;
import com.lalke.parser.SymbolTable;

public class LogoTextDocumentService implements TextDocumentService {
    private LogoLanguageServer server;
    private LanguageClient client;
    private final Map<String, String> documentContentMap = new ConcurrentHashMap<>();
    private final Map<String, SymbolTable> symbolTableMap = new ConcurrentHashMap<>();

    public LogoTextDocumentService(LogoLanguageServer server) {
        this.server = server;
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    private LogoParser createParser(String content) {
        CharStream stream = CharStreams.fromString(content);
        CaseChangingCharStream caseInsensitiveStream = new CaseChangingCharStream(stream);
        LogoLexer lexer = new LogoLexer(caseInsensitiveStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new LogoParser(tokens);
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
    }

    private void updateSymbolTable(String uri, String content) {
        SymbolTable symbolTable = new SymbolTable();
        LogoParser parser = createParser(content);
        ParseTree tree = parser.prog();

        SymbolListener listener = new SymbolListener(symbolTable, uri);
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
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletionHelper.getCompletion(params, documentContentMap, symbolTableMap);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
        return DefinitionHelper.getDefinition(params, documentContentMap, symbolTableMap);
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        String uri = params.getTextDocument().getUri();
        String code = documentContentMap.get(uri);
        if (code == null) return CompletableFuture.completedFuture(new SemanticTokens(new ArrayList<>()));

        SymbolTable symbolTable = symbolTableMap.get(uri);

        LogoParser parser = createParser(code);
        ParseTree tree = parser.prog();

        SemanticsListener listener = new SemanticsListener(symbolTable);
        ParseTreeWalker.DEFAULT.walk(listener, tree);

        return CompletableFuture.completedFuture(new SemanticTokens(listener.getData()));
    }

    @Override public void didSave(DidSaveTextDocumentParams params) {}
}
