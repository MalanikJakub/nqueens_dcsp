package mas.agent.student;

import java.util.*;

import mas.agent.MASQueenAgent;
import cz.agents.alite.communication.Message;
import static java.lang.Math.*;
import java.util.ArrayList;

public class MyQueenAgent extends MASQueenAgent {

    int[] state;
    int mystate;
    boolean sent = false;
    List<NoGood> nogoods;
    int x = 0;
    int pos;
    boolean stop = false;
    boolean[] doneList;

    public MyQueenAgent(int agentId, int nAgents) {
        super(agentId, nAgents);
    }

    @Override
    protected void start(int agentId, int nAgents) {

        state = new int[nAgents];
        nogoods = new ArrayList<NoGood>();
        doneList = new boolean[nAgents];

        for (int i = 0; i < state.length; i++) {
            state[i] = -1;
        }
        pos = 0;
        state[getAgentId()] = 0;
        for (int i = agentId + 1; i < nAgents; i++) {
            sendMessage(Integer.toString(i), new StringContent("ok? " + agentId + " " + pos));
        }
        mystate = agentId;
    }

    @Override
    protected void processMessages(List<Message> newMessages) {

        for (Message message : newMessages) {

            Scanner sc = new Scanner(message.getContent().toString());
            String token = sc.next();
            int A, d;

            sent = false;
            if (!stop) {
                switch (token) {
                    case "ok?":
                        A = sc.nextInt();
                        d = sc.nextInt();
                        handleOk(A, d);
                        break;
                    case "nogood":
                        NoGood ng = new NoGood();
                        while (sc.hasNext()) {
                            String t;
                            t = sc.next();
                            if (t.equals(">")) {
                                d = sc.nextInt();
                                ng.addPos(d);
                            } else {
                                d = sc.nextInt();
                                ng.addOne(Integer.parseInt(t), d);
                            }
                        }
                        handleNoGood(Integer.parseInt(message.getSender()), ng);

                        break;
                    case "done?":
                        if (consistentView()) {
                            sendMessage(Integer.toString(nAgents() - 1), new StringContent("yesDone " + getAgentId()));
                        } else {
                            sendMessage(Integer.toString(nAgents() - 1), new StringContent("noDone " + getAgentId()));
                        }
                        break;
                    case "yesDone":
                        A = sc.nextInt();
                        doneList[A] = true;
                        // vse konsistentni, poslat vsem zpravu o ukonceni
                        if (allDone()) {
                            for (int i = 0; i < nAgents()-1; i++) {
                                sendMessage(Integer.toString(i), new StringContent("notifySolution"));
                            }
                            notifySolutionFound(pos);
                        }
                        break;
                    case "noDone":
                        A = sc.nextInt();
                        sendMessage(Integer.toString(A), new StringContent("done?"));
                        break;
                    case "notifySolution":
                        notifySolutionFound(pos);
                        break;
                    case "stop":
                        stop = true;
                        break;
                    default:
                        System.out.println("unexpected content!");
                        System.out.println(token);
                        break;
                }
            }
        }
    }

    public void handleOk(int sender, int value) {
        state[sender] = value;
        if (!checkConsistency(pos)) {
            if (!findConsistentNG()) {
                bt();
                findConsistentNG();
            }
            for (int i = getAgentId() + 1; i < nAgents(); i++) {
                sendMessage(Integer.toString(i), new StringContent("ok? " + getAgentId() + " " + pos));
            }
        }
        if (getAgentId() == nAgents() - 1) {
            if (consistentView()) {
                for (int i = 0; i < nAgents() - 1; i++) {
                    sendMessage(Integer.toString(i), new StringContent("done?"));
                    doneList[getAgentId()] = true;
                }
            }
        }
    }

    public void handleNoGood(int sender, NoGood ng) {
        if (!nogoods.contains(ng)) {
            nogoods.add(ng);
        }
        if (!verifyNoGood(ng) || ng.banPos != pos) {
            sendMessage(Integer.toString(sender), new StringContent("ok? " + getAgentId() + " " + pos));
        } else {
            if (!findConsistentNG()) {
                if (getAgentId() == 0) {
                    for (int i = 1; i < nAgents(); i++) {
                        sendMessage(Integer.toString(i), new StringContent(("stop")));
                    }
                    stop = true;
                    notifySolutionDoesNotExist();
                    return;
                }
                bt();
                findConsistentNG();
            }
            for (int i = getAgentId() + 1; i < nAgents(); i++) {
                sendMessage(Integer.toString(i), new StringContent("ok? " + getAgentId() + " " + pos));
            }
        }
    }

    public void bt() {
        int A = getAgentId() - 1;
        if (state[A] != -1) {
            NoGood pomng = new NoGood();
            int receiver = 0;
            for (int i = 0; i < A; i++) {
                pomng.addOne(i, state[i]);
            }
            pomng.addPos(state[A]);

            sendMessage(Integer.toString(A), new StringContent(pomng.toString()));
        }
        state[A] = -1;
    }

    public boolean checkConsistency(int p) {
        int pom = 0;
        for (int i = 0; i < getAgentId(); i++) {
            if (state[i] != -1) {
                if (p == state[i]) {
                    return false;
                }
                pom = abs(getAgentId() - i) - abs(p - state[i]);
                if (pom == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean findConsistent() {
        for (int i = 0; i < nAgents(); i++) {
            if (checkConsistency(i)) {
                pos = i;
                state[getAgentId()] = i;
                return true;
            }
        }
        return false;
    }

    public boolean findConsistentNG() {
        for (int i = 0; i < nAgents(); i++) {
            boolean cantPlace = false;
            for (Iterator<NoGood> iterator = nogoods.iterator(); iterator.hasNext();) {
                NoGood ng = iterator.next();
                if (ng.banPos == i) {
                    if (verifyNoGood(ng)) {
                        cantPlace = true;
                        break;
                    }
                }
            }
            if (!cantPlace && checkConsistency(i)) {
                pos = i;
                state[getAgentId()] = i;
                return true;
            }
        }
        return false;
    }

    // vraci true, pokud je nogood ng platny pro dany agent_view
    public boolean verifyNoGood(NoGood ng) {
        for (int i = 0; i < ng.As.size(); i++) {
            if (state[ng.As.get(i)] != ng.ds.get(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean consistentView() {
        int pom;
        for (int p = 0; p <= getAgentId(); p++) {
            if (state[p] == -1) {
                return false;
            }
            for (int i = 0; i <= getAgentId(); i++) {
                if (p != i) {
                    if (state[p] == state[i]) {
                        return false;
                    }
                    pom = abs(p - i) - abs(state[p] - state[i]);
                    if (pom == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public boolean allDone() {
        for (int i = 0; i < nAgents(); i++) {
            if (!doneList[i]) {
                return false;
            }
        }
        return true;
    }

    private static void printoutSolution(int[] solution) {
        for (int i = 0; i < solution.length; i++) {
            System.out.print("|");
            for (int j = 0; j < solution.length; j++) {
                if (solution[i] == j) {
                    System.out.print("Q|");
                } else {
                    System.out.print(" |");
                }
            }
            System.out.println();
        }
    }

}
