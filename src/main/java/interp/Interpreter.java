package interp;

import ast.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {

    // Scope-Stack: top = aktueller Scope
    private final Deque<Map<String, Binding>> scopes = new ArrayDeque<>();
    private final Map<String, java.util.List<ast.FunctionDecl>> functions = new HashMap<>();
    private final Map<String, ClassInfo> classes = new HashMap<>();

    private interp.InstanceValue currentReceiver = null;


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

    // --------- Scope helpers (NEU) ---------

    private void define(String name, Binding binding) {
        scopes.peek().put(name, binding);
    }

    private Binding lookupBinding(String name) {
        for (Map<String, Binding> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        // Wenn wir gerade in einer Methode sind: unqualifizierte Namen dürfen Felder sein
        if (currentReceiver != null) {
            // Feld-Cell im Objekt suchen
            Cell c = currentReceiver.fieldCells.get(name);
            if (c != null) {
                // Feldtyp aus der registrierten Klasse holen (so sauber wie möglich)
                ClassInfo ci = classes.get(currentReceiver.dynamicClass);
                ast.TypeNode ft = (ci != null) ? ci.fields.get(name) : null;

                // Fallback: wenn Typ gerade nicht bekannt ist, trotzdem binden
                if (ft == null) ft = new ast.IntTypeNode(); // minimaler Fallback

                return new RefBinding(ft, c);
            }
        }

        throw new RuntimeException("Undefined variable: " + name);
    }

    private Object lookupValue(String name) {
        return lookupBinding(name).cell().get();
    }

    private void assign(String name, Object value) {
        lookupBinding(name).cell().set(value);
    }

    // --------- LValue helper ---------

    private Cell evalLValue(Expr e) {
        if (e == null) {
            throw new RuntimeException("BUG: evalLValue got null Expr (ASTBuilder created null)");
        }
        if (e instanceof VarExpr v) {
            return lookupBinding(v.name).cell();
        }
        if (e instanceof ast.FieldAccessExpr fa) {
            Object ov = eval(fa.obj);
            if (!(ov instanceof interp.InstanceValue inst)) {
                throw new RuntimeException("Field access on non-object");
            }
            Cell c = inst.fieldCells.get(fa.field);
            if (c == null) throw new RuntimeException("Unknown field: " + fa.field);
            return c;
        }
        throw new RuntimeException("Not an lvalue: " + e.getClass().getSimpleName());
    }


    // --------- Execution ---------

    private Object exec(ASTNode node) {
        if (node == null) {
            throw new RuntimeException("BUG: exec() got null AST node");
        }

        if (node instanceof Program p) {
            // Pass 1: Klassen und Funktionen registrieren
            for (ASTNode decl : p.declarations) {
                if (decl instanceof ast.ClassDecl cd) exec(cd);       // ruft registerClass
                if (decl instanceof ast.FunctionDecl fd) exec(fd);    // registriert functions
            }

            // Pass 2: restliche Statements (falls vorhanden)
            Object last = null;
            for (ASTNode decl : p.declarations) {
                if (decl instanceof ast.Statement s) last = exec(s);
            }
            return last;
        }


        if (node instanceof ast.ClassDecl c) {
            if (classes.containsKey(c.name)) {
                throw new RuntimeException("Class redefined: " + c.name);
            }
            registerClass(c);
            return null;
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

            // Referenz: T& r = <lvalue>;
            if (v.type instanceof RefTypeNode) {
                if (v.init == null) {
                    throw new RuntimeException("Reference must be initialized: " + v.name);
                }
                Cell target = evalLValue(v.init);             // muss lvalue sein
                define(v.name, new RefBinding(v.type, target)); // Alias
                return null;
            }

            // Klassentyp: T x;  -> Default-Konstruktor / Default-Init
            if (v.type instanceof ast.ClassTypeNode ct && v.init == null) {
                // InstanceValue mit Feldern anlegen
                interp.InstanceValue inst = newInstance(ct.name);  // ct.name ggf. anpassen
                define(v.name, new ValueBinding(v.type, new Cell(inst)));
                return null;
            }

            // normale Variable: T x = expr;
            Object value = (v.init != null) ? eval(v.init) : 0; // default int=0
            define(v.name, new ValueBinding(v.type, new Cell(value)));
            return null;
        }


        if (node instanceof ExprStmt es) {
            Object result = eval(es.expr);

            if (!(es.expr instanceof BinaryExpr be && "=".equals(be.op))
                    && !(es.expr instanceof FunctionCallExpr)
                    && result != null) {
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
        if (e instanceof ast.BoolLiteral bl) return bl.value;
        if (e instanceof ast.CharLiteral cl) return cl.value;
        if (e instanceof ast.StringLiteral sl) return sl.value;

        if (e instanceof ast.FunctionCallExpr fc) {

            // ---------- BUILTINS ----------
            if (fc.name.equals("print_int")) {
                Object v = eval(fc.args.get(0));
                System.out.println((Integer) v);
                return null;
            }
            if (fc.name.equals("print_bool")) {
                Object v = eval(fc.args.get(0));
                System.out.println((Boolean) v ? "true" : "false");
                return null;
            }
            if (fc.name.equals("print_char")) {
                Object v = eval(fc.args.get(0));
                System.out.println((Character) v);
                return null;
            }
            if (fc.name.equals("print_string")) {
                Object v = eval(fc.args.get(0));
                System.out.println((String) v);
                return null;
            }

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

            // Argument-Typen bestimmen
            java.util.List<ast.TypeNode> argTypes = new java.util.ArrayList<>();
            for (ast.Expr arg : fc.args) {
                ast.TypeNode t = inferType(arg);
                if (t == null) {
                    throw new RuntimeException("Cannot infer type of argument in call to " + fc.name);
                }
                argTypes.add(t);
            }

// Kandidaten nach Param-Typen filtern
            java.util.List<ast.FunctionDecl> typedMatches = new java.util.ArrayList<>();
            for (ast.FunctionDecl cand : candidates) {
                boolean ok = true;
                for (int i = 0; i < cand.params.size(); i++) {
                    ast.TypeNode paramType = cand.params.get(i).type;
                    ast.TypeNode argType = argTypes.get(i);

                    if (paramType instanceof RefTypeNode rt) {
                        // 1) Basistyp muss passen (int& akzeptiert int)
                        if (!sameType(rt.base, argType)) {
                            ok = false;
                            break;
                        }
                        // 2) Argument MUSS lvalue sein
                        try {
                            evalLValue(fc.args.get(i));
                        } catch (RuntimeException ex) {
                            ok = false;
                            break;
                        }
                    } else {
                        // normaler by-value Parameter
                        if (!sameType(paramType, argType)) {
                            ok = false;
                            break;
                        }
                    }
                }

                if (ok) typedMatches.add(cand);
            }

            if (typedMatches.isEmpty()) {
                throw new RuntimeException("No matching overload for " + fc.name + " with given argument types");
            }
            if (typedMatches.size() > 1) {
                throw new RuntimeException("Ambiguous overload for " + fc.name + " with given argument types");
            }

            ast.FunctionDecl f = typedMatches.get(0);

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
                    Param p = f.params.get(i);
                    Expr argExpr = fc.args.get(i);

                    if (p.type instanceof RefTypeNode) {
                        // by-reference: Argument muss lvalue sein
                        Cell argCell = evalLValue(argExpr);
                        define(p.name, new RefBinding(p.type, argCell));
                    } else {
                        // by-value: Kopie
                        Object v = eval(argExpr);
                        define(p.name, new ValueBinding(p.type, new Cell(v)));
                    }
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
            return lookupValue(ve.name);
        }

        if (e instanceof ast.FieldAccessExpr fa) {
            return evalLValue(fa).get();
        }

        if (e instanceof ast.MethodCallExpr mc) {
            // 1) Receiver auswerten
            Object rv = eval(mc.obj);
            if (!(rv instanceof interp.InstanceValue inst)) {
                throw new RuntimeException("Method call on non-object");
            }

            // 2) Klasse holen
            ClassInfo ci = classes.get(inst.dynamicClass);
            if (ci == null) throw new RuntimeException("Unknown class: " + inst.dynamicClass);

            java.util.List<MethodInfo> overloads = ci.methods.get(mc.method);
            if (overloads == null || overloads.isEmpty()) {
                throw new RuntimeException("Undefined method: " + inst.dynamicClass + "." + mc.method);
            }

            // 3) Kandidaten nach Arity filtern
            java.util.List<MethodInfo> candidates = new java.util.ArrayList<>();
            for (MethodInfo cand : overloads) {
                if (cand.params.size() == mc.args.size()) candidates.add(cand);
            }
            if (candidates.isEmpty()) {
                throw new RuntimeException("No matching overload for " + mc.method +
                        " with " + mc.args.size() + " args");
            }

            // 4) Argumenttypen bestimmen (wie bei Funktionen)
            java.util.List<ast.TypeNode> argTypes = new java.util.ArrayList<>();
            for (ast.Expr arg : mc.args) {
                ast.TypeNode t = inferType(arg);
                if (t == null) throw new RuntimeException("Cannot infer type of argument in call to " + mc.method);
                argTypes.add(t);
            }

            // 5) Exakt matchen inkl. & (gleiches Schema wie bei FunctionCall)
            MethodInfo target = null;
            outer:
            for (MethodInfo cand : candidates) {
                for (int i = 0; i < cand.params.size(); i++) {
                    ast.TypeNode paramType = cand.params.get(i).type;
                    ast.TypeNode argType = argTypes.get(i);

                    if (paramType instanceof ast.RefTypeNode rt) {
                        if (!sameType(rt.base, argType)) continue outer;
                        try { evalLValue(mc.args.get(i)); } catch (RuntimeException ex) { continue outer; }
                    } else {
                        if (!sameType(paramType, argType)) continue outer;
                    }
                }
                target = cand;
                break;
            }

            if (target == null) {
                throw new RuntimeException("No matching overload for " + mc.method + " with given argument types");
            }

            // 6) Call ausführen: Receiver setzen + Scope
            interp.InstanceValue prevRecv = currentReceiver;
            currentReceiver = inst;

            scopes.push(new java.util.HashMap<>());
            try {
                // Parameter binden (by-value / by-ref)
                for (int i = 0; i < target.params.size(); i++) {
                    ast.Param p = target.params.get(i);
                    ast.Expr argExpr = mc.args.get(i);

                    if (p.type instanceof ast.RefTypeNode) {
                        Cell argCell = evalLValue(argExpr);
                        define(p.name, new RefBinding(p.type, argCell));
                    } else {
                        Object v = eval(argExpr);
                        define(p.name, new ValueBinding(p.type, new Cell(v)));
                    }
                }

                try {
                    exec(target.body);
                    return null;
                } catch (interp.ReturnValue rv2) {
                    return rv2.value;
                }

            } finally {
                scopes.pop();
                currentReceiver = prevRecv;
            }
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
                    Cell left = evalLValue(be.left);   // VarExpr oder FieldAccessExpr
                    left.set(r);
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

    private boolean sameType(ast.TypeNode a, ast.TypeNode b) {
        if (a == null || b == null) return false;

        // Exakter Klassenvergleich (int != bool != char != string)
        if (a.getClass() != b.getClass()) return false;

        // Klassentypen: Name muss gleich sein
        if (a instanceof ast.ClassTypeNode ca && b instanceof ast.ClassTypeNode cb) {
            return ca.name.equals(cb.name);
        }

        // Referenzen: Basis-Typ muss gleich sein
        if (a instanceof ast.RefTypeNode ra && b instanceof ast.RefTypeNode rb) {
            return sameType(ra.base, rb.base);
        }

        return true;
    }

    private ast.TypeNode typeOfValue(Object v) {
        if (v instanceof Integer) return new ast.IntTypeNode();
        if (v instanceof Boolean) return new ast.BoolTypeNode();
        if (v instanceof Character) return new ast.CharTypeNode();
        if (v instanceof String) return new ast.StringTypeNode();
        return null; // Klassen/Refs später
    }

    private ast.TypeNode inferType(ast.Expr e) {
        // Literale
        if (e instanceof ast.IntLiteral) return new ast.IntTypeNode();
        if (e instanceof ast.BoolLiteral) return new ast.BoolTypeNode();
        if (e instanceof ast.CharLiteral) return new ast.CharTypeNode();
        if (e instanceof ast.StringLiteral) return new ast.StringTypeNode();

        // Variable
        if (e instanceof ast.VarExpr ve) {
            Object v = lookupValue(ve.name);
            ast.TypeNode t = typeOfValue(v);
            if (t == null) {
                throw new RuntimeException("Cannot infer type of variable: " + ve.name);
            }
            return t;
        }

        // Binäre Ausdrücke (vereinfachte Regeln)
        if (e instanceof ast.BinaryExpr be) {
            return switch (be.op) {
                case "+", "-", "*", "/", "%" -> new ast.IntTypeNode();
                case "==", "!=", "<", "<=", ">", ">=", "&&", "||" -> new ast.BoolTypeNode();
                case "=" -> inferType(be.left); // Zuweisung: Typ der linken Seite
                default -> null;
            };
        }

        // Funktionsaufrufe: Rückgabetyp (später sauber, jetzt nicht nötig)
        return null;
    }

    private void registerClass(ast.ClassDecl c) {
        if (classes.containsKey(c.name)) {
            throw new RuntimeException("Class redefined: " + c.name);
        }

        // ClassInfo anlegen
        ClassInfo ci = new ClassInfo(c.name, c.baseName); // falls baseName bei dir anders heißt, anpassen

        // Members einsammeln: bei dir sind das vermutlich VarDeclStmt und FunctionDecl
        for (ast.ASTNode m : c.members) {                 // falls members anders heißt, anpassen
            if (m instanceof ast.VarDeclStmt v) {
                // Feld: Typ + Name
                if (ci.fields.containsKey(v.name)) throw new RuntimeException("Duplicate field: " + v.name);
                ci.fields.put(v.name, v.type);
            } else if (m instanceof ast.FunctionDecl f) {
                // Methode: als MethodInfo speichern (minimal)
                MethodInfo mi = new MethodInfo(
                        f.name,
                        /* returnType */ null,            // wenn ihr ReturnType noch nicht im AST habt
                        f.params,
                        f.body,
                        /* isVirtual */ false,
                        c.name
                );
                ci.methods.computeIfAbsent(f.name, k -> new java.util.ArrayList<>()).add(mi);
            } else {
                throw new RuntimeException("Unknown class member: " + m.getClass().getSimpleName());
            }
        }

        classes.put(c.name, ci);
    }

    private Object defaultValue(ast.TypeNode t) {
        if (t instanceof ast.IntTypeNode) return 0;
        if (t instanceof ast.BoolTypeNode) return false;
        if (t instanceof ast.CharTypeNode) return '\0';
        if (t instanceof ast.StringTypeNode) return "";
        if (t instanceof ast.ClassTypeNode ct) return newInstance(ct.name);
        throw new RuntimeException("No default value for type: " + t.getClass().getSimpleName());
    }

    private interp.InstanceValue newInstance(String className) {
        ClassInfo ci = classes.get(className);
        if (ci == null) throw new RuntimeException("Unknown class: " + className);

        java.util.LinkedHashMap<String, Cell> fieldCells = new java.util.LinkedHashMap<>();
        // Felder (ohne Vererbung erstmal)
        for (var e : ci.fields.entrySet()) {
            fieldCells.put(e.getKey(), new Cell(defaultValue(e.getValue())));
        }
        return new interp.InstanceValue(className, fieldCells);
    }





}
