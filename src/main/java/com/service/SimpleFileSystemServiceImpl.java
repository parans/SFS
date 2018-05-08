package com.service;

import javax.jws.WebService;
 
//Service Implementation
@WebService(endpointInterface = "com.service.SimpleFileSystemService")
public class SimpleFileSystemServiceImpl implements SimpleFileSystemService {
 
	@Override
	public String getHelloWorldAsString(String name) {
		return "Hello World JAX-WS " + name;
	}
 
}

