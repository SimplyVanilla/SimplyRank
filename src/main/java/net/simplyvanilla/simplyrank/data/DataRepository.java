package net.simplyvanilla.simplyrank.data;

import java.io.IOException;
import java.util.UUID;

public interface DataRepository {
    /*

        All methods are synchronous, unless specified

    */
    public PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, IOException> callback);
    public GroupData loadGroupData(String groupName, IOCallback<GroupData, IOException> callback);

    public void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, IOException> callback);
    public void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, IOException> callback);

    public boolean groupExists(String groupName);

}
