package pagerank;

/**
 * PageRank driver
 * 
 * @author yishang
 *
 */
public class PageRankDriver {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Usage: [crawled data directory path] " + "[page rank data directory] "
                    + "[output directory path]");
            return;
        }

        String crawledInputFilePath = args[0];
        String pageRankInputDir = args[1];
        String outputFilePath = args[2];

        if (!pageRankInputDir.endsWith("/")) {
            pageRankInputDir += "/";
        }
        String pageRankInputFile = pageRankInputDir + "iteration";

        // 1. init
        // from raw crawled data to formatted PR data
        PageRankTask.task(crawledInputFilePath, pageRankInputFile + "0", PageRankTask.INIT);

        // 2. run
        // run certain number of iterations to tune data
        int numOfIteration = 2;
        for (int i = 0; i < numOfIteration; i++) {
            PageRankTask.task(pageRankInputFile + i, pageRankInputFile + (i + 1), PageRankTask.RUN);
        }

        // 3. clean
        // output useful data
        PageRankTask.task(pageRankInputFile + numOfIteration, outputFilePath, PageRankTask.CLEAN);
    }

}
