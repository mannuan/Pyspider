import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class test_jsoup{

    public static void main(String args[])throws IOException{

        Document doc = Jsoup.connect("http://www.19lou.com/forum-269-1.html")
                .data("query", "Java")
                .userAgent("Mozilla")
                .cookie("auth", "token")
                .timeout(3000)
                .post();
        Elements content = doc.getElementsByAttribute("作者");
    }

}
