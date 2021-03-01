package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

import org.xml.sax.SAXException;

public class PdfParse {

	public void parsearPdf(String fichero) throws IOException, TikaException, SAXException {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(new File(fichero));
		ParseContext pcontext = new ParseContext();

		// parsing the document using PDF parser
		PDFParser pdfparser = new PDFParser();
		pdfparser.parse(inputstream, handler, metadata, pcontext);

		// getting the content of the document
		try (PrintStream out = new PrintStream(new FileOutputStream("temp/temp.txt"))) {
			out.print(handler.toString());
		}

		// System.out.println("Contents of the PDF :" + handler.toString());

	}
}