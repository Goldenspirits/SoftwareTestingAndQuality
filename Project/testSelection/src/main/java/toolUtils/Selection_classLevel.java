package toolUtils;

import com.ibm.wala.ipa.callgraph.CGNode;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;

import toolUtils.GenerateDotFile;

public class Selection_classLevel {
    public static void selection(ArrayList<CGNode[]> cgNodeRelation, String project_name, BufferedReader br) {
        // 1.将图节点关系中的类关系写在dot文件中
        ArrayList<String> classDotLine = new ArrayList<>();
        for (CGNode[] map : cgNodeRelation
        ) {
            classDotLine.add("\"" + map[0].getMethod().getDeclaringClass().getName().toString() + "\" -> \"" + map[1].getMethod().getDeclaringClass().getName().toString() + "\";");
        }

        classDotLine.sort(Comparator.naturalOrder());
        LinkedHashSet<String> set = new LinkedHashSet<String>(classDotLine);
        ArrayList<String> sortedClassDotLine = new ArrayList<String>(set);
        //将关系字符串写到了名字里面
        try {
            //写出dot文件
            String dotFilePath = "Reports/class-" + project_name + ".dot";
            GenerateDotFile.generateDotFile(sortedClassDotLine,dotFilePath,true);

            // 2.根据dot文件通过命令调用graphviz画出pdf
            String command = "dot -T pdf -o class-" + project_name + ".pdf class-" + project_name + ".dot";
            ExecuteCommand.executeCommand(command, new File("Reports"));
            System.out.println("生成了:"+"class-"+project_name+".pdf");

            // 3.根据change_info找到改变的类
            HashSet<String> changedClass = new HashSet<String>();
            String line = "";
            line = br.readLine();
            while (line != null) {
                changedClass.add(line.split(" ")[0]);
                line = br.readLine(); // 一次读入一行数据
            }
            // 4.只要这个节点依赖与改变的类的init方法，那么这个节点就受到影响，要选出来重新测试。
            String selectionClassPath = "./selection-class.txt";
            File file1 = new File(selectionClassPath);
            PrintStream ps1 = new PrintStream(new FileOutputStream(file1));
            HashSet<String> outputs = getAllAffectedNodes( cgNodeRelation, changedClass);
            for (String s : outputs
            ) {
                ps1.println(s);
            }
            System.out.println("All affected tests were found");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private static HashSet<String> getAllAffectedNodes(ArrayList<CGNode[]> cgNodeRelation,HashSet<String> changedClass){
        HashSet<String> outputs = new HashSet<>();
        for (CGNode[] nodes : cgNodeRelation
        ) {
            if (changedClass.contains(nodes[0].getMethod().getDeclaringClass().getName().toString())) {
                if (!nodes[1].getMethod().getSignature().contains("Test")) {
                    changedClass.add(nodes[1].getMethod().getDeclaringClass().getName().toString());
                }
            }
        }
        for (CGNode[] nodes : cgNodeRelation
        ) {
            if (changedClass.contains(nodes[0].getMethod().getDeclaringClass().getName().toString())) {
                if (nodes[1].getMethod().getSignature().contains("Test") && !nodes[1].getMethod().getSignature().contains("<init>")) {
                    outputs.add(nodes[1].getMethod().getDeclaringClass().getName().toString() + " " + nodes[1].getMethod().getSignature());
                }
            }
        }
        return outputs;
    }

}
