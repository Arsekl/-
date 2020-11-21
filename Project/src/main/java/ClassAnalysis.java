import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class ClassAnalysis extends  Analysis{
    /**
     * 获取changeInfo.txt的信息
     * @param path
     * @return 文件每行的集合
     * @throws IOException
     */
    protected Set<String> getFileSet(String path) throws IOException {
        Set<String> res = new HashSet<String>();
        FileReader changeInfoFile = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(changeInfoFile);
        String line = bufferedReader.readLine();
        while (line!=null){
            if (line.length()==0) continue;
            res.add(line.split(" ")[0].trim());//只需要类
            line=bufferedReader.readLine();
        }
        return res;
    }
    /**
     * 获取受影响的测试类
     * @param targetPath
     * @param changeInfoPath
     * @return 受影响测试类集合
     * @throws IOException
     * @throws InvalidClassFileException
     * @throws CancelException
     * @throws WalaException
     */
    public Set<String> getChangeClass(String targetPath, String changeInfoPath) throws IOException, InvalidClassFileException, CancelException, WalaException {
        String srcPath = targetPath + "\\classes\\net\\mooctest";
        String testPath = targetPath + "\\test-classes\\net\\mooctest";
        Set<String> changeClass = new HashSet<String>();
        Set<String> resClass = new HashSet<String>();
        Set<String> testChangeClass = new HashSet<String>();
        changeClass = getFileSet(changeInfoPath);
        CHACallGraph cg = getGraph(testPath);
        CHACallGraph scg = getGraph(srcPath);
        //挑选测试类
        for (CGNode node: cg){
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if (!isMethodValid(method)) continue;
                for (CallSiteReference c: method.getCallSites()){
                    String className = c.getDeclaredTarget().getDeclaringClass().getName().toString();
                    if (changeClass.contains(className)) {
                        testChangeClass.add(method.getDeclaringClass().getName().toString());
                    }
                }
            }
        }
        //获取受变更类影响的类
        for(CGNode node: scg){
            if(node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if(!isMethodValid(method))continue;
                if(!changeClass.contains(method.getDeclaringClass().getName().toString())){
                    continue;
                }
                for(CallSiteReference c: method.getCallSites()){
                    String className = c.getDeclaredTarget().getDeclaringClass().getName().toString();
                    changeClass.add(className);
                }
            }
        }
        //获取受影响的测试类方法
        for (CGNode node: cg){
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if (!isMethodValid(method)) continue;
                for (CallSiteReference c: method.getCallSites()){
                    String className = c.getDeclaredTarget().getDeclaringClass().getName().toString();
                    if (changeClass.contains(className)&&testChangeClass.contains(method.getDeclaringClass().getName().toString())){
                        resClass.add(method.getDeclaringClass().getName().toString() + " " +  method.getSignature());
                        break;
                    }
                }
            }
        }
        return resClass;
    }

    /**
     * 获取依赖图边关系
     * @param targetPath
     * @param changeInfoPath
     * @return 代表边关系的map
     * @throws CancelException
     * @throws WalaException
     * @throws InvalidClassFileException
     * @throws IOException
     */
    public  Map<String,Set<String>> getClassMap(String targetPath,String changeInfoPath) throws CancelException, WalaException, InvalidClassFileException, IOException {
        String srcPath = targetPath + "\\classes\\net\\mooctest";
        String testPath = targetPath + "\\test-classes\\net\\mooctest";
        Map<String,Set<String>> classMap = new HashMap<String, Set<String>>();
        CHACallGraph cg = getGraph(srcPath,testPath);
        for (CGNode node:cg){
            if(node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if(!isMethodValid(method) ||
                        !method.getSignature().contains("mooctest"))continue;
                String methodClassName = method.getDeclaringClass().getName().toString();
                for(CallSiteReference c: method.getCallSites()){
                    String callSiteName = c.getDeclaredTarget().getDeclaringClass().getName().toString();
                    if(!callSiteName.contains("mooctest")||callSiteName.contains("$"))continue;
                    if(classMap.containsKey(callSiteName)){
                        classMap.get(callSiteName).add(methodClassName);
                    }
                    else{
                        Set<String> temp = new HashSet<String>();
                        temp.add(methodClassName);
                        classMap.put(callSiteName,temp);
                    }
                }
            }
        }
        return classMap;
    }
}
