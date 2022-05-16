package net.simplyvanilla.simplyrank.data;

import java.io.IOException;
import java.util.UUID;

public interface DataRepository {
    /*

        All methods are synchronous, unless specified

    */
    public PlayerData loadPlayerData(UUID uuid, IOCallback<PlayerData, Exception> callback);
    public GroupData loadGroupData(String groupName, IOCallback<GroupData, Exception> callback);

    public void savePlayerData(String uuidString, PlayerData playerData, IOCallback<Void, Exception> callback);
    public void saveGroupData(String groupName, GroupData groupData, IOCallback<Void, Exception> callback);

    public boolean groupExists(String groupName);

}
