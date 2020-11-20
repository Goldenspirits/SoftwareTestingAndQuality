


import com.ibm.wala.ipa.callgraph.CGNode;

import toolUtils.Selection_classLevel;
import toolUtils.Selection_methodLevel;
//两个自己的工具类
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args)throws Exception {
        String targetProject;
        String changeInfoFile;
        String projectName;
        if(args[0].equals("-c") || args[0].equals("-m") && args.length==3){
            targetProject=args[1];
            projectName=getProgectName(targetProject);
            System.out.println(projectName);
            changeInfoFile = args[2];

            BufferedReader changeInfoFileBR = new BufferedReader(new InputStreamReader(new FileInputStream(new File(changeInfoFile))));
            ArrayList<CGNode[]> cgNodeRelation= RelationshipAnalysis.analyse(targetProject);
            //todo utils里面的三个工具类
            // 5.分别从类级和方法级来处理
            // 具体分别处理-c 和-m
            if (args[0].equals("-c")) {
                //类级测试选择
                System.out.println("Selection_classLevel");
                Selection_classLevel.selection(cgNodeRelation, projectName, changeInfoFileBR);
            } else {
                //方法级测试选择
                System.out.println("Selection_methodLevel");
                Selection_methodLevel.selection(cgNodeRelation, projectName, changeInfoFileBR);
            }

        }else{
            throw new Exception("You have entered the wrong parameter");
        }
    }

    private static String getProgectName(String targetProject)throws Exception {
        String addressSeparator="/|\\\\";
        List<String> filePathList = Arrays.asList(targetProject.split(addressSeparator));
        if (!filePathList.get(filePathList.size() - 1).equals("target")) {
            throw new Exception("Error in traget project path");
        } else {
//            project_name = getProjectName(project_target);

            String fileSeparator = "\\";
            int nums = targetProject.lastIndexOf(fileSeparator);
            if (nums == -1) {
                fileSeparator = "/";
                nums = targetProject.lastIndexOf(fileSeparator);
            }
            File pomFile = new File(targetProject.substring(0, nums) + File.separator + "pom.xml");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(pomFile);
                NodeList nodeList = doc.getElementsByTagName("artifactId");
                return nodeList.item(0).getFirstChild().getNodeValue();
            } catch (Exception e) {
                System.err.println("Failed to read the XML file");
                e.printStackTrace();
            }
        }
        return null;
    }
}
