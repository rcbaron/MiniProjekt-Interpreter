package interp;

import ast.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {

    // Scope-Stack: top = aktueller Scope
    private final Deque<Map<String, Object>> scopes = new ArrayDeque<>();
    private final Map<String, java.util.List<ast.FunctionDecl>> functions = new HashMap<>();

    public Interpreter() {
        // globaler Scope
        scopes.push(new HashMap<>());
    }

    // --------- Public API ---------

    public Object run(ASTNode node) {
        // 1) Programm "ausführen" = Funktionen registrieren + evtl. Top-Level-Statements
        exec(node);

        java.util.List<ast.FunctionDecl> mains = functions.get("main");
        if (mains == null || mains.isEmpty()) {
            throw new RuntimeException("No main function");
        }
        if (mains.size() != 1) {
            throw new RuntimeException("Ambiguous main()");
        }
        ast.FunctionDecl main = mains.get(0);


        // 3) main ausführen (mit ReturnValue catch)
        try {
            exec(main.body);
            return null;
        } catch (interp.ReturnValue rv) {
            return rv.value;
        }
    }

    // --------- Scope helpers ---------

    private void define(String name, Object value) {
        // define im aktuellen Scope
        scopes.peek().put(name, value);
    }

    private void assign(String name, Object value) {
        // suche Variable von innen nach außen
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    private Object lookup(String name) {
        for (Map<String, Object> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    // --------- Execution ---------

    private Object exec(ASTNode node) {
        if (node == null) {
            throw new RuntimeException("BUG: exec() got null AST node");
        }

        // Program: alle Declarations/Top-Level Nodes ausführen
        if (node instanceof Program p) {
            Object last = null;
            for (ASTNode decl : p.declarations) {
                last = exec(decl);
            }
            return last;
        }

        if (node instanceof FunctionDecl f) {
            functions.computeIfAbsent(f.name, k -> new java.util.ArrayList<>()).add(f);
            return null;
        }


        // BlockStmt: neuer Scope
        if (node instanceof BlockStmt b) {
            scopes.push(new HashMap<>());
            try {
                Object last = null;
                for (Statement s : b.statements) {
                    last = exec(s);
                }
                return last;
            } finally {
                scopes.pop();
            }
        }

        // VarDeclStmt
        if (node instanceof VarDeclStmt v) {
            Object value = (v.init != null) ? eval(v.init) : 0; // default int=0
            define(v.name, value);
            return null;
        }

        if (node instanceof ExprStmt es) {
            Object result = eval(es.expr);

            // NICHT drucken, wenn der Ausdruck eine Zuweisung ist
            if (!(es.expr instanceof BinaryExpr be && "=".equals(be.op))) {
                System.out.println(result);
            }
            return result;
        }

        if (node instanceof IfStmt is) {
            boolean c = toBool(eval(is.cond));
            if (c) {
                return exec(is.thenStmt);
            } else if (is.elseStmt != null) {
                return exec(is.elseStmt);
            }
            return null;
        }

        if (node instanceof WhileStmt ws) {
            Object last = null;
            while (toBool(eval(ws.cond))) {
                last = exec(ws.body);
            }
            return last;
        }

        if (node instanceof ast.ReturnStmt rs) {
            Object v = (rs.expr != null) ? eval(rs.expr) : null;
            throw new interp.ReturnValue(v);
        }



        throw new RuntimeException("Unknown AST node in exec: " + node.getClass().getSimpleName());
    }

    // --------- Expression evaluation ---------

    private Object eval(Expr e) {
        if (e instanceof IntLiteral il) return il.value;

        if (e instanceof ast.FunctionCallExpr fc) {
            java.util.List<ast.FunctionDecl> overloads = functions.get(fc.name);
            if (overloads == null || overloads.isEmpty()) {
                throw new RuntimeException("Undefined function: " + fc.name);
            }

            java.util.List<ast.FunctionDecl> candidates = new java.util.ArrayList<>();
            for (ast.FunctionDecl cand : overloads) {
                if (cand.params.size() == fc.args.size()) {
                    candidates.add(cand);
                }
            }

            if (candidates.isEmpty()) {
                throw new RuntimeException("No matching overload for " + fc.name +
                        " with " + fc.args.size() + " args");
            }
            if (candidates.size() > 1) {
                throw new RuntimeException("Ambiguous overload for " + fc.name +
                        " with " + fc.args.size() + " args");
            }

            ast.FunctionDecl f = candidates.get(0);


            // Arity check
            if (fc.args.size() != f.params.size()) {
                throw new RuntimeException("Arity mismatch for " + fc.name +
                        ": expected " + f.params.size() + " got " + fc.args.size());
            }

            // Argumente auswerten
            java.util.List<Object> values = new java.util.ArrayList<>();
            for (ast.Expr arg : fc.args) {
                values.add(eval(arg));
            }

            // Neuer Scope für den Funktionsaufruf
            scopes.push(new java.util.HashMap<>());
            try {
                // Parameter binden: a=..., b=...
                for (int i = 0; i < f.params.size(); i++) {
                    define(f.params.get(i).name, values.get(i));
                }

                // Body ausführen + return abfangen
                try {
                    exec(f.body);
                    return null;
                } catch (interp.ReturnValue rv) {
                    return rv.value;
                }

            } finally {
                scopes.pop();
            }
        }


        if (e instanceof VarExpr ve) {
            return lookup(ve.name);
        }

        if (e instanceof BinaryExpr be) {
            if ("&&".equals(be.op)) {
                boolean lb = toBool(eval(be.left));
                if (!lb) return false;          // short-circuit: rechts NICHT auswerten
                return toBool(eval(be.right));
            }

            if ("||".equals(be.op)) {
                boolean lb = toBool(eval(be.left));
                if (lb) return true;            // short-circuit: rechts NICHT auswerten
                return toBool(eval(be.right));
            }

            Object l = eval(be.left);
            Object r = eval(be.right);

            return switch (be.op) {
                case "+" -> (Integer) l + (Integer) r;
                case "-" -> (Integer) l - (Integer) r;
                case "*" -> (Integer) l * (Integer) r;

                case "/" -> {
                    int rr = (Integer) r;
                    if (rr == 0) throw new RuntimeException("Division by zero");
                    yield (Integer) l / rr;
                }

                case "%" -> {
                    int rr = (Integer) r;
                    if (rr == 0) throw new RuntimeException("Modulo by zero");
                    yield (Integer) l % rr;
                }

                case "=" -> {
                    if (!(be.left instanceof VarExpr v)) {
                        throw new RuntimeException("Left side of assignment must be a variable");
                    }
                    // rechts ist schon eval()'d (r)
                    assign(v.name, r);
                    yield r;
                }

                case "==" -> ((Integer) l).intValue() == ((Integer) r).intValue();
                case "!=" -> ((Integer) l).intValue() != ((Integer) r).intValue();
                case "<"  -> ((Integer) l).intValue() <  ((Integer) r).intValue();
                case "<=" -> ((Integer) l).intValue() <= ((Integer) r).intValue();
                case ">"  -> ((Integer) l).intValue() >  ((Integer) r).intValue();
                case ">=" -> ((Integer) l).intValue() >= ((Integer) r).intValue();


                default -> throw new RuntimeException("Unknown operator: " + be.op);
            };
        }

        throw new RuntimeException("Unknown Expr node: " + e.getClass().getSimpleName());
    }

    private boolean toBool(Object v) {
        if (v instanceof Boolean b) return b;
        if (v instanceof Integer i) return i != 0;
        throw new RuntimeException("Condition is not bool/int (yet): " + v);
    }

}
