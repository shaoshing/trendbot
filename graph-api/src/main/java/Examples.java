import iub.trendbot.graph.Graph;
import iub.trendbot.graph.Page;

import java.util.ArrayList;

/**
 * Created by shaoshing on 4/15/14.
 */
public class Examples {
    public static void main(String [ ] args)
    {
        Graph graph = new Graph("http://localhost:7474/db/data/");
        ArrayList<Page> pages;

        System.out.println("\n================ Testing graph.searchPages");
        pages = graph.searchPages("Terry Bradshaw");
        printPages(pages);

        System.out.println("\n================ Testing graph.getOutgoingPages");
        pages = graph.getOutgoingPages("KGTV");
        printPages(pages);
    }

    private static void printPages(ArrayList<Page> pages){
        for(Page page : pages){
            System.out.println(page.id);
            System.out.println(page.title);
        }
    }
}
