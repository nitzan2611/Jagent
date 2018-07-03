package agent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
//to create call
//javac Agent.java & jar cmf manifest.txt ./../../agent.jar Agent.class
public class Agent {
	static String filepath="";
	public static String ignorePath;
	/**
	 * This is the java agent that intercepts a running of another java program
	 * @author Nitzan Farhi
	 * @param args
	 * 	if 2 args are given, split them by ,
		the first argument is a path to jminor code
		the second argument is a path to the ignore file
		else this is just the ignore file and 
	 * @param instrumentation
	 */
	public static void premain(String args, Instrumentation instrumentation) {
	

		    if(args.contains(","))
		    {
		    	String[] arguments = args.split(",");
			 	filepath=arguments[0];
			 	ignorePath=arguments[1];    	
		    }
		    else {
			    instrumentation.addTransformer(new transformer());    // this creates the code in the spec file
		    	filepath="garb/code.txt";
		    	ignorePath=args;
		    }
		    
		    //this allows the methods in AllMethod.java to intercept method calls and method exit
		    //Annoted with @checker
	        new AgentBuilder.Default()
	                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
	                .type((ElementMatchers.any()))
	                .transform((builder, typeDescription, classLoader, module) -> builder
	                        .method((ElementMatchers.isAnnotatedWith(checker.class)).or(ElementMatchers.isAnnotatedWith(checker2.class)))
	                        .intercept(Advice.to(AllMethod.class))
	                        //  .intercept(FixedValue.value("Hello World ByteBuddy!"))

	                ).installOn(instrumentation);
	
	         
	 }
}

class transformer implements ClassFileTransformer
{
 /** The transform method is called for each non-system class as they are being loaded
  *   
  */
 public byte[] transform(ClassLoader loader, String className, 
                         Class<?> classBeingRedefined, ProtectionDomain protectionDomain, 
                         byte[] classfileBuffer) throws IllegalClassFormatException
 {
   if (className != null)
   {
     // Skip all system classes
     if (!className.startsWith("java") && 
         !className.startsWith("sun") && 
         !className.startsWith("javax") && 
         !className.startsWith ("com") && 
         !className.startsWith("jdk") && 
         !className.startsWith("net") && 
         !className.startsWith("org"))
     {
       System.out.println("Dumping: " + className);

       // Replace all separator charactors
       String classNameWithoutEnd = className.replaceAll("/", "#");
       String newName = className.replaceAll("/", "#") + ".class";
       try
       {
    	 new File("garb").mkdir();
    	 //dump class file into garb/xxx.class
         FileOutputStream fos = new FileOutputStream("garb/"+newName);
         fos.write(classfileBuffer);
         fos.close();
         
         //convert xxx.class to xxx.java
         String command = "java -jar fernflower.jar "+"garb/"+newName +" garb";

         Process proc = Runtime.getRuntime().exec(command);
 		 String s = new String(Files.readAllBytes(Paths.get("garb/"+classNameWithoutEnd+".java")));
 		 String afterParse = parseChecker(s); //parse only annotated function from xxx.java
		 afterParse = removeEqual(afterParse);
 		 try (PrintStream out = new PrintStream(new FileOutputStream("garb/code.txt"))) {
		     out.print(afterParse);
		 }
       }
       catch (Exception ex)
       {
         System.out.println("Exception while writing: " + newName);
       } 
     }
   }
   // We are not modifying the bytecode in anyway, so return it as-is
   return classfileBuffer;
 }

private String removeEqual(String afterParse) {
	int index = afterParse.indexOf("-=");
	while (index >= 0) {
	    afterParse = replaceSign(afterParse,index,"-");
	    index = afterParse.indexOf("-=", index + 1);
	}	
	index = afterParse.indexOf("+=");
	while (index >= 0) {
	    afterParse = replaceSign(afterParse,index,"+");
		index = afterParse.indexOf("+=", index + 1);
	}
	return afterParse;		
}
private String replaceSign(String afterParse, int index, String string) {
	
	int enter = afterParse.lastIndexOf("\n", index);
	int exit = afterParse.indexOf("\n", index);
	if(enter>0 && exit>0)
	{
		System.out.println("THIS IS GO "+enter+" "+exit+": "+afterParse.substring(enter, exit));
		String line = afterParse.substring(enter,exit);
		String[] assignment = line.split(string+"=");
		String left  = assignment[0];
		String right = assignment[1];
		String newline = left +"="+left+string+right;
		return afterParse.replace(line,newline.replace("\n","").replace("\t","").replace(" ", ""));
	}
	return afterParse;
}
/**
 * Parses only the annoted method body from the java class
 * @author Nitzan Farhi
 * @param s
 * @return
 */
private String parseChecker(String s) {
	int locA=s.indexOf("@checker");
	int loc=locA;
	while(s.charAt(loc)!='{') loc+=1;
	loc+=1;
	locA=loc;
	/*int parnNum=1;
	while(parnNum!=0)
	{
		if(s.charAt(loc)=='{')
			parnNum+=1;
		else if(s.charAt(loc)=='}')
			parnNum-=1;
		loc+=1;
	}
	int locB=loc-1;
	*/
	
	int locB = s.indexOf("return",locA);
	int enterOfReturn = s.indexOf("\n", locB);
	String ass = s.substring(locB,enterOfReturn).split("return ")[1];
	
	return s.substring(locA,locB)+"\n	res = "+ass+"\n";
}
}

;