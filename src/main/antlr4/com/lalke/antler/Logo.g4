/*
BSD License

Copyright (c) 2013, Tom Everett
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Tom Everett nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging
grammar Logo;   

prog
    : (line | EOL)* EOF
    ;

line
    : (cmd | print_)+ comment?
    | comment
    | procedureDeclaration
    ;

cmd
    : repeat_
    | fd
    | bk
    | rt
    | lt
    | cs
    | pu
    | pd
    | ht
    | st
    | home
    | label
    | setxy
    | make
    | procedureInvocation
    | ife
    | stop
    | fore
    | setwidth
    | setcolor
    | setpencolor
    | local
    | setx
    | sety
    | seth
    | arc
    | ellipse
    | pos
    | xcor
    | ycor
    | heading
    | towards
    | fill
    | filled
    | setlabelheight
    | wrap
    | window
    | fence
    | wait_cmd
    | bye
    | ifelse
    | test
    | iffalse
    | iftrue
    | while
    | until
    | dowhile
    | dountil
    | dotimes
    | sum
    | minus
    | modulo
    | power
    | word
    | listp
    | arrayp
    | numberp
    | emptyp
    | equalp
    | notequalp
    | beforep
    | list_cmd
    | first
    | last
    | butfirst
    | butlast
    | item
    | pick
    | readword
    | readlist
    | name_cmd
    | localmake
    ;

localmake
    : 'localmake' STRINGLITERAL terminal
    ;

readword
    : 'readword' terminal
    ;

readlist
    : 'readlist' terminal
    ;

word
    : ('word' | 'word?') terminal
    ;

listp
    : ('listp' | 'list?') terminal
    ;

arrayp
    : ('arrayp' | 'array?') terminal
    ;

numberp
    : ('numberp' | 'number?') terminal
    ;

emptyp
    : ('emptyp' | 'empty?') terminal
    ;

equalp
    : ('equalp' | 'equal?') terminal terminal
    ;

notequalp
    : ('notequalp' | 'notequal?') terminal terminal
    ;

beforep
    : ('beforep' | 'before?') terminal terminal
    ;

list_cmd
    : 'list' terminal+;

first
    : 'first' terminal;

last
    : 'last' terminal ;

butfirst
    : ('butfirst' | 'bf') terminal ;

butlast
    : ('butlast' | 'bl') terminal ;

item
    : 'item' expression terminal ;

pick
    : 'pick' terminal ;

sum
    : 'sum' expression expression;

minus
    : 'minus' expression expression;

modulo
    : 'modulo' expression expression;

power
    : 'power' expression expression; 

ifelse
    : 'ifelse' expression block block;

test
    : 'test' expression;

iftrue
    : 'iftrue' block;

iffalse
    : 'iffalse' block;

while
    : 'while' expression block;

dowhile
    : 'do.while' block expression;

dountil
    : 'do.until' block expression;

until
    : 'until' expression block;

dotimes
    : 'dotimes' '[' name expression ']' block
    ;

setx 
    : 'setx' expression;

sety
    : 'sety' expression;

seth
    : ('setheading' | 'seth' | 'sh') expression;

arc
    : 'arc' expression expression;

ellipse
    : 'ellipse' expression expression;

pos
    : 'pos';

xcor
    : 'xcor';

ycor
    : 'ycor';

heading
    : 'heading';

towards
    : 'towards';

fill
    : 'fill';

filled
    : 'filled' terminal;

wrap
    : 'wrap';

window
    : 'window';

fence
    : 'fence';

wait_cmd
    : 'wait' expression;

bye
    : 'bye';

setlabelheight
    : 'setlabelheight' expression;

procedureInvocation
    : name expression*;

procedureDeclaration
    : 'to' name parameter* EOL? (line | EOL)* 'end'
    ;

parameter
    : ':' name;

func_
    : random
    | word
    | listp
    | arrayp
    | numberp
    | emptyp
    | equalp
    | notequalp
    | beforep
    | readword
    | readlist
    ;

repeat_
    : 'repeat' value block
    ;

block
    : '[' (line | EOL)* ']'
    ;

list
    : '[' (terminal | EOL | WS)* ']'
    ;

ife
    : 'if' expression block
    ;

make
    : 'make' STRINGLITERAL terminal
    ;

name_cmd
    : 'name' terminal STRINGLITERAL;

print_
    : 'print' (value | quotedstring)
    ;

quotedstring
    : '[' (quotedstring | ~ ']')* ']'
    ;

name
    : STRING
    ;

value
    : STRINGLITERAL
    | expression
    | deref
    | list
    ;

local : 'local' STRINGLITERAL ;

signExpression
    : (('+' | '-'))* (number | deref | func_)
    ;

expression
    : comparisonExpression
    ;

comparisonExpression
    : arithmeticExpression (comparisonOperator arithmeticExpression)*
    ;

comparisonOperator
    : '<' | '>' | '=' | '<=' | '>='
    ;

arithmeticExpression
    : multiplyingExpression (('+' | '-') multiplyingExpression)*
    ;

multiplyingExpression
    : signExpression (('*' | '/') signExpression)*
    ;
terminal
    : '(' expression ')'
    | expression
    | STRINGLITERAL
    | deref
    | list
    | func_
    ;

deref
    : ':' name
    ;

fd
    : ('fd' | 'forward') expression
    ;

bk
    : ('bk' | 'backward') expression
    ;

rt
    : ('rt' | 'right') expression
    ;

lt
    : ('lt' | 'left') expression
    ;

cs
    : 'cs'
    | 'clearscreen'
    ;

pu
    : 'pu'
    | 'penup'
    ;

pd
    : 'pd'
    | 'pendown'
    ;

ht
    : 'ht'
    | 'hideturtle'
    ;

st
    : 'st'
    | 'showturtle'
    ;

home
    : 'home'
    ;

stop
    : 'stop'
    ;

label
    : 'label' STRINGLITERAL
    ;

setxy
    : 'setxy' expression expression
    ;

setwidth
    : 'setwidth' expression
    ;

setcolor
    : 'setcolor' terminal
    ;

setpencolor
    : 'setpencolor' terminal
    ;

random
    : 'random' expression
    ;

fore
    : 'for' '[' name expression expression expression ']' block
    ;

number
    : NUMBER
    ;

comment
    : COMMENT
    ;

STRINGLITERAL
    : ('"' | '\'') ~[ \t\r\n]+
    ;

STRING
    : [a-zA-Z] [a-zA-Z0-9_]*
    ;

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

COMMENT
    : ';' ~ [\r\n]*
    ;

EOL
    : ( '\r' | '\n' | '\r\n' )
    ;

WS
    : [ \t]+ -> skip
    ;