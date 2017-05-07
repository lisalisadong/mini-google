package searchengine;

import edu.upenn.cis455.webserver.HttpEnum;
import searchengine.amazon.AmazonItem;
import searchengine.amazon.ItemSearchService;
import searchengine.dictionary.DictionaryService;
import searchengine.weather.WeatherInfo;
import searchengine.weather.WeatherSearchService;
import utils.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by QingxiaoDong on 4/6/17.
 */
public class SearchEngineServlet extends HttpServlet {
    Logger logger = new Logger(getClass().getSimpleName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.warn("Received get request");
        String servletPath = request.getServletPath();
        logger.warn(servletPath);
        PrintWriter writer = response.getWriter();
        switch (servletPath) {
        case "/":
            String contents = new String(Files.readAllBytes(Paths.get("resources/sites/main.html")));
            writer.println(contents);
            break;
        case "/search":
            handleSearch(request, response);
            break;
        case "/scripts":
            try {
                contents = new String(Files.readAllBytes(Paths.get("resources/scripts" + request.getPathInfo())));
                writer.println(contents);
            } catch (Exception e) {
                response.sendError(404);
            }
            break;
        case "/images":
            try {
                response.setContentType(HttpEnum.CONTENT_IMAGE_PNG.text());
                contents = new String(Files.readAllBytes(Paths.get("resources/images" + request.getPathInfo())));
                writer.println(contents);
            } catch (Exception e) {
                response.sendError(404);
            }
            break;
        default:
            break;
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getParameter("query") == null) {
            response.sendError(404, "Page Not Found");
        } else {
            long time = System.nanoTime();
            String query = request.getParameter("query");
            String page = request.getParameter("page");
            int p = 0;
            if (page != null) {
                p = Integer.parseInt(page) - 1;
            }
            query = query.trim().toLowerCase();
            logger.warn("The query is {}", query);
            int numResults = SearchEngineService.preSearch(query);
            ResultEntry[] entries = SearchEngineService.search(query, p * 10, 10);
            double seconds = 1.0 * (System.nanoTime() - time) / 1000000000;
            String contents = new String(Files.readAllBytes(Paths.get("resources/sites/result.html")));

            // num results in num seconds
            contents = contents.replace("${num-results}", NumberFormat.getNumberInstance(Locale.US).format(numResults));
            contents = contents.replace("${num-seconds}", String.format("%.6f", seconds));

            // correction
            String correction;
            if ((correction = DictionaryService.correct(query)) != null) {
                contents = contents.replace("${correction}",
                        "<p style=\"font-weight:bold;color:grey;font-family:Cochin;font-size:20px\">" +
                        "Did you mean " + "<a href=\"/search?query=" + correction + "\">" + correction + "</a>?</p>");
            } else {
                contents = contents.replace("${correction}", "");
            }
            DictionaryService.put(query);

            // results
            String results = "";
            if (entries != null) {
                for (ResultEntry entry : entries) {
                    results += resultToHtml(entry);
                }
            }
            contents = contents.replace("${results}", results);
            int fixPage = numResults % 10 == 0 ? 1 : 0;
            contents = contents.replace("${indices}", indicesToHtml(query, p, numResults / 10 - fixPage));

            // api
            contents = contents.replace("${api}", getAPI(query));

            PrintWriter writer = response.getWriter();
            writer.println(contents);
        }
    }

    private String indicesToHtml(String query, int page, int upperbound) {
        String[] colors = {"red", "orange", "yellow", "olive", "green", "teal", "blue", "violet", "purple", "pink", "grey"};
        int cdx = 0;
        String indices = "";
        if (page > 0) {
            indices += "<a class=\"ui " + colors[cdx++] + " circular label\" href=search?query=" + encode(query) +
                    "&page=" + page + ">prev</a>";
        }
        int i = 0;
        int j = 9;
        if (page >= 6) {
            i = page - 5;
            j = j + i;
        }
        j = Math.min(j, upperbound);
        for (;i < page; i++) {
            indices += "<a class=\"ui " + colors[cdx++] + " circular label\" href=search?query=" + encode(query) +
                    "&page=" + (i+1) + ">" + (i+1) + "</a>";
        }
        indices += "<div class=\"ui black circular label not-active\">" + (i+1) + "</div>";
        i++;
        for (;i <= j; i++) {
            indices += "<a class=\"ui " + colors[cdx++] + " circular label\" href=search?query=" + encode(query) +
                    "&page=" + (i+1) + ">" + (i+1) + "</a>";
        }
        if (page + 1 <= upperbound) {
            indices += "<a class=\"ui " + colors[cdx++] + " circular label\" href=search?query=" + encode(query) +
                    "&page=" + (page + 2) + ">next</a>";
        }
        return indices;
    }

    private String resultToHtml(ResultEntry entry) {
        String html = "<a class=\"ui fluid card\" href=\"${location}\">\n" +
        "                <div class=\"content\">\n" +
        "                    <i class=\"right floated like thumbs up icon\"></i>\n" +
        "                    <i class=\"right floated star thumbs down icon\"></i>\n" +
        "                    <div class=\"header\" style=\"color:crimson;font-family:Cochin;font-size:20px\">${header}</div>\n" +
        "                    <div class=\"meta\" style=\"color:steelblue;font-family:Cochin;font-size:15px\">\n" +
        "                        <span class=\"category\">${location}</span>\n" +
        "                    </div>\n" +
        "                    <div class=\"description\" style=\"color:grey;font-family:Cochin;font-size:15px\">\n" +
        "                        <text>${digest}</text>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </a>\n";
        html = html.replace("${header}", entry.title);
        html = html.replace("${location}", entry.location);
        html = html.replace("${digest}", entry.digest);
        return html;
    }

    private String getAPI(String query) {
        String res = "";
        if (query.contains("weather")) {
            WeatherInfo current = WeatherSearchService.searchCurrent(query);
            WeatherInfo[] forecast = WeatherSearchService.searchForcast(query);
            if (current != null) {
                res += weatherToHtml(current);
            }
            if (forecast != null) {
                res += forecastToHtml(forecast);
            }
        }
        ArrayList<AmazonItem> items = ItemSearchService.search(query);
        return res;
    }

    private String weatherToHtml(WeatherInfo weatherInfo) {
        String html = "<a class=\"ui fluid link card\" href=\"https://openweathermap.org/find?q=${city}\">\n" +
        "                <div class=\"content\">\n" +
        "                    <div class=\"header\" style=\"color:steelblue;font-family:Cochin;font-size:20px\">Weather | ${city}</div>\n" +
        "                    <div class=\"meta\" style=\"color:crimson;font-family:Cochin;font-size:15px\">\n" +
        "                        <span class=\"category\">https://openweathermap.org</span>\n" +
        "                    </div>\n" +
        "                    <div class=\"description\" style=\"color:grey;font-family:Cochin;font-size:15px\">\n" +
        "                        <img src=\"${image}\">\n" +
        "                        <text style=\"font-weight:bold;font-size:28px\">${temp} &#8451; | ${weather}</text>\n" +
        "                        <p>Max/Min: ${max}/${min}</p>\n" +
        "                        <p>Humidity: ${humidity}</p>\n" +
        "                        <p>Wind: ${wind}</p>\n" +
        "                        <p>Last Update: ${time} UTC</p>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </a>\n";
        html = html.replace("${city}", weatherInfo.city);
        html = html.replace("${image}", weatherInfo.icon);
        html = html.replace("${temp}", weatherInfo.value);
        html = html.replace("${weather}", weatherInfo.weather);
        html = html.replace("${max}", weatherInfo.max);
        html = html.replace("${min}", weatherInfo.min);
        html = html.replace("${humidity}", weatherInfo.humidity);
        html = html.replace("${wind}", weatherInfo.wind);
        html = html.replace("${time}", weatherInfo.time);
        return html;
    }

    private String forecastToHtml(WeatherInfo[] forecast) {
        String html = "<a class=\"ui fluid link card\" href=\"https://openweathermap.org/find?q=${city}\">\n" +
        "                <div class=\"content\">\n" +
        "                    <div class=\"header\" style=\"color:steelblue;font-family:Cochin;font-size:20px\">Forecast | ${city}</div>\n" +
        "                    <div class=\"meta\" style=\"color:crimson;font-family:Cochin;font-size:15px\">\n" +
        "                        <span class=\"category\">https://openweathermap.org</span>\n" +
        "                    </div>\n" +
        "                    <div class=\"description\" style=\"color:grey;font-family:Cochin;font-size:15px\">\n" +
        "                        ${entries}\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </a>\n";
        html = html.replace("${city}", forecast[0].city);
        String entries = "";
        for (WeatherInfo info : forecast) {
            entries += forecastEntryToHtml(info);
        }
        html = html.replace("${entries}", entries);
        return html;
    }

    private String forecastEntryToHtml(WeatherInfo forecastEntry) {
        String html = "          <div class=\"ui divider\"></div>\n" +
        "                        <img src=\"${image}\">\n" +
        "                        <text>${max} &deg; / ${min} &deg; / ${weather}</text>\n" +
        "                        <text style=\"width:100%;text-align:right\"> [ ${time} ]</text>\n";
        html = html.replace("${image}", forecastEntry.icon);
        html = html.replace("${max}", forecastEntry.max);
        html = html.replace("${min}", forecastEntry.min);
        html = html.replace("${weather}", forecastEntry.weather);
        html = html.replace("${time}", forecastEntry.time);
        return html;
    }

    private String encode(String content) {
        try {
            return URLEncoder.encode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return content;
        }
    }
}
