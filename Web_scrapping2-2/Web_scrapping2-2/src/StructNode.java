
import org.jsoup.nodes.Element;

import java.util.*;
import java.util.List;


public class StructNode {
    private Element elm;
    private StructNode parent;
    String tag;
    private List<String> tagAttrib;
    int tagAttribID;
    int tagID;
    int htpID;
    List<Integer> structure;
    int structID;
    int size;
    int height;
    private int depth;
    List<StructNode> children;
    List<StructNode> nodeSequence = new ArrayList<>();
    private int startIndex;
    int index;
    private int endIndex;
    private Map<String, Integer> tagAttrib2ID = new HashMap<>();
    private Map<String, Integer> tag2ID = new HashMap<>();
    private Map<List<Integer>, Integer> htp2ID = new HashMap<>();
    private Map<List<Integer>, Integer> struct2ID = new HashMap<>();
    private Map<Integer, List<Integer>> structID2Index = new HashMap<>();
    private Map<Integer, Integer> structFreqency = new HashMap<>();
    private Map<Integer, Integer> structSize = new HashMap<>();
    private Map<Integer, Integer> structHeight = new HashMap<>();

    public void assignID() {
        assignIDHelper(this, new HashSet<>(), new HashSet<>());
    }

    private void assignIDHelper(StructNode node, Set<Integer> visitedNodes, Set<Integer> visitedStructs) {
        if (visitedNodes.contains(node.index)) {
            return;  // Skip already visited nodes to avoid infinite loops
        }

//        node.tagAttribID = tagAttrib2ID.computeIfAbsent(node.tagAttrib, key -> tagAttrib2ID.size() + 1);
        node.tagID = tag2ID.computeIfAbsent(node.tag, key -> tag2ID.size() + 1);

        List<Integer> htpNew = buildHTP(node);
        node.htpID = htp2ID.computeIfAbsent(htpNew, key -> htp2ID.size() + 1);

        List<Integer> structure = buildStructure(node);
        node.structID = struct2ID.computeIfAbsent(structure, key -> struct2ID.size() + 1);
        // Build structure and assign structure ID
        structID2Index.computeIfAbsent(node.structID, key -> new ArrayList<>()).add(node.index);

        // Update struct frequency, size, and height
        structFreqency.put(node.structID, structFreqency.getOrDefault(node.structID, 0) + 1);
        structSize.put(node.structID, node.size);
        structHeight.put(node.structID, node.height);

        visitedNodes.add(node.index);

        // Recursively process child nodes
        for (StructNode child : node.children) {
            assignIDHelper(child, visitedNodes, visitedStructs);
        }
    }

    // Build the Hierarchical Tag Path (HTP) for a node
    private List<Integer> buildHTP(StructNode node) {
        List<Integer> htp = new ArrayList<>();
//        htp.addAll(node.htp);
        htp.add(node.tagID);
        return htp;
    }

    private List<Integer> buildStructure(StructNode node) {
        List<Integer> structure = new ArrayList<>();
        for (StructNode child : node.children) {
            structure.add(child.structID);
        }
        structure.add(node.tagAttribID);
        return structure;
    }
    public StructNode(Element elm, int depth, StructNode parent) {
        this.elm = elm;
        this.parent = parent;
        this.tag = elm.tagName();
        this.tagAttrib = new ArrayList<>(List.of(elm.tagName()));
        for (String key : elm.attributes().asList().stream()
                .filter(attr -> !Constants.ATTRIB_BLACK_LIST.contains(attr.getKey()))
                .map(attr -> attr.getKey())
                .sorted()
                .toArray(String[]::new)) {
            tagAttrib.add(key);
        }
        this.tagAttribID = -1;
        this.tagID = -1;
        this.htpID = -1;
        this.structure = null;
        this.structID = -1;
        this.size = 1;
        this.height = 1;
        this.depth = depth;
        this.children = new ArrayList<>();
        this.startIndex = this.nodeSequence.size();
        for (Element childElement : elm.children()) {
            if (childElement.hasAttr("tag") && childElement.tagName().equals("tag")) {
                continue;
            }
            StructNode childStructNode = new StructNode(childElement, depth + 1, this);
            this.children.add(childStructNode);
            this.size += childStructNode.size;
            this.height = Math.max(this.height, childStructNode.height + 1);
        }
        this.index = nodeSequence.size();
        nodeSequence.add(this);
        this.endIndex = this.index + 1;
        this.elm.attr("data-height", String.valueOf(this.height));
        this.elm.attr("data-size", String.valueOf(this.size));
        this.elm.attr("data-index", String.valueOf(this.index));
        this.elm.attr("data-depth", String.valueOf(this.depth));

    }

    public TagAttributes getTagAttrib() {
        return new TagAttributes(tag, tagAttrib);
    }

    public int[] ancestorIndexes() {
        if (parent == null) {
            return new int[]{index};
        }
        int[] parentAncestors = parent.ancestorIndexes();
        int[] ancestors = new int[parentAncestors.length + 1];
        System.arraycopy(parentAncestors, 0, ancestors, 0, parentAncestors.length);
        ancestors[parentAncestors.length] = index;
        return ancestors;
    }
}

class TagAttributes {
    private String tagName;
    private List<String> attributes;

    public TagAttributes(String tagName, List<String> attributes) {
        this.tagName = tagName;
        this.attributes = new ArrayList<>(attributes);
    }

    public String getTagName() {
        return tagName;
    }

    public List<String> getAttributes() {
        return new ArrayList<>(attributes);
    }
}
