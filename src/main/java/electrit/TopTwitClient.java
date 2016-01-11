package electrit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TopTwitClient {

    private static final Logger logger = LoggerFactory.getLogger(TopTwitClient.class);

    private static final String TOPTWIT_URL = "http://toptwit.ru/users/?page={}";

    private final CloseableHttpClient httpClient;
    private final DocumentBuilder documentBuilder;
    private final XPathExpression xPathExpression;

    public TopTwitClient() throws ParserConfigurationException, XPathExpressionException {
        httpClient = HttpClients.createSystem();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        xPathExpression = xpath.compile("//a[@class='rating' and @href]");
    }

    public List<String> getTopUsers(int total) throws Exception {
        List<String> result = new ArrayList<>();
        for (int page = 1; result.size() < total; page++) {
            List<String> pageUsers = getUsersOnPage(page);
            result.addAll(pageUsers);
            Thread.sleep(500);
        }
        return result;
    }

    public List<String> getUsersOnPage(int page) throws Exception {
        String pageUrl = TOPTWIT_URL.replace("{}", Integer.toString(page));
        logger.debug("Getting page={} at {}", page, pageUrl);

        StringBuilder sb = new StringBuilder();
        try (CloseableHttpResponse response = httpClient.execute(new HttpGet(pageUrl))) {
            // unfortunate hack to "fix" malformed xml
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(" async ")) {
                    line = line.replaceAll(" async ", " ");
                }
                sb.append(line).append("\n");
            }
        }

        String html = sb.toString();

        return parse(html);
    }

    private List<String> parse(String html) throws SAXException, IOException, XPathExpressionException {
        Document document = documentBuilder.parse(new InputSource(new StringReader(html)));

        List<String> result = new ArrayList<>();
        NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node hrefAttr = nodes.item(i).getAttributes().getNamedItem("href");
            String userPath = hrefAttr.getNodeValue();
            String screenName = userPath.substring(7);
            logger.debug("userPath={}, screenName={}", userPath, screenName);

            result.add(screenName);
        }

        return result;
    }

}
