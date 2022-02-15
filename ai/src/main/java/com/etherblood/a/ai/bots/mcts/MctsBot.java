package com.etherblood.ethercards.ai.bots.mcts;

import com.etherblood.ethercards.ai.MoveBotGame;
import com.etherblood.ethercards.ai.bots.Bot;
import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.rules.moves.Move;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MctsBot implements Bot {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBot.class);

    private static final int MILLI_TO_NANO = 1_000_000;
    private final boolean verbose;

    private final MctsBotSettings<Move, MoveBotGame> settings;
    private final MoveBotGame sourceGame;
    private final Supplier<MoveBotGame> simulationGameSupply;

    private MctsNode rootNode;
    private int rootNodeHistoryPointer = 0;

    public MctsBot(MoveBotGame sourceGame, Supplier<MoveBotGame> simulationGameSupply, MctsBotSettings<Move, MoveBotGame> settings) {
        this.settings = settings;
        this.sourceGame = sourceGame;
        this.simulationGameSupply = simulationGameSupply;
        this.verbose = settings.verbose;
    }

    @Override
    public Move findMove(int playerIndex) throws InterruptedException {
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
        if (rootNode == null) {
            rootNode = createNode();
        }

        List<Move> moves = new ArrayList<>(sourceGame.generateMoves(playerIndex));
        if (moves.size() > 1) {
            MctsRaveScores raveScores = initRaveScores();

            List<MctsBotWorker> workers = new ArrayList<>();
            for (int i = 0; i < settings.maxThreads; i++) {
                workers.add(new MctsBotWorker(sourceGame, simulationGameSupply.get(), settings, playerIndex, rootNode, raveScores));
            }
            BooleanSupplier isActive;
            int strength = settings.strength;
            switch (settings.termination) {
                case NODE_COUNT:
                    isActive = () -> rootNode.visits() < strength;
                    break;
                case MILLIS_ELAPSED:
                    long startNanos = System.nanoTime();
                    long endNanos = MILLI_TO_NANO * strength + startNanos;
                    isActive = () -> System.nanoTime() < endNanos;
                    break;
                default:
                    throw new AssertionError(settings.termination.name());
            }
            workers.parallelStream().forEach(x -> x.run(isActive));

            MctsNode node = rootNode;
            moves.sort(Comparator.comparingDouble(move -> -visits(node, move)));
            if (verbose) {
                LOG.info("Move scores:");
                for (Move move : moves) {
                    LOG.info("{}: {}", Math.round(visits(node, move)), sourceGame.toMoveString(move));
                }
                IntList branching = new IntList();
                List<MctsNode> nodes = Collections.singletonList(rootNode);
                while (!nodes.isEmpty()) {
                    branching.add(nodes.size());
                    List<MctsNode> nextNodes = new ArrayList<>();
                    for (MctsNode n : nodes) {
                        nextNodes.addAll(n.getChilds());
                    }
                    nodes = nextNodes;
                }
                LOG.info("Tree dimensions: {} - {}", branching.size(), branching.toArray());
                LOG.info("Expected win-rate: {}%", Math.round(100 * rootNode.score(playerIndex) / rootNode.visits()));
            }
        }
        Move selected = moves.get(0);
        float score = visits(rootNode, selected);
        int identicalMovesCount = 1;
        while (identicalMovesCount < moves.size() && visits(rootNode, moves.get(identicalMovesCount)) == score) {
            identicalMovesCount++;
        }
        return moves.get(settings.random.nextInt(identicalMovesCount));
    }

    private float visits(MctsNode node, Move move) {
        MctsNode child = node.getChildOrDefault(move, null);
        if (child == null) {
            return 0;
        }
        return child.visits();
    }

    private MctsRaveScores initRaveScores() {
        MctsRaveScores raveScores = new MctsRaveScores(playerCount());
        raveScores.getDefaultScore().updateScores(1f / playerCount());
        initRaveScores(raveScores, rootNode);
        return raveScores;
    }

    private void initRaveScores(MctsRaveScores raveScores, MctsNode node) {
        for (Move move : node.getMoves()) {
            MctsNode child = node.getChildOrDefault(move, null);
            if (child != null) {
                raveScores.updateScores(move, child.getScores());
                raveScores.getDefaultScore().updateScores(child.getScores());
                initRaveScores(raveScores, child);
            }
        }
    }

    private MctsNode getChild(MctsNode node, Move move) {
        return node.getChildOrDefault(move, null);
    }

    private MctsNode createNode() {
        return new MctsNode(playerCount());
    }

    private int playerCount() {
        return sourceGame.playerCount();
    }

    public MoveBotGame getSourceGame() {
        return sourceGame;
    }
}
