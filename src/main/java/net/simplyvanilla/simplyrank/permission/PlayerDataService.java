package net.simplyvanilla.simplyrank.permission;

import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service which handles the loading and saving of player data.
 */
public final class PlayerDataService {

    private final GroupRepository groupRepository;
    private final PlayerDataRepository playerDataRepository;

    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final Map<String, GroupData> groupDataCache = new ConcurrentHashMap<>();

    public PlayerDataService(GroupRepository groupRepository, PlayerDataRepository playerDataRepository) {
        this.groupRepository = groupRepository;
        this.playerDataRepository = playerDataRepository;
    }

    public PlayerData loadPlayerData(UUID uuid) {
        return this.playerDataCache.computeIfAbsent(uuid, this.playerDataRepository::findById);
    }

    public GroupData loadGroupData(String groupName) {
        return this.groupDataCache.computeIfAbsent(groupName, s -> this.groupRepository.findByName(groupName).orElse(null));
    }

    public void savePlayerData(
        UUID playerId, PlayerData playerData) {
        this.playerDataCache.put(playerId, playerData);

        this.playerDataRepository.save(
            playerId, playerData);
    }

    public void saveGroupData(
        String groupName, GroupData groupData) {
        this.groupDataCache.put(groupName, groupData);
        this.groupRepository.save(groupName, groupData);
    }

    public boolean groupExists(String groupName) {
        return this.groupRepository.existsByName(groupName);
    }

    public void invalidatePlayerData(UUID uniqueId) {
        this.playerDataCache.remove(uniqueId);
    }
}
