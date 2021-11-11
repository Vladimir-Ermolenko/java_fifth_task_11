import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        // Докинуть проверок данных на вход + после последнего вопроса предложить выбрать конкретно что изменить
        String boolCheck = "";
        String boolExit = "";
        String date = "";
        String id = "";
        while (!Objects.equals(boolExit, "N") ) {
            while (!Objects.equals(boolCheck, "Y")) {
                Scanner userScan = new Scanner(System.in);
                System.out.println("Enter the date in (DD/MM/YYYY) format: ");
                date = userScan.nextLine();
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Date ch = formatter.parse(date);

                    System.out.println("Enter the currency ID: ");
                    id = userScan.nextLine();
                    System.out.println("You want to get the exchange rate for currency with ID " + id + " on the " +
                            date + "? (Y/N)");
                    boolCheck = userScan.nextLine().toUpperCase(Locale.ROOT);

                } catch (ParseException e) {
                    System.out.println("Please, type the date in the required format");
                    break;
                }
            }

            HttpResponse<String> xmlString = Unirest.get("http://www.cbr.ru/scripts/XML_daily.asp?date_req={date}")
                    .routeParam("date", date).asString();
            java.io.FileWriter fw = new java.io.FileWriter("data.xml");
            fw.write(xmlString.getBody());
            fw.close();

            String FILENAME = "data.xml";
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new File(FILENAME));
                doc.getDocumentElement().normalize();

                NodeList list = doc.getElementsByTagName("Valute");
                for (int temp = 0; temp < list.getLength(); temp++) {
                    Node node = list.item(temp);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String idXML = element.getAttribute("ID");

                        if (Objects.equals(id, idXML)) {
                            String value = element.getElementsByTagName("Value").item(0).getTextContent();
                            System.out.println("The exchange rate for your currency at " + date + " is " + value);
                            Scanner userScan = new Scanner(System.in);
                            System.out.println("\n" + "Would you like to get another exchange rate?");
                            boolExit = userScan.nextLine().toUpperCase(Locale.ROOT);
                            if (Objects.equals(boolExit, "Y")) {
                                break;
                            }
                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                System.out.println("Please, provide us with valid data");
            }
        }
    }
}