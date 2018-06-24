package agent;

import net.bytebuddy.asm.Advice;

public class AllMethod {

	 @Advice.OnMethodExit
	    public static void getParametrs(@Advice.Origin String method, @Advice.AllArguments Object[] para) throws Exception {

	        System.out.println(method+":");
	        /*This is use for get class of parameters to get
	        *an idea what we can do using this advice
	        */	        
	        for (int i = 0; i < para.length; i++) {
	            System.out.println("	"+para[i].getClass() +" : " + para[i]);
	        }
	        /*
	        This is a way we can access to each parameters separately using agent advice
	        For this we want an idea about parameters class , so from above we can get class of parameters and we can use
	         them as follows or as we need.
	         */
	        
	    }
}