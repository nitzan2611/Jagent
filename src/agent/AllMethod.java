package agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.bytebuddy.asm.Advice;

public class AllMethod {

	/*
Manifest-Version: 1.0
Created-By: 1.5.0_18 (Sun Microsystems Inc.)
Premain-Class: agent.Agent

	 */
	
	public static PrintWriter writer;
	public static String filename;
	public static ArrayList<String> arguments;
	/**
	 * Finishes writing the current test example and adds a new example
	 * @author Nitzan Farhi
	 * @param returned value from method
	 */
	@Advice.OnMethodExit
	  public static void onExit(@Advice.Return Object method) {
		try {
		writer.println("] ->  "+"...");//close last example
		writer.println("  }");
		writer.print("}");
		writer.flush();
		writer.close();
		
		String[] traces = parseTraces(filename); //get all traces
		
		String trace = traces[traces.length-1];  //get the last example's trace (of which we just closed)
		
		removeLastBracket(filename); //removes last char ('}') from filename
		writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));

		//writes the trace to the file
		writer.println("  example {");
		writer.println("    ["+arguments.get(0)+"]");		
		writer.println(traceSeperate(trace));
		writer.println("  }");
		
		writer.print("}");
		writer.flush();
		writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	  }
	//add traces rows into final output
	 public static String traceSeperate(String string) {
		 try {

		 String res="";
		 String[] splited = string.split(",");
		 String ret="";
		 for(int i=0;i<splited.length-2;i++)
		 {
			 ret+="    -> "+splited[i].replace(" "," ")+"\n";
		 }
		 return ret;

		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
			 return "";
		 }
	}
	@Advice.OnMethodEnter
	 	public static void getValue(@Advice.Origin String method, @Advice.AllArguments Object[] para) throws FileNotFoundException, UnsupportedEncodingException
	 	{
		 try {
	     arguments = new ArrayList<String>();
		 File file = new File("jAgentBenchmarks");
		 file.mkdir();
		 String[] updatedMethod = updateMethod(method);
		 //we will create a file appropiate for this method
		 filename= "jAgentBenchmarks\\"+updatedMethod[1]+".spec";
		 // if the file wasn't initialized before we initialize it
		 if(!initedFile(filename))
		 {
			 writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));
			 writer.println("//INIT"); //this is how we signal the method was created
			 writer.print(updatedMethod[1]+"("); //here we insert the method name
			 	
			char name ='a';
			//here we put the params
			for (int i = 0; i < para.length; i++) {
				String myparam = makeparams(""+name++,para[i].getClass().getSimpleName());
				if(i<para.length-1)
					writer.print(myparam+",");
				else
					writer.print(myparam);
			}
			writer.print(")->");
			writer.println("(res:"+updatedMethod[0].replaceAll("java.lang.", "")+"){");
			writer.println(readCode(Agent.filepath));
			writer.println("");
			
			writer.flush();
			writer.close();
		 }
		 
		 removeLastBracket(filename);
		 writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));
		 writer.println("  test example {");
		 writer.print("    [");
		 char name ='a';
		 String res_param="";
		 for (int i = 0; i < para.length; i++) {
			String paraname = name++ +"=="+(para[i]);
		 	if(i<para.length-1)
		 	{
		 		res_param+=paraname+"&&";
		 		writer.print(paraname+"&&");
		 	}
		 	else
		 	{
		 		res_param+=paraname;
		 		writer.print(paraname);
		 	}
		 }
		 arguments.add(res_param);
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 
	 	}
	public static String[] parseTraces(String filename) {
		try {
		String command = "java -jar trace_gen.jar "+filename+" "+Agent.ignorePath;
		Process proc = Runtime.getRuntime().exec(command);
		InputStream in = proc.getInputStream();
		//System.out.println(new String(proc.getErrorStream().readAllBytes()));
		//String s =new String(in.readAllBytes());
		//String[] arr = s.split("\n");
		return new String[1];
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String readCode(String filepath) {
		try {
		 byte[] encoded = Files.readAllBytes(Paths.get(filepath));
		 String s =  new String(encoded, StandardCharsets.UTF_8);
		 return s;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "[CODE NOT FOUND]";
		}
	}
	public static String makeparams(String c, String class1) {
		boolean ismut=false;
		HashMap<String,String> map = new HashMap<>();
		map.put("Java.Lang.String", "String");
		map.put("Integer", "int");

		if(class1.contains("String"))
			ismut=false;
		else
			ismut=true;
		String s="";
		s+=(ismut?"mut ":" ");
		s+=(c+":");
		s+=map.get(class1)!=null?map.get(class1):class1;

		return s;
	}
	public static void removeLastBracket(String filename)
	{
		try {
		 byte[] encoded = Files.readAllBytes(Paths.get(filename));
		 String s =  new String(encoded, StandardCharsets.UTF_8);
		 int m = s.lastIndexOf('}');
		 s = s.substring(0, s.length()-1);
		 BufferedWriter writer = new BufferedWriter( new FileWriter( filename));
		 writer.write(s);
		 writer.close();
		}
		catch(Exception e)
		{
			
		}
	}
	public static String[] updateMethod(String method) {
		// TODO Auto-generated method stub
		String[] splitBySpace = method.split(" ");
		String[] nameparts = splitBySpace[3].split("\\(");
		String[] namenames=(nameparts[0]).split("\\.");
		String[] res = new String[]{splitBySpace[2],namenames[2]};
		return res;
	}
	public static Boolean initedFile(String path) 
	{
		try {
			 File mfile = new File(path);
			 mfile.createNewFile();
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  String s =  new String(encoded, StandardCharsets.UTF_8);
			  return s.contains("//INIT");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

}