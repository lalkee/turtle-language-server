package com.lalke.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class SymbolTable {
    public static class VariableInfo {
        public final String name;
        public final Location location;
        public final Range scope;

        public VariableInfo(String name, Location location, Range scope) {
            this.name = name;
            this.location = location;
            this.scope = scope;
        }
    }

    private final Map<String, Location> procedures = new HashMap<>();
    private final List<VariableInfo> variables = new ArrayList<>();

    public void addProcedure(String name, String uri, Range range) {
        procedures.put(name.toLowerCase(), new Location(uri, range));
    }

    public void addVariable(String name, String uri, Range definitionRange, Range scope) {
        variables.add(new VariableInfo(name.toLowerCase(), new Location(uri, definitionRange), scope));
    }

    public Location getProcedureDefinition(String name) {
        return procedures.get(name.toLowerCase());
    }

    public Location getVariableDefinition(String name, Position pos) {
        String lowerName = name.toLowerCase();
        
        for (int i = variables.size() - 1; i >= 0; i--) {
            VariableInfo info = variables.get(i);
            
            if (info.name.equals(lowerName)) {
                if (isPositionInRange(pos, info.scope)) {
                    if (isBefore(info.location.getRange().getStart(), pos)) {
                        return info.location;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, Location> getProcedures() {
        return procedures;
    }

    public List<String> getVariablesAt(Position pos) {
        List<String> result = new ArrayList<>();
        for (VariableInfo info : variables) {
            if (isPositionInRange(pos, info.scope)) {
                if (!result.contains(info.name)) {
                    result.add(info.name);
                }
            }
        }
        return result;
    }

    private boolean isPositionInRange(Position pos, Range range) {
        if (range == null) return true;
        
        if (pos.getLine() < range.getStart().getLine() || pos.getLine() > range.getEnd().getLine()) {
            return false;
        }
        if (pos.getLine() == range.getStart().getLine() && pos.getCharacter() < range.getStart().getCharacter()) {
            return false;
        }
        if (pos.getLine() == range.getEnd().getLine() && pos.getCharacter() > range.getEnd().getCharacter()) {
            return false;
        }
        return true;
    }

    private boolean isBefore(Position a, Position b) {
        if (a.getLine() < b.getLine()) return true;
        if (a.getLine() > b.getLine()) return false;
        return a.getCharacter() <= b.getCharacter();
    }

    public void clear() {
        procedures.clear();
        variables.clear();
    }
}