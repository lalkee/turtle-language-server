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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
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
        
        if (content == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        /*if user starts with colon, variableMode will ensure only variables are suggested */
        String rawPrefix = getPrefixAt(content, pos);
        boolean variableMode = rawPrefix.startsWith(":");
        String prefix = (variableMode ? rawPrefix.substring(1) : rawPrefix).toLowerCase();

        List<CompletionItem> items = new ArrayList<>();
        SymbolTable symbolTable = symbolTableMap.get(uri);

        if (symbolTable != null) {
            if (variableMode) {
                symbolTable.getVariablesAt(pos).stream()
                        .filter(v -> prefix.isEmpty() || v.toLowerCase().startsWith(prefix))
                        .forEach(v -> {
                            CompletionItem item = new CompletionItem(":" + v);
                            item.setKind(CompletionItemKind.Variable);
                            item.setSortText(v);
                            Range replaceRange = getReplaceRange(pos, rawPrefix);
                            item.setTextEdit(Either.forLeft(new TextEdit(replaceRange, ":" + v)));
                            items.add(item);
                        });
            } else {
                /*[number]_ is used for sorting suggestions. procedures will be shown first, than
                variables, and than keywords*/
                symbolTable.getProcedures().keySet().stream()
                        .filter(p -> prefix.isEmpty() || p.toLowerCase().startsWith(prefix))
                        .forEach(p -> {
                            CompletionItem item = new CompletionItem(p);
                            item.setKind(CompletionItemKind.Function);
                            item.setSortText("0_" + p);
                            items.add(item);
                        });

                symbolTable.getVariablesAt(pos).stream()
                        .filter(v -> prefix.isEmpty() || v.toLowerCase().startsWith(prefix))
                        .forEach(v -> {
                            CompletionItem item = new CompletionItem(":" + v);
                            item.setKind(CompletionItemKind.Variable);
                            item.setSortText("1_" + v);

                            Range replaceRange = getReplaceRange(pos, rawPrefix);
                            item.setTextEdit(Either.forLeft(new TextEdit(replaceRange, ":" + v)));

                            items.add(item);
                        });

                LogoKeywords.KEYWORDS.stream()
                        .filter(kw -> prefix.isEmpty() || kw.startsWith(prefix))
                        .forEach(kw -> {
                            CompletionItem item = new CompletionItem(kw);
                            item.setKind(CompletionItemKind.Keyword);
                            item.setSortText("2_" + kw);
                            items.add(item);
                        });
            }
        }

        return CompletableFuture.completedFuture(Either.forLeft(items));
    }


    //finds the part of the word user has already typed
    private static String getPrefixAt(String content, Position pos) {
        //matches both windows and unix line endings
        String[] lines = content.split("\\r?\\n", -1);
        if (pos.getLine() >= lines.length) return "";
        String line = lines[pos.getLine()];
        int col = Math.min(pos.getCharacter(), line.length());
        int start = col;

        while (start > 0 && isLogoNamePart(line.charAt(start - 1))) start--;

        //includes a colon if there is one
        if (start > 0 && line.charAt(start - 1) == ':') start--;

        return line.substring(start, col);
    }


    /*find the range of text that should be replaced by completion.
    for variables, the colon is part of the token to replace.*/
    private static Range getReplaceRange(Position pos, String rawPrefix) {
        int line = pos.getLine();
        int endChar = pos.getCharacter();
        int startChar = endChar - rawPrefix.length();
        
        return new Range(new Position(line, startChar), new Position(line, endChar));
    }

    private static boolean isLogoNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '?' || c == '.';
    }
}