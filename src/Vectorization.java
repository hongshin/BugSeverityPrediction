package BugSeverityPrediction ;

import java.util.* ;
import java.io.* ;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.commons.configuration.* ;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Vectorization
{
	public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;

	public static HashSet<Integer> loadTargetReportIDs(String fname, String keyword)
	{
		HashSet<Integer> ids = new HashSet<Integer>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder() ;
			Document doc = dBuilder.parse(new File(fname)) ;
			doc.getDocumentElement().normalize() ;

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i) ;
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				NodeList updates = report.getElementsByTagName("update") ;
				Node update = updates.item(updates.getLength() - 1) ;

				NodeList attr = ((Element) update).getElementsByTagName("what") ;

				if (attr.item(0).getTextContent().indexOf("Layout") != -1) 
					ids.add(reportID) ;
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1) ;
		}
		return ids ;
	}

	public static HashMap<Integer, Boolean> loadSeverityLabel(String fname, HashSet<Integer> reportIDs)
	{
		HashMap<Integer, Boolean> labels = new HashMap<Integer, Boolean>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File(fname));
			doc.getDocumentElement().normalize();

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i);
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				if (reportIDs.contains(new Integer(reportID)) == false)
					continue ;

				NodeList updates = report.getElementsByTagName("update") ;
				Element lastUpdate = (Element) updates.item(updates.getLength() - 1) ;

				String severity = lastUpdate.getElementsByTagName("what").item(0).getTextContent() ;
					
				switch (severity) {
					case "critical":
					case "blocker":
					case "major:":
						labels.put(reportID, true) ;
						break ;

					default:
						labels.put(reportID, false) ;
						break ;
				}
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return labels ;
	}

	public static HashMap<Integer, String> loadDescription(String fname, HashSet<Integer> reportIDs)
	{
		HashMap<Integer, String> descriptions = new HashMap<Integer, String>() ;

		try {
			DocumentBuilder dBuilder = factory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File(fname));
			doc.getDocumentElement().normalize();

			NodeList reports = doc.getElementsByTagName("report");

			for (int i = 0; i < reports.getLength(); i++) {
				Element report = (Element) reports.item(i);
				int reportID = Integer.parseInt(report.getAttribute("id")) ;

				if (reportIDs.contains(new Integer(reportID)) == false)
					continue ;

				NodeList updates = report.getElementsByTagName("update") ;
				Element lastUpdate = (Element) updates.item(updates.getLength() - 1) ;
				String description = lastUpdate.getElementsByTagName("what").item(0).getTextContent() ;
				
				descriptions.put(reportID, description) ;
			}
			dBuilder.reset() ;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return descriptions ;
	}

	public static TreeMap<String, Integer> buildDictionary(HashMap<Integer, String> descriptions, int threshold) {
		TreeMap<String, Integer> dictionary = new TreeMap<String, Integer>() ;
		TreeMap<String, Integer> frequency = new TreeMap<String, Integer>() ;

		int nWords = 0 ;

		// TO-DO: implement here

		return dictionary ;
	}

	public static double [] getVector(TreeMap<String, Integer> dictionary, String description) {
		description = description.toLowerCase() ;

		double [] v = new double[dictionary.keySet().size()] ;

		// TO-DO: implement here

		return v ;
	}

	public static PropertiesConfiguration loadConfig(String fname) 
	{
		PropertiesConfiguration config = null ;
		try {
			config = new PropertiesConfiguration(fname) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
		return config ;
	}


	public static void main(String[] args)
	{
		HashSet<Integer> 			reportIDs ;
		HashMap<Integer, Boolean>	labels ;
		HashMap<Integer, String> 	descriptions ;
		TreeMap<String, Integer> 	dictionary ;

		PropertiesConfiguration config = loadConfig("config.properties") ;

		reportIDs = loadTargetReportIDs(config.getString("data.dir") + "/component.xml", config.getString("data.module")) ;
		labels = loadSeverityLabel(config.getString("data.dir") + "/severity.xml", reportIDs) ;
		descriptions = loadDescription(config.getString("data.dir") + "/short_desc.xml", reportIDs) ;

		dictionary = buildDictionary(descriptions, config.getInt("dictionary.minEvidences")) ;

		// Print out arff file
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(config.getString("arff.filename"))) ;

			out.println("@relation bugreport") ;
			for (int i = 0 ; i < dictionary.keySet().size() ; i++)
				out.println("@attribute c" + i +" numeric") ;
			out.println("@attribute l {nonsevere, severe}") ;
		
			out.println("@data") ;
			for (Iterator<Integer> i = reportIDs.iterator() ; i.hasNext() ; ) {
				Integer reportID = i.next().intValue() ;

				double [] v = getVector(dictionary, descriptions.get(reportID)) ;
				for (int j = 0 ; j < v.length ; j++)
					out.print(v[j] + ",") ;

				out.println(labels.get(reportID) ? "severe" : "nonsevere") ;
			}
			out.close() ;
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}
}
