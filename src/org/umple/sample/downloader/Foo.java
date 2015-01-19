package org.umple.sample.downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Foo {
	
	public static void main(String[] args) throws IOException {
		Document doc = Jsoup.connect("http://www.emn.fr/z-info/atlanmod/index.php/YUML").get();
		Elements links = doc.select("div#bodyContent p + ul li a");
		
		
		
		File dir = new File("/tmp/output");
		dir.createNewFile();
//		FileUtils.copyURLToFile(source, destination);
//		
		for (Element e : links) {
			URL url = new URL(e.attr("href"));
			System.out.printf("path %s\n\tfile: %s\n", url.getPath(), url.getFile());
		}
		
	}
}
