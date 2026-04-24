package com.lalke;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import com.lalke.server.LogoLanguageServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LspLauncher {


    public static void main(String[] args) throws Exception {
        startServer(System.in, System.out);
    }

    public static void startServer(InputStream in, OutputStream out) throws Exception {

        LogoLanguageServer server = new LogoLanguageServer();
        Launcher<LanguageClient> launcher =
                LSPLauncher.createServerLauncher(server, in, out);
        Future<?> startListening = launcher.startListening();
        server.connect(launcher.getRemoteProxy());
        startListening.get();
    }
}