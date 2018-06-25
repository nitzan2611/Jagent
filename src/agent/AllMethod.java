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

	
	public static PrintWriter writer;
	public static String filename;
	public static ArrayList<String> arguments;
	@Advice.OnMethodExit
	  public static void onExit(@Advice.Return Object method) {
		try {
		writer.println("] ->  "+"...");
		writer.println("  }");
		writer.print("}");
		writer.flush();
		writer.close();
		
		String[] traces = parseTraces(filename);
		
		String trace = traces[traces.length-1];
		
		removeLastBracket(filename);
		writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));

		
		writer.println("  example {");
		writer.println("    ["+arguments.get(0)+"]");
		
		writer.println(traceSeperate(trace));
		
		writer.println("  }");
		
		writer.print("}");
		writer.flush();
		writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	  }
	 public static String traceSeperate(String string) {
		 try {

		 String res="";
		 String[] splited = string.split(",");
		 System.out.println(Arrays.toString(splited));
		 String ret="";
		 for(int i=0;i<splited.length-1;i++)
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
		 filename= "jAgentBenchmarks\\"+updatedMethod[1]+".spec";
		 if(!initedFile(filename))
		 {
			 writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));
			 writer.println("//INIT");
			 writer.print(updatedMethod[1]+"(");
			/*This is use for get class of parameters to get
			*an idea what we can do using this advice
			*/	        
			char name ='a';
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
		String command = "java -jar trace_gen.jar "+filename;
		Process proc = Runtime.getRuntime().exec(command);
		InputStream in = proc.getInputStream();
		System.out.println(new String(proc.getErrorStream().readAllBytes()));
		String s =new String(in.readAllBytes());
		String[] arr = s.split("\n");
		return arr;
		}
		catch(Exception e)
		{
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