package agent;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
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
	 public static void premain(String args, Instrumentation instrumentation) {
	

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

;