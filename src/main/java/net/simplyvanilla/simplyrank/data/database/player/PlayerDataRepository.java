package net.simplyvanilla.simplyrank.data.database.player;

import java.util.UUID;

public interface PlayerDataRepository {
    PlayerData findById(UUID playerId);

    void save(UUID playerId, PlayerData playerData);
}
