package com.lalke.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.lalke.LogoKeywords;
import com.lalke.parser.SymbolTable;

public class CompletionHelper {

    public static CompletableFuture<Either<List<CompletionItem>, CompletionList>> getCompletion(
            CompletionParams params,
            Map<String, String> documentContentMap,
            Map<String, SymbolTable> symbolTableMap) {
        
        String uri = params.getTextDocument().getUri();
        Position pos = params.getPosition();
        String content = documentContentMap.get(uri);
        if (content == null) return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));

        String prefix = getPrefixAt(content, pos).toLowerCase();
        if (prefix.isEmpty()) return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));

        List<CompletionItem> items = new ArrayList<>();
        SymbolTable symbolTable = symbolTableMap.get(uri);

        if (symbolTable != null) {
            symbolTable.getProcedures().keySet().stream()
                .filter(p -> p.toLowerCase().startsWith(prefix))
                .forEach(p -> {
                    CompletionItem item = new CompletionItem(p);
                    item.setKind(CompletionItemKind.Function);
                    item.setSortText("0_" + p);
                    items.add(item);
                });

            symbolTable.getVariablesAt(pos).stream()
                .filter(v -> v.toLowerCase().startsWith(prefix))
                .forEach(v -> {
                    CompletionItem item = new CompletionItem(":" + v);
                    item.setKind(CompletionItemKind.Variable);
                    item.setSortText("1_" + v);
                    items.add(item);
                });
        }

        LogoKeywords.KEYWORDS.stream()
            .filter(kw -> kw.startsWith(prefix))
            .forEach(kw -> {
                CompletionItem item = new CompletionItem(kw);
                item.setKind(CompletionItemKind.Keyword);
                item.setSortText("2_" + kw);
                items.add(item);
            });

        return CompletableFuture.completedFuture(Either.forLeft(items));
    }

    private static String getPrefixAt(String content, Position pos) {
        String[] lines = content.split("\\r?\\n", -1);
        if (pos.getLine() >= lines.length) return "";
        String line = lines[pos.getLine()];
        int col = Math.min(pos.getCharacter(), line.length());
        int start = col;
        while (start > 0 && isLogoNamePart(line.charAt(start - 1))) start--;
        return line.substring(start, col);
    }

    private static boolean isLogoNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '?' || c == '.';
    }
}
