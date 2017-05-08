package searchengine;

import edu.upenn.cis455.webserver.HttpEnum;
import searchengine.amazon.AmazonItem;
import searchengine.amazon.ItemSearchService;
import searchengine.dictionary.DictionaryService;
import searchengine.weather.WeatherInfo;
import searchengine.weather.WeatherSearchService;
import utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by QingxiaoDong on 4/6/17.
 */
public class SearchEngineServlet extends HttpServlet {
    Logger logger = new Logger(getClass().getSimpleName());

    @Override
    public void init() throws ServletException {
        super.init();
        SearchEngineService.init(getServletConfig().getInitParameter("page_rank_dat"));
    }

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
            try {
                handleSearch(request, response);
            } catch (ExecutionException e) {
                response.sendError(500);
            } catch (InterruptedException e) {
                System.out.println("Interrupted!");
                response.sendError(500);
            }
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

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws IOException, ExecutionException, InterruptedException {
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

            // amazon
            FutureTask<ArrayList<AmazonItem>> amazonThread = new FutureTask<ArrayList<AmazonItem>>(new AmazonThread(query));
            new Thread(amazonThread).start();

            // pre search : get number of results
            FutureTask<Integer> preSearchThread = new FutureTask<Integer>(new PreSearchThread(query));
            new Thread(preSearchThread).start();
            int numResults = preSearchThread.get();
            logger.warn("number of seconds used get number of results " + 1.0 * (System.nanoTime() - time) / 1000000000);

            // search results
            FutureTask<ResultEntry[]> searchThread = new FutureTask<ResultEntry[]>(new SearchThread(query, p * 10, 10));
            new Thread(searchThread).start();

            // correction
            FutureTask<String> correctionThread = null;
            if (numResults < 1000) {
                correctionThread = new FutureTask<String>(new CorrectionThread(query));
                new Thread(correctionThread).start();
            }

            // weather
            FutureTask<WeatherInfo> weatherThread = null;
            FutureTask<WeatherInfo[]> forecastThread = null;
            if (query.contains("weather")) {
                // weather thread
                weatherThread = new FutureTask<WeatherInfo>(new WeatherThread(query));
                new Thread(weatherThread).start();

                // forecast thread
                forecastThread = new FutureTask<WeatherInfo[]>(new ForecastThread(query));
                new Thread(forecastThread).start();
            }


//            int numResults = SearchEngineService.preSearch(query);
//            ResultEntry[] entries = SearchEngineService.search(query, p * 10, 10);

            String contents = new String(Files.readAllBytes(Paths.get("resources/sites/result.html")));

            ResultEntry[] entries = searchThread.get();
            logger.warn("number of seconds used get results " + 1.0 * (System.nanoTime() - time) / 1000000000);
            double seconds = 1.0 * (System.nanoTime() - time) / 1000000000;

            // num results in num seconds
            contents = contents.replace("${num-results}", NumberFormat.getNumberInstance(Locale.US).format(numResults));
            contents = contents.replace("${num-seconds}", String.format("%.6f", seconds));

            // correction
            if (correctionThread != null) {
                String correction = correctionThread.get();
                logger.warn("number of seconds used get correction " + 1.0 * (System.nanoTime() - time) / 1000000000);
                if (correction != null) {
                    contents = contents.replace("${correction}",
                            "<p style=\"font-weight:bold;color:grey;font-family:Cochin;font-size:20px\">" +
                                    "Did you mean " + "<a href=\"/search?query=" + correction + "\">" + correction + "</a>?</p>");
                } else {
                    contents = contents.replace("${correction}", "");
                }
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
            String api = "";
            if (query.contains("weather")) {
                WeatherInfo current = weatherThread.get();
                WeatherInfo[] forecast = forecastThread.get();
                if (current != null) {
                    api += weatherToHtml(current);
                }
                if (forecast != null) {
                    api += forecastToHtml(forecast);
                }
            }
            logger.warn("number of seconds used get weather " + 1.0 * (System.nanoTime() - time) / 1000000000);
            ArrayList<AmazonItem> items = amazonThread.get();
            if (items != null && items.size() > 0) {
                api += amazonItemsToHtml(items);
            }
            contents = contents.replace("${api}", api);

            PrintWriter writer = response.getWriter();
            writer.println(contents);
            logger.warn("number of seconds used in total " + 1.0 * (System.nanoTime() - time) / 1000000000);
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

    private String amazonItemsToHtml(ArrayList<AmazonItem> amazonItems) {
        String html = "<div class=\"ui fluid link card\">\n" +
                "    <div class=\"content\">\n" +
                "        <div class=\"header\" style=\"color:steelblue;font-family:Cochin;font-size:20px\">Item Search | Amazon</div>\n" +
                "        <div class=\"meta\" style=\"color:crimson;font-family:Cochin;font-size:15px\">\n" +
                "            <span class=\"category\">https://www.amazon.com</span>\n" +
                "        </div>\n" +
                "        <div class=\"ui divider\"></div>\n" +
                "        <div class=\"description\">\n" +
                "            <div class=\"ui link divided items\">\n" +
                "                ${items}" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</div>\n";
        String items = "";
        for (AmazonItem item : amazonItems) {
            items += amazonToHtml(item);
        }
        html = html.replace("${items}", items);
        return html;
    }

    private String amazonToHtml(AmazonItem amazonItem) {
        String html = "<a class=\"item\" href=\"${url}\">\n" +
        "                    <div class=\"ui tiny image\">\n" +
        "                        <img src=\"${image}\">\n" +
        "                    </div>\n" +
        "                    <div class=\"middle aligned content\">\n" +
        "                        <div class=\"header\" style=\"color:grey;font-family:Cochin;font-size:18px\">${title}</div>\n" +
        "                        <div class=\"meta\">\n" +
        "                            <span class=\"label\">${label}</span>\n" +
        "                        </div>\n" +
        "                        <div class=\"extra\">\n" +
        "                            <div class=\"ui label\">${binding}</div>\n" +
        "                            <div class=\"ui label blue\"><i class=\"dollar icon\"></i>${price}</div>\n" +
        "                        </div>\n" +
        "                    </div>\n" +
        "                </a>\n";
        html = html.replace("${url}", amazonItem.url);
        html = html.replace("${image}", amazonItem.image);
        html = html.replace("${title}", amazonItem.title);
        html = html.replace("${label}", amazonItem.label);
        html = html.replace("${binding}", amazonItem.binding);
        html = html.replace("${price}", amazonItem.price.substring(1));
        return html;
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

    class PreSearchThread implements Callable<Integer> {
        private String query;

        public PreSearchThread(String query) {
            this.query = query;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Integer call() throws Exception {
            return SearchEngineService.preSearch(query);
        }
    }

    class SearchThread implements Callable<ResultEntry[]> {
        private String query;
        private int rank;
        private int num;

        public SearchThread(String query, int rank, int num) {
            this.query = query;
            this.rank = rank;
            this.num = num;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public ResultEntry[] call() throws Exception {
            return SearchEngineService.search(query, rank, num);
        }
    }

    class CorrectionThread implements Callable<String> {
        private String query;

        public CorrectionThread(String query) {
            this.query = query;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public String call() throws Exception {
            return DictionaryService.correct(query);
        }
    }

    class AmazonThread implements Callable<ArrayList<AmazonItem>> {

        private String query;

        public AmazonThread(String query) {
            this.query = query;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public ArrayList<AmazonItem> call() throws Exception {
            return ItemSearchService.search(query);
        }
    }

    class WeatherThread implements Callable<WeatherInfo> {

        private String query;

        public WeatherThread(String query) {
            this.query = query;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public WeatherInfo call() throws Exception {
            return WeatherSearchService.searchCurrent(query);
        }
    }

    class ForecastThread implements Callable<WeatherInfo[]> {

        private String query;

        public ForecastThread(String query) {
            this.query = query;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public WeatherInfo[] call() throws Exception {
            return WeatherSearchService.searchForcast(query);
        }
    }
}
