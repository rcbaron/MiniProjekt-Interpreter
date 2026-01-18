package ast;

import java.util.ArrayList;
import java.util.List;

public class Program extends ASTNode {
    public final List<ASTNode> declarations = new ArrayList<>();
}
