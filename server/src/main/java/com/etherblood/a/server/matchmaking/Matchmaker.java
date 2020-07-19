package com.etherblood.a.server.matchmaking;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.server.GamePlayerMapping;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Matchmaker {

    private static final Logger LOG = LoggerFactory.getLogger(Matchmaker.class);

    private final Random random = new SecureRandom();
    private final List<MatchmakeRequest> matchRequests = new ArrayList<>();
    private final long botId;
    private final String botName;
    private final RawLibraryTemplate botLibrary;

    public Matchmaker(long botId, String botName, RawLibraryTemplate botLibrary) {
        this.botId = botId;
        this.botName = botName;
        this.botLibrary = botLibrary;
    }

    public synchronized void enqueue(MatchmakeRequest request) {
        matchRequests.add(request);
    }

    public synchronized void remove(int connectionId) {
        Iterator<MatchmakeRequest> iterator = matchRequests.iterator();
        while (iterator.hasNext()) {
            MatchmakeRequest request = iterator.next();
            if (request.connectionId == connectionId) {
                iterator.remove();
                LOG.info("Removed game request of connection {} due to disconnect.", connectionId);
            }
        }
    }

    public synchronized MatchmakeResult matchmake() {
        List<List<MatchmakeRequest>> buckets = new ArrayList<>();
        for (MatchmakeRequest current : matchRequests) {
            boolean found = false;
            for (List<MatchmakeRequest> bucket : buckets) {
                if (current.isMatch(bucket.get(0))) {
                    bucket.add(current);
                    found = true;
                    break;
                }
            }
            if (!found) {
                buckets.add(new ArrayList<>(Arrays.asList(current)));
            }
        }

        for (List<MatchmakeRequest> bucket : buckets) {
            MatchmakeRequest first = bucket.get(0);
            if (first.humanCount > bucket.size()) {
                continue;
            }
            UUID gameId = UUID.randomUUID();
            List<GamePlayerMapping> playerMappings = new ArrayList<>();
            List<BotRequest> botRequests = new ArrayList<>();
            List<MatchmakeRequest> requests = new ArrayList<>(bucket.subList(0, first.humanCount));
            matchRequests.removeAll(requests);
            Collections.shuffle(requests, random);
            IntList teamHumanCounts = new IntList(first.teamHumanCounts);
            teamHumanCounts.shuffle(random::nextInt);
            int nextRequest = 0;
            RawGameSetup setup = new RawGameSetup();
            setup.teamCount = first.teamCount;
            setup.players = new RawPlayerSetup[first.teamCount * first.teamSize];
            for (int globalPlayerIndex = 0; globalPlayerIndex < setup.players.length; globalPlayerIndex++) {
                int teamIndex = globalPlayerIndex / first.teamSize;
                int teamPlayerIndex = globalPlayerIndex % first.teamSize;
                int teamHumanCount = teamHumanCounts.get(teamIndex);
                RawPlayerSetup playerSetup = new RawPlayerSetup();
                if (teamPlayerIndex < teamHumanCount) {
                    MatchmakeRequest request = requests.get(nextRequest++);
                    playerSetup.id = request.userId;
                    playerSetup.name = request.name;
                    playerSetup.library = request.library;

                    playerMappings.add(new GamePlayerMapping(gameId, request.userId, globalPlayerIndex, request.connectionId));
                } else {
                    playerSetup.id = botId;
                    playerSetup.name = botName;
                    playerSetup.library = botLibrary;

                    botRequests.add(new BotRequest(globalPlayerIndex, first.strength));
                }
                playerSetup.teamIndex = teamIndex;
                setup.players[globalPlayerIndex] = playerSetup;
            }
            return new MatchmakeResult(gameId, setup, playerMappings, botRequests);
        }

        return null;
    }

}
