package toolUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class GenerateDotFile {
    public static void generateDotFile(ArrayList<String> lines, String fileName,boolean isClass) {
        try {
            String parentPath = "Reports/";
            File pfile = new File(parentPath);
            if(!pfile.exists())
            {
                pfile.mkdir();
            }
            File file = new File(fileName);
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            if(isClass){
                ps.println("digraph " + "class {");
            }else {
                ps.println("digraph " + "method {");
            }

            for (String s : lines
            ) {
                ps.println("    " + s);
            }
            ps.println("}");
            System.out.println("生成了:"+fileName);
        }catch (IOException ioe){
            ioe.printStackTrace();
        }

    }
}
