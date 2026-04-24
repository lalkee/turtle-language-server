package com.lalke.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

public class LogoLanguageServer implements LanguageServer, LanguageClientAware {

    private LanguageClient client;
    private final LogoTextDocumentService textService;

    public LogoLanguageServer() {
        this.textService = new LogoTextDocumentService(this);
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
        this.textService.setClient(client);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        InitializeResult res = new InitializeResult();
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setCompletionProvider(new CompletionOptions());
        res.setCapabilities(capabilities);
        return CompletableFuture.completedFuture(res);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return new WorkspaceService() {
            @Override
            public void didChangeConfiguration(DidChangeConfigurationParams params) {}

            @Override
            public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {}
        };
    }

    
}