package net.simplyvanilla.simplyrank.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroupPermissionManager {

    private final Map<String, Map<String, Boolean>> permissionDataMap = new HashMap<>();

    public Map<String, Boolean> getPermissions(String group) {
        return Collections.unmodifiableMap(permissionDataMap.computeIfAbsent(group, k -> new HashMap<>()));
    }

    public void setPermission(String group, String permission, boolean value) {
        permissionDataMap.computeIfAbsent(group, k -> new HashMap<>()).put(permission, value);
    }

}
