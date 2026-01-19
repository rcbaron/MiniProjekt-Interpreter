grammar MiniCpp;

/**
 * Startregel (Entry Point):
 * Ein Programm besteht aus einer Folge von Funktionsdeklarationen,
 * Klassendeklarationen oder direkten Statements (REPL/Scripting).
 */
program
    : (functionDecl | classDecl | stmt)* EOF
    ;

/**
 * Funktionsdeklaration:
 * - Optionales 'virtual' (fuer Methoden in Klassen).
 * - Rueckgabetyp, Name, Parameterliste in Klammern, gefolgt vom Body (Block).
 */
functionDecl
    : ('virtual')? type ID '(' paramList? ')' block
    ;

// Liste von Parametern, durch Komma getrennt (z.B. "int a, bool b")
paramList
    : param (',' param)*
    ;

// Einzelner Parameter: Typ und Name (z.B. "int x" oder "int& y")
param
    : type ID
    ;

/**
 * Klassendeklaration:
 * - Beginnt mit 'class'.
 * - Optional: Vererbung via ': public BaseClass'.
 * - Body in geschweiften Klammern mit Membern (Felder, Konstruktoren, Methoden).
 * - Optionales Semikolon am Ende (C++).
 */
classDecl
    : 'class' ID (':' 'public' ID)? '{' ('public' ':')? classMember* '}' ';'?
    ;

// Ein Member einer Klasse kann eine Variable (Feld), ein Konstruktor oder eine Methode sein.
classMember
    : varDecl                 # VarDeclMember
    | constructorDecl         # CtorDeclMember
    | functionDecl            # FuncDeclMember
    ;

// Konstruktor-Deklaration: Heisst wie die Klasse (ID), kein Rueckgabetyp.
constructorDecl
    : ID '(' paramList? ')' block
    ;

// Variablendeklaration: Typ, Name, optionale Initialisierung (z.B. "int x = 5;").
varDecl
    : type ID ('=' expr)? ';'
    ;

// Ein Block besteht aus geschweiften Klammern und beliebig vielen Statements.
block
    : '{' stmt* '}'
    ;

/**
 * Statements (Anweisungen):
 * Hier werden Kontrollfluss und Struktur definiert.
 */
stmt
    : varDecl                                        # VarDeclStmt
    | e=expr ';'                                     # ExprStmt
    | 'if' '(' expr ')' stmt ('else' stmt)?          # IfStmt
    | 'while' '(' expr ')' stmt                      # WhileStmt
    | 'return' expr? ';'                             # ReturnStmt
    | block                                          # BlockStmt
    ;

/**
 * Expressions (Ausdruecke):
 * Die Reihenfolge der Regeln definiert die Operator-Praezedenz (Bindungsstaerke).
 * Je weiter unten eine Regel steht, desto stärker bindet sie (Punkt vor Strich).
 */

 // Oberste Ebene: Zuweisung (niedrigste Prioritaet, rechtsassoziativ).
expr
    : assignment
    ;

assignment
    : orExpr                       # AssignPass // Fall-through zur naechsten Ebene
    | orExpr '=' assignment         # Assign    // Rekursiv rechts: a = b = c
    ;

// Logisches ODER (||)
orExpr
    : andExpr                       # OrPass
    | orExpr '||' andExpr           # Or
    ;

// Logisches UND (&&)
andExpr
    : equalityExpr                  # AndPass
    | andExpr '&&' equalityExpr     # And
    ;

// Gleichheit (==, !=)
equalityExpr
    : relationalExpr                # EqPass
    | equalityExpr '==' relationalExpr  # Eq
    | equalityExpr '!=' relationalExpr  # Neq
    ;

// Vergleiche (<, <=, >, >=)
relationalExpr
    : additiveExpr                  # RelPass
    | relationalExpr '<' additiveExpr   # Lt
    | relationalExpr '<=' additiveExpr  # Le
    | relationalExpr '>' additiveExpr   # Gt
    | relationalExpr '>=' additiveExpr  # Ge
    ;

// Addition und Subtraktion (+, -)
additiveExpr
    : multiplicativeExpr            # AddPass
    | additiveExpr '+' multiplicativeExpr  # Add
    | additiveExpr '-' multiplicativeExpr  # Sub
    ;

// Multiplikation, Division, Modulo (*, /, %) - Hohe Prioritaet
multiplicativeExpr
    : unaryExpr                     # MulPass
    | multiplicativeExpr '*' unaryExpr  # Mul
    | multiplicativeExpr '/' unaryExpr  # Div
    | multiplicativeExpr '%' unaryExpr  # Mod
    ;

// Unaere Operatoren (!, -) und Postfix-Ausdruecke
unaryExpr
    : '!' unaryExpr                 # Not
    | '-' unaryExpr                 # UnaryMinus
    | postfix                       # UnaryPass
    ;

/**
 * Postfix-Ausdruck:
 * Behandelt Member-Zugriffe (.) und Methodenaufrufe.
 * Beispiel: a.b.c()
 * - Startet mit einem Atom.
 * - Danach beliebig viele Zugriffe auf Member (ID) oder Methoden (ID + Klammern).
 */
postfix
    : atom ('.' ID ('(' argList? ')')? )*
    ;

/**
 * Atome (Die kleinsten Einheiten):
 * - Funktions-/Konstruktoraufrufe
 * - Literale (Zahlen, Strings, Bools)
 * - Variablenzugriffe
 * - Klammerung von Ausdruecken
 */
atom
    : ID '(' argList? ')'           # CallOrCtor    // Funktionsaufruf oder Konstruktor (z.B. "f()" oder "A()")
    | INT                           # IntLiteral    // Ganzzahl: 42
    | BOOL                          # BoolLiteral   // true/false
    | CHAR                          # CharLiteral   // 'a'
    | STRING                        # StringLiteral // "text"
    | ID                            # Var           // Variablenname: x
    | '(' expr ')'                  # Parens        // Klammerung: (a + b)
    ;

// Argumentliste fuer Funktionsaufrufe (z.B. "1, 2, x")
argList
    : expr (',' expr)*
    ;

/**
 * Typen:
 * Unterstuetzt primitive Typen, Referenzen (int&) und benutzerdefinierte Klassen (ID).
 */
type
    : 'int'       # IntType
    | 'bool'      # BoolType
    | 'char'      # CharType
    | 'string'    # StringType
    | type '&'    # RefType
    | ID          # ClassType
    ;

// ---------------- LEXER REGELN ----------------

// Literale und Schluesselwoerter
 INT    : [0-9]+ ;
 BOOL   : 'true' | 'false' ;
 CHAR   : '\'' ( ~['\\] | '\\' . ) '\'' ;
 STRING : '"' ( ~["\\] | '\\' . )* '"' ;

// Bezeichner (Variablen-, Funktions-, Klassennamen)
 ID     : [a-zA-Z_][a-zA-Z0-9_]* ;

// Whitespace (Leerzeichen, Tabs, Newlines) -> ignorieren
 WS     : [ \t\r\n]+ -> skip ;

 // Kommentare (einzeilig //) -> ignorieren
 COMMENT : '//' ~[\r\n]* -> skip ;

 // Block-Kommentare (/* ... */) -> ignorieren
 BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

 // Praeprozessor (#include etc.) -> ignorieren
 PREPROCESSOR : '#' ~[\r\n]* -> skip ;