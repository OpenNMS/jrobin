package jrobin.mrtg.server;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Aug 1, 2003
 * Time: 1:01:04 PM
 * To change this template use Options | File Templates.
 */
class Marshaller {

	static Hardware unmarshal(InputStream inputStream) {
		Hardware hardware = new Hardware();
		/*
		throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(inputStream);
		Element root = doc.getDocumentElement();
        NodeList routerNodes = root.getElementsByTagName("router");
		Vector routers = new Vector();
		for(int i = 0; i < routerNodes.getLength(); i++) {
			routers.add(new Router(routerNodes.item(i)));
		} */
		return hardware;
	}
}
