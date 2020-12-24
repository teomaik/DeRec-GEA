package calculator;

import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import me.tongfei.progressbar.ProgressBar;
import containers.ClassMetricsContainer;
import containers.PackageMetricsContainer;
import metrics.ProjectMetrics;
import containers.ProjectMetricsContainer;
import output.PrintResults;
import visitors.ClassVisitor;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class MetricsCalculator {

    private static final ClassMetricsContainer classMetricsContainer = new ClassMetricsContainer();
    private static final PackageMetricsContainer packageMetricsContainer = new PackageMetricsContainer();
    private static final ProjectMetricsContainer projectMetricsContainer = new ProjectMetricsContainer();
    private static final HashSet<String> classesToAnalyse = new HashSet<>();
    private static String currentProject;

    public static void start(String projectDir) throws IOException {
        currentProject = projectDir;
        ProjectRoot projectRoot = getProjectRoot();
        List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        startProcedure(sourceRoots);
    }

    private static ProjectRoot getProjectRoot(){
        return new SymbolSolverCollectionStrategy()
                .collect(Paths.get(getCurrentProject()));
    }

    private static void startProcedure(List<SourceRoot> sourceRoots) throws IOException {
        try { createSymbolSolver(); } catch (IllegalStateException e){ return; }
        createClassSet(sourceRoots);
        startCalculations(sourceRoots);
    }

    private static void startCalculations(List<SourceRoot> sourceRoots) throws IOException {
        for (SourceRoot sourceRoot : ProgressBar.wrap(sourceRoots, "Calculating Metrics...")) {
            String srcRoot = sourceRoot.getRoot().toString().replace("\\", "/");
            if (srcRoot.contains("/test/"))
                continue;
            for (ParseResult<CompilationUnit> res : sourceRoot.tryToParse())
                if (res.getResult().isPresent())
                    calc(res.getResult().get(), srcRoot);
        }
    }

    private static void createClassSet(List<SourceRoot> sourceRoots) {
        try {
            for (SourceRoot sourceRoot : ProgressBar.wrap(sourceRoots, "Parsing Java Files...")) {
                String srcRoot = sourceRoot.getRoot().toString().replace("\\", "/");
                if (srcRoot.contains("/test/"))
                    continue;
                for (ParseResult<CompilationUnit> res : sourceRoot.tryToParse())
                    if (res.getResult().isPresent())
                        addToClassSet(res.getResult().get());
            }
        } catch (IOException ignored) {}
    }

    private static void calc(CompilationUnit cu, String sourceRoot){
        try {
            if (cu.findFirst(ClassOrInterfaceDeclaration.class).isPresent())
                cu.accept(new ClassVisitor(cu.findFirst(ClassOrInterfaceDeclaration.class).get(), sourceRoot, getClassMetricsContainer()), null);
        }catch (NoSuchElementException ignored){
            try {
                if (cu.findFirst(EnumDeclaration.class).isPresent())
                    cu.accept(new ClassVisitor(cu.findFirst(EnumDeclaration.class).get(), sourceRoot, getClassMetricsContainer()), null);
            }catch (Exception ignored1){}
        }
        calculateAllMetrics(getCurrentProject());
    }

    private static void addToClassSet(CompilationUnit cu) {
        try {
            for (ClassOrInterfaceDeclaration c : cu.findAll(ClassOrInterfaceDeclaration.class))
                classesToAnalyse.add(c.resolve().getQualifiedName());
        } catch (Exception ignored) {}
        try {
            for (EnumDeclaration en : cu.findAll(EnumDeclaration.class)) {
                classesToAnalyse.add(en.resolve().getQualifiedName());
            }
        } catch (Exception ignored) {}
    }

    private static void createSymbolSolver(){
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(currentProject));
        TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();

        combinedSolver.add(reflectionTypeSolver);
        combinedSolver.add(javaParserTypeSolver);

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser
                .getConfiguration()
                .setSymbolResolver(symbolSolver);
    }

    private static void calculateAllMetrics(String project) {
        ProjectMetrics projectMetrics = getProjectMetricsContainer().getMetrics(project);
        projectMetrics.calculateAllMetrics(project);
    }

    public static StringReader printResults() {
        PrintResults handler = new PrintResults();

        HashMap<?, ?> projects = getProjectMetricsContainer().getProjects();
        Set<?> projectSet = projects.entrySet();
        Map.Entry<?, ?> currentProject;
        for (Object o : projectSet) {
            currentProject = (Map.Entry<?, ?>) o;
            handler.handleProject((String) currentProject.getKey(), (ProjectMetrics) currentProject.getValue());
        }
        return handler.getOutput();
    }

    public static ClassMetricsContainer getClassMetricsContainer() { return classMetricsContainer; }

    public static PackageMetricsContainer getPackageMetricsContainer() { return packageMetricsContainer; }

    public static ProjectMetricsContainer getProjectMetricsContainer() { return projectMetricsContainer; }

    public static String getCurrentProject() { return currentProject; }

    public static boolean withinAnalysisBounds(String className) { return classesToAnalyse.contains(className); }
}
