package net.simplyvanilla.simplyrank.database;

import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataRepositoryMock implements PlayerDataRepository {
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataRepositoryMock(Map<UUID, PlayerData> playerDataMap) {
        this.playerDataMap = playerDataMap;
    }

    public PlayerDataRepositoryMock() {
        this(new HashMap<>());
    }

    @Override
    public PlayerData findById(UUID playerId) {
        return this.playerDataMap.getOrDefault(playerId, PlayerData.getDefault());
    }

    @Override
    public void save(UUID playerId, PlayerData playerData) {
        this.playerDataMap.put(playerId, playerData);
    }
}
