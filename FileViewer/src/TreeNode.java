import java.util.HashMap;
import java.util.Set;

public class TreeNode {
    private TreeNode parent;
    private HashMap<String, TreeNode> children;
    private String name, path;
    private long fileSize;
    private int objType;

    public TreeNode(TreeNode parent, String path, String name, int objType) {
        this.parent = parent;
        this.name = name;
        this.path = path;
        this.objType = objType;
        this.children = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getChildrenKeys() { return children.keySet(); }

    public String getPath() {
        return path + "\\" + name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getChild(String childName) {
        return this.children.get(childName);
    }

    public int getChildNum() { return this.children.size(); }

    public void setChildren(String childName, TreeNode childNode) {
        this.children.put(childName, childNode);
    }

    public int getObjType() {
        return objType;
    }

    public long getFileSize() {
        switch (objType) {
            case 0:
                fileSize = 0;
                for (String key: children.keySet()) {
                    TreeNode tempChild = children.get(key);
                    fileSize += tempChild.getFileSize();
                }
                return fileSize;
            case 1:
                return fileSize;
            default:
                return 0;
        }
    }

    public static String getStringFileSize(long fileSize) {
        double tempSize = fileSize / 1024;
        if (tempSize >= 1024) {
            tempSize = tempSize / 1024;
            if (tempSize >= 1024){
                tempSize = tempSize / 1024;
                return String.format("%.2f GB", tempSize);
            } else {
                return String.format("%.2f MB", tempSize);
            }
        } else {
            return String.format("%.2f KB", tempSize);
        }
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
