package com.lalke.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import com.lalke.antler.LogoParser;

public class TreeUtil {

    public static String toPrettyTree(ParseTree tree, LogoParser parser) {
        StringBuilder builder = new StringBuilder();
        recursiveFormat(tree, parser, 0, builder);
        return builder.toString();
    }

    private static void recursiveFormat(ParseTree node, LogoParser parser, int level, StringBuilder builder) {
        for (int i = 0; i < level; i++) {
            builder.append("  ");
        }

        String nodeText = Trees.getNodeText(node, parser);
        builder.append(nodeText).append("\n");

        for (int i = 0; i < node.getChildCount(); i++) {
            recursiveFormat(node.getChild(i), parser, level + 1, builder);
        }
    }
}