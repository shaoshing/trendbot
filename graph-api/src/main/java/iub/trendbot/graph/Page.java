package iub.trendbot.graph;

/**
 * Created by shaoshing on 4/15/14.
 */
public class Page {
    public Long id;
    public String title;

    public Long category_id;
    public String category_title;
    public Long category_level;

    public Page(Long id, String title){
        this.id = id;
        this.title = title;
    }
    public Page(Long id, String title, Long category_id, String category_title, Long category_level){
        this.id = id;
        this.title = title;
        this.category_id= category_id;
        this.category_title = category_title;
        this.category_level = category_level;
    }
}
