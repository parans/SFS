package com.function;

import java.net.MalformedURLException;

import javax.xml.ws.Endpoint;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.client.SimpleFileSystemClient;
import com.client.SimpleFileSystemDefaultClientImpl;
import com.service.SimpleFileSystemServiceImpl;

public class FileOperations {
	
	private static Endpoint endpoint;
	private static SimpleFileSystemClient sfsClient;
	
    @BeforeClass
	public static void initialize() throws MalformedURLException {
    	endpoint = Endpoint.publish("http://localhost:9999/ws/sfs", new SimpleFileSystemServiceImpl());
        //assertTrue(endpoint.isPublished());
        //assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http", endpoint.getBinding().getBindingID());
    	sfsClient = new SimpleFileSystemDefaultClientImpl("http://localhost:9999/ws/sfs");
    }

    @AfterClass
    public static void shutDown() {
    	endpoint.stop();
    }
    
    @Test
    public void testTakeOff() {
    	sfsClient.getHelloWorldAsString("Take off");
    }
}
