import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class RelationshipAnalysis {
    public static ArrayList<CGNode[]> analyse(String targetProject) throws Exception{
        File targetProjectFile = new File(targetProject); // target文件夹
        ArrayList<String> classFileList = new ArrayList<String>();
        getAllClassFileNames(targetProjectFile, classFileList);
        if (classFileList.size() == 0) {
            throw new Exception("There is no class file");
        }

        // 0.构建分析域（AnalysisScope）对象scope
        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", new File( "exclusion.txt"), Main.class.getClassLoader());
        for (String className : classFileList) {
            scope.addClassFileToScope(ClassLoaderReference.Application, new File(className + ".class"));
        }

        // 1.生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);

        // 2.生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);

        // 3.利用CHA算法构建调用图
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);

        ArrayList<CGNode[]> cgNodeRelation = new ArrayList<CGNode[]>();
        // 4.遍历cg中所有的节点
        for (CGNode node : cg) {
            // 仅仅筛选我们感兴趣的部分信息
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    if (classInnerName.contains("$")) {
                        continue;
                    }
                    Iterator<CGNode> succIter = cg.getSuccNodes(node);
                    getAllSuccNodes(succIter, cgNodeRelation,node);
                    Iterator<CGNode> preIter = cg.getPredNodes(node);
//                        preIter.forEachRemaining();
                    getAllPreNodes(preIter, cgNodeRelation,node);

                }
            }
        }
        return cgNodeRelation;
    }
    public static void getAllSuccNodes(Iterator<CGNode> Iter, ArrayList<CGNode[]> cgNodeRelation,CGNode node){
        while (Iter.hasNext()) {
            CGNode succNode = Iter.next();
            if (succNode.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod succMethod = (ShrikeBTMethod) succNode.getMethod();
                if ("Application".equals(succMethod.getDeclaringClass().getClassLoader().toString())) {
                    String succClassInnerName = succMethod.getDeclaringClass().getName().toString();
                    if (succClassInnerName.contains("$")) {
                        continue;
                    }
                    cgNodeRelation.add(new CGNode[]{succNode, node});
                }
            }
        }
    }
    public static void getAllPreNodes(Iterator<CGNode> Iter, ArrayList<CGNode[]> cgNodeRelation,CGNode node){
        while (Iter.hasNext()) {
            CGNode preNode = Iter.next();
            if (preNode.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod preMethod = (ShrikeBTMethod) preNode.getMethod();
                if ("Application".equals(preMethod.getDeclaringClass().getClassLoader().toString())) {
                    String preClassInnerName = preMethod.getDeclaringClass().getName().toString();
                    if (preClassInnerName.contains("$")) {
                        continue;
                    }
                    cgNodeRelation.add(new CGNode[]{node, preNode});
                }
            }
        }
    }

    public static void getAllClassFileNames(File rootFile, ArrayList<String> classNames) throws IOException {
        File[] allFiles = rootFile.listFiles();
        for (File file : allFiles) {
            if (file.isDirectory()) {
                getAllClassFileNames(file, classNames);
            } else {
                String path = file.getCanonicalPath();
                int i = path.indexOf(".class");
                if (i != -1) {
                    classNames.add(path.substring(0, i));
                }
            }
        }
    }

}
