package minicpp;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import parser.MiniCppLexer;
import parser.MiniCppParser;

import Visitor.ASTBuilder;
import ast.ASTNode;
import interp.Interpreter;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Interpreter einmalig instanziieren, damit Variablen (Scope) erhalten bleiben
        Interpreter interpreter = new Interpreter();
        ASTBuilder builder = new ASTBuilder();
        Scanner scanner = new Scanner(System.in);

        System.out.println("MiniCpp Interpreter REPL");
        System.out.println("Geben Sie Code ein (z.B. 'int a = 10;' oder 'a + 5;').");
        System.out.println("Tippen Sie 'exit', um zu beenden.");

        while (true) {
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine();

            // Abbruchbedingung
            if (line.trim().equalsIgnoreCase("exit")) {
                break;
            }

            // Leere Eingaben überspringen
            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                // 1. Lexer erstellen
                MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(line));

                // 2. Parser erstellen
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                MiniCppParser parser = new MiniCppParser(tokens);

                // Einfaches Error-Handling: Bei Syntaxfehlern Exception werfen
                parser.removeErrorListeners();
                parser.addErrorListener(new BaseErrorListener() {
                    @Override
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                        throw new RuntimeException("Syntax Error at col " + charPositionInLine + ": " + msg);
                    }
                });

                // 3. ParseTree erstellen (Einstiegspunkt 'program')
                // Wir nutzen 'program', damit sowohl Statements als auch Funktionsdeklarationen möglich sind.
                ParseTree tree = parser.program();

                // 4. AST bauen
                ASTNode ast = builder.visit(tree);

                // 5. Interpretieren
                interpreter.run(ast);

            } catch (Exception ex) {
                System.out.println("Fehler: " + ex.getMessage());
                // Hier kein 'break', damit die REPL weiterläuft
            }
        }

        scanner.close();
        System.out.println("Auf Wiedersehen!");
    }
}