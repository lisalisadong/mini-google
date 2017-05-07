package searchengine;

import edu.upenn.cis455.webserver.HttpEnum;
import searchengine.dictionary.DictionaryService;
import utils.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by QingxiaoDong on 5/6/17.
 */
public class APIServlet extends HttpServlet {

    Logger logger = new Logger(getClass().getSimpleName());

    @Override
    public void init() throws ServletException {
        super.init();
        DictionaryService.init(getServletConfig().getInitParameter("query-cache"), getServletConfig().getInitParameter("backup-dict"));
    }

    @Override
    public void destroy() {
        super.destroy();
        DictionaryService.destroy(getServletConfig().getInitParameter("query-cache"));
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.warn("Received get request");
        String servletPath = request.getServletPath();
        logger.warn(servletPath);
        PrintWriter writer = response.getWriter();
        switch (servletPath) {
            case "/api":
                logger.warn(request.getPathInfo());
                if ("/predict".equals(request.getPathInfo())) {
                    String word = request.getParameter("q");
                    String resp = DictionaryService.search(word);
                    response.setContentType("text/json");
                    writer.println(resp);
                }
                break;
            default:
                break;
        }
    }
}
