package kursai;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Main {

	private static Document getXMLDocument(String query) {
		try {
			String url = "https://old.lb.lt/webservices/fxrates/FxRates.asmx/getFxRatesForCurrency";
			URLConnection connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", "UTF-8");		
			InputStream response = connection.getInputStream();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(response);
			return doc;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public static void main(String[] args) {
		try {
			Scanner scanner = new Scanner(System.in);
			boolean kartoti = true;
			while (kartoti) {
				//Part 1: GET PARAMETERS

				System.out.println("Programa pateiks euro ir Jūsų nurodytų valiutų santykius ir suskaičiuos "
						+ "kursų pokytį Jūsų nurodytu laikotarpiu. \nPirma, įveskite valiutų kodus,"
						+ " atskirdami juos kableliu, pvz.: \"AMD,CHF\".");
				String valiutuKodai = scanner.nextLine().toUpperCase().replaceAll("\\s","");
				String[] valiutuKodaiArray = valiutuKodai.split(",");
				System.out.println("Dabar pasirinkite, kokių datų ar laikotarpio valiutų kursus norite matyti.\n"
						+ "Jei norite pasirinkti atskiras datas, įveskite jas atskirdami kableliu, pvz.: \"2017-05-05,2018-05-05\".\n"
						+ "Jei norite pasirinkti laikotarpį, atskirkite datas dvitaškiu, pvz.: \"2017-05-05:2017-06-06\".\n"
						+ "PASTABA -  Įrašai duomenų bazėje yra nuo 2014 m. rugsėjo 30 d. ");
				String datos = scanner.nextLine().replaceAll("\\s","");
				String[] datosArray = null;
				boolean laikotarpis = false;
				if (datos.contains(":")) {
					datosArray = datos.split(":");
					laikotarpis = true;
				} else if (datos.contains(",") ) {				
					datosArray = datos.split(",");
				} else {
					datosArray = datos.split("$");
				}

				//Part 2: GET DATA

				String tipas = "EU";
				for (String valiuta : valiutuKodaiArray) {
					if (laikotarpis) {
						String query = String.format("tp=%s&ccy=%s&dtFrom=%s&dtTo=%s", tipas, valiuta, datosArray[0], datosArray[1]);
						Document doc = getXMLDocument(query);
						NodeList valiutos = doc.getElementsByTagName("FxRate");
						double nuo = 0.0;
						double iki = 0.0;

						System.out.println("Valiutos kodas: " + valiuta);
						for (int i = 0; i < valiutos.getLength(); i++) {
							Node iNode = valiutos.item(i);
							if (iNode.getNodeType() == Node.ELEMENT_NODE) {
								Element eElement = (Element) iNode;
								System.out.println("Data : "
										+ eElement
										.getElementsByTagName("Dt")
										.item(0)
										.getTextContent());
								String kursas = eElement.getElementsByTagName("Amt").item(1).getTextContent();

								if (i == 0) {
									iki = Double.parseDouble(kursas);
								} else if (i == valiutos.getLength() - 1) {
									nuo = Double.parseDouble(kursas);
								}
								System.out.println("Kursas : " + kursas);
							}
						}
						System.out.println("\n Pokytis: " + (iki - nuo) + "\n");
					} else {
						for (String data : datosArray) {
							String query = String.format("tp=%s&ccy=%s&dtFrom=%s&dtTo=%s", tipas, valiuta, data, data);
							Document doc = getXMLDocument(query);
							System.out.println("Valiutos kodas: " + valiuta);
							NodeList kursai = doc.getElementsByTagName("FxRate");
							if (kursai.getLength() == 0) {
								System.out.println("Nėra informacijos.");
							} else {
								for (int i = 0; i < kursai.getLength(); i++) {
									Node iNode = kursai.item(i);
									if (iNode.getNodeType() == Node.ELEMENT_NODE) {
										Element eElement = (Element) iNode;
										System.out.println("Data : "
												+ eElement
												.getElementsByTagName("Dt")
												.item(0)
												.getTextContent());

										System.out.println("Kursas : " 
												+ eElement
												.getElementsByTagName("Amt")
												.item(1)
												.getTextContent());
									}
								}
							}
							System.out.println("");
						}
					}
				}
				System.out.println("Jeigu norite tęsti, įveskite 1, jei norite baigti - bet kurį kitą simbolį.");
				String k = scanner.next();
				kartoti = (k.equals("1")) ? true : false;
				scanner.nextLine();
			}
			scanner.close();
		} catch (Exception e) {
			System.out.println("Įvyko klaida! Įjunkite programą iš naujo.");
		}
	}
}

