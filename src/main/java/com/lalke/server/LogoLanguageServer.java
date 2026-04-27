package com.lalke.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

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
        
        capabilities.setDefinitionProvider(true);
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

        SemanticTokensWithRegistrationOptions semanticTokensOptions = new SemanticTokensWithRegistrationOptions();
        semanticTokensOptions.setLegend(new SemanticTokensLegend(
            Arrays.asList(
                "keyword",    // 0
                "variable",   // 1
                "function",   // 2
                "number",     // 3
                "string",     // 4
                "operator"    // 5 (used for brackets)
            ),
            new ArrayList<>()
        ));
        semanticTokensOptions.setFull(true);
        capabilities.setSemanticTokensProvider(semanticTokensOptions);

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