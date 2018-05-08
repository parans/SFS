package com.client;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import com.service.SimpleFileSystemService;

public class SimpleFileSystemDefaultClientImpl implements SimpleFileSystemClient {

	private SimpleFileSystemService sfs;
	
	public SimpleFileSystemDefaultClientImpl(final String url) throws MalformedURLException {
		URL uri = new URL(url);
		QName qname = new QName(uri.toString(), "SimpleFileSystemServiceImpl");
		Service service = Service.create(uri, qname);
		sfs = service.getPort(SimpleFileSystemService.class);
	}
	
	@Override
	public String getHelloWorldAsString(final String name) {
		return sfs.getHelloWorldAsString(name);
	}
}
