package com.lalke.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    //procedures are global, so keeping scope info is not needed
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

    /*returns "closest" definition of variable. when document is parsed,
    variable definitions are added in order they appear, so if variable with same name
    is declared multiple times, local declaration is the one with highest index. thats why
    variables are iterated over backwards.*/
    public Location getVariableDefinition(String name, Position pos) {
        return IntStream.iterate(variables.size() - 1, i -> i >= 0, i -> i - 1)
                        .mapToObj(variables::get)
                        .filter(info -> info.name.equals(name.toLowerCase()))
                        .filter(info -> isPositionInRange(pos, info.scope))
                        .filter(info -> isBefore(info.location.getRange().getStart(), pos))
                        .map(info -> info.location)
                        .findFirst()
                        .orElse(null);
    }

    public Map<String, Location> getProcedures() {
        return procedures;
    }

    //returns all variable names in scope of current position
    public List<String> getVariablesAt(Position pos) {
        return variables.stream()
            .filter(info -> isPositionInRange(pos, info.scope))
            .map(info -> info.name)
            .distinct()
            .collect(Collectors.toList());
    }

    private boolean isPositionInRange(Position pos, Range range) {
        if (range == null) return true;
        
        if (pos.getLine() < range.getStart().getLine() || pos.getLine() > range.getEnd().getLine())
            return false;

        if (pos.getLine() == range.getStart().getLine() && pos.getCharacter() < range.getStart().getCharacter())
            return false;

        return !(pos.getLine() == range.getEnd().getLine() && pos.getCharacter() > range.getEnd().getCharacter());
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