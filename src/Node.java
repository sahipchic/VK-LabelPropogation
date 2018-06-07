import java.util.HashSet;
import java.util.Set;

/**
 * Created by Илья on 05.06.2018.
 */
public class Node {
    private int id;
    private int label;
    private Set<Integer> neighboors;

    public Node(int id, int label){
        this.id = id;
        this.label = label;
        this.neighboors = new HashSet<>();
    }

    public int getId(){
        return this.id;
    }

    public int getLabel(){
        return this.label;
    }

    public void setLabel(int label){
        this.label = label;
    }

    public Set<Integer> getNeighboors(){
        return this.neighboors;
    }

    public void addNeighboor(int id){
        this.neighboors.add(id);
    }
}
