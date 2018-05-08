package com.company;

import javax.xml.ws.Endpoint;
import com.service.SimpleFileSystemServiceImpl;

public class Main {
    public static void main (String[] args) throws java.lang.Exception
    {
    	Endpoint.publish("http://localhost:9999/ws/sfs", 
    			new SimpleFileSystemServiceImpl());
    }

}
