import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String input = """
            int main() {
                1 + 2;
            }
        """;

        MiniCppLexer lexer = new MiniCppLexer(
                CharStreams.fromString(input)
        );
        MiniCppParser parser = new MiniCppParser(
                new CommonTokenStream(lexer)
        );

        ParseTree tree = parser.program();
        System.out.println(tree.toStringTree(parser));
    }
}
