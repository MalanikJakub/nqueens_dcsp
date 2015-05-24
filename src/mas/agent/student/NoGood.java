package mas.agent.student;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author malanjak
 */
public class NoGood {

    // A - oznaceni radku
    List<Integer> As;
    // d - pozice na radku A
    List<Integer> ds;
    // pozice ktera je zakazana za predpokladu validnich As a ds
    int banPos;

    public NoGood() {
        this.As = new ArrayList<Integer>();
        this.ds = new ArrayList<Integer>();
        banPos = -1;
    }

    public void addOne(int A, int d) {
        this.As.add(A);
        this.ds.add(d);
    }

    public void addPos(int banPos) {
        this.banPos = banPos;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.As);
        hash = 59 * hash + Objects.hashCode(this.ds);
        hash = 59 * hash + this.banPos;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        final NoGood other = (NoGood) obj;
        
        if (!this.As.equals(other.As)) {
            return false;
        }
        if (!this.ds.equals(other.ds)) {
            return false;
        }
        
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.banPos == other.banPos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nogood ");
        for (int i = 0; i < this.As.size(); i++) {
            sb.append(As.get(i));
            sb.append(" ");
            sb.append(ds.get(i));
            sb.append(" ");
        }
        sb.append("> ");
        sb.append(banPos);
        return sb.toString();        
    }

}
