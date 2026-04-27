package com.lalke.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

public class LogoSymbolTable {
    //as procedures and variables can have same name, i kept them seperate
    private final Map<String, Location> procedures = new HashMap<>();
    private final Map<String, Location> variables = new HashMap<>();

    public void addProcedure(String name, String uri, Range range) {
        procedures.put(name.toLowerCase(), new Location(uri, range));
    }

    public void addVariable(String name, String uri, Range range) {
        variables.put(name.toLowerCase(), new Location(uri, range));
    }

    public Location getProcedureDefinition(String name) {
        return procedures.get(name.toLowerCase());
    }

    public Location getVariableDefinition(String name) {
        return variables.get(name.toLowerCase());
    }

    public void clear() {
        procedures.clear();
        variables.clear();
    }
}
