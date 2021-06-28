package com.wstutorial.ws;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface HelloWorld {
	@WebMethod
	String sayHelloWorld(String content);
}
