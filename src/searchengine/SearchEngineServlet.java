package searchengine;

import edu.upenn.cis455.webserver.HttpEnum;
import searchengine.dictionary.DictionaryService;
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
            DictionaryService.put(query);
            int numResults = SearchEngineService.preSearch(query);
            ResultEntry[] entries = SearchEngineService.search(query, p * 10, 10);
            double seconds = 1.0 * (System.nanoTime() - time) / 1000000000;
            String contents = new String(Files.readAllBytes(Paths.get("resources/sites/result.html")));
            contents = contents.replace("${num-results}", NumberFormat.getNumberInstance(Locale.US).format(numResults));
            contents = contents.replace("${num-seconds}", String.format("%.6f", seconds));
            String results = "";
            for (ResultEntry entry : entries) {
                results += resultToHtml(entry);
            }
            contents = contents.replace("${results}", results);
            int fixPage = numResults % 10 == 0 ? 1 : 0;
            contents = contents.replace("${indices}", indicesToHtml(query, p, numResults / 10 - fixPage));
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
        "                        <text>${date} - </text>\n" +
        "                        <text>${digest}</text>\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </a>\n";
        html = html.replace("${header}", entry.title);
        html = html.replace("${location}", entry.location);
        html = html.replace("${date}", entry.lastModified);
        html = html.replace("${digest}", entry.digest);
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
