package org.umple.sample.downloader;

import java.net.URL;
import java.util.List;


public interface DownloadSpecification {

	
	public String url();
	
	public FileType type();
	
	public List<URL> getResources();
	
	
}
