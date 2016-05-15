package demo;

import reddit.MarkdownStripper;
import sun.jvm.hotspot.oops.Mark;

/**
 * Created by simongray on 15/05/16.
 */
public class TestMarkdownStripper {
    public static void main(String[] args) {
        String example = "This is a *piece* of __markdown__. I'm _trying_ to **rid it** of [bad things](http://ur.mom).\n"
                + "~~please disregard that~~.\n> this is a quote\nThis is a reply to the quote.";

        MarkdownStripper stripper = new MarkdownStripper();

        System.out.println(stripper.strip(example));
    }
}
