package com.lalke.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

public class LogoTextDocumentService implements TextDocumentService {
    private LogoLanguageServer server;
    private LanguageClient client;

    public LogoTextDocumentService(LogoLanguageServer server) {
        this.server = server;
    }

    public void setClient(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
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
    
}
