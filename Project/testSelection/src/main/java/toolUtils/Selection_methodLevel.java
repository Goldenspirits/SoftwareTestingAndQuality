package toolUtils;

import com.ibm.wala.ipa.callgraph.CGNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Selection_methodLevel {
    public static void selection(ArrayList<CGNode[]> cgNodeRelation, String project_name, BufferedReader br) {
        // 1.将图节点关系中的类关系写在dot文件中
        ArrayList<String> methodDotLine = new ArrayList<>();
//        LinkedHashSet<String> methodDotLine = new LinkedHashSet<String>();

        for (
                CGNode[] map : cgNodeRelation
        ) {
            methodDotLine.add("\"" + map[0].getMethod().getSignature() + "\" -> \"" + map[1].getMethod().getSignature() + "\";");
        }
        methodDotLine.sort(Comparator.naturalOrder());
        LinkedHashSet<String> set = new LinkedHashSet<String>(methodDotLine);
        ArrayList<String> sortedMethodDotLine = new ArrayList<String>(set);

        try {
            String dotFilePath = "Reports/method-" + project_name + ".dot";
            GenerateDotFile.generateDotFile(sortedMethodDotLine,dotFilePath,false);

            // 2.使用graphviz画出pdf
            String command = "dot -T pdf -o method-" + project_name + ".pdf method-" + project_name + ".dot";
            ExecuteCommand.executeCommand(command, new File("Reports"));
            System.out.println("生成了:"+"method-"+project_name+".pdf");
            // 3.根据change_info找到改变的方法
            HashSet<String> changedMethod = new HashSet<String>();
            String line = "";
            line = br.readLine();
            while (line != null) {
                changedMethod.add(line.split(" ")[1]);
                line = br.readLine(); // 一次读入一行数据
            }
            // 4.只要这个节点依赖与改变的类的init方法，那么这个节点就受到影响，要选出来重新测试。
            String selectionMethodPath = "./selection-method.txt";
            File file1 = new File(selectionMethodPath);
            PrintStream ps1 = new PrintStream(new FileOutputStream(file1));
            HashSet<String> outputs =getAllAffectedNodes( cgNodeRelation, changedMethod);

            for (String s : outputs
            ) {
                ps1.println(s);
            }
            System.out.println("All affected tests were found");
        } catch (
                IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private static HashSet<String> getAllAffectedNodes(ArrayList<CGNode[]> cgNodeRelation,HashSet<String> changedMethod){
        HashSet<String> outputs = new HashSet<>();
        for (CGNode[] nodes : cgNodeRelation
        ) {
            if (changedMethod.contains(nodes[0].getMethod().getSignature())) {
                if (!nodes[1].getMethod().getSignature().contains("Test")) {
                    changedMethod.add(nodes[1].getMethod().getSignature());
                }
            }
        }
        for (CGNode[] nodes : cgNodeRelation
        ) {
            if (changedMethod.contains(nodes[0].getMethod().getSignature())) {
                if (nodes[1].getMethod().getSignature().contains("Test") && !nodes[1].getMethod().getSignature().contains("<init>")) {
                    outputs.add(nodes[1].getMethod().getDeclaringClass().getName().toString() + " " + nodes[1].getMethod().getSignature());
                }
            }
        }
        return outputs;
    }

}
