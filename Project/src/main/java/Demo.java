import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

public class Demo {
    String type;
    String targetPath;
    String changeInfoPath;

    /**
     * 命令解析
     * @param cmd
     */
    private void commandAnalysis(String[] cmd){
        if (cmd[0].equals("-c")) type="class";
        else type="method";
        targetPath=cmd[1];
        changeInfoPath=cmd[2];
    }

    /**
     * 将边关系转换成dot文件
     * @param path
     * @param map
     * @throws IOException
     */
    private void printDot(String path, Map<String, Set<String>> map) throws IOException {
        File file = new File(path);
        Writer writer = new FileWriter(file);
        writer.write("digraph myMethod_class{\n");
        for (String key:map.keySet()){
            for (String value:map.get(key)){
                writer.write("\""+key+"\""+"->"+"\""+value+"\";\n");

            }
        }
        writer.write("}");
        writer.close();
    }

    /**
     * 将变更内容输出为txt文件
     * @param set
     * @param path
     * @throws IOException
     */
    private void printTxt(Set<String> set,String path) throws IOException {
    File file = new File(path);
    Writer writer = new FileWriter(file);
    for (String s : set) {
        writer.write(s + "\n");
    }
    writer.close();
}
    public static void main(String args[]) throws IOException, ClassHierarchyException, InvalidClassFileException, CancelException {
//        String[] tasks = {"0-CMD","1-ALU","2-DataLog","3-BinaryHeap","4-NextDay","5-MoreTriangle"};
//        String path="E:\\0学习资料\\0软件工程\\自动化测试\\大作业\\Homework\\ClassicAutomatedTesting\\";
        Demo demo = new Demo();
//        demo.targetPath=path+tasks[0]+"\\target";
//        demo.changeInfoPath=path+tasks[0]+"\\data\\change_info.txt";
//        demo.type="method";
        demo.commandAnalysis(args);
        try {
            if (demo.type.equals("class")) {
                ClassAnalysis classAnalysis = new ClassAnalysis();
                Set<String> resClass = classAnalysis.getChangeClass(demo.targetPath, demo.changeInfoPath);
                Map<String, Set<String>> classMap = classAnalysis.getClassMap(demo.targetPath, demo.changeInfoPath);
                demo.printDot(".\\class.dot", classMap);
                demo.printTxt(resClass,".\\selection-class.txt");
            }
            else if (demo.type.equals("method")){
                MethodAnalysis methodAnalysis = new MethodAnalysis();
                Set<String> resMethod = methodAnalysis.getChangeMethod(demo.targetPath,demo.changeInfoPath);
                Map<String,Set<String>> methodMap = methodAnalysis.getMethodMap(demo.targetPath,demo.changeInfoPath);
                demo.printDot(".\\method.dot", methodMap);
                demo.printTxt(resMethod,".\\selection-method.txt");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
