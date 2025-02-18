package com.wstutorial.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public interface HelloWorld {

	@WebMethod
	String sayHelloWorld(String content);
}
