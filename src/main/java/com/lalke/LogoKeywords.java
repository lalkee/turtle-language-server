package com.lalke;

import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.Vocabulary;

import com.lalke.antler.LogoLexer;

public class LogoKeywords {
    public static final Set<String> KEYWORDS = new HashSet<>();

    static {
        Vocabulary vocab = LogoLexer.VOCABULARY;
        int max = vocab.getMaxTokenType();
        
        for (int i = 0; i <= max; i++) {
            String literalName = vocab.getLiteralName(i);
            if (literalName != null) {
                // removea antlrs single quotes
                String clean = literalName.replace("'", "");
                
                if (clean.matches("[a-zA-Z].*")) {
                    KEYWORDS.add(clean.toLowerCase());
                }
            }
        }
    }
}