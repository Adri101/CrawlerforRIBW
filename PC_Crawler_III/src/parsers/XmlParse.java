package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.xml.XMLParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

public class XmlParse {

	public void parsearXML(String fichero) throws IOException, SAXException, TikaException {
		// detecting the file type
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(new File(fichero));
		ParseContext pcontext = new ParseContext();

		// Xml parser
		XMLParser xmlparser = new XMLParser();
		xmlparser.parse(inputstream, handler, metadata, pcontext);

		// getting the content of the document
		try (PrintStream out = new PrintStream(new FileOutputStream("temp/temp.txt"))) {
			out.print(handler.toString());
		}
	}
}