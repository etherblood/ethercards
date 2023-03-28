package com.etherblood.ethercards.ai.library.builder;

import com.etherblood.ethercards.ai.MoveBotGame;
import com.etherblood.ethercards.ai.bots.Bot;
import com.etherblood.ethercards.ai.bots.mcts.MctsBot;
import com.etherblood.ethercards.ai.bots.mcts.MctsBotSettings;
import com.etherblood.ethercards.rules.Game;
import com.etherblood.ethercards.rules.moves.Move;
import com.etherblood.ethercards.templates.api.setup.RawLibraryTemplate;
import com.google.gson.GsonBuilder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class Main {
    public static void main(String... args) throws InterruptedException {
        String assetsPath = "../assets/";
        String hero = "nerfed_ahri";
        List<String> cardPool = List.of(
                "blue_eyes_white_dragon",
                "boneyard_wurm",
                "ditto",
                "bad_dog",
                "dragonlord_silumgar",
                "goblin",
                "haunted_creeper",
                "release_the_dogs",
                "spined_wurm",
                "stingerfling_spider",
                "treeborn_frog",
                "voyaging_satyr",
                "wolfrider"
        );
        ToIntFunction<String> cardLimit = id -> switch (id) {
            case "bad_dog" -> 100;
            case "blue_eyes_white_dragon" -> 3;
            default -> 2;
        };

        Random random = new Random(1);
        int totalLibraryCount = 100;
        int mutationsPerGen = 5;
        int quality = 8;
        int survivorsPerGen = totalLibraryCount / 2;
        List<RawLibraryTemplate> initialGeneration = new ArrayList<>();
        for (int i = 0; i < totalLibraryCount; i++) {
            initialGeneration.add(rollLibrary(cardPool, cardLimit, hero, random));
        }

        BattleSetup setup = new BattleSetup(assetsPath + "templates/", new SecureRandom());
        MctsBotSettings<Move, MoveBotGame> botSettings = new MctsBotSettings<>();
        botSettings.strength = 100;


        List<RawLibraryTemplate> generation = initialGeneration;
        int numGenerations = 20;
        for (int i = 0; i < numGenerations; i++) {
            System.out.println("Simulating generation " + i);
            List<RawLibraryTemplate> best = findRoughlyBest(setup, botSettings, generation, survivorsPerGen, random, quality);
            List<RawLibraryTemplate> childs = new ArrayList<>();
            int missing = totalLibraryCount - best.size();
            for (int j = 0; j < missing; j++) {
                RawLibraryTemplate parentA = best.get(random.nextInt(best.size()));
                RawLibraryTemplate parentB = best.get(random.nextInt(best.size()));
                childs.add(childLibrary(parentA, parentB, random));
            }
            for (int j = 0; j < mutationsPerGen; j++) {
                int childIndex = random.nextInt(best.size());
                childs.set(childIndex, mutateLibrary(childs.get(childIndex), cardPool, cardLimit, random));
            }
            generation = new ArrayList<>();
            generation.addAll(best);
            generation.addAll(childs);
            System.out.println("best:");
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(best.get(0)));
        }
        System.out.println("final generation:");
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(generation));
    }

    private static List<RawLibraryTemplate> findRoughlyBest(BattleSetup setup, MctsBotSettings<Move, MoveBotGame> botSettings, List<RawLibraryTemplate> libraries, int amount, Random random, int quality) throws InterruptedException {
        if (libraries.size() % 2 != 0) {
            throw new RuntimeException("Generation size must be divisible by 2.");
        }
        // TODO: many unnecessary games are played which could be pruned.
        // eg. an agent that already won 6/10 games (when half the agents survive) won't need to play any more games, it is already guaranteed to survive
        // eg. cache match results of previous matches (older & same generations) and reuse them somehow?
        // Given that there are expected to be many duplicate matches this might help a lot.
        // make sure that optimizations don't change behaviour (negatively)
        ArrayList<BattleAgent> agents = new ArrayList<>();
        for (RawLibraryTemplate library : libraries) {
            agents.add(new BattleAgent(library));
        }
        for (int i = 0; i < quality; i++) {
            Collections.shuffle(agents, random);
            for (int j = 0; j < agents.size(); j += 2) {
                BattleAgent a = agents.get(j);
                BattleAgent b = agents.get(j + 1);

                if (a.library.equals(b.library)) {
                    a.draws++;
                    b.draws++;
                    continue;
                }
                Game game = setup.startGame(a.library, b.library);

                Bot bot = new MctsBot(new MoveBotGame(game), () -> new MoveBotGame(setup.simulationGame(game)), botSettings);
                int playerA = game.findPlayerByIndex(0);
                int playerB = game.findPlayerByIndex(1);
                while (!game.isGameOver()) {
                    int index;
                    if (game.isPlayerActive(playerA)) {
                        index = 0;
                    } else {
                        index = 1;
                    }
                    Move move = bot.findMove(index);
                    game.getMoves().apply(move);
                }

                if (game.hasPlayerWon(playerA)) {
                    a.wins++;
                    b.losses++;
                } else if (game.hasPlayerWon(playerB)) {
                    a.losses++;
                    b.wins++;
                } else {
                    a.draws++;
                    b.draws++;
                }
            }
            System.out.println("finished round " + i);
        }

        return agents.stream()
                .sorted(Comparator.comparingDouble(agent -> -(2d * agent.wins + agent.draws) / (2d * (agent.wins + agent.draws + agent.losses))))
                .map(agent -> agent.library)
                .limit(amount)
                .collect(Collectors.toList());
    }

    private static RawLibraryTemplate rollLibrary(List<String> cardPool, ToIntFunction<String> cardLimit, String hero, Random random) {
        return new RawLibraryTemplate(
                hero,
                cardPool.stream()
                        .collect(Collectors.toMap(card -> card, card -> random.nextInt(cardLimit.applyAsInt(card) + 1))));
    }

    private static RawLibraryTemplate mutateLibrary(RawLibraryTemplate library, List<String> cardPool, ToIntFunction<String> cardLimit, Random random) {
        RawLibraryTemplate template = new RawLibraryTemplate(library);
        String card = cardPool.get(random.nextInt(cardPool.size()));
        int roll = random.nextInt(cardLimit.applyAsInt(card));
        template.cards().put(card, roll);
        return template;
    }

    private static RawLibraryTemplate childLibrary(RawLibraryTemplate a, RawLibraryTemplate b, Random random) {
        Set<String> cards = new HashSet<>();
        cards.addAll(a.cards().keySet());
        cards.addAll(b.cards().keySet());

        return new RawLibraryTemplate(
                random.nextBoolean() ? a.hero() : b.hero(),
                cards.stream().collect(Collectors.toMap(
                        Function.identity(),
                        card -> random.nextBoolean() ? a.cards().getOrDefault(card, 0) : b.cards().getOrDefault(card, 0))));
    }
}
