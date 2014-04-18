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

        System.out.println("\n================ Testing graph.getOutgoingPages");
        pages = graph.getIncomingPages("Curling,_Newfoundland_and_Labrador");
        printPages(pages);

        System.out.println("\n================ Testing graph.getCategoryPages level 1");
        pages = graph.getCategoryPages("27_Club", 1);
        printPagesAndCategories(pages);

        System.out.println("\n================ Testing graph.getCategoryPages level 2");
        pages = graph.getCategoryPages("27_Club", 2);
        printPagesAndCategories(pages);

        System.out.println("\n================ Testing graph.getCategoryPages level 3");
        pages = graph.getCategoryPages("Prince", 3);
        printPagesAndCategories(pages);
    }

    private static void printPages(ArrayList<Page> pages){
        for(Page page : pages){
            System.out.printf("%d, %s\n", page.id, page.title);
        }
    }

    private static void printPagesAndCategories(ArrayList<Page> pages){
        for(Page page : pages){
            System.out.printf("%d, %s -> %d, %s\n", page.id, page.title, page.category_id, page.category_title);
        }
    }
}
