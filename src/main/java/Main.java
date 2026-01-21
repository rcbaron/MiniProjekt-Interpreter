import visitor.ASTBuilder;
import ast.Program;
import interp.Interpreter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.MiniCppLexer;
import parser.MiniCppParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        Interpreter interpreter = new Interpreter();

        // 1) Optional: Datei laden
        if (args.length > 0) {
            String code = Files.readString(Path.of(args[0]));
            Program p = parseProgram(code);
            interpreter.loadProgram(p);

            // optional main() ausführen, falls vorhanden
            Object ret = interpreter.runMainIfPresent();
            if (ret != null) {
                System.out.println("main returned: " + ret);
            }
        }

        // 2) REPL starten
        runRepl(interpreter);
    }

    private static void runRepl(Interpreter interpreter) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder buf = new StringBuilder();
        boolean firstLine = true;

        while (true) {
            System.out.print(firstLine ? ">>> " : "... ");
            String line = br.readLine();
            if (line == null) break;

            String trimmed = line.trim();
            if (firstLine && (trimmed.equals("exit") || trimmed.equals("quit") || trimmed.equals(":q"))) {
                break;
            }

            buf.append(line).append("\n");

            if (!isInputComplete(buf.toString())) {
                firstLine = false;
                continue;
            }

            String input = buf.toString();
            buf.setLength(0);
            firstLine = true;

            try {
                Program p = parseProgram(input);
                interpreter.execReplProgram(p);
            } catch (RuntimeException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Error: " + ex);
            }
        }
    }

    private static Program parseProgram(String code) {
        CharStream cs = CharStreams.fromString(code);
        MiniCppLexer lexer = new MiniCppLexer(cs);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCppParser parser = new MiniCppParser(tokens);

        // Syntaxfehler als Exception
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line, int charPositionInLine,
                                    String msg, RecognitionException e) {
                throw new RuntimeException("Syntax error at " + line + ":" + charPositionInLine + " - " + msg);
            }
        });

        ParseTree tree = parser.program();
        ASTBuilder builder = new ASTBuilder();
        return (Program) builder.visit(tree);
    }

    // Heuristik fuer Mehrzeilen-Eingaben: Klammern/Blöcke/Strings/Kommentare balancieren
    private static boolean isInputComplete(String s) {
        int paren = 0, brace = 0;
        boolean inStr = false, inChar = false;
        boolean inLineComment = false, inBlockComment = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char n = (i + 1 < s.length()) ? s.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') inLineComment = false;
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && n == '/') { inBlockComment = false; i++; }
                continue;
            }

            if (!inStr && !inChar) {
                if (c == '/' && n == '/') { inLineComment = true; i++; continue; }
                if (c == '/' && n == '*') { inBlockComment = true; i++; continue; }
                if (c == '#') { inLineComment = true; continue; }
            }

            if (inStr) {
                if (c == '\\') { i++; continue; }
                if (c == '"') inStr = false;
                continue;
            }
            if (inChar) {
                if (c == '\\') { i++; continue; }
                if (c == '\'') inChar = false;
                continue;
            }

            if (c == '"') { inStr = true; continue; }
            if (c == '\'') { inChar = true; continue; }

            if (c == '(') paren++;
            else if (c == ')') paren--;
            else if (c == '{') brace++;
            else if (c == '}') brace--;
        }

        return !inStr && !inChar && !inBlockComment && paren == 0 && brace == 0;
    }
}
