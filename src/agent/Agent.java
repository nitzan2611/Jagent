package agent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


//todo

//get bytecode of method with @checker
//decompile bytecode to jimple
// for each jimple line we need to add add print it and the state after it
// compile jimple to bytecode
//replace original bytecode with the new bytecode

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
	
		    instrumentation.addTransformer(new transformer());    // this creates the code in the spec file
	    	filepath="garb/code.txt";
	    	ignorePath=args;

		    //this allows the methods in AllMethod.java to intercept method calls and method exit
		    //Annoted with @checker
	      /*  new AgentBuilder.Default()
	                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
	                .type((ElementMatchers.any()))
	                .transform((builder, typeDescription, classLoader, module) -> builder
	                        .method((ElementMatchers.isAnnotatedWith(checker.class)).or(ElementMatchers.isAnnotatedWith(checker2.class)))
	                        .intercept(Advice.to(AllMethod.class))
	                        //  .intercept(FixedValue.value("Hello World ByteBuddy!"))

	                ).installOn(instrumentation);
	*/
	         
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
       String classFileName = className.replaceAll("/", "#") + ".class";
       try
       {
    	 //dump class file into xxx.class
         FileOutputStream fos = new FileOutputStream(classFileName);
         fos.write(classfileBuffer);
         fos.close();
         System.out.println("AAAA");
         //convert class to jimple
 		 String command = String.format("java -cp sootclasses-trunk-jar-with-dependencies.jar soot.Main -cp . -pp %s -d garb -f J\r\n",classNameWithoutEnd);
 		 System.out.println(command);
 		 System.out.println(ExecuteCommand(command));
 		 String handledJimple = "garb\\"+classNameWithoutEnd+".jimple";
 		 concatToJimple(handledJimple);//TODO 1
 		 byte[] a = compileJimpleToByteCode(handledJimple);
 		 

         
       }
       catch(Exception e)
       {
    	   e.printStackTrace();
       }
     }
   }
   // We are not modifying the bytecode in anyway, so return it as-is
   return classfileBuffer;
 }


/**
 * MATAN! :)
 * @param handledJimple - relative path to jimple text file that needs to be compiled
 * @return compiled byte code of the jimple file 
 */
private byte[] compileJimpleToByteCode(String handledJimple) {
	// TODO Auto-generated method stub
	return null;
}



private void concatToJimple(String handledJimple) {
	// TODO Auto-generated method stub
	
}



private String ExecuteCommand(String command) throws IOException {
	try {
		Process proc = Runtime.getRuntime().exec(command);
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
		String err ="";
		String line;
		while ((line = in.readLine()) != null) {
			err+=line;
		}
		System.out.println(err);
		in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String out ="";
		while ((line = in.readLine()) != null) {
			out+=line;
		}
		return out;
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return "";
}

}

/*
 String command = "java -jar fernflower.jar "+"garb/"+classFileName +" garb";

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
         System.out.println("Exception while writing: " + classFileName);
       } 
 */
