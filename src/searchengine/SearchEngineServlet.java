package searchengine;

import edu.upenn.cis455.webserver.HttpEnum;
import utils.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        case "/result":
            contents = new String(Files.readAllBytes(Paths.get("resources/sites/result.html")));
            writer.println(contents);
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
            String query = request.getParameter("query");
            logger.warn("The query is {}", query);
            int numResults = SearchEngineService.preSearch(query);
            String contents = new String(Files.readAllBytes(Paths.get("resources/sites/result.html")));
            PrintWriter writer = response.getWriter();
            writer.println(contents);
            response.sendRedirect("/result");
        }
    }
}
