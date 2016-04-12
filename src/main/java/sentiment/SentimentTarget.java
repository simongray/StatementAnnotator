package sentiment;

public class SentimentTarget {
    private int sentiment;

    public SentimentTarget(int sentiment) {
        this.sentiment = sentiment;
    }

    public int getSentiment() {
        return sentiment;
    }

    @Override
    public String toString() {
        return "" + sentiment;
    }
}
