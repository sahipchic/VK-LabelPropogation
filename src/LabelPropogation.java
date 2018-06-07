import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Илья on 05.06.2018.
 */

public class LabelPropogation {
    Set<Integer> vertices = new HashSet<>();
    Vector<Node> nodeList;
    Vector<Integer> nodeOrder;
    Map<Integer, Set<Integer> > communitiesAndNodes;
    public LabelPropogation() {
    }

    public void readGraph(int numNodes, String path) throws IOException {
        nodeList = new Vector<>(numNodes);
        nodeOrder = new Vector<>(numNodes);
        BufferedReader br = new BufferedReader(new FileReader(path));
        int i = 0;
        while (i < numNodes) {
            nodeList.add(new Node(i, i));
            nodeOrder.add(i);
            i++;
        }
        while (br.ready()) {
            String line = br.readLine();
            String args[] = line.split(" ");
            int from = Integer.valueOf(args[0]);
            int to = Integer.valueOf(args[1]);
            nodeList.get(from).addNeighboor(to);
            nodeList.get(to).addNeighboor(from);
            vertices.add(from);
            vertices.add(to);
        }
        br.close();
    }

    public void recordingLinksToFile(String path) throws IOException {
        FileOutputStream out = new FileOutputStream(path);
        OutputStreamWriter fileWriter = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        for (Node node : nodeList) {
            fileWriter.write("nodeId: " + node.getId() + " -> communityId: " + node.getLabel() + "\n");
        }
        fileWriter.close();
        out.close();
    }

    public void readingLinksFromFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        communitiesAndNodes = new HashMap<>();
        while (br.ready()) {
            String line = br.readLine();
            String args[] = line.split(" ");
            Integer nodeId = Integer.valueOf(args[1]);
            Integer communityId = Integer.valueOf(args[4]);
            nodeList.get(nodeId).setLabel(communityId);
            Set<Integer> s = new HashSet<>();
            if(communitiesAndNodes.containsKey(communityId)){
                s = communitiesAndNodes.get(communityId);
                s.add(nodeId);
                communitiesAndNodes.put(communityId, s);
            }
            else {
                s.add(nodeId);
                communitiesAndNodes.put(communityId, s);
            }
        }
        int otv = 0;
        for (Map.Entry<Integer, Set<Integer>> entry : communitiesAndNodes.entrySet()) {
            if(entry.getValue().size() > 1) otv++;
        }
        System.out.println(otv);
        br.close();
    }

    public void recordingLinksSortedByCommunities(String path) throws IOException {
        Map<Integer, Set<Integer>> communitues = new HashMap<>();
        int communityId = 1;
        for (Node aNodeList : nodeList) {
            int label = aNodeList.getLabel();
            Set<Integer> value = communitues.get(label);
            if (value == null) {
                value = new HashSet<>();
                if(vertices.contains(aNodeList.getId())) {
                    value.add(aNodeList.getId());
                    communitues.put(label, value);
                }
            }
            else{
                if(vertices.contains(aNodeList.getId())) {
                    value.add(aNodeList.getId());
                    communitues.put(label, value);
                }
            }
        }
        FileOutputStream out = new FileOutputStream(path);
        OutputStreamWriter fileWriter = new OutputStreamWriter(out, Charset.forName("UTF-8"));

        for (Map.Entry<Integer, Set<Integer>> entry : communitues.entrySet()) {
//            if(entry.getValue().size() == 1 && entry.getValue().contains(entry.getKey())){
//                continue;
//            }
            fileWriter.write("communityId" + communityId + " : " + entry.getValue() + "\n");
            System.out.println("communityId" + communityId + " : " + entry.getValue());
            communityId++;
        }
        fileWriter.close();
        out.close();

    }



    public void searchForCommunities(String path, int threads) throws InterruptedException, ExecutionException, IOException {
        ExecutorService service = Executors.newFixedThreadPool(threads);
        Vector<LabelPropogationWorker> workers = new Vector<>(threads);
        for (int i = 0; i < threads; i++) workers.add(new LabelPropogationWorker(nodeList));
        int iteration = 0;
        int lastNumOfChangedNodes = 1;
        while (lastNumOfChangedNodes > 0) {
            lastNumOfChangedNodes = 0;
            iteration++;
            Collections.shuffle(nodeOrder);
            for (int i = 0; i < nodeList.size(); i += threads) {
                for (int curThread = 0; curThread < threads; curThread++) {
                    if (i + curThread < nodeList.size()) {
                        workers.get(curThread).setNodeToProcess(nodeOrder.get(i + curThread));
                    } else workers.get(curThread).setNodeToProcess(-1);
                }
                List<Future<Boolean>> res = service.invokeAll(workers);
                for (Future<Boolean> re : res) {
                    Boolean curRes = re.get();
                    if (curRes != null && curRes) {
                        lastNumOfChangedNodes++;
                        break;
                    }
                }
            }
            if (path != null) {
                recordingLinksToFile(path + iteration + "_links.txt");
            }
        }
        System.out.println("Detection complete!");

        service.shutdown();
    }
}
