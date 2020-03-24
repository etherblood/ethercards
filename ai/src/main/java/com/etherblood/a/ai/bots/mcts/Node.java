package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.moves.Move;
import java.util.HashMap;
import java.util.Map;

public class Node {
    public float score, visits;
    public Map<Move, Node> childs = new HashMap<>();//TODO: use memory efficient Map implementation
}
