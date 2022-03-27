package sample;

import java.util.ArrayList;
import java.util.Comparator;

public class Scroller {
    ArrayList<Double> layouts;
    int current;
    Comparator comp;

    public Scroller() {
        layouts = new ArrayList();
        current = -1;
        comp = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return (int)((double)o1-(double)o2);
            }
        };
    }

    public void addValue(double yLayout) {
//        if(!layouts.contains(yLayout)) // no duplicates allowed.
            layouts.add(yLayout);
        layouts.sort(comp); // synchronization
    }

    public Double moveUp() {
        if(current-1>=0)
            current-=1;
        if(current==-1)
            return (double)-1;
        return layouts.get(current);
    }

    public Double moveDown() {
        if(current+1<layouts.size())
            current+=1;
        if(current==-1)
            return (double)-1;
        return layouts.get(current);
    }
}