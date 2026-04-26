package com.lalke.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.lalke.antler.LogoLexer;
import com.lalke.antler.LogoParser;

public class ParserUtil {
    public static ParseTree parse(String content) {
        CharStream stream = CharStreams.fromString(content);
        LogoLexer lexer = new LogoLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LogoParser parser = new LogoParser(tokens);
        ParseTree tree = parser.prog();
        return tree; 
    }
}
