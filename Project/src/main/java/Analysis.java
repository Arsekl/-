import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;




public class Analysis {

    /**
     * 获得分析域
     * @param path
     * @return AnalysisScope
     * @throws IOException
     * @throws InvalidClassFileException
     */
    protected AnalysisScope getAnalysisScope(String... path) throws IOException, InvalidClassFileException {
        File exFile=new File("src/main/resources/exclusion.txt");
        AnalysisScope scope=AnalysisScopeReader.readJavaScope("scope.txt",exFile,Demo.class.getClassLoader());
        for(String p:path){
            File[] files=new File(p).listFiles();
            for (File file:files){
                if(file.getName().endsWith(".class")){
                    scope.addClassFileToScope(ClassLoaderReference.Application,file);
                }
            }
        }
        return scope;
    }

    /**
     *生成调用图
     * @param path
     * @return 调用图
     * @throws IOException
     * @throws InvalidClassFileException
     * @throws ClassHierarchyException
     * @throws CancelException
     */
    protected CHACallGraph getGraph(String... path) throws IOException, InvalidClassFileException, WalaException, CancelException {
        AnalysisScope scope = getAnalysisScope(path);
        ClassHierarchy ch = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope,ch);
        CHACallGraph cg = new CHACallGraph(ch);
        cg.init(eps);
        return cg;
    }


    /**
     * 判断方法是否合法
     * @param method
     * @return
     */
    protected boolean isMethodValid(ShrikeBTMethod method){
        boolean cFlag=!method.getSignature().contains("initialize")&&!method.getSignature().contains(".<init>()V");
        return "Application".equals(method.getDeclaringClass().getClassLoader().toString())&&cFlag;
    }
}
