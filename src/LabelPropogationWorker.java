import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

/**
 * Created by Илья on 05.06.2018.
 */
public class LabelPropogationWorker implements Callable<Boolean> {
    private Vector<Integer> domLabels;
    private Vector<Integer> labelCounts;
    private int nodeId; 
    private Random randGenerator;
    private Vector<Node> nodesList;
    public LabelPropogationWorker(Vector<Node> nodesList){
        domLabels = new Vector<>();
        labelCounts = new Vector<>(nodesList.size());
        for(int i = 0; i < nodesList.size(); i++){
            labelCounts.add(0);
        }
        randGenerator = new Random();
        this.nodesList = nodesList;
    }
    public void setNodeToProcess(int nodeId){
        this.nodeId = nodeId;
    }

    @Override
    public Boolean call() throws Exception {
        if(nodeId == -1) return Boolean.FALSE;
        boolean continueRunning = false;
        Collections.fill(labelCounts, 0);
        domLabels.clear();
        Node curNode = nodesList.get(nodeId);
        int maxCount = 0;
        for(Integer nId : curNode.getNeighboors()){
            Integer nLabel = nodesList.get(nId).getLabel();
            if(nLabel == 0) continue;
            int nLabelCount = labelCounts.get(nLabel) + 1;
            labelCounts.set(nLabel, nLabelCount);
            if(maxCount < nLabelCount){
                maxCount = nLabelCount;
                domLabels.clear();
                domLabels.add(nLabel);
            }
            else if(maxCount == nLabelCount){
                domLabels.add(nLabel);
            }
        }
        if(domLabels.size() > 0){
            int rand = randGenerator.nextInt(domLabels.size());
            int randLabel = domLabels.get(rand);
            if(labelCounts.get(curNode.getLabel()) != maxCount) continueRunning = true;
            curNode.setLabel(randLabel);
        }
        return continueRunning;
    }
}
