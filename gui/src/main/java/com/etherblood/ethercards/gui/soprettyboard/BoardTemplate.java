package com.etherblood.ethercards.gui.soprettyboard;

import com.etherblood.ethercards.entities.collections.IntList;
import com.etherblood.ethercards.gui.PlayerZones;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class BoardTemplate {

    private final Map<Integer, PlayerZones> playerZones = new LinkedHashMap<>();

    public BoardTemplate(int self, Map<Integer, IntList> teamToPlayer, Vector2f teamZoneSize, Vector2f cardSize) {
        if (teamToPlayer.size() != 2) {
            throw new IllegalArgumentException();
        }
        int team = teamOf(teamToPlayer, self);

        Quaternion rotation = new Quaternion();

        IntList teamPlayers = teamToPlayer.get(team);
        createTeamPlayerZones(teamPlayers, self, teamZoneSize, rotation, cardSize);

        for (IntList otherTeamPlayers : teamToPlayer.values()) {
            if (teamPlayers == otherTeamPlayers) {
                continue;
            }
            rotation = rotation.mult(new Quaternion().fromAngles(0, FastMath.TWO_PI / teamToPlayer.size(), 0));
            createTeamPlayerZones(otherTeamPlayers, null, teamZoneSize, rotation, cardSize);
        }
    }

    private void createTeamPlayerZones(IntList teamPlayers, Integer self, Vector2f teamZoneSize, Quaternion rotation, Vector2f cardSize) {
        Vector2f playerZoneSize = new Vector2f(teamZoneSize.x / teamPlayers.size(), teamZoneSize.y);
        PlayerZonesTemplate playerZonesTemplate = new PlayerZonesTemplate(new Vector4f(0, 0, playerZoneSize.x, playerZoneSize.y), cardSize);
        Vector3f position = new Vector3f(-teamPlayers.size() * playerZoneSize.x / 2, 0, 0);
        if (self != null) {
            PlayerZones playerZone = playerZonesTemplate.create(rotation.mult(position), rotation);
            playerZones.put(self, playerZone);
            position.addLocal(playerZoneSize.x, 0, 0);
        }
        for (int player : teamPlayers) {
            if (Objects.equals(self, player)) {
                continue;
            }
            PlayerZones playerZone = playerZonesTemplate.create(rotation.mult(position), rotation);
            playerZones.put(player, playerZone);
            position.addLocal(playerZoneSize.x, 0, 0);
        }
    }

    private static int teamOf(Map<Integer, IntList> teamToPlayer, int self) {
        for (Map.Entry<Integer, IntList> entry : teamToPlayer.entrySet()) {
            if (entry.getValue().contains(self)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException();
    }

    public Map<Integer, PlayerZones> getPlayerZones() {
        return playerZones;
    }

}
