package agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import net.bytebuddy.asm.Advice;

public class AllMethod {

	
	 public static PrintWriter writer;
	@Advice.OnMethodExit
	  public static void onExit(@Advice.Return Object method) {
		writer.println("] ->  "+method);
		writer.println("\t}");
		writer.print("}");
		writer.flush();
		writer.close();
		System.out.println("[RETURN VALUE] "+ method);

	  }
	 @Advice.OnMethodEnter
	 	public static void getValue(@Advice.Origin String method, @Advice.AllArguments Object[] para) throws FileNotFoundException, UnsupportedEncodingException
	 	{
		 try {
		 File file = new File("jAgentBenchmarks");
		 file.mkdir();
		 String[] updatedMethod = updateMethod(method);
		 String filename= "jAgentBenchmarks\\"+updatedMethod[1]+".spec";
		 if(!initedFile(filename))
		 {
			 System.out.println("[NOT_INITED]");
			 writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));
			 writer.println("//INIT");
			 writer.print(updatedMethod[1]+"(");
			/*This is use for get class of parameters to get
			*an idea what we can do using this advice
			*/	        
			char name ='a';
			for (int i = 0; i < para.length; i++) {
				if(i<para.length-1)
					writer.print(makeparams(""+name++,para[i].getClass().getSimpleName())+",");
				else
					writer.print(makeparams(""+name++,para[i].getClass().getSimpleName()));
			}
			writer.print(")->");
			writer.println("(res:"+updatedMethod[0].replaceAll("java.lang.", "")+"){");
			writer.println("[INSERT CODE HERE]");
			writer.print("}");
			
			writer.flush();
			writer.close();
		 }

		 removeLastBracket(filename);
		 writer = new PrintWriter(new FileOutputStream( new File(filename),  true ));
		 writer.println("\ttest example {");
		 writer.print("\t\t[");
		 char name ='a';
		 for (int i = 0; i < para.length; i++) {
		 	if(i<para.length-1)
		 		writer.print(name++ +"=="+(para[i])+",");
		 	else
		 		writer.print(name++ +"=="+(para[i]));
		 }

		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
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