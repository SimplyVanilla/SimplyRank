package net.simplyvanilla.simplyrank.permission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service which handles the caching of group permissions.
 */
public class GroupPermissionService {

    private final Map<String, Map<String, Boolean>> permissionDataMap = new HashMap<>();

    public Map<String, Boolean> getPermissions(String group) {
        return Collections.unmodifiableMap(
            permissionDataMap.computeIfAbsent(group, k -> new HashMap<>()));
    }

    public void setPermission(String group, String permission, boolean value) {
        permissionDataMap.computeIfAbsent(group, k -> new HashMap<>()).put(permission, value);
    }
}
