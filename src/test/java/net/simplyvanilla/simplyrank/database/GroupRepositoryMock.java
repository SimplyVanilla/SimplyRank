package net.simplyvanilla.simplyrank.database;

import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.database.group.GroupRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GroupRepositoryMock implements GroupRepository {

    private final Map<String, GroupData> groupDataMap;

    public GroupRepositoryMock(Map<String, GroupData> groupDataMap) {
        this.groupDataMap = groupDataMap;
    }

    public GroupRepositoryMock() {
        this(new HashMap<>());
    }

    @Override
    public Optional<GroupData> findByName(String groupName) {
        return Optional.ofNullable(this.groupDataMap.get(groupName));
    }

    @Override
    public void save(String groupName, GroupData groupData) {
        this.groupDataMap.put(groupName, groupData);
    }

    @Override
    public boolean existsByName(String groupName) {
        return this.groupDataMap.containsKey(groupName);
    }
}
