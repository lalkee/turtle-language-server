package com.lalke.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;

public class LowercaseCharStream implements CharStream {
    private final CharStream delegate;

    public LowercaseCharStream(CharStream delegate) {
        this.delegate = delegate;
    }

    /*la = look ahead. it is used in antlr to peek at next character from stream.
    by overriding it this way, lexer will see all characters as lowercase.
    this is needed as logo is completely case insensitive*/
    @Override
    public int LA(int i) {
        int c = delegate.LA(i);
        //eof is -1
        if (c <= 0) return c;
        return Character.toLowerCase(c);
    }

    @Override
    public String getText(Interval interval) {
        return delegate.getText(interval);
    }

    @Override public void consume() { delegate.consume(); }
    @Override public int mark() { return delegate.mark(); }
    @Override public void release(int marker) { delegate.release(marker); }
    @Override public int index() { return delegate.index(); }
    @Override public void seek(int index) { delegate.seek(index); }
    @Override public int size() { return delegate.size(); }
    @Override public String getSourceName() { return delegate.getSourceName(); }
}
