package me.McPlayHD.ping;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateChecker {
	
	private Ping plugin;
	private URL filesFeed;
	
	private String version;
	private String link;
	
	public UpdateChecker(Ping plugin, String url) {
		this.plugin = plugin;
		try {
			this.filesFeed = new URL(url);
		} catch(Exception ex) { 
			ex.printStackTrace();
		}
	}
	
	public boolean updateNeeded() {
		try {
			InputStream input = this.filesFeed.openConnection().getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();
			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			if(this.version.startsWith(".")) {
				this.version = this.version.replaceFirst(".", "");
			}
			this.link = children.item(3).getTextContent();
			double version = Double.parseDouble(this.version);
			if(Double.parseDouble(plugin.getDescription().getVersion()) < version) {
				return true;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getLink() {
		return link;
	}

}
