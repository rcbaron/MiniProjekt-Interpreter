grammar MiniCpp;

@header {
package minicpp.antlr;
}

/* ============================================================
 * Parser Rules
 * ============================================================ */

program
    : (classDecl | functionDecl)* EOF
    ;

/* ---------------- Classes ---------------- */

classDecl
    : CLASS Identifier
      ( ':' PUBLIC Identifier )?
      '{' classMember* '}'
      ';'
    ;

classMember
    : fieldDecl
    | methodDecl
    ;

/* ---------------- Functions / Methods ---------------- */

functionDecl
    : type Identifier '(' parameterList? ')' block
    ;

methodDecl
    : (VIRTUAL)?
      type Identifier '(' parameterList? ')' block
    ;

/* ---------------- Fields / Variables ---------------- */

fieldDecl
    : type Identifier ';'
    ;

varDecl
    : type Identifier ('=' expression)? ';'
    ;

/* ---------------- Parameters ---------------- */

parameterList
    : parameter (',' parameter)*
    ;

parameter
    : type Identifier
    ;

/* ---------------- Types ---------------- */

type
    : baseType
    | Identifier
    | type '&'
    ;

baseType
    : INT
    | BOOL
    | CHAR
    | STRING
    | VOID
    ;

/* ---------------- Statements ---------------- */

block
    : '{' statement* '}'
    ;

statement
    : block
    | varDecl
    | assignment ';'
    | ifStmt
    | whileStmt
    | returnStmt
    | exprStmt
    ;

ifStmt
    : IF '(' expression ')' statement (ELSE statement)?
    ;

whileStmt
    : WHILE '(' expression ')' statement
    ;

returnStmt
    : RETURN expression? ';'
    ;

exprStmt
    : expression ';'
    ;

/* ---------------- Expressions ---------------- */

expression
    : assignment
    ;

assignment
    : logicOr ('=' assignment)?
    ;

logicOr
    : logicAnd ('||' logicAnd)*
    ;

logicAnd
    : equality ('&&' equality)*
    ;

equality
    : comparison (('==' | '!=') comparison)*
    ;

comparison
    : term (('<' | '<=' | '>' | '>=') term)*
    ;

term
    : factor (('+' | '-') factor)*
    ;

factor
    : unary (('*' | '/' | '%') unary)*
    ;

unary
    : ('!' | '+' | '-') unary
    | primary
    ;

primary
    : atom suffix*
    ;

atom
    : literal
    | Identifier
    | functionCall
    | '(' expression ')'
    ;

suffix
    : '.' Identifier
    | '(' argumentList? ')'
    ;

/* ---------------- Calls / Access ---------------- */

functionCall
    : Identifier '(' argumentList? ')'
    ;

fieldAccess
    : primary '.' Identifier
    ;

argumentList
    : expression (',' expression)*
    ;

/* ---------------- Literals ---------------- */

literal
    : INT_LITERAL
    | CHAR_LITERAL
    | STRING_LITERAL
    | TRUE
    | FALSE
    ;

/* ============================================================
 * Lexer Rules
 * ============================================================ */

/* ---------------- Keywords ---------------- */

INT     : 'int';
BOOL    : 'bool';
CHAR    : 'char';
STRING  : 'string';
VOID    : 'void';

TRUE    : 'true';
FALSE   : 'false';

IF      : 'if';
ELSE    : 'else';
WHILE   : 'while';
RETURN  : 'return';

CLASS   : 'class';
PUBLIC  : 'public';
VIRTUAL : 'virtual';

/* ---------------- Identifiers ---------------- */

Identifier
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

/* ---------------- Literals ---------------- */

INT_LITERAL
    : [0-9]+
    ;

CHAR_LITERAL
    : '\'' ( '\\' . | ~['\\] ) '\''
    ;

STRING_LITERAL
    : '"' ( '\\' . | ~["\\] )* '"'
    ;

/* ---------------- Comments & Whitespace ---------------- */

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

PREPROCESSOR
    : '#' ~[\r\n]* -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
