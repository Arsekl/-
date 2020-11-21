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

public class MethodAnalysis extends Analysis{
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
            res.add(line.trim());
            line=bufferedReader.readLine();
        }
        return res;
    }
    /**
     * 深搜子节点，边界条件为当前子节点被搜索过
     * @param name
     * @param set
     * @param map
     */
    public void dfs(String name, Set<String> set, Map<String,Set<String>> map){
        if(!set.contains(name)) set.add(name);
        else return;
        if(!map.containsKey(name)) return;
        for(String s:map.get(name)){
            dfs(s,set,map);
        }
    }
    public Set<String> getChangeMethod(String targetPath,String changeInfoPath) throws WalaException, CancelException, InvalidClassFileException, IOException {
        Set<String> resMethod = new HashSet<String>();
        Set<String> changeMethod = new HashSet<String>();
        Map<String,Set<String>> methodMap = new HashMap<String, Set<String>>();
        String srcPath = targetPath + "\\classes\\net\\mooctest";
        String testPath = targetPath + "\\test-classes\\net\\mooctest";
        //获取方法Map
        CHACallGraph scg = getGraph(srcPath);
        for (CGNode node:scg){
            if (node.getMethod() instanceof ShrikeBTMethod){
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if (!isMethodValid(method)) continue;;
                String methodName = method.getDeclaringClass().getName().toString() + " " +  method.getSignature();
                for (CallSiteReference c:method.getCallSites()){
                    String callSiteName = c.getDeclaredTarget().getDeclaringClass().getName().toString() + " " +  c.getDeclaredTarget().getSignature();
                    if (callSiteName.contains("<init>")) continue;
                    if (methodMap.containsKey(callSiteName)){
                        methodMap.get(callSiteName).add(methodName);
                    }
                    else{
                        Set<String> temp = new HashSet<String>();
                        temp.add(methodName);
                        methodMap.put(callSiteName,temp);
                    }
                }
            }
        }
        Set<String> changeMethodSet = getFileSet(changeInfoPath);
        for(String s:changeMethodSet) dfs(s,changeMethod,methodMap);
        CHACallGraph cg = getGraph(testPath);
        for(CGNode node:cg){
            if (node.getMethod() instanceof ShrikeBTMethod){
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if (!isMethodValid(method)) continue;
                for (CallSiteReference c:method.getCallSites()){
                    String callSiteName = c.getDeclaredTarget().getDeclaringClass().getName().toString() + " " +  c.getDeclaredTarget().getSignature();
                    if (changeMethod.contains(callSiteName)){
                        resMethod.add(method.getDeclaringClass().getName().toString() + " " +  method.getSignature());
                    }
                }
            }
        }
        return resMethod;
    }

    /**
     * 获取依赖图边关系
     * @param targetPath
     * @param changeInfoPath
     * @return
     * @throws WalaException
     * @throws CancelException
     * @throws InvalidClassFileException
     * @throws IOException
     */
    public Map<String,Set<String>> getMethodMap(String targetPath, String changeInfoPath) throws WalaException, CancelException, InvalidClassFileException, IOException {
        String srcDirPath = targetPath + "\\classes\\net\\mooctest";
        String testDirPath = targetPath + "\\test-classes\\net\\mooctest";
        Map<String,Set<String>> methodMap = new HashMap<String, Set<String>>();
        CHACallGraph cg = getGraph(srcDirPath,testDirPath);
        for(CGNode node: cg) {
            if(node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if(!isMethodValid(method))continue;
                String nodeMethodName = method.getSignature();
                for(CallSiteReference c: method.getCallSites()){
                    String callSiteName = c.getDeclaredTarget().getSignature();
                    if(!callSiteName.contains("mooctest")||callSiteName.contains("<init>"))continue;
                    if(methodMap.containsKey(callSiteName)){
                        methodMap.get(callSiteName).add(nodeMethodName);
                    }else{
                        Set<String> temp = new HashSet<String>();
                        temp.add(nodeMethodName);
                        methodMap.put(callSiteName,temp);
                    }
                }
            }
        }
        return methodMap;
    }
}
