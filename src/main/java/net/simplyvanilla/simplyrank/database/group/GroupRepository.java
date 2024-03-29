package net.simplyvanilla.simplyrank.database.group;

import java.util.Optional;

public interface GroupRepository {
    Optional<GroupData> findByName(String groupName);

    void save(String groupName, GroupData groupData);

    boolean existsByName(String groupName);
}
