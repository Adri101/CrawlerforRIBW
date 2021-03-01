
/*
 * PC Crawler - RIBW 19/20
 * Adrian Fernandez Ramos
 * Oscar Mogollon Gutierrez
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import parsers.PdfParse;
import parsers.XmlParse;

public class Crawler {

	private ArrayList<String> ficherosYDirectorios;
	private Map<String, Integer> diccionario;
	private ArrayList<String> thesaurus;
	private final String FICHEROTEMP = "temp/temp.txt";

	public Crawler() {
		ficherosYDirectorios = new ArrayList<String>(); // El tratatamiento será como una cola
		diccionario = new TreeMap<String, Integer>();
		thesaurus = new ArrayList<String>();
	}

	public ArrayList<String> getFicherosYDirectorios() {
		return ficherosYDirectorios;
	}

	public void setFicherosYDirectorios(ArrayList<String> ficherosYDirectorios) {
		this.ficherosYDirectorios = ficherosYDirectorios;
	}

	public Map<String, Integer> getDiccionario() {
		return diccionario;
	}

	public void setDiccionario(Map<String, Integer> diccionario) {
		this.diccionario = diccionario;
	}

	public ArrayList<String> getThesaurus() {
		return thesaurus;
	}

	public void setThesaurus(ArrayList<String> thesaurus) {
		this.thesaurus = thesaurus;
	}

	/**
	 * Carga el contenido del diccionario
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public void cargarDiccionario() throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream("Diccionario.txt"));
		Map<String, Integer> readObject = (Map<String, Integer>) ois.readObject();
		this.setDiccionario(readObject);
	}

	/**
	 * Guarda el diccionario generado al examinar una ruta en un fichero
	 * 
	 * @throws IOException
	 */
	public void guardarDiccionario() throws IOException {
		FileOutputStream fos = new FileOutputStream("Diccionario.txt");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this.getDiccionario());
		oos.close();
	}

	/**
	 * Muestra por pantalla en contenido del diccionario
	 */
	public void mostrarDiccionario() {
		List<String> claves = new ArrayList<String>(this.getDiccionario().keySet());
		Collections.sort(claves);
		Iterator<String> i = claves.iterator();
		while (i.hasNext()) {
			Object k = i.next();
			System.out.println(k + " : " + this.getDiccionario().get(k));
		}
	}

	/**
	 * Procesa el Thesaurus almacenando cada token en una lista de string
	 * 
	 * @throws IOException
	 */
	public void leerThesaurus() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("Thesaurus_es_ES.txt")));
		String linea;

		while ((linea = br.readLine()) != null) {
			if (!linea.startsWith("#")) {
				StringTokenizer st = new StringTokenizer(linea, "()[].,:;{}\"\'\\");
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					int parentesis = s.lastIndexOf(" ");
					if (parentesis != -1) {
						this.getThesaurus().add(s.substring(0, parentesis));
					} else {
						this.getThesaurus().add(s);
					}
				}
			}
		}
		br.close();
	}

	/**
	 * Permite obtener la extension de un fichero
	 * @param fichero
	 * @return extension del fichero
	 */
	private String obtenerExtension(String fichero) {
		String extension = "";
		
		int index = fichero.lastIndexOf('.');
		if(index != -1) {
			extension = fichero.substring(index);
		}
		
		return extension;
	}

	/**
	 * Metodo encargado de comprobar si un fichero tiene extensión textual.
	 * 
	 * @param fichero
	 * @return
	 */
	public boolean esExtensionTextual(File fichero) {
		String[] extensionesTextuales = { ".c", ".cpp", ".py", ".java", ".txt" };
		boolean esTextual = false;

		int i = 0;

		while (!esTextual && i < extensionesTextuales.length) {
			if (extensionesTextuales[i].equals(obtenerExtension(fichero.getName()))) {
				esTextual = true;
			}
			i++;
		}

		return esTextual;
	}

	/**
	 * Calculo de la frecuencia de un termino para un fichero dado
	 * @param fichero Fichero a examinar
	 * @throws IOException
	 */
	private void calcularFrecuencia(String fichero) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fichero));
		String linea;

		while ((linea = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(linea, " ()[].,:;{}\"\'\\");

			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				Object o = this.getDiccionario().get(s);
				if (o == null) {
					if (this.getThesaurus().contains(s.toLowerCase()))
						this.getDiccionario().put(s.toLowerCase(), new Integer(1));
				} else {
					Integer cont = (Integer) o;
					this.getDiccionario().put(s, new Integer(cont.intValue() + 1));
				}
			}
		}
		br.close();
	}

	/**
	 * Procesamiento de la ruta a examinar y calculo de la frecuencia de cada
	 * termino
	 * 
	 * @throws IOException
	 * @throws SAXException 
	 * @throws TikaException 
	 */
	public void analizarRuta() throws IOException, TikaException, SAXException {
		// Mientras queden ficheros o directorios en la cola, esta se procesara
		while (this.getFicherosYDirectorios().size() > 0) {
			File f = new File(this.ficherosYDirectorios.get(0));

			// Si el fichero es directorio, lo eliminamos de la cola y añadimos los
			// ficheros que contiene
			if (f.isDirectory()) {
				String[] listaFicheros = f.list();
				this.getFicherosYDirectorios().remove(0);
				for (int i = 0; i < listaFicheros.length; i++) {
					this.getFicherosYDirectorios().add(f.getAbsolutePath() + "/" + listaFicheros[i]);
					//System.out.println(this.getFicherosYDirectorios().get(i));
				}

			} else {
				// Si es un fichero, se procesa. Una vez procesado, se elimina de la cola
				try {
					if (this.esExtensionTextual(f)) {
						calcularFrecuencia(this.getFicherosYDirectorios().get(0));
					} else {
						
						switch (this.obtenerExtension(this.getFicherosYDirectorios().get(0))) {
						case ".pdf":
							PdfParse pdfParse = new PdfParse();
							pdfParse.parsearPdf(this.getFicherosYDirectorios().get(0));
							calcularFrecuencia(FICHEROTEMP);
							break;
						case ".xml":
							XmlParse xmlParse = new XmlParse();
							xmlParse.parsearXML(this.getFicherosYDirectorios().get(0));
							calcularFrecuencia(FICHEROTEMP);
							break;
						default:
							break;
						}
					}
					this.getFicherosYDirectorios().remove(0);

				} catch (FileNotFoundException fnfe) {
					System.out.println("Fichero desaparecido en combate  ;-)");
				}
			}
		}
	}

	/**
	 * Permite al usuario introducir una cadena a buscar
	 */
	public void busqueda() {
		// Pedimos al usuario que introduzca la palabra a buscar
		Scanner scanner = new Scanner(System.in);
		String terminoABuscar = "";

		do {
			System.out.println("Introduce la cadena a buscar (Presione Enter para salir)");
			terminoABuscar = scanner.nextLine();

			if (!terminoABuscar.isEmpty()) {
				if (this.getDiccionario().containsKey(terminoABuscar)) {
					System.out.println("\t El termino - " + terminoABuscar + " - existe. Su frecuencia: "
							+ this.getDiccionario().get(terminoABuscar).intValue());
				} else {
					System.out.println("\t El termino - " + terminoABuscar + " - no existe.");
				}
			}
		} while (!terminoABuscar.isEmpty());
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, TikaException, SAXException {
		File ficheroSalida = new File("Diccionario.txt");
		Crawler crawler = new Crawler();
		crawler.leerThesaurus();

		// Comprobamos si existe el diccionario
		if (ficheroSalida.isFile()) {
			// Si existe, se carga
			crawler.cargarDiccionario();
		} else {
			// Si no existe, se crea un diccionario a partir de la ruta dada como parametro
			if (args.length < 1) {
				System.out.println("No hay ningun diccionario. Introduczca la ruta que desea examinar"
						+ "con el comando: java Crawler rutaAExaminar");
				return;
			}

			System.out.println("Creando diccionario...");

			String fichEntrada = args[0];
			// El elemento dado por parametro será el primer elemento de la cola
			crawler.getFicherosYDirectorios().add(fichEntrada);
			crawler.analizarRuta();
			crawler.guardarDiccionario();
		}
		crawler.busqueda();
		crawler.mostrarDiccionario();

	}
}
