package agent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
	 public static void premain(String args, Instrumentation instrumentation) {
	
		    instrumentation.addTransformer(new transformer());    

		 	String[] arguments = args.split(",");
		 	filepath=arguments[0];
		 	ignorePath=arguments[1];
		 	System.out.println(args);
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
 // The transform method is called for each non-system class as they are being loaded  
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
         FileOutputStream fos = new FileOutputStream("garb/"+newName);
         fos.write(classfileBuffer);
         fos.close();
         String command = "java -jar fernflower.jar "+"garb/"+newName +" garb";
         Process proc = Runtime.getRuntime().exec(command);
 		 String s = new String(Files.readAllBytes(Paths.get("garb/"+classNameWithoutEnd+".java")));
 		 System.out.println(s);
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
}

;