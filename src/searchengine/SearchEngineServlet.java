package searchengine;

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
            if (request.getParameter("query") == null) {
                response.sendError(404, "Page Not Found");
            } else {
                writer.println("This is the result page");
                writer.println("query is: " + request.getParameter("query"));
            }
            break;
        case "/scripts":
            contents = new String(Files.readAllBytes(Paths.get("resources/scripts/main.js")));
            writer.println(contents);
            break;
        default:
            break;
        }
    }
}
