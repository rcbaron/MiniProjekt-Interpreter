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

    // Session-Root: bleibt für main + REPL offen
    private final Map<String, Binding> sessionRoot = new HashMap<>();

    // Während Funktions-/Methodenaufrufen: Session nicht sichtbar
    private boolean hideSessionForCalls = false;


    public Interpreter() {
        scopes.push(sessionRoot); // Session-Scope ist der unterste Scope
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
            execBlockInCurrentScope(main.body); // KEIN neuer Scope -> Variablen bleiben in Session
            return null;
        } catch (interp.ReturnValue rv) {
            return rv.value;
        }
    }

    // --------- REPL / Program API ---------

    public void loadProgram(ast.Program p) {
        // nutzt exec(Program) -> registriert Klassen/Funktionen, führt top-level statements aus (falls ihr welche habt)
        exec(p);
    }

    /**
     * Führt ein REPL-Snippet aus.
     * Regeln:
     * - Neue Funktionen/Klassen werden registriert (global).
     * - Statements laufen im Session-Scope.
     * - define-before-use (keine Mehrpass-Auflösung im REPL).
     */
    public Object execReplProgram(ast.Program p) {
        Object last = null;

        for (ast.ASTNode n : p.declarations) {
            if (n instanceof ast.FunctionDecl || n instanceof ast.ClassDecl) {
                exec(n); // registrieren
            } else if (n instanceof ast.Statement s) {
                last = exec(s); // ausführen (im Session-Scope)
            } else {
                // falls bei euch Expr o.ä. direkt in declarations landen sollte (eher nicht)
                last = exec(n);
            }
        }
        return last;
    }

    /**
     * Führt main() aus, falls vorhanden. Wenn keine main() existiert -> null.
     * main läuft im Session-Scope und der Scope bleibt offen.
     */
    public Object runMainIfPresent() {
        java.util.List<ast.FunctionDecl> mains = functions.get("main");
        if (mains == null || mains.isEmpty()) return null;
        if (mains.size() != 1) throw new RuntimeException("Ambiguous main()");

        ast.FunctionDecl main = mains.get(0);

        try {
            // WICHTIG: main.body NICHT als BlockStmt ausführen (würde Scope push/pop machen),
            // sondern direkt die Statements im aktuellen Session-Scope ausführen.
            return execBlockInCurrentScope(main.body);
        } catch (interp.ReturnValue rv) {
            return rv.value;
        }
    }


    // --------- Scope helpers (NEU) ---------

    private void define(String name, Binding binding) {
        scopes.peek().put(name, binding);
    }

    private Binding lookupBinding(String name) {
        for (Map<String, Binding> scope : scopes) {
            // Wenn wir in einem Call sind: Session-Scope NICHT durchsuchen
            if (hideSessionForCalls && scope == sessionRoot) break;

            Binding b = scope.get(name);
            if (b != null) return b;
        }

        // Wenn wir gerade in einer Methode sind: unqualifizierte Namen dürfen Felder sein
        if (currentReceiver != null) {
            Cell c = currentReceiver.fieldCells.get(name);
            if (c != null) {
                ClassInfo ci = classes.get(currentReceiver.dynamicClass);
                ast.TypeNode ft = (ci != null) ? ci.fields.get(name) : null;
                if (ft == null) ft = new ast.IntTypeNode(); // Fallback
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

    private boolean isCallThroughRef(ast.Expr recv) {
        if (recv instanceof ast.VarExpr v) {
            return lookupBinding(v.name).type() instanceof ast.RefTypeNode;
        }
        return false;
    }

    private boolean isSubclass(String sub, String base) {
        if (sub.equals(base)) return true;
        ClassInfo ci = classes.get(sub);
        if (ci == null || ci.baseName == null) return false;
        return isSubclass(ci.baseName, base);
    }

    private java.util.LinkedHashMap<String, ast.TypeNode> collectAllFields(String className) {
        ClassInfo ci = classInfo(className);
        var res = new java.util.LinkedHashMap<String, ast.TypeNode>();
        if (ci.baseName != null) res.putAll(collectAllFields(ci.baseName));
        res.putAll(ci.fields);
        return res;
    }

    private interp.InstanceValue sliceTo(String base, interp.InstanceValue inst) {
        var fieldTypes = collectAllFields(base);
        var cells = new java.util.LinkedHashMap<String, Cell>();

        for (var e : fieldTypes.entrySet()) {
            String fname = e.getKey();
            ast.TypeNode ftype = e.getValue();
            Cell src = inst.fieldCells.get(fname);
            Object v = (src != null) ? src.get() : defaultValue(ftype);
            cells.put(fname, new Cell(v));
        }
        return new interp.InstanceValue(base, cells);
    }

    private Object execBlockInCurrentScope(ast.BlockStmt b) {
        Object last = null;
        for (ast.Statement s : b.statements) {
            last = exec(s);
        }
        return last;
    }

    private void callCtor(String className, java.util.List<Object> args, interp.InstanceValue receiver) {
        ClassInfo ci = classInfo(className);

        // passenden ctor suchen: exakt Arity + Typen (minimal)
        CtorInfo target = null;
        outer:
        for (CtorInfo cand : ci.ctors) {
            if (cand.params.size() != args.size()) continue;

            for (int i = 0; i < cand.params.size(); i++) {
                ast.TypeNode pt = cand.params.get(i).type;
                ast.TypeNode at = typeOfValue(args.get(i));
                if (at == null || !sameType(pt, at)) continue outer;
            }
            target = cand;
            break;
        }

        if (target == null) {
            throw new RuntimeException("No matching constructor for " + className + " with " + args.size() + " args");
        }

        // Basisklassen-Default-Konstruktor zuerst (Pflicht)
        if (ci.baseName != null) {
            callCtor(ci.baseName, java.util.List.of(), receiver);
        }

        // ctor ausführen: wie Methoden-Call: currentReceiver setzen, Session ausblenden
        interp.InstanceValue prevRecv = currentReceiver;
        boolean prevHide = hideSessionForCalls;
        currentReceiver = receiver;
        hideSessionForCalls = true;

        scopes.push(new java.util.HashMap<>());
        try {
            // Parameter binden (by value reicht hier)
            for (int i = 0; i < target.params.size(); i++) {
                ast.Param p = target.params.get(i);
                define(p.name, new ValueBinding(p.type, new Cell(args.get(i))));
            }

            exec(target.body);

        } finally {
            scopes.pop();
            hideSessionForCalls = prevHide;
            currentReceiver = prevRecv;
        }
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
            Object value = (v.init != null) ? eval(v.init) : 0;

            if (value instanceof interp.InstanceValue instVal
                    && !(v.type instanceof RefTypeNode)) {
                value = instVal.deepCopy();
            }

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

            // ---------- CTOR CALL: A(args) ----------
            if (!functions.containsKey(fc.name) && classes.containsKey(fc.name)) {
                // new instance (mit Feldern inkl. Basisklassen)
                interp.InstanceValue inst = newInstance(fc.name);

                java.util.List<Object> args = new java.util.ArrayList<>();
                for (ast.Expr a : fc.args) args.add(eval(a));

                callCtor(fc.name, args, inst);
                return inst; // liefert Objektwert (wird bei "A a = A(7);" kopiert)
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

            boolean prevHide = hideSessionForCalls;
            hideSessionForCalls = true;

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
                hideSessionForCalls = prevHide;
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

            ast.TypeNode staticT = inferType(mc.obj);
            if (!(staticT instanceof ast.ClassTypeNode st)) {
                throw new RuntimeException("Receiver has no class type");
            }
            String staticClass = st.name;


            // 2) Overloads in Klassenhierarchie suchen (inkl. Basisklassen)
            java.util.List<MethodInfo> overloads = getMethodOverloadsInHierarchy(staticClass, mc.method);
            if (overloads.isEmpty()) {
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
            // virtual dispatch wie C++: nur wenn statische Methode virtual ist UND Call über Referenz passiert
            if (target.isVirtual && isCallThroughRef(mc.obj)) {
                target = resolveOverride(inst.dynamicClass, target.name, target.params);
            }

            // 6) Call ausführen: Receiver setzen + Scope
            interp.InstanceValue prevRecv = currentReceiver;
            currentReceiver = inst;

            // 👇 REPL-Regel: Session in Calls nicht sichtbar machen
            boolean prevHide = hideSessionForCalls;
            hideSessionForCalls = true;

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
                hideSessionForCalls = prevHide;     // 👈 WICHTIG: zurücksetzen
                currentReceiver = prevRecv;         // Receiver zurücksetzen
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
                    Cell left = evalLValue(be.left);
                    Object right = eval(be.right);

                    // Slicing: Base b; b = d;  (b ist ClassType Base, right ist InstanceValue von Subklasse)
                    if (be.left instanceof ast.VarExpr lv) {
                        ast.TypeNode lt = lookupBinding(lv.name).type();

                        if (lt instanceof ast.ClassTypeNode lct && right instanceof interp.InstanceValue instR) {
                            // RHS darf Subklasse sein -> slice auf LHS-Typ
                            if (!instR.dynamicClass.equals(lct.name) && isSubclass(instR.dynamicClass, lct.name)) {
                                right = sliceTo(lct.name, instR);
                            }
                        }
                    }

                    left.set(right);
                    yield right;
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
            Binding b = lookupBinding(ve.name);
            ast.TypeNode t = b.type();

            // Beim Verwenden einer Referenz zählt der Basistyp
            if (t instanceof ast.RefTypeNode rt) return rt.base;

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
        for (ast.ASTNode m : c.members) {
            if (m instanceof ast.VarDeclStmt v) {
                if (ci.fields.containsKey(v.name))
                    throw new RuntimeException("Duplicate field: " + v.name);
                ci.fields.put(v.name, v.type);

            } else if (m instanceof ast.FunctionDecl f) {
                MethodInfo mi = new MethodInfo(
                        f.name,
                        null,
                        f.params,
                        f.body,
                        f.isVirtual,
                        c.name
                );
                ci.methods
                        .computeIfAbsent(f.name, k -> new java.util.ArrayList<>())
                        .add(mi);

            } else if (m instanceof ast.ConstructorDecl cd) {
                ci.ctors.add(new CtorInfo(cd.className, cd.params, cd.body));

            } else {
                throw new RuntimeException("Unknown class member: "
                        + m.getClass().getSimpleName());
            }
        }

        if (ci.ctors.isEmpty()) {
            ci.ctors.add(
                    new CtorInfo(
                            c.name,
                            java.util.List.of(),
                            new ast.BlockStmt()
                    )
            );
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
        var allFields = collectAllFields(className);
        for (var e : allFields.entrySet()) {
            fieldCells.put(e.getKey(), new Cell(defaultValue(e.getValue())));
        }
        return new interp.InstanceValue(className, fieldCells);
    }

    private java.util.List<MethodInfo> getMethodOverloadsInHierarchy(String className, String methodName) {
        ClassInfo ci = classInfo(className);

        java.util.List<MethodInfo> here = ci.methods.get(methodName);
        if (here != null && !here.isEmpty()) return here;

        if (ci.baseName != null) return getMethodOverloadsInHierarchy(ci.baseName, methodName);
        return java.util.List.of();
    }


    private ClassInfo classInfo(String name) {
        ClassInfo ci = classes.get(name);
        if (ci == null) throw new RuntimeException("Unknown class: " + name);
        return ci;
    }

    private boolean sameParamTypes(java.util.List<ast.Param> a, java.util.List<ast.Param> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!sameType(a.get(i).type, b.get(i).type)) return false;
        }
        return true;
    }

    private MethodInfo resolveOverride(String dynClass, String name, java.util.List<ast.Param> params) {
        ClassInfo ci = classInfo(dynClass);

        java.util.List<MethodInfo> overloads = ci.methods.get(name);
        if (overloads != null) {
            for (MethodInfo m : overloads) {
                if (sameParamTypes(m.params, params)) return m;
            }
        }

        if (ci.baseName != null) return resolveOverride(ci.baseName, name, params);

        throw new RuntimeException("BUG: override resolution failed for " + name);
    }









}
