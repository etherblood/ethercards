package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.BotGame;
import com.etherblood.a.entities.collections.IntList;

import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MctsBot<Move, Game extends BotGame<Move, Game>> {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBot.class);
    private static final float EPSILON = 1e-6f;

    private final boolean verbose;
    private final float uctConstant;
    private final float firstPlayUrgency;
    private final Function<Game, float[]> evaluation;

    private final Game sourceGame, simulationGame;
    private final Random random;
    private final int strength;
    private final float raveMultiplier;

    private Node<Move> rootNode;
    private int rootNodeHistoryPointer = 0;

    public MctsBot(Game sourceGame, Game simulationGame, MctsBotSettings<Move, Game> settings) {
        this.sourceGame = sourceGame;
        this.simulationGame = simulationGame;
        this.random = settings.random;
        this.strength = settings.strength;
        this.verbose = settings.verbose;
        this.uctConstant = settings.uctConstant;
        this.firstPlayUrgency = settings.firstPlayUrgency;
        this.evaluation = settings.evaluation;
        this.raveMultiplier = settings.raveMultiplier;
    }

    public Move findBestMove(int playerIndex) throws InterruptedException {
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
            Map<Move, RaveScore> raveScores = initRaveScores();

            while (rootNode.visits() < strength) {
                simulationGame.copyStateFrom(sourceGame);
                // randomize opponents hand cards
                // TODO: use a priori knowledge for better approximation of real hand cards
                // TODO: the simulated opponent also only 'knows' our hand...
                simulationGame.randomizeHiddenInformation(random, playerIndex);
                iteration(rootNode, raveScores);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }

            Node<Move> node = rootNode;
            moves.sort(Comparator.comparingDouble(move -> -visits(node, move)));
            if (verbose) {
                LOG.info("Move scores:");
                for (Move move : moves) {
                    LOG.info("{}: {}", Math.round(visits(node, move)), sourceGame.toMoveString(move));
                }
                IntList branching = new IntList();
                List<Node<Move>> nodes = Collections.singletonList(rootNode);
                while (!nodes.isEmpty()) {
                    branching.add(nodes.size());
                    List<Node<Move>> nextNodes = new ArrayList<>();
                    for (Node<Move> n : nodes) {
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

    private float visits(Node<Move> node, Move move) {
        Node<Move> child = node.getChildOrDefault(move, null);
        if (child == null) {
            return 0;
        }
        return child.visits();
    }

    private Map<Move, RaveScore> initRaveScores() {
        Map<Move, RaveScore> raveScores = new HashMap<>();
        RaveScore defaultScore = new RaveScore(playerCount());
        defaultScore.updateScores(1f / playerCount());
        raveScores.put(null, defaultScore);
        initRaveScores(raveScores, rootNode);
        return raveScores;
    }

    private void initRaveScores(Map<Move, RaveScore> raveScores, Node<Move> node) {
        for (Move move : node.getMoves()) {
            Node<Move> child = node.getChildOrDefault(move, null);
            if (child != null) {
                raveScores.computeIfAbsent(move, x -> new RaveScore(playerCount())).updateScores(child.getScores());
                raveScores.get(null).updateScores(child.getScores());
                initRaveScores(raveScores, child);
            }
        }
    }

    private void iteration(Node<Move> rootNode, Map<Move, RaveScore> raveScores) {
        Deque<Node<Move>> nodePath = new LinkedList<>();
        Deque<Move> movePath = new LinkedList<>();
        nodePath.add(rootNode);
        Move selectedMove = select(nodePath, movePath, raveScores);

        if (selectedMove != null) {
            Node<Move> child = createNode();
            nodePath.getLast().addChild(selectedMove, child);
            nodePath.add(child);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
        }

        float[] result = evaluation.apply(simulationGame);
        for (Node<Move> node : nodePath) {
            node.updateScores(result);
        }
        for (Move move : movePath) {
            raveScores.computeIfAbsent(move, x -> new RaveScore(playerCount())).updateScores(result);
        }
        float[] avgWeights = Arrays.copyOf(result, result.length);
        for (int i = 0; i < avgWeights.length; i++) {
            avgWeights[i] *= movePath.size();
        }
        raveScores.get(null).updateScores(avgWeights);
    }

    private Move select(Deque<Node<Move>> nodePath, Deque<Move> movePath, Map<Move, RaveScore> raveScores) {
        Node<Move> node = nodePath.getLast();
        Move selectedMove = uctSelect(node, raveScores);
        while ((node = getChild(node, selectedMove)) != null) {
            nodePath.add(node);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
            if (simulationGame.isGameOver()) {
                return null;
            }
            selectedMove = uctSelect(nodePath.getLast(), raveScores);
        }
        return selectedMove;
    }

    private Move uctSelect(Node<Move> node, Map<Move, RaveScore> raveScores) {
        List<Move> moves = simulationGame.generateMoves();
        if (moves.size() == 1) {
            return moves.get(0);
        }
        int playerIndex = simulationGame.activePlayerIndex();
        List<Move> bestMoves = new ArrayList<>();
        float bestValue = Float.NEGATIVE_INFINITY;
        for (Move move : moves) {
            float score;
            Node<Move> child = getChild(node, move);
            if (child == null) {
                score = firstPlayUrgency;
            } else {
                score = calcUtc(node.visits(), child.visits(), child.score(playerIndex));
            }

            RaveScore raveScore = raveScores.get(move);
            if (raveScore == null) {
                raveScore = raveScores.get(null);
            }
            float raveValue = raveScore.getScore(playerIndex) / (node.visits() + 1);
            score += raveMultiplier * raveValue;

            if (score > bestValue) {
                bestMoves.clear();
                bestValue = score;
            }
            if (score == bestValue) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.size() == 1) {
            return bestMoves.get(0);
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private float calcUtc(float parentTotal, float childTotal, float childScore) {
        float exploitation = childScore / (childTotal + EPSILON);
        float exploration = (float) (Math.sqrt(uctConstant * Math.log(parentTotal + 1) / (childTotal + EPSILON)));
        return exploitation + exploration;
    }

    private Node<Move> getChild(Node<Move> node, Move move) {
        return node.getChildOrDefault(move, null);
    }

    private Node<Move> createNode() {
        return new Node<>(playerCount());
    }

    private int playerCount() {
        return sourceGame.playerCount();
    }

    public Game getSourceGame() {
        return sourceGame;
    }
}
