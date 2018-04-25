package com.pocket.network;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.zzy.smarttouch.Common;

import android.util.Log;

public class CostomSaxParser
{
	private SAXParser sp;
	private SaxParserExInterface callback;

	public CostomSaxParser(SaxParserExInterface callback)
	{
		this.callback = callback;
		try
		{
			this.sp = SAXParserFactory.newInstance().newSAXParser();
		} 
		catch (ParserConfigurationException e)
		{
			Common.LogEx("SaxParserEx ParserConfigurationException error:"+e.getMessage());
		} 
		catch (SAXException e)
		{
			Common.LogEx("SaxParserEx SAXException error:"+e.getMessage());
		}
	}
	
	public void Parse(InputStream InputStream)
	{
		MyHandler m = new MyHandler(callback);
		XMLReader xr;

		try
		{
			xr = this.sp.getXMLReader();
			xr.setContentHandler(m);
			xr.parse(new InputSource(InputStream));
		} 
		catch (IOException e)
		{
			Common.LogEx("SaxParserEx IOException error:"+e.getMessage());
		} 
		catch (SAXException e)
		{
			Common.LogEx("SaxParserEx SAXException error:"+e.getMessage());
		}
	}
	
	public static interface SaxParserExInterface
	{
		 public void startElement(String uri, String localName, String qName, Attributes attributes);
		 public void characters(char[] ch, int start, int length);
		 public void endElement(String uri, String localName, String qName);
		 
	}


	private class MyHandler extends DefaultHandler
	{
		private StringBuilder sb = new StringBuilder();
		private SaxParserExInterface callback;
		public MyHandler(SaxParserExInterface callback)
		{
			this.callback = callback;
		}

		public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException
		{
			this.sb.setLength(0);
			callback.startElement(uri, localName, qName, attributes);
		}

		public void characters(char[] ch, int start, int length)throws SAXException
		{
			this.sb.append(ch, start, length);
			//callback.characters(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			callback.endElement(uri, localName,this.sb.toString());
		}
	}

}
