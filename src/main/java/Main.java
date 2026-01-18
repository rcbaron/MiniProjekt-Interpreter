import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import parser.MiniCppLexer;
import parser.MiniCppParser;

import Visitor.ASTBuilder;
import ast.ASTNode;
import interp.Interpreter;

public class Main {

    public static void main(String[] args) throws Exception {

        // --------- Testeingabe ---------
        String input = """
               class B {
                                  public:
                                    int x;
                                }
                                
                                class D : public B {
                                  public:
                                    int y;
                                }
                                
                                int main() {
                                  D d;
                                  d.x = 1;
                                  d.y = 2;
                                
                                  B b;
                                  b = d;     // slicing
                                
                                  b.x;       // muss 1 sein
                                  
                                }
                                
        """;


        // --------- Lexer ---------
        MiniCppLexer lexer = new MiniCppLexer(
                CharStreams.fromString(input)
        );

        // --------- Parser ---------
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCppParser parser = new MiniCppParser(tokens);

        // --------- ParseTree ---------
        ParseTree tree = parser.program();
        System.out.println(tree.toStringTree(parser));


        // --------- AST bauen ---------
        ASTBuilder builder = new ASTBuilder();
        ASTNode ast = builder.visit(tree);


        // --------- Interpretieren ---------
        try {
            Interpreter interpreter = new Interpreter();
            Object ret = interpreter.run(ast);
            System.out.println("main returned: " + ret);
        } catch (RuntimeException ex) {
            System.out.println("Runtime error: " + ex.getMessage());
        }

    }
}

