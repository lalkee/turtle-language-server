package com.lalke.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.lalke.parser.SymbolTable;

public class DefinitionHelper {

    public static CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> getDefinition(
            DefinitionParams params,
            Map<String, String> documentContentMap,
            Map<String, SymbolTable> symbolTableMap) {
        
        String uri = params.getTextDocument().getUri();
        Position pos = params.getPosition();
        SymbolTable symbolTable = symbolTableMap.get(uri);
        String content = documentContentMap.get(uri);

        if (symbolTable == null || content == null) {
            return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));
        }

        String word = getWordAt(content, pos);
        if (word == null) return CompletableFuture.completedFuture(Either.forLeft(new ArrayList<>()));

        List<Location> locations = new ArrayList<>();
        String normalizedWord = word.toLowerCase();
        
        Location procLoc = symbolTable.getProcedureDefinition(normalizedWord);
        if (procLoc != null) locations.add(procLoc);
        
        Location varLoc = symbolTable.getVariableDefinition(normalizedWord, pos);
        if (varLoc != null) locations.add(varLoc);

        return CompletableFuture.completedFuture(Either.forLeft(locations));
    }

    private static String getWordAt(String content, Position pos) {
        String[] lines = content.split("\\r?\\n", -1);
        if (pos.getLine() >= lines.length) return null;
        String line = lines[pos.getLine()];
        int col = pos.getCharacter();
        if (col >= line.length()) return null;
        int start = col;
        while (start > 0 && isLogoNamePart(line.charAt(start - 1))) start--;
        int end = col;
        while (end < line.length() && isLogoNamePart(line.charAt(end))) end++;
        return (start == end) ? null : line.substring(start, end);
    }

    private static boolean isLogoNamePart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '?' || c == '.';
    }
}
