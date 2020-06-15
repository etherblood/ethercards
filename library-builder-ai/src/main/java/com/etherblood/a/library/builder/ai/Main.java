package com.etherblood.a.library.builder.ai;

import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        Properties properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get("config.properties")));
        int initialMutations = Integer.parseInt(properties.getProperty("initialMutations"));
        int agentCount = Integer.parseInt(properties.getProperty("agentCount"));
        int mutationCandidatesCount = Integer.parseInt(properties.getProperty("mutationCandidatesCount"));
        int numIterations = Integer.parseInt(properties.getProperty("numIterations"));
        int iterationMutationCount = Integer.parseInt(properties.getProperty("iterationMutationCount"));
        int collissionMutationCount = Integer.parseInt(properties.getProperty("collissionMutationCount"));
        int botStrength = Integer.parseInt(properties.getProperty("botStrength"));
        String templatesPath = properties.getProperty("templatesPath");

        if (mutationCandidatesCount > agentCount) {
            throw new IllegalArgumentException("mutationCandidatesCount must be smaller than agentCount.");
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Random random = new Random();
        Function<String, JsonElement> assetLoader = x -> {
            try {
                return gson.fromJson(Files.newBufferedReader(Paths.get(templatesPath + x)), JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        List<String> cardPool = Arrays.asList(new Gson().fromJson(assetLoader.apply("card_pool.json"), String[].class));

        RawLibraryTemplate startLibrary = new RawLibraryTemplate();
        startLibrary.hero = "minions/shyvana.json";
        startLibrary.cards = cardPool.stream().collect(Collectors.toMap(x -> x, x -> 1));

        LibraryAgent[] agents = new LibraryAgent[agentCount];
        for (int i = 0; i < agentCount; i++) {
            LibraryAgent agent = new LibraryAgent(agentCount);
            agent.library = copy(startLibrary);
            agents[i] = agent;
            for (int j = 0; j < initialMutations; j++) {
                mutateLibrary(agent.library, cardPool, random);
            }
        }

        BattleSetup battleSetup = new BattleSetup(assetLoader, botStrength);
        for (int i = 0; i + 1 < agentCount; i++) {
            LibraryAgent a = agents[i];
            for (int j = i + 1; j < agentCount; j++) {
                if (i == j) {
                    continue;
                }
                LibraryAgent b = agents[j];
                System.out.print("battle " + i + " vs " + j + ": ");
                int result = battleSetup.battle(a.library, b.library);
                System.out.println(result);
                a.scores[j] = result;
                b.scores[i] = -result;
            }
        }
        for (int iteration = 0; iteration < numIterations; iteration++) {
            System.out.println(Instant.now() + " iteration " + (iteration + 1) + "/" + numIterations);
            LibraryAgent[] sortedAgents = Arrays.stream(agents).sorted(Comparator.comparing(x -> x.score())).toArray(LibraryAgent[]::new);
            for (LibraryAgent agent : agents) {
                System.out.print(Arrays.asList(agents).indexOf(agent) + ": " + agent.score() + " ");
                System.out.println(gson.toJson(agent.library.cards));
                System.out.println();
            }

            RawLibraryTemplate newLibrary = copy(agents[agentCount - random.nextInt(mutationCandidatesCount) - 1].library);
            for (int mutation = 0; mutation < iterationMutationCount; mutation++) {
                mutateLibrary(newLibrary, cardPool, random);
            }
            while (Arrays.stream(agents).anyMatch(x -> x.library.equals(newLibrary))) {
                for (int mutation = 0; mutation < collissionMutationCount; mutation++) {
                    mutateLibrary(newLibrary, cardPool, random);
                }
            }
            int worstAgentIndex = Arrays.asList(agents).indexOf(sortedAgents[0]);
            LibraryAgent worstAgent = agents[worstAgentIndex];
            worstAgent.library = newLibrary;
            System.out.println("Replaced agent " + worstAgentIndex);
            System.out.println();
            System.out.println();
            for (int i = 0; i < agentCount; i++) {
                if (i == worstAgentIndex) {
                    continue;
                }
                LibraryAgent opponent = agents[i];
                System.out.print("battle " + worstAgentIndex + " vs " + i + ": ");
                int result = battleSetup.battle(worstAgent.library, opponent.library);
                System.out.println(result);
                worstAgent.scores[i] = result;
                opponent.scores[worstAgentIndex] = -result;
            }
        }
        Comparator<LibraryAgent> comparator = Comparator.comparing(x -> x.score());
        comparator = comparator.thenComparing(x -> random.nextInt());
        LibraryAgent[] sortedAgents = Arrays.stream(agents).sorted(comparator).toArray(LibraryAgent[]::new);
        LibraryAgent bestAgent = sortedAgents[agentCount - 1];
        System.out.print(Arrays.asList(agents).indexOf(bestAgent) + ": " + bestAgent.score() + " ");
        System.out.println(gson.toJson(bestAgent.library.cards));
        System.out.println();
    }

    private static void mutateLibrary(RawLibraryTemplate library, List<String> cardpool, Random random) {
        if (library.cards.isEmpty() || random.nextBoolean()) {
            String card = cardpool.get(random.nextInt(cardpool.size()));
            library.cards.merge(card, 1, Main::sum);
        } else {
            List<String> cards = new ArrayList<>(library.cards.keySet());
            String card = cards.get(random.nextInt(cards.size()));
            library.cards.merge(card, -1, Main::sum);
        }
    }

    private static RawLibraryTemplate copy(RawLibraryTemplate library) {
        RawLibraryTemplate result = new RawLibraryTemplate();
        result.hero = library.hero;
        result.cards = new HashMap<>(library.cards);
        return result;
    }

    private static Integer sum(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        int result = a + b;
        if (result == 0) {
            return null;
        }
        return result;
    }
}
