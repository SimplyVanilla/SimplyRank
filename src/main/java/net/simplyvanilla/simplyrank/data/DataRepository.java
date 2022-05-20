package net.simplyvanilla.simplyrank.data;

import java.util.UUID;

public interface DataRepository {
    /*

        All methods are synchronous, unless specified

    */
    PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, Exception> callback);
    GroupData loadGroupData(String groupName, IOCallback<GroupData, Exception> callback);

    void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback);
    void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, Exception> callback);

    boolean groupExists(String groupName);

}
