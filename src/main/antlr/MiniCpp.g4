grammar MiniCpp;

program
    : functionDecl
    ;

functionDecl
    : 'int' 'main' '(' ')' block
    ;

block
    : '{' stmt* '}'
    ;

stmt
    : expr ';'
    ;

expr
    : expr '+' expr
    | INT
    ;

INT : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;
