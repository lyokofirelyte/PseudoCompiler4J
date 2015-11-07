package com.github.lyokofirelyte.PseudoCompiler4J;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class GenUtils {
	
	// A lot of code taken from http://stackoverflow.com/questions/21544446/how-do-you-dynamically-compile-and-load-external-java-classes

    public static void compileAndRun(String code, String save) {

    	 StringBuilder sb = new StringBuilder();
         sb.append("package csrc;\n");
         sb.append("public class Compile implements com.github.lyokofirelyte.PseudoCompiler4J.GenUtils.DoStuff {\n");
         sb.append("    public void doStuff() {\n");
         sb.append("        main();\n");
         sb.append("    }\n");
         sb.append(code);
         sb.append("}\n");

         File toCompile = new File("csrc/Compile.java");
         
         if (toCompile.exists()){
        	 toCompile.delete();
         }
         
         if (toCompile.getParentFile().exists() || toCompile.getParentFile().mkdirs()) {

             try {
                 Writer writer = null;
                 try {
                     writer = new FileWriter(toCompile);
                     writer.write(sb.toString());
                     writer.flush();
                 } finally {
                     try {
                         writer.close();
                     } catch (Exception e) {}
                 }

                 /** Compilation Requirements *********************************************************************************************/
                 DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                 JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                 StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, Charset.defaultCharset());

                 // This sets up the class path that the compiler will use.
                 // I've added the .jar file that contains the DoStuff interface within in it...
                 List<String> optionList = new ArrayList<String>();
                 optionList.add("-classpath");
                 optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");

                 Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(toCompile));
                 JavaCompiler.CompilationTask task = compiler.getTask(
                     null, 
                     fileManager, 
                     diagnostics, 
                     optionList, 
                     null, 
                     compilationUnit);
                 /********************************************************************************************* Compilation Requirements **/
                 if (task.call()) {
                     /** Load and execute *************************************************************************************************/
                     // Create a new custom class loader, pointing to the directory that contains the compiled
                     // classes, this should point to the top of the package structure!
                     URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
                     // Load the class from the classloader by name....
                     Class<?> loadedClass = classLoader.loadClass("csrc.Compile");
                     // Create a new instance...
                     Object obj = loadedClass.newInstance();
                     // Santity check
                     if (obj instanceof DoStuff) {
                         // Cast to the DoStuff interface
                         DoStuff stuffToDo = (DoStuff)obj;
                         // Run it baby
                         stuffToDo.doStuff();
                     }
                     /************************************************************************************************* Load and execute **/
                 } else {
                     for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                         System.out.format("Error on line %d in %s%n",
                                 diagnostic.getLineNumber(),
                                 diagnostic.getSource().toUri());
                     }
                 }
                 fileManager.close();
             } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exp) {
                 exp.printStackTrace();
             }
         }
     }

     public static interface DoStuff {

         public void doStuff();
     }

}