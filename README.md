✅ Projekt-Checkliste: Compiler / Interpreter mit REPL
📁 Projektsetup

☐ Projektordner strukturiert angelegt

☐ Build-System eingerichtet (z. B. Gradle)

☐ Trennung in Module (Lexer, Parser, AST, Interpreter, REPL)

🔤 Lexer

Wird von ANTLR übernommen

🌳 AST (Abstract Syntax Tree)

☐ Abstrakte Basisklassen für Expressions

☐ Abstrakte Basisklassen für Statements

☐ Konkrete Expression-Knoten implementiert

☐ Konkrete Statement-Knoten implementiert

☐ Speicherverwaltung sauber gelöst (unique_ptr)

🧩 Parser

Wird von ANTLR übernommen

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
