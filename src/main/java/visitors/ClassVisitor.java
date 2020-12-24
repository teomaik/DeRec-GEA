package visitors;

import calculator.MetricsCalculator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import containers.ClassMetricsContainer;
import metrics.ClassMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ClassVisitor extends VoidVisitorAdapter<Void> {

    private final String myClassName;
    private final ClassMetricsContainer classMetricsContainer;
    private final ClassMetrics classMetrics;
    private final HashSet<String> efferentCoupledClasses = new HashSet<>();
    private String srcRoot;

    private final HashSet<String> responseSet = new HashSet<>();
    private final ArrayList<String> methodsCalled = new ArrayList<>();

    ArrayList<TreeSet<String>> methodIntersection = new ArrayList<>();

    public ClassVisitor(ClassOrInterfaceDeclaration jc, String srcRoot, ClassMetricsContainer classMap) {
        this.classMetricsContainer = classMap;
        this.myClassName = jc.resolve().getQualifiedName();
        this.classMetrics = this.classMetricsContainer
                .getMetrics(this.myClassName);
        this.srcRoot = srcRoot;
    }

    public ClassVisitor(EnumDeclaration jc, String srcRoot, ClassMetricsContainer classMap) {
        this.classMetricsContainer = classMap;
        this.myClassName = jc.resolve().getQualifiedName();
        this.classMetrics = this.classMetricsContainer
                .getMetrics(this.myClassName);
        this.srcRoot = srcRoot;
    }

    @Override
    public void visit(EnumDeclaration en, Void arg) {
        super.visit(en, arg);

        this.classMetrics.setVisited();
        if (en.isPublic())
            this.classMetrics.setPublic();

        // Get package name
        String packageName = en.resolve().getPackageName();

        MetricsCalculator.getPackageMetricsContainer().addClassToPackage(packageName, this.myClassName, this.classMetrics);
        MetricsCalculator.getPackageMetricsContainer().addPackage(packageName);

        try {
            // Get implemented types & register coupling caused by implemented types
            en.getImplementedTypes().forEach(it -> registerCoupling(it.resolve().getQualifiedName()));
        } catch (Exception ignored) {}

        for (MethodDeclaration method : en.getMethods())
            visitMethod(en, method);

        calculateMetrics(en);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration javaClass, Void arg) {
        super.visit(javaClass, arg);

        this.classMetrics.setVisited();
        if (javaClass.isPublic())
            this.classMetrics.setPublic();
        if (javaClass.isAbstract())
            this.classMetrics.setAbstract();

        // Get package name
        String packageName = javaClass.resolve().getPackageName();

        MetricsCalculator.getPackageMetricsContainer().addClassToPackage(packageName, this.myClassName, this.classMetrics);
        MetricsCalculator.getPackageMetricsContainer().addPackage(packageName);

        String superClassName;
        // Get superclass name
        try {
            superClassName = javaClass.getExtendedTypes().get(0).resolve().getQualifiedName();
        } catch (Exception e) {
            superClassName = "";
        }
        if (!superClassName.isEmpty())
            registerCoupling(superClassName);

        try {
            // Get implemented types & register coupling caused by implemented types
            javaClass.getImplementedTypes().forEach(it -> registerCoupling(it.resolve().getQualifiedName()));
        } catch (Exception ignored) {}

        // Methods accept visitor
        for (MethodDeclaration method : javaClass.getMethods())
            visitMethod(javaClass, method);

        if (!superClassName.isEmpty()) {
            this.classMetricsContainer
                    .getMetrics(superClassName)
                    .incNoc();
        }
        calculateMetrics(javaClass);
    }

    private int calculateDit(String className, ResolvedReferenceType superClass) {
        if (className.equals("java.lang.Object"))
            return 0;

        int dit = this.classMetricsContainer.getMetrics(className).getDit();

        if (dit != -1)
            return dit;

        dit = 1;

        if (superClass != null) {
            if (withinAnalysisBounds(superClass)) {
                ResolvedReferenceType newSuperClass=null;
                try {
                    newSuperClass = superClass.getAllAncestors().get(superClass.getAllAncestors().size() - 1);
                }catch (UnsolvedSymbolException e){
                    dit += calculateDit(superClass.getQualifiedName(), null);
                }
                dit += calculateDit(superClass.getQualifiedName(), newSuperClass);
            }
            List<ResolvedReferenceType> interfaces = getValidInterfaces(superClass);
            for (ResolvedReferenceType anInterface : interfaces) {
                int tmpDit = 1;
                if (withinAnalysisBounds(anInterface)) {
                    try {
                        ResolvedReferenceType ancestor = anInterface.getAllAncestors().get(superClass.getAllAncestors().size() - 1);
                        tmpDit += calculateDit(anInterface.getQualifiedName(), ancestor);
                    } catch (Exception ignored) {}
                }

                if (tmpDit > dit)
                    dit = tmpDit;
            }
        }
        this.classMetricsContainer.getMetrics(className).setDit(dit);
        return dit;
    }

    private int calculateAna(ClassOrInterfaceDeclaration javaClass) {
        ArrayList<ResolvedReferenceType> ancestors = new ArrayList<>();
        HashSet<ResolvedReferenceType> ancestorsSet = new HashSet<>();
        if (withinAnalysisBounds(javaClass.resolve().getQualifiedName())) {
            try {
                ancestors.add(javaClass.resolve().getAncestors()
                        .get(javaClass.resolve().getAncestors().size() - 1));
                ancestors.addAll(getValidInterfaces(javaClass.resolve().getAllAncestors()));
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < ancestors.size(); ++i) {
            ResolvedReferenceType ancestor = ancestors.get(i);
            if (!ancestorsSet.contains(ancestor)) {
                if (withinAnalysisBounds(ancestor)) {
                    ancestorsSet.add(ancestor);
                    try {
                        ancestors.addAll(getValidInterfaces(ancestor));
                    }catch (NullPointerException ignored){}
                    if (withinAnalysisBounds(ancestor.getQualifiedName())) {
                        try {
                            ancestors.add(ancestor.getAllClassesAncestors().get(ancestor.getAllClassesAncestors().size() - 1));
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return ancestorsSet.size();
    }

    private float calculateDam(ClassOrInterfaceDeclaration javaClass) {
        float public_attributes = 0.0f;

        ArrayList<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        float total_attributes = (float) javaClassAttribute.size();

        for (int i = 0; i < total_attributes; ++i) {
            FieldDeclaration a = javaClassAttribute.get(i);
            if ((!a.isProtected()) && (!a.isPrivate())) {
                ++public_attributes;
            }
        }
        return total_attributes == 0 ? -1 : ((total_attributes - public_attributes) / total_attributes);
    }

    private float calculateDam(EnumDeclaration javaClass) {
        float public_attributes = 0.0f;

        ArrayList<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        float total_attributes = (float) javaClassAttribute.size();

        for (int i = 0; i < total_attributes; ++i) {
            FieldDeclaration a = javaClassAttribute.get(i);
            if ((!a.isProtected()) && (!a.isPrivate())) {
                ++public_attributes;
            }
        }
        return total_attributes == 0 ? -1 : ((total_attributes - public_attributes) / total_attributes);
    }

    private int calculateMoa(ClassOrInterfaceDeclaration javaClass) {
        ArrayList<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        HashSet<String> types = new HashSet<>();

        for (FieldDeclaration field : javaClassAttribute) {
            try {
                String typeName = field.resolve().getType().describe();
                if ((withinAnalysisBounds(typeName)))
                    types.add(typeName);
            } catch (Exception ignored){}
        }
        return types.size();
    }

    private int calculateMoa(EnumDeclaration javaClass) {
        ArrayList<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        HashSet<String> types = new HashSet<>();

        for (FieldDeclaration field : javaClassAttribute) {
            try {
                String typeName = field.resolve().getType().describe();
                if ((withinAnalysisBounds(typeName)))
                    types.add(typeName);
            }catch (Exception ignored){}
        }
        return types.size();
    }

    private double calculateWmcCc(ClassOrInterfaceDeclaration javaClass) {
        if (javaClass.isInterface())
            return -1;

        List<MethodDeclaration> methods = javaClass.getMethods();
        float total_ifs = 0.0f;
        float valid_classes = 0.0f;
        for (MethodDeclaration method : methods) {
            int ifs;
            if (!method.isAbstract() && !method.isNative()) {
                ifs = countIfs(method) + countSwitch(method) + 1;
                total_ifs += ifs;
                ++valid_classes;
            }
        }
        if (javaClass.getConstructors().size() == 0)
            ++valid_classes;

        if (valid_classes > 0)
            return (total_ifs / valid_classes);
        else
            return -1;
    }

    private double calculateWmcCc(EnumDeclaration javaClass) {
        List<MethodDeclaration> methods = javaClass.getMethods();
        float total_ifs = 0.0f;
        float valid_classes = 0.0f;
        for (MethodDeclaration method : methods) {
            int ifs;
            if (!method.isAbstract() && !method.isNative()) {
                ifs = countIfs(method) + countSwitch(method) + 1;
                total_ifs += ifs;
                ++valid_classes;
            }
        }
        if (javaClass.getConstructors().size() == 0)
            ++valid_classes;

        if (valid_classes > 0)
            return (total_ifs / valid_classes);
        else
            return -1;
    }

    private int countSwitch(MethodDeclaration method) {
        int count = 0;
        for (SwitchStmt switchStmt : method.findAll(SwitchStmt.class))
            count += switchStmt.getEntries().size();
        return count;
    }

    private int countIfs(MethodDeclaration method) {
        return method.findAll(IfStmt.class).size();
    }

    private int calculateSize1(ClassOrInterfaceDeclaration javaClass) {
        int size = 0;
        for (MethodDeclaration method : javaClass.getMethods()) {
            if (method.isAbstract() || method.isNative())
                ++size;
            else
            if (method.getBegin().isPresent() && method.getEnd().isPresent())
                size += method.getEnd().get().line - method.getBegin().get().line + 1;
        }
        size += javaClass.getFields().size();

        return size;
    }

    private int calculateSize1(EnumDeclaration javaClass) {
        int size = 0;
        for (MethodDeclaration method : javaClass.getMethods()) {
            if (method.isAbstract() || method.isNative())
                ++size;
            else
                if (method.getBegin().isPresent() && method.getEnd().isPresent())
                    size += method.getEnd().get().line - method.getBegin().get().line + 1;
        }
        size += javaClass.getFields().size();

        return size;
    }

    private int calculateSize2(ClassOrInterfaceDeclaration javaClass) {
        return javaClass.getFields().size() + javaClass.getMethods().size();
    }

    private int calculateSize2(EnumDeclaration javaClass) {
        return javaClass.getFields().size() + javaClass.getMethods().size();
    }

    private int calculateDac(ClassOrInterfaceDeclaration javaClass) {
        int dac = 0;
        for (FieldDeclaration field : javaClass.getFields()) {
            if (field.getElementType().isPrimitiveType())
                continue;
            String typeName;
            try {
                typeName = field.getElementType().resolve().describe();
            } catch (Exception e) {
                continue;
            }
            if (withinAnalysisBounds(typeName)) {
                CompilationUnit cu = null;
                try {
                    try {
                        String path = srcRoot + "/" + typeName.replace(".", "/") + ".java";
                        cu = StaticJavaParser.parse(new File(path));
                    }catch (FileNotFoundException ignored1){}
                    try {
                        Optional<ClassOrInterfaceDeclaration> cl = cu.getClassByName(field.getElementType().asString());
                        if (cl.isPresent())
                            continue;
                        ++dac;
                    }catch (NullPointerException ignored){}
                } catch (Exception ignored){}
            }
        }
        return dac;
    }

    private int calculateDac(EnumDeclaration javaClass) {
        int dac = 0;
        for (FieldDeclaration field : javaClass.getFields()) {
            if (field.getElementType().isPrimitiveType()) continue;
            String typeName;
            try {
                typeName = field.getElementType().resolve().describe();
            } catch (Exception e) {
                continue;
            }
            if (withinAnalysisBounds(typeName)) {
                CompilationUnit cu = null;
                try {
                    try {
                        String path = MetricsCalculator.getCurrentProject() + "/" + typeName.replace(".", "/") + ".java";
                        cu = StaticJavaParser.parse(new File(path));
                    }catch (FileNotFoundException ignored1){}
                    try {
                        Optional<ClassOrInterfaceDeclaration> cl = cu.getClassByName(field.getElementType().asString());
                        if (cl.isPresent()) continue;
                        if (cl.get().isAbstract()) ++dac;
                    }catch (NullPointerException ignored){}
                } catch (Exception ignored){}
            }
        }
        return dac;
    }

    private double calculateCamc(ClassOrInterfaceDeclaration javaClass) {
        List<MethodDeclaration> allMethods = javaClass.getMethods();
        int num_of_methods = allMethods.size();
        ArrayList<String> num;
        ArrayList<String> denum = new ArrayList<>();
        double numerator = 0;

        for (MethodDeclaration all_method : allMethods) {
            num = new ArrayList<>();
            List<Type> t = new ArrayList<>();
            for (Parameter p : all_method.getParameters())
                t.add(p.getType());

            for (Type type : t) {
                if (!num.contains(type.asString()))
                    num.add(type.asString());
                if (!denum.contains(type.asString()))
                    denum.add(type.asString());
            }
            numerator += +num.size();

        }
        return (num_of_methods == 0 || denum.isEmpty()) ? -1 : numerator / (num_of_methods * denum.size());
    }

    private double calculateCamc(EnumDeclaration javaClass) {
        List<MethodDeclaration> allMethods = javaClass.getMethods();
        int num_of_methods = allMethods.size();
        ArrayList<String> num;
        ArrayList<String> denum = new ArrayList<>();
        double numerator = 0;

        for (MethodDeclaration all_method : allMethods) {
            num = new ArrayList<>();
            List<Type> t = new ArrayList<>();
            for (Parameter p : all_method.getParameters())
                t.add(p.getType());

            for (Type type : t) {
                if (!num.contains(type.asString()))
                    num.add(type.asString());
                if (!denum.contains(type.asString()))
                    denum.add(type.asString());
            }
            numerator += +num.size();

        }
        return (num_of_methods == 0 || denum.isEmpty()) ? -1 : numerator / (num_of_methods * denum.size());
    }

    private int calculateNop(ClassOrInterfaceDeclaration javaClass) {
        int count = 0;
        if (javaClass.isInterface())
            return javaClass.getMethods().size();
        for (MethodDeclaration method : javaClass.getMethods())
            if (method.isAbstract())
                ++count;
        return count;
    }

    private int calculateNop(EnumDeclaration javaClass) {
        int count = 0;
        for (MethodDeclaration method : javaClass.getMethods())
            if (method.isAbstract())
                ++count;
        return count;
    }

    private float calculateMfa(ClassOrInterfaceDeclaration javaClass) {
        ArrayList<ResolvedReferenceType> ancestors = new ArrayList<>();
        ResolvedReferenceType superClass;
        try {
            superClass = javaClass.getExtendedTypes().get(0).resolve();
        } catch (Exception e) { return 0.0F; }
        if (superClass == null)
            return 0.0F;
        if (withinAnalysisBounds(superClass))
            ancestors.add(superClass);
        try {
            ancestors.addAll(getValidInterfaces(javaClass.resolve().getAllAncestors()));
        }catch (UnsolvedSymbolException ignored) {}

        HashSet<ResolvedReferenceType> ancestorsSet = new HashSet<>();
        ArrayList<MethodDeclaration> javaClassMethods = new ArrayList<>(javaClass.getMethods());
        HashSet<ResolvedMethodDeclaration> ancestorMethods = new HashSet<>();

        for (int i = 0; i < ancestors.size(); i++) {
            ResolvedReferenceType ancestor = ancestors.get(i);
            if (!ancestorsSet.contains(ancestor)) {
                if (withinAnalysisBounds(ancestor)) {
                    ancestorsSet.add(ancestor);
                    ancestors.addAll(getValidInterfaces(ancestor));
                    try {
                        ResolvedReferenceType ancestorSuperClass = ancestor.getAllClassesAncestors().get(ancestor.getAllClassesAncestors().size() - 1);
                        if (withinAnalysisBounds(ancestorSuperClass)) {
                            ancestors.add(ancestorSuperClass);
                        }
                    }catch (UnsolvedSymbolException ignored){}
                    try {
                        ancestorMethods.addAll(ancestor.getAllMethods());
                    }catch (UnsolvedSymbolException ignored){}
                }
            }
        }

        // remove all javaClass methods from ancestors
        for (ResolvedMethodDeclaration ancestorMethod : ancestorMethods)
            for (MethodDeclaration method : javaClassMethods)
                try {
                    if (ancestorMethod.getQualifiedSignature().equals(method.resolve().getQualifiedSignature()))
                        ancestorMethods.remove(ancestorMethod);
                } catch (Exception ignored) {}

        ancestorMethods.removeIf(method -> (method.accessSpecifier().equals(AccessSpecifier.PRIVATE)) || (method.toAst().isPresent() && method.toAst().get().isConstructorDeclaration()));
        javaClassMethods.removeIf(BodyDeclaration::isConstructorDeclaration);
        if (ancestorMethods.size() + javaClassMethods.size() == 0)
            return 0.0F;

        return (float) ancestorMethods.size() / (ancestorMethods.size() + javaClassMethods.size());
    }

    private int calculateLCOM(){
        int lcom = 0;
        for (int i = 0; i < this.methodIntersection.size(); ++i) {
            for (int j = i + 1; j < this.methodIntersection.size(); ++j) {
                TreeSet<?> intersection = (TreeSet<?>) (this.methodIntersection.get(i)).clone();
                if ((!intersection.isEmpty()) || (!this.methodIntersection.isEmpty())) {
                    intersection.retainAll(this.methodIntersection.get(j));
                    if (intersection.size() == 0)
                        ++lcom;
                    else
                        --lcom;
                }
            }
        }
        return this.methodIntersection.size() == 0 ? -1 : Math.max(lcom, 0);
    }

    public void visitMethod(ClassOrInterfaceDeclaration javaClass, MethodDeclaration method) {

        this.methodIntersection.add(new TreeSet<>());
        if (!method.isConstructorDeclaration())
            this.classMetrics.incWmc();

        registerCoupling(method.resolve().getReturnType().describe());
        incRFC(method.resolve().getQualifiedName());

        investigateExceptions(method);
        investigateModifiers(method);
        investigateParameters(method);
        investigateInvocation(method);
        investigateFieldAccess(method, javaClass.getFields());

    }

    public void visitMethod(EnumDeclaration en, MethodDeclaration method) {

        this.methodIntersection.add(new TreeSet<>());
        if (!method.isConstructorDeclaration())
            this.classMetrics.incWmc();

        registerCoupling(method.resolve().getReturnType().describe());
        incRFC(method.resolve().getQualifiedName());

        investigateExceptions(method);
        investigateModifiers(method);
        investigateParameters(method);
        investigateInvocation(method);
        investigateFieldAccess(method, en.getFields());

    }

    private void investigateFieldAccess(MethodDeclaration m, List<FieldDeclaration> classFields){
        try {
            List<NameExpr> nameExpressions = m.findAll(NameExpr.class);
            for (NameExpr expr : nameExpressions)
                for (FieldDeclaration field : classFields)
                    for (VariableDeclarator var : field.getVariables()) {
                        if (var.getNameAsString().equals(expr.getNameAsString()))
                            registerFieldAccess(expr.getNameAsString());
                    }
        } catch (Exception ignored) {}
    }

    private void investigateExceptions(MethodDeclaration m){
        try {
            for (ResolvedType exception : m.resolve().getSpecifiedExceptions())
                registerCoupling(exception.describe());
        } catch (Exception ignored) {}
    }

    private void investigateModifiers(MethodDeclaration m){
        for (Modifier mod : m.getModifiers())
            if (mod.getKeyword().equals(Modifier.Keyword.PUBLIC))
                this.classMetrics.incNpm();
    }

    private void investigateParameters(MethodDeclaration m){
        try {
            for (Parameter p : m.getParameters()) {
                try {
                    registerCoupling(p.getType().resolve().describe());
                } catch (Exception ignored) {}
            }
        }catch (Exception ignored){}
    }

    private void investigateInvocation(MethodDeclaration m){
        for(MethodCallExpr methodCall : m.findAll(MethodCallExpr.class)) {
            try {
                registerMethodInvocation(methodCall.resolve().getPackageName()+"."+methodCall.resolve().getClassName(), methodCall.resolve().getQualifiedSignature());
            }catch (Exception ignored){}
        }
    }

    private void registerCoupling(String className) {
        if ((withinAnalysisBounds(className)) && (!this.myClassName.equals(className))) {
            this.efferentCoupledClasses.add(className);
            this.classMetricsContainer.getMetrics(className).addAfferentCoupling(this.myClassName);
        }
    }

    private void registerFieldAccess(String fieldName) {
        registerCoupling(this.myClassName);
        this.methodIntersection.get(this.methodIntersection.size() - 1).add(fieldName);
    }

    private void registerMethodInvocation(String className, String signature) {
        registerCoupling(className);
        incRFC(signature);
        incMPC(signature);
    }

    private void incMPC(String signature) {
        this.methodsCalled.add(signature);
    }

    private void incRFC(String signature) {
        this.responseSet.add(signature);
    }

    private List<ResolvedReferenceType> getValidInterfaces(ResolvedReferenceType javaClass) {

        List<ResolvedReferenceType> ancestorsIf;

        try {
            ancestorsIf = javaClass.getAllInterfacesAncestors();
        }catch (UnsolvedSymbolException e){ return new ArrayList<>(); }

        ArrayList<ResolvedReferenceType> validInterfaces = new ArrayList<>();
        for (ResolvedReferenceType resolvedReferenceType : ancestorsIf) {
            try {
                if (withinAnalysisBounds(resolvedReferenceType.getQualifiedName()))
                    validInterfaces.add(resolvedReferenceType);
            } catch (Exception ignored) {}
        }
        return validInterfaces;
    }

    private List<ResolvedReferenceType> getValidInterfaces(List<ResolvedReferenceType> ancestors) {

        ArrayList<ResolvedReferenceType> validInterfaces = new ArrayList<>();
        try {
            for (ResolvedReferenceType ancestor : ancestors) {
                for (int i = 0; i < ancestor.getAllInterfacesAncestors().size(); ++i) {
                    try {
                        if (withinAnalysisBounds(ancestor.getAllInterfacesAncestors().get(i).getQualifiedName()))
                            validInterfaces.add(ancestor.getAllInterfacesAncestors().get(i));
                    } catch (Exception ignored) {}
                }
            }
        }catch (Exception e){ return new ArrayList<>(); }
        return validInterfaces;
    }

    private boolean withinAnalysisBounds(String className) {
        return MetricsCalculator.withinAnalysisBounds(className);
    }

    private boolean withinAnalysisBounds(ResolvedReferenceType superClass) {
        if (superClass == null)
            return false;
        return withinAnalysisBounds(superClass.getQualifiedName());
    }

    public void calculateMetrics(ClassOrInterfaceDeclaration javaClass) {
        String superClassName;
        try {
            superClassName = javaClass.getExtendedTypes().get(0).resolve().getQualifiedName();
        }catch (Exception e){
            superClassName = "";
        }

        this.classMetrics.setAna(calculateAna(javaClass));
        this.classMetrics.setDit(calculateDit(javaClass.resolve().getQualifiedName(), !superClassName.isEmpty()
                && javaClass.resolve().getAncestors().size() != 0
                ? javaClass.resolve().getAncestors().get(javaClass.resolve().getAncestors().size() - 1) : null));
        this.classMetrics.setMfa(calculateMfa(javaClass));
        this.classMetrics.setDam(calculateDam(javaClass));
        this.classMetrics.setNop(calculateNop(javaClass));
        this.classMetrics.setMoa(calculateMoa(javaClass));

        this.classMetrics.setDac(calculateDac(javaClass));
        this.classMetrics.setSize2(calculateSize2(javaClass));
        this.classMetrics.setCamc(calculateCamc(javaClass));
        this.classMetrics.setSize1(calculateSize1(javaClass));
        this.classMetrics.setWmcCc(calculateWmcCc(javaClass));

        this.classMetrics.setCbo(this.efferentCoupledClasses.size());
        this.classMetrics.setRfc(this.responseSet.size() + this.classMetrics.getWmc()); //WMC as CIS angor
        this.classMetrics.setMpc(this.methodsCalled.size());    //angor
        this.classMetrics.setLcom(calculateLCOM());
    }

    public void calculateMetrics(EnumDeclaration en){
        this.classMetrics.setAna(0.0f);
        this.classMetrics.setDit(0);
        this.classMetrics.setMfa(0.0f);
        this.classMetrics.setDam(calculateDam(en));
        this.classMetrics.setNop(calculateNop(en));
        this.classMetrics.setMoa(calculateMoa(en));

        this.classMetrics.setDac(calculateDac(en));
        this.classMetrics.setSize2(calculateSize2(en));
        this.classMetrics.setCamc(calculateCamc(en));
        this.classMetrics.setSize1(calculateSize1(en));
        this.classMetrics.setWmcCc(calculateWmcCc(en));
        this.classMetrics.setRfc(this.responseSet.size() + this.classMetrics.getWmc()); //WMC as CIS angor
        this.classMetrics.setMpc(this.methodsCalled.size());    //angor
        this.classMetrics.setCbo(this.efferentCoupledClasses.size());
        this.classMetrics.setLcom(calculateLCOM());
    }
}