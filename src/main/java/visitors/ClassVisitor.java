package visitors;

import calculator.MetricsCalculator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAbstractModifier;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import containers.ClassMetricsContainer;
import metrics.ClassMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ClassVisitor extends VoidVisitorAdapter<Void> {

    private String myClassName;
    private final ClassMetricsContainer classMetricsContainer;
    private ClassMetrics classMetrics;
    private final Set<String> efferentCoupledClasses = new HashSet<>();
    private String srcRoot;

    private final Set<String> responseSet = new HashSet<>();
    private final List<String> methodsCalled = new ArrayList<>();

    private final List<TreeSet<String>> methodIntersection = new ArrayList<>();

    public ClassVisitor(TypeDeclaration<?> jc, String srcRoot, ClassMetricsContainer classMap) {
        this.classMetricsContainer = classMap;
        try {
            this.myClassName = jc.resolve().getQualifiedName();
        }catch (Exception e){
            return;
        }
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

        String packageName = getPackageName(en);

        if (packageName == null) return;

        MetricsCalculator.getPackageMetricsContainer().addClassToPackage(packageName, this.myClassName, this.classMetrics);
        MetricsCalculator.getPackageMetricsContainer().addPackage(packageName);

        try {
            en.getImplementedTypes().forEach(it -> registerCoupling(it.resolve().getQualifiedName()));
        } catch (Exception ignored) {}

        visitAllClassMethods(en);

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

        String packageName = getPackageName(javaClass);

        if (packageName == null) return;

        MetricsCalculator.getPackageMetricsContainer().addClassToPackage(packageName, this.myClassName, this.classMetrics);
        MetricsCalculator.getPackageMetricsContainer().addPackage(packageName);

        String superClassName = getSuperClassName(javaClass);
        if (superClassName != null) registerCoupling(superClassName);

        try {
            javaClass.getImplementedTypes().forEach(it -> registerCoupling(it.resolve().getQualifiedName()));
        } catch (Exception ignored) {}

        visitAllClassMethods(javaClass);

        if (superClassName != null) {
            this.classMetricsContainer
                    .getMetrics(superClassName)
                    .incNoc();
        }
        calculateMetrics(javaClass);
    }

    /**
     * Visit all class methods & register metrics values
     *
     * @param  javaClass  class or enum we are referring to
     */
    private void visitAllClassMethods(TypeDeclaration<?> javaClass){

        for (MethodDeclaration method : javaClass.getMethods())
            visitMethod(javaClass, method);
    }

    /**
     * Get superclass name of class we are referring to
     *
     * @param  javaClass  class or enum we are refering to
     *
     * @return superclass name
     */
    private String getSuperClassName(ClassOrInterfaceDeclaration javaClass){
        try {
            return javaClass.getExtendedTypes().get(0).resolve().getQualifiedName();
        } catch (Exception e) { return null; }
    }

    /**
     * Get package name of enum we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return package name
     */
    private String getPackageName(TypeDeclaration<?> javaClass){
        try{
            return javaClass.resolve().getPackageName();
        }catch (Exception ignored){ return null; }
    }

    /**
     * Calculate DIT metric value for the class we are referring to
     *
     * @param  className the class we are referring to
     * @param  superClass the class we are referring to
     *
     * @return DIT metric value
     */
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

    /**
     * Calculate ANA metric value for the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return ANA metric value
     */
    private int calculateAna(TypeDeclaration<?> javaClass) {
        List<ResolvedReferenceType> ancestors = new ArrayList<>();
        Set<ResolvedReferenceType> ancestorsSet = new HashSet<>();
        try {
            if (withinAnalysisBounds(javaClass.resolve().getQualifiedName())) {
                ancestors.add(javaClass.resolve().getAncestors()
                        .get(javaClass.resolve().getAncestors().size() - 1));
                ancestors.addAll(getValidInterfaces(javaClass.resolve().getAllAncestors()));
            }
        }catch (Exception ignored) {}

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

    /**
     * Calculate DAM metric value for the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return DAM metric value
     */
    private float calculateDam(TypeDeclaration<?> javaClass) {
        float public_attributes = 0.0f;

        List<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        float total_attributes = (float) javaClassAttribute.size();

        for (int i = 0; i < total_attributes; ++i) {
            FieldDeclaration a = javaClassAttribute.get(i);
            if ((!a.isProtected()) && (!a.isPrivate())) {
                ++public_attributes;
            }
        }
        return total_attributes == 0 ? -1 : ((total_attributes - public_attributes) / total_attributes);
    }

    /**
     * Calculate MOA metric value for the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return MOA metric value
     */
    private int calculateMoa(TypeDeclaration<?> javaClass) {
        List<FieldDeclaration> javaClassAttribute = new ArrayList<>(javaClass.getFields());
        Set<String> types = new HashSet<>();

        for (FieldDeclaration field : javaClassAttribute) {
            try {
                String typeName = field.resolve().getType().describe();
                if ((withinAnalysisBounds(typeName)))
                    types.add(typeName);
            } catch (Exception ignored){}
        }
        return types.size();
    }

    /**
     * Calculate CC (Cyclomatic Complexity) metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return CC metric value
     */
    private double calculateWmcCc(TypeDeclaration<?> javaClass) {

        if (javaClass instanceof ClassOrInterfaceDeclaration){
            ClassOrInterfaceDeclaration jc = (ClassOrInterfaceDeclaration) javaClass;
            if (jc.isInterface()) return -1;
        }

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

    /**
     * Count how many switch statements there are within a method
     *
     * @param  method the method we are referring to
     *
     * @return switch count
     */
    private int countSwitch(MethodDeclaration method) {
        final int[] count = {0};
        method.findAll(SwitchStmt.class).forEach(switchStmt -> count[0] += switchStmt.getEntries().size());
        return count[0];
    }

    /**
     * Count how many if statements there are within a method
     *
     * @param  method the method we are referring to
     *
     * @return if count
     */
    private int countIfs(MethodDeclaration method) {
        return method.findAll(IfStmt.class).size();
    }

    /**
     * Calculate Size1 (LOC) metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return Size1 metric value
     */
    private int calculateSize1(TypeDeclaration<?> javaClass) {
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

    /**
     * Calculate Size2 (Fields + Methods size) metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return Size2 metric value
     */
    private int calculateSize2(TypeDeclaration<?> javaClass) {
        return javaClass.getFields().size() + javaClass.getMethods().size();
    }

    /**
     * Calculate DAC metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return DAC metric value
     */
    private int calculateDac(TypeDeclaration<?> javaClass) {
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
                        if (!cl.isPresent())
                            continue;
                        ++dac;
                    }catch (NullPointerException ignored){}
                } catch (Exception ignored){}
            }
        }
        return dac;
    }

    /**
     * Calculate CAMC metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return CAMC metric value
     */
    private double calculateCamc(TypeDeclaration<?> javaClass) {
        List<MethodDeclaration> allMethods = javaClass.getMethods();
        int num_of_methods = allMethods.size();
        List<String> num;
        List<String> denum = new ArrayList<>();
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

    /**
     * Calculate NOP metric value for
     * the class we are referring to
     *
     * @param  javaClass the class or enum we are referring to
     *
     * @return NOP metric value
     */
    private int calculateNop(TypeDeclaration<?> javaClass) {
        return (int) javaClass.getMethods().stream().filter(NodeWithAbstractModifier::isAbstract).count();
    }

    /**
     * Calculate MFA metric value for
     * the class we are referring to
     *
     * @param  javaClass the class we are referring to
     *
     * @return MFA metric value
     */
    private float calculateMfa(ClassOrInterfaceDeclaration javaClass) {
        List<ResolvedReferenceType> ancestors = new ArrayList<>();
        ResolvedReferenceType superClass;

        try {
            superClass = javaClass.getExtendedTypes().get(0).resolve();
        } catch (Exception e) {
            return 0.0F;
        }

        if (superClass == null)
            return 0.0F;

        if (withinAnalysisBounds(superClass))
            ancestors.add(superClass);

        try {
            ancestors.addAll(getValidInterfaces(javaClass.resolve().getAllAncestors()));
        }catch (UnsolvedSymbolException ignored){}

        Set<ResolvedReferenceType> ancestorsSet = new HashSet<>();
        List<MethodDeclaration> javaClassMethods = new ArrayList<>(javaClass.getMethods());
        Set<ResolvedMethodDeclaration> ancestorMethods = new HashSet<>();

        for (int i = 0; i < ancestors.size(); ++i) {
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

        /* remove all javaClass methods from ancestors */
        try {
            ancestorMethods.forEach(ancestorMethod -> javaClassMethods.stream()
                    .filter(method -> ancestorMethod.getQualifiedSignature().equals(method.resolve().getQualifiedSignature()))
                    .forEach(method -> ancestorMethods.remove(ancestorMethod)));
        } catch (Exception ignored) {}

        ancestorMethods.removeIf(method -> (method.accessSpecifier().equals(AccessSpecifier.PRIVATE)) || (method.toAst().isPresent() && method.toAst().get().isConstructorDeclaration()));
        javaClassMethods.removeIf(BodyDeclaration::isConstructorDeclaration);
        if (ancestorMethods.size() + javaClassMethods.size() == 0)
            return 0.0F;

        return (float) ancestorMethods.size() / (ancestorMethods.size() + javaClassMethods.size());
    }

    /**
     * Calculate LCOM metric value for
     * the class we are referring to
     *
     * @return LCOM metric value
     */
    private int calculateLCOM(){
        int lcom = 0;
        for (int i = 0; i < this.methodIntersection.size(); ++i) {
            for (int j = i + 1; j < this.methodIntersection.size(); ++j) {
                AbstractSet<?> intersection = (TreeSet<?>) (this.methodIntersection.get(i)).clone();
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

    /**
     * Visit the method given & register metrics values
     *
     * @param  javaClass the class we are referring to
     * @param  method the method of javaClass we are referring to
     */
    public void visitMethod(TypeDeclaration<?> javaClass, MethodDeclaration method) {

        this.methodIntersection.add(new TreeSet<>());
        if (!method.isConstructorDeclaration())
            this.classMetrics.incWmc();

        try {
            registerCoupling(method.resolve().getReturnType().describe());
        }catch (Exception ignored){}

        incRFC(method.resolve().getQualifiedName());
        investigateExceptions(method);
        investigateModifiers(method);
        investigateParameters(method);
        investigateInvocation(method);
        investigateFieldAccess(method, javaClass.getFields());
    }

    /**
     * Register field access of method given
     *
     * @param  method the method we are referring to
     * @param  classFields the class fields of class we are
     *                     referring to
     */
    private void investigateFieldAccess(MethodDeclaration method, List<FieldDeclaration> classFields){
        try {
            method.findAll(NameExpr.class).forEach(expr -> classFields.forEach(classField -> classField.getVariables()
                    .stream().filter(var -> var.getNameAsString().equals(expr.getNameAsString()))
                    .forEach(var -> registerFieldAccess(expr.getNameAsString()))));
        } catch (Exception ignored) {}
    }

    /**
     * Register exception usage of method given
     *
     * @param  method the method we are referring to
     */
    private void investigateExceptions(MethodDeclaration method){
        try {
            method.resolve().getSpecifiedExceptions().forEach(exception -> registerCoupling(exception.describe()));
        } catch (Exception ignored) {}
    }

    /**
     * Register modifiers usage of method given
     *
     * @param  method the method we are referring to
     */
    private void investigateModifiers(MethodDeclaration method){
        try {
            method.getModifiers().stream().filter(mod -> mod.getKeyword().equals(Modifier.Keyword.PUBLIC)).forEach(mod -> this.classMetrics.incNpm());
        }catch (Exception ignored) {}
    }

    /**
     * Register parameters of method given
     *
     * @param  method the method we are referring to
     */
    private void investigateParameters(MethodDeclaration method){
        try {
            method.getParameters().forEach(p -> registerCoupling(p.getType().resolve().describe()));
        }catch (Exception ignored) {}
    }

    /**
     * Register invocation of method given
     *
     * @param  method the method we are referring to
     */
    private void investigateInvocation(MethodDeclaration method){
        try {
            method.findAll(MethodCallExpr.class)
                    .forEach(methodCall -> registerMethodInvocation(methodCall.resolve().getPackageName()+"."+methodCall.resolve().getClassName(), methodCall.resolve().getQualifiedSignature()));
        }catch (Exception ignored){}
    }

    /**
     * Register coupling of java class given
     *
     * @param  className class name coupled with
     *                   the class we are referring to
     */
    private void registerCoupling(String className) {
        if ((withinAnalysisBounds(className)) && (!this.myClassName.equals(className))) {
            this.efferentCoupledClasses.add(className);
            this.classMetricsContainer.getMetrics(className).addAfferentCoupling(this.myClassName);
        }
    }

    /**
     * Register field access
     *
     * @param  fieldName the field we are referring to
     */
    private void registerFieldAccess(String fieldName) {
        registerCoupling(this.myClassName);
        this.methodIntersection.get(this.methodIntersection.size() - 1).add(fieldName);
    }

    /**
     * Register method invocation of class given
     *
     * @param  className the name of the class we are referring to
     */
    private void registerMethodInvocation(String className, String signature) {
        registerCoupling(className);
        incRFC(signature);
        incMPC(signature);
    }

    /**
     * Increase MPC metric value with class signature given
     *
     * @param  signature the signature of the method we are referring to
     */
    private void incMPC(String signature) {
        this.methodsCalled.add(signature);
    }

    /**
     * Increase RFC metric value with class signature given
     *
     * @param  signature the signature of the method we are referring to
     */
    private void incRFC(String signature) {
        this.responseSet.add(signature);
    }

    /**
     * Get valid interfaces (ancestors) of class given
     *
     * @param  javaClass the class we are referring to
     *
     * @return list of valid interfaces
     */
    private List<ResolvedReferenceType> getValidInterfaces(ResolvedReferenceType javaClass) {

        List<ResolvedReferenceType> ancestorsIf;

        try {
            ancestorsIf = javaClass.getAllInterfacesAncestors();
        }catch (UnsolvedSymbolException e){ return new ArrayList<>(); }

        List<ResolvedReferenceType> validInterfaces = new ArrayList<>();
        for (ResolvedReferenceType resolvedReferenceType : ancestorsIf) {
            try {
                if (withinAnalysisBounds(resolvedReferenceType.getQualifiedName()))
                    validInterfaces.add(resolvedReferenceType);
            } catch (Exception ignored) {}
        }
        return validInterfaces;
    }

    /**
     * Get valid interfaces (ancestors) of ancestors given
     *
     * @param  ancestors list of ancestors
     *
     * @return list of valid interfaces
     */
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

    /**
     * Check if the class with the given class name
     * is within analysis bounds (is user-defined)
     *
     * @param  className java class name given
     *
     * @return true if class is within analysis bounds,
     * false otherwise
     */
    private boolean withinAnalysisBounds(String className) {
        return MetricsCalculator.withinAnalysisBounds(className);
    }

    /**
     * Check if the superclass of a class with the given class name
     * is within analysis bounds (is user-defined)
     *
     * @param  superClass superclass given
     *
     * @return true if class is within analysis bounds,
     * false otherwise
     */
    private boolean withinAnalysisBounds(ResolvedReferenceType superClass) {
        if (superClass == null)
            return false;
        return withinAnalysisBounds(superClass.getQualifiedName());
    }

    /**
     * Calculates all values of metrics the tool supports
     *
     * @param  javaClass the class we are referring to
     */
    public void calculateMetrics(ClassOrInterfaceDeclaration javaClass) {
        String superClassName;
        try {
            superClassName = getSuperClassName(javaClass);
        }catch (Exception e){
            superClassName = null;
        }

        this.classMetrics.setAna(calculateAna(javaClass));
        try {
            this.classMetrics.setDit(calculateDit(javaClass.resolve().getQualifiedName(), superClassName != null
                    && javaClass.resolve().getAncestors().size() != 0
                    ? javaClass.resolve().getAncestors().get(javaClass.resolve().getAncestors().size() - 1) : null));
        }catch (Exception ignored){}
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

    /**
     * Calculates all values of metrics the tool supports
     *
     * @param  en  the enumeration we are referring to
     */
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