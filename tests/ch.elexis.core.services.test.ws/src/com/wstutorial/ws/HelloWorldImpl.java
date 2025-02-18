package com.wstutorial.ws;

import jakarta.jws.WebService;

@WebService(endpointInterface = "com.wstutorial.ws.HelloWorld")
public class HelloWorldImpl implements HelloWorld {

	@Override
	public String sayHelloWorld(String content) {
		return "Hello " + content + " !";
	}

}