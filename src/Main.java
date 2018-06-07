import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        LabelPropogation lp = new LabelPropogation();

        int maxNumNodes = 347;
        int numThreads = 5;
        lp.readGraph(maxNumNodes + 1, "input.edges");
        lp.searchForCommunities("interim_calculations_", numThreads);
        lp.recordingLinksToFile("links.txt");
        lp.readingLinksFromFile("links.txt");
        lp.recordingLinksSortedByCommunities("sorted_links.txt");

    }
}
