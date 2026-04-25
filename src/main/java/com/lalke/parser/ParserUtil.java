package com.lalke.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;

public class ParserUtil {
    public static ParseTree parseLogo(String content) {
        // 1. Convert string to stream
        CharStream stream = CharStreams.fromString(content);
        // 2. Lexical analysis (Tokens)
        LogoLexer lexer = new LogoLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // 3. Parsing (AST/Parse Tree)
        LogoParser parser = new LogoParser(tokens);
        
        // Replace 'prog' with the starting rule defined in your logo.g4
        return parser.prog(); 
    }
}
