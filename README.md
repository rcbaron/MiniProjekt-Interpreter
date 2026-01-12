✅ Projekt-Checkliste: Compiler / Interpreter mit REPL
📁 Projektsetup

☐ Projektordner strukturiert angelegt

☐ Build-System eingerichtet (z. B. CMake)

☐ Trennung in Module (Lexer, Parser, AST, Interpreter, REPL)

🔤 Lexer

☐ Token-Typen als Enum definiert

☐ Token-Struktur (Typ, Lexem, Position)

☐ Whitespace wird ignoriert

☐ Kommentare werden ignoriert

☐ Keywords erkannt

☐ Zahlen & Identifikatoren korrekt erkannt

☐ Lexikalische Fehler werden gemeldet

🌳 AST (Abstract Syntax Tree)

☐ Abstrakte Basisklassen für Expressions

☐ Abstrakte Basisklassen für Statements

☐ Konkrete Expression-Knoten implementiert

☐ Konkrete Statement-Knoten implementiert

☐ Speicherverwaltung sauber gelöst (unique_ptr)

🧩 Parser

☐ Token-Stream Verwaltung (advance, peek, match)

☐ Parser für Ausdrücke implementiert

☐ Operator-Prioritäten korrekt umgesetzt

☐ Parser für Statements implementiert

☐ Block-Strukturen ({}) unterstützt

☐ Sinnvolle Syntaxfehler ausgegeben

🧠 Interpreter

☐ Symboltabelle / Umgebung implementiert

☐ Ausdrucksauswertung funktioniert

☐ Variablen lesen & schreiben

☐ Kontrollstrukturen (if, while) funktionieren

☐ Block-Scopes korrekt behandelt

☐ Laufzeitfehler abgefangen (z. B. undefinierte Variable)

🔁 REPL

☐ Endlosschleife zum Einlesen von Eingaben

☐ Lexer → Parser → Interpreter Pipeline integriert

☐ Umgebung bleibt zwischen Eingaben erhalten

☐ Ergebnisse werden ausgegeben

☐ Fehler beenden die REPL nicht

🧪 Tests

☐ Einzelne Ausdrücke getestet

☐ Mehrere Statements getestet

☐ Kontrollstrukturen getestet

☐ Fehlerfälle getestet

☐ Beispielprogramme vorhanden

🎯 Abgabe-Ready

☐ Code kompiliert ohne Fehler

☐ Keine toten Dateien / Debug-Ausgaben

☐ README mit Build- & Run-Anleitung

☐ Projektumfang klar abgegrenzt
