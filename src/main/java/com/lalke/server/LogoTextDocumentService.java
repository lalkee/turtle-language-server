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
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;
import com.lalke.parser.LogoSemanticsListener;
import com.lalke.parser.TreeUtil;

public class LogoTextDocumentService implements TextDocumentService {
    private LogoLanguageServer server;
    private LanguageClient client;
    private final Map<String, String> documentContentMap = new ConcurrentHashMap<>();

    public LogoTextDocumentService(LogoLanguageServer server) {
        this.server = server;
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        documentContentMap.put(params.getTextDocument().getUri(), params.getTextDocument().getText());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String content = params.getContentChanges().get(0).getText();
        
        documentContentMap.put(uri, content);

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

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        documentContentMap.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
        List<CompletionItem> completionItems = new ArrayList<>();

        CompletionItem item1 = new CompletionItem("test");
        item1.setInsertText("test1");
        item1.setDetail("aaaaaaaaaaa");
        completionItems.add(item1);

        CompletionItem item2 = new CompletionItem("test2");
        item2.setInsertText("test2");

        completionItems.add(item2);

        return CompletableFuture.completedFuture(Either.forLeft(completionItems));
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
