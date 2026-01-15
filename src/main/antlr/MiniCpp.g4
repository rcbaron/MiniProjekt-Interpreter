grammar MiniCpp;

program
    : (functionDecl | classDecl | stmt)* EOF
    ;

functionDecl
    : type ID '(' paramList? ')' block
    ;

paramList
    : param (',' param)*
    ;

param
    : type ID
    ;

classDecl
    : 'class' ID (' : public ' ID)? '{' classMember* '}'
    ;

classMember
    : varDecl          # VarDeclMember
    | functionDecl     # FuncDeclMember
    ;
varDecl
    : type ID ('=' expr)? ';'
    ;

block
    : '{' stmt* '}'
    ;

stmt
    : varDecl        # VarDeclStmt
    | e=expr ';'                     # ExprStmt
    | 'if' '(' expr ')' stmt ('else' stmt)?  # IfStmt
    | 'while' '(' expr ')' stmt      # WhileStmt
    | 'return' expr? ';'             # ReturnStmt
    | block                          # BlockStmt
    ;

expr
    : assignment
    ;

assignment
    : orExpr                       # AssignPass
    | orExpr '=' assignment         # Assign
    ;

orExpr
    : andExpr                       # OrPass
    | orExpr '||' andExpr           # Or
    ;

andExpr
    : equalityExpr                  # AndPass
    | andExpr '&&' equalityExpr     # And
    ;

equalityExpr
    : relationalExpr                # EqPass
    | equalityExpr '==' relationalExpr  # Eq
    | equalityExpr '!=' relationalExpr  # Neq
    ;

relationalExpr
    : additiveExpr                  # RelPass
    | relationalExpr '<' additiveExpr   # Lt
    | relationalExpr '<=' additiveExpr  # Le
    | relationalExpr '>' additiveExpr   # Gt
    | relationalExpr '>=' additiveExpr  # Ge
    ;

additiveExpr
    : multiplicativeExpr            # AddPass
    | additiveExpr '+' multiplicativeExpr  # Add
    | additiveExpr '-' multiplicativeExpr  # Sub
    ;

multiplicativeExpr
    : unaryExpr                     # MulPass
    | multiplicativeExpr '*' unaryExpr  # Mul
    | multiplicativeExpr '/' unaryExpr  # Div
    | multiplicativeExpr '%' unaryExpr  # Mod
    ;

unaryExpr
    : '!' unaryExpr                 # Not
    | '-' unaryExpr                 # UnaryMinus
    | primary                       # UnaryPass
    ;

primary
    : INT                           # IntLiteral
    | BOOL                          # BoolLiteral
    | ID                            # Var
    | '(' expr ')'                  # Parens
    ;

argList
    : expr (',' expr)*
    ;

type
    : 'int'       # IntType
    | 'bool'      # BoolType
    | 'char'      # CharType
    | 'string'    # StringType
    | type '&'    # RefType
    | ID          # ClassType
    ;


// Schlüsselwörter
 INT    : [0-9]+ ;
 BOOL   : 'true' | 'false' ;
 CHAR   : '\'' ( ~['\\] | '\\' . ) '\'' ;
 STRING : '"' ( ~["\\] | '\\' . )* '"' ;

 // Identifier
 ID     : [a-zA-Z_][a-zA-Z0-9_]* ;

 // Operatoren & Symbole
 WS     : [ \t\r\n]+ -> skip ;
 COMMENT : '//' ~[\r\n]* -> skip ;
 BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
 PREPROCESSOR : '#' ~[\r\n]* -> skip ;