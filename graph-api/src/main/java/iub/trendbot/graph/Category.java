package iub.trendbot.graph;

/**
 * Created by shaoshing on 4/15/14.
 */
public class Category {
    public int id;
    public String title;
    public boolean isMainCategory;

    public Category(int id, String title, boolean isMainCategory){
        this.id = id;
        this.title = title;
        this.isMainCategory = isMainCategory;
    }
}
