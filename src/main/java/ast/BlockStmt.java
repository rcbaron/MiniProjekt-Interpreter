package ast;

import java.util.ArrayList;
import java.util.List;

public class BlockStmt extends Statement {
    public final List<Statement> statements = new ArrayList<>();
}
