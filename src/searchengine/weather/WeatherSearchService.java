package searchengine.weather;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by QingxiaoDong on 5/5/17.
 */
public class WeatherSearchService {
    private static final String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?units=metric&appid=c8e43a26d625ce8e89db25631fa4e3e0&mode=xml&q=";
    private static final String FORECAST_API = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=xml&units=metric&appid=c8e43a26d625ce8e89db25631fa4e3e0&q=";

    public static WeatherInfo searchCurrent(String keywords) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(WEATHER_API + keywords);

            WeatherInfo weatherInfo = new WeatherInfo();

            // city
            Node node = doc.getElementsByTagName("city").item(0);
            if (node == null) return null;
            weatherInfo.city = node.getAttributes().getNamedItem("name").getNodeValue();

            // temperature
            node = doc.getElementsByTagName("temperature").item(0);
            if (node == null) return null;
            weatherInfo.value = node.getAttributes().getNamedItem("value").getNodeValue() + "\u00b0C";
            weatherInfo.min = node.getAttributes().getNamedItem("min").getNodeValue() + "\u00b0C";
            weatherInfo.max = node.getAttributes().getNamedItem("max").getNodeValue() + "\u00b0C";

            // humidity
            node = doc.getElementsByTagName("humidity").item(0);
            if (node == null) return null;
            weatherInfo.humidity = node.getAttributes().getNamedItem("value").getNodeValue() + node.getAttributes().getNamedItem("unit").getNodeValue();

            // wind
            node = doc.getElementsByTagName("speed").item(0);
            if (node == null) return null;
            weatherInfo.wind = node.getAttributes().getNamedItem("name").getNodeValue();

            // weather
            node = doc.getElementsByTagName("weather").item(0);
            if (node == null) return null;
            weatherInfo.weather = node.getAttributes().getNamedItem("value").getNodeValue();
            weatherInfo.icon = "http://openweathermap.org/img/w/" + node.getAttributes().getNamedItem("icon").getNodeValue() + ".png";

            // time
            node = doc.getElementsByTagName("lastupdate").item(0);
            if (node == null) return null;
            weatherInfo.time = node.getAttributes().getNamedItem("value").getNodeValue().replace("T", " ");

            return weatherInfo;
        } catch (Exception e) {
            return null;
        }
    }

    public static WeatherInfo[] searchForcast(String keywords) {
        WeatherInfo[] infos = new WeatherInfo[7];
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(FORECAST_API + keywords);

            WeatherInfo weatherInfo = new WeatherInfo();

            // city
            NodeList list = doc.getElementsByTagName("time");
            for (int i = 0; i < 7; i++) {
                Document daily = list.item(i).getOwnerDocument();

                // temperature
                Node node = daily.getElementsByTagName("temperature").item(0);
                if (node == null) return null;
                weatherInfo.value = node.getAttributes().getNamedItem("day").getNodeValue() + "\u00b0C";
                weatherInfo.min = node.getAttributes().getNamedItem("min").getNodeValue() + "\u00b0C";
                weatherInfo.max = node.getAttributes().getNamedItem("max").getNodeValue() + "\u00b0C";

                // humidity
                node = daily.getElementsByTagName("humidity").item(0);
                if (node == null) return null;
                weatherInfo.humidity = node.getAttributes().getNamedItem("value").getNodeValue() + node.getAttributes().getNamedItem("unit").getNodeValue();

                // wind
                node = daily.getElementsByTagName("windSpeed").item(0);
                if (node == null) return null;
                weatherInfo.wind = node.getAttributes().getNamedItem("name").getNodeValue();

                // weather
                node = daily.getElementsByTagName("symbol").item(0);
                if (node == null) return null;
                weatherInfo.weather = node.getAttributes().getNamedItem("name").getNodeValue();
                weatherInfo.icon = "http://openweathermap.org/img/w/" + node.getAttributes().getNamedItem("var").getNodeValue() + ".png";

                // time
                weatherInfo.time = list.item(i).getAttributes().getNamedItem("day").getNodeValue();

                infos[i] = weatherInfo;
            }


            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
