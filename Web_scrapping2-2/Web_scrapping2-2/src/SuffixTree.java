import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;


public class SuffixTree {
    private Document document;

    public SuffixTree(){

    }

    public SuffixTree(List<Integer> nodeEncodingSequence) {
        this.document = createDocumentFromEncoding(nodeEncodingSequence);
    }

    public Element getNode(int index) {
        List<Element> allElements = document.getAllElements();
        if (index >= 0 && index < allElements.size()) {
            return allElements.get(index);
        } else {
            return null; // Handle out-of-bounds index gracefully
        }
    }

    public Document createDocumentFromEncoding(List<Integer> nodeEncodingSequence) {
        Document doc = new Document("");
        Element currentElement = doc;

        for (Integer nodeId : nodeEncodingSequence) {
            Element newElement = new Element(Tag.valueOf("node"), "");
            newElement.attr("id", nodeId.toString());
            currentElement.appendChild(newElement);
            currentElement = newElement;
        }

        return doc;
    }

    /**
     * Count the number of leaf nodes
     * @param node
     * @param leafCount
     */
    private void countLeaf(Element node, Map<Element, Integer> leafCount) {
        int count = 0;

        if (node.children().isEmpty()) {
            count = 1;
        } else {
            for (Element child : node.children()) {
                if (leafCount.containsKey(child)) {
                    count += leafCount.get(child);
                }
            }
        }

        leafCount.put(node, count);
    }

    private void postOrderCount(Document document, Map<Element, Integer> leafCount) {
        Elements allElements = document.getAllElements();
        List<Element> elementsInReverse = new ArrayList<>(allElements);
        elementsInReverse.sort((e1, e2) -> Integer.compare(e2.siblingIndex(), e1.siblingIndex()));

        for (Element element : elementsInReverse) {
            countLeaf(element, leafCount);
        }
    }

    public Map<String, List<Interval>> frequentPattern(int freqThresh, int lenThresh, boolean greedy) {
        Map<Element, Integer> leafCount = new HashMap<>();
        postOrderCount(document, leafCount);
        Set<Interval> allLeafIntervals = new HashSet<>();
        List<Interval> intervals = new ArrayList<>();

        // Collect leaf node intervals
        for (Element element : document.getAllElements()) {
            if (leafCount.get(element) >= freqThresh) {
                int startIndex = element.elementSiblingIndex();
                int endIndex = startIndex + element.getAllElements().size();
                Interval interval = new Interval(startIndex, endIndex);
                allLeafIntervals.add(interval);
                intervals.add(interval);
            }
        }

        Map<String, List<Interval>> ans = new HashMap<>();

        for (Interval interval : intervals) {
            if (interval.size() >= lenThresh) {
                Set<String> patterns = new HashSet<>();
                for (int i = interval.getStartIndex(); i < interval.getEndIndex(); i++) {
                    Element element = document.getAllElements().get(i);
                    patterns.add(element.toString());
                }

                for (String pattern : patterns) {
                    if (!greedy) {
                        allLeafIntervals.remove(interval);
                    }

                    if (!ans.containsKey(pattern)) {
                        ans.put(pattern, new ArrayList<>());
                    }

                    ans.get(pattern).add(interval);
                }
            }
        }

        return ans;
    }

    // Just for local testing.
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: SuffixTree <input_file>");
            return;
        }

        String inputFilePath = "/Users/srikanth/Documents/WebRecordExtractionWithInvariants/src/main/java/org/example/amazon.html";

        try {
            List<Integer> nodeEncodingSequence = Arrays.asList(1, 2, 3, 4, 5); // Replace with your actual encoding sequence
            SuffixTree suffixTree = new SuffixTree(nodeEncodingSequence);

            int freqThresh = 3;
            int lenThresh = 3;
            boolean greedyPattern = false;

            Map<String, List<Interval>> frequentPatterns = suffixTree.frequentPattern(freqThresh, lenThresh, greedyPattern);

            for (Map.Entry<String, List<Interval>> entry : frequentPatterns.entrySet()) {
                String pattern = entry.getKey();
                List<Interval> intervals = entry.getValue();
                System.out.println("Pattern: " + pattern);
                System.out.println("Intervals: " + intervals);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating the suffix tree: " + e.getMessage());
        }
    }
}




/**
 * Class to represent Interval
 */
class Interval {
    private int startIndex;
    private int endIndex;

    public Interval(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int size() {
        return endIndex - startIndex;
    }

    @Override
    public String toString() {
        return "[" + startIndex + ", " + endIndex + "]";
    }
}
