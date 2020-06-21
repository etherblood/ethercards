package com.etherblood.a.ai.bots.mcts.multithread;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.moves.Move;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultithreadMctsBot {

    private static final Logger LOG = LoggerFactory.getLogger(MultithreadMctsBot.class);

    private final boolean verbose;

    private final MctsBotSettings<Move, MoveBotGame> settings;
    private final MoveBotGame sourceGame;
    private final Supplier<MoveBotGame> simulationGameSupply;

    private MultithreadNode rootNode;
    private int rootNodeHistoryPointer = 0;

    public MultithreadMctsBot(MoveBotGame sourceGame, Supplier<MoveBotGame> simulationGameSupply, MctsBotSettings<Move, MoveBotGame> settings) {
        this.settings = settings;
        this.sourceGame = sourceGame;
        this.simulationGameSupply = simulationGameSupply;
        this.verbose = settings.verbose;
    }

    public Move findBestMove(int playerIndex, int threadsLimit) throws InterruptedException {
        assert !sourceGame.isGameOver();
        if (sourceGame.getMoveHistory() == null) {
            rootNode = null;
        } else {
            while (rootNodeHistoryPointer < sourceGame.getMoveHistory().size()) {
                Move move = sourceGame.getMoveHistory().get(rootNodeHistoryPointer);
                if (rootNode != null) {
                    rootNode = getChild(rootNode, move);
                }
                rootNodeHistoryPointer++;
            }
        }

        List<Move> moves = new ArrayList<>(sourceGame.generateMoves());
        if (moves.size() > 1) {
            if (rootNode == null) {
                rootNode = createNode();
            }
            MultithreadRaveScores raveScores = initRaveScores();

            List<MultithreadMctsBotWorker> workers = new ArrayList<>();
            for (int i = 0; i < threadsLimit; i++) {
                workers.add(new MultithreadMctsBotWorker(sourceGame, simulationGameSupply.get(), settings, playerIndex, rootNode, raveScores));
            }
            workers.parallelStream().forEach(x -> x.run());
            assert rootNode.visits() >= settings.strength;

            MultithreadNode node = rootNode;
            moves.sort(Comparator.comparingDouble(move -> -visits(node, move)));
            if (verbose) {
                LOG.info("Move scores:");
                for (Move move : moves) {
                    LOG.info("{}: {}", Math.round(visits(node, move)), sourceGame.toMoveString(move));
                }
                IntList branching = new IntList();
                List<MultithreadNode> nodes = Collections.singletonList(rootNode);
                while (!nodes.isEmpty()) {
                    branching.add(nodes.size());
                    List<MultithreadNode> nextNodes = new ArrayList<>();
                    for (MultithreadNode n : nodes) {
                        nextNodes.addAll(n.getChilds());
                    }
                    nodes = nextNodes;
                }
                LOG.info("Tree dimensions: {} - {}", branching.size(), branching.toArray());
                LOG.info("Expected win-rate: {}%", Math.round(100 * rootNode.score(playerIndex) / rootNode.visits()));
            }
        }
        return moves.get(0);
    }

    private float visits(MultithreadNode node, Move move) {
        MultithreadNode child = node.getChildOrDefault(move, null);
        if (child == null) {
            return 0;
        }
        return child.visits();
    }

    private MultithreadRaveScores initRaveScores() {
        MultithreadRaveScores raveScores = new MultithreadRaveScores(playerCount());
        raveScores.getDefaultScore().updateScores(1f / playerCount());
        initRaveScores(raveScores, rootNode);
        return raveScores;
    }

    private void initRaveScores(MultithreadRaveScores raveScores, MultithreadNode node) {
        for (Move move : node.getMoves()) {
            MultithreadNode child = node.getChildOrDefault(move, null);
            if (child != null) {
                raveScores.updateScores(move, child.getScores());
                raveScores.getDefaultScore().updateScores(child.getScores());
                initRaveScores(raveScores, child);
            }
        }
    }

    private MultithreadNode getChild(MultithreadNode node, Move move) {
        return node.getChildOrDefault(move, null);
    }

    private MultithreadNode createNode() {
        return new MultithreadNode(playerCount());
    }

    private int playerCount() {
        return sourceGame.playerCount();
    }

    public MoveBotGame getSourceGame() {
        return sourceGame;
    }
}
