package com.etherblood.a.library.builder.ai;

import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        Properties properties = new Properties();
        try ( Reader reader = Files.newBufferedReader(Paths.get("config.properties"))) {
            properties.load(reader);
        }
        int perCardLimit = Integer.parseInt(properties.getProperty("perCardLimit"));
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
            try ( Reader reader = Files.newBufferedReader(Paths.get(templatesPath + x))) {
                return gson.fromJson(reader, JsonElement.class);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
        Map<String, Integer> cardPool = Arrays.stream(new Gson().fromJson(assetLoader.apply("card_pool.json"), String[].class)).collect(Collectors.toMap(x -> x, x -> perCardLimit));

        RawLibraryTemplate startLibrary = new RawLibraryTemplate();
        startLibrary.hero = "minions/shyvana.json";
        startLibrary.cards = new HashMap<>();

        Paths.get("agents").toFile().mkdir();
        LibraryAgent[] agents = new LibraryAgent[agentCount];
        for (int i = 0; i < agentCount; i++) {
            Path filePath = Paths.get("agents/library" + i + ".json");
            LibraryAgent agent = new LibraryAgent(filePath, agentCount);
            if (filePath.toFile().exists()) {
                agent.library = loadLibrary(filePath, gson);
            } else {
                agent.library = copy(startLibrary);
                for (int j = 0; j < initialMutations; j++) {
                    mutateLibrary(agent.library.cards, cardPool, random);
                }
                saveLibrary(agent.filePath, agent.library, gson);
            }
            agents[i] = agent;
        }

        BattleSetup battleSetup = new BattleSetup(assetLoader, botStrength);
        for (int i = 0; i + 1 < agentCount; i++) {
            LibraryAgent a = agents[i];
            for (int j = i + 1; j < agentCount; j++) {
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
            LibraryAgent[] sortedAgents = Arrays.stream(agents).sorted(Comparator.comparing(LibraryAgent::score)).toArray(LibraryAgent[]::new);

            RawLibraryTemplate newLibrary = copy(agents[agentCount - random.nextInt(mutationCandidatesCount) - 1].library);
            for (int mutation = 0; mutation < iterationMutationCount; mutation++) {
                mutateLibrary(newLibrary.cards, cardPool, random);
            }
            while (Arrays.stream(agents).anyMatch(x -> x.library.equals(newLibrary))) {
                for (int mutation = 0; mutation < collissionMutationCount; mutation++) {
                    mutateLibrary(newLibrary.cards, cardPool, random);
                }
            }
            int worstAgentIndex = Arrays.asList(agents).indexOf(sortedAgents[0]);
            System.out.println("Replacing agent " + worstAgentIndex + " with:");
            LibraryAgent worstAgent = agents[worstAgentIndex];
            worstAgent.library = newLibrary;
            saveLibrary(worstAgent.filePath, worstAgent.library, gson);
            System.out.println(gson.toJson(worstAgent.library.cards));
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
        Comparator<LibraryAgent> comparator = Comparator.comparing(LibraryAgent::score);
        comparator = comparator.thenComparing(x -> Arrays.stream(agents).mapToInt(other -> distance(other.library, x.library)).sum());
        LibraryAgent[] sortedAgents = Arrays.stream(agents).sorted(comparator).toArray(LibraryAgent[]::new);
        LibraryAgent bestAgent = sortedAgents[agentCount - 1];
        System.out.print(Arrays.asList(agents).indexOf(bestAgent) + ": " + bestAgent.score() + " ");
        System.out.println(gson.toJson(bestAgent.library.cards));
        System.out.println();
    }

    private static void saveLibrary(Path filePath, RawLibraryTemplate library, Gson gson) throws IOException {
        try ( Writer writer = Files.newBufferedWriter(filePath)) {
            gson.toJson(library, writer);
        }
    }

    private static RawLibraryTemplate loadLibrary(Path filePath, Gson gson) throws IOException {
        try ( Reader writer = Files.newBufferedReader(filePath)) {
            return gson.fromJson(writer, RawLibraryTemplate.class);
        }
    }

    private static void mutateLibrary(Map<String, Integer> library, Map<String, Integer> cardpool, Random random) {
        List<String> cards = new ArrayList<>(cardpool.keySet());
        String card = cards.get(random.nextInt(cards.size()));
        if (random.nextBoolean()) {
            if (!library.getOrDefault(card, 0).equals(cardpool.get(card))) {
                library.merge(card, 1, Main::sum);
            }
        } else {
            if (library.containsKey(card)) {
                library.merge(card, -1, Main::sum);
            }
        }
    }

    private static Map<String, Integer> crossLibraries(Map<String, Integer> a, Map<String, Integer> b, Random random) {
        Set<String> cards = new HashSet<>(a.keySet());
        cards.addAll(b.keySet());
        Map<String, Integer> result = new HashMap<>();
        for (String card : cards) {
            int aCount = a.getOrDefault(card, 0);
            int bCount = b.getOrDefault(card, 0);
            int min = Math.min(aCount, bCount);
            int max = Math.max(aCount, bCount);
            int newCount = min + random.nextInt(max - min + 1);
            if (newCount != 0) {
                result.put(card, newCount);
            }
        }
        return result;
    }

    private static RawLibraryTemplate copy(RawLibraryTemplate library) {
        RawLibraryTemplate result = new RawLibraryTemplate();
        result.hero = library.hero;
        result.cards = new HashMap<>(library.cards);
        return result;
    }

    private static int distance(RawLibraryTemplate a, RawLibraryTemplate b) {
        Set<String> cards = new HashSet<>(a.cards.keySet());
        cards.addAll(b.cards.keySet());
        int sum = 0;
        for (String card : cards) {
            sum += Math.abs(a.cards.getOrDefault(card, 0) - b.cards.getOrDefault(card, 0));
        }
        return sum;
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
