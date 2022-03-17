import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class User {
    private static User instance;
    private MainUI mainUI;
    private TreeNode root;
    public static AtomicInteger atomicIntegerBegin = new AtomicInteger(0);
    public static AtomicInteger atomicIntegerEnd = new AtomicInteger(0);

    private String rootFileLocation;
    private String[] blackListFileLocation;

    public static void main(String[] args) {
        User.getInstance().createMainUI();
    }

    private void createMainUI() {
        mainUI = new MainUI();
        mainUI.renderFrame();
    }

    public static User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    public void setRootFileLocation(String location) { rootFileLocation = location; }

    public String getRootFileLocation() { return  rootFileLocation; }

    public void setBlackListFileLocation(String[] blackListFileLocation) {
        this.blackListFileLocation = blackListFileLocation;
    }

    public String[] getBlackListFileLocation() {
        return blackListFileLocation;
    }

    public boolean blackListFileLocationConatins(String obj) {
        for (String str: blackListFileLocation) {
            if (str.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public void startScanner() {
        root = new TreeNode(null, rootFileLocation,"", 0);
        FileThread thread = new FileThread(rootFileLocation, root);
        thread.start();
        while (atomicIntegerBegin.get() <= 0) {

        }
        scannerListener();
    }

    public void scannerListener() {
        int lastBeginCounter = 0;
        while (true) {
            try {
                lastBeginCounter = atomicIntegerBegin.get();
                TimeUnit.SECONDS.sleep(5);
                if (lastBeginCounter == atomicIntegerBegin.get() && atomicIntegerEnd.get() == atomicIntegerBegin.get()) {
                    mainUI.addPieChart(root);
                    mainUI.addFileNavigation(root);
                    System.out.println("Program finished");
                    return;
                }
//                System.out.println(atomicInteger.get());
            } catch (Exception e){
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    public TreeNode getRoot() {
        return root;
    }

    public void showMsg(String msg)  {
        mainUI.showMsg(msg);
    }

}

class FileThread implements Runnable {
    private java.lang.Thread t;
    private String threadName;
    private TreeNode node;

    public FileThread(String name, TreeNode node) {
        this.threadName = name;
        this.node = node;
    }

    @Override
    public void run() {
//        System.out.println("Running " + threadName + ", #" + User.getInstance().atomicInteger.getAndIncrement());
        User.getInstance().atomicIntegerBegin.getAndIncrement();
        try {
            File file = new File(node.getPath());
            File[] tempList = file.listFiles();
            for (File child: tempList) {
                if (User.getInstance().blackListFileLocationConatins(child.getPath())) {
                    continue;
                }
                if (child.isDirectory()) {
                    TreeNode newNode = new TreeNode(node, node.getPath(), child.getName(), 0);
                    node.setChildren(child.getName(), newNode);
                    FileThread thread = new FileThread(newNode.getPath(), newNode);
                    thread.start();
                    continue;
                }
                if (child.isFile()) {
                    node.setChildren(child.getName(), new TreeNode(node, node.getPath(), child.getName(), 1));
                    node.getChild(child.getName()).setFileSize(child.length());
                    continue;
                }
            }
        }catch (Exception e) {
//            System.out.println("Thread " +  threadName + " error: " + e.getLocalizedMessage());
        }
//        System.out.println("Thread " +  threadName + " exiting");
        int endNume = User.getInstance().atomicIntegerEnd.getAndIncrement();
        System.out.println("Remain #" + (User.getInstance().atomicIntegerBegin.get() - User.getInstance().atomicIntegerEnd.get()));
    }

    public void start() {
        if (t == null) {
            t = new java.lang.Thread(this, threadName);
            t.start();
        }
    }
}
