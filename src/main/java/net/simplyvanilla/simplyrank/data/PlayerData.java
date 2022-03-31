package net.simplyvanilla.simplyrank.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerData {

    public static final transient PlayerData DEFAULT = new PlayerData(new ArrayList<>(Collections.singleton("default")));

    private List<String> groups;

    public PlayerData() {
    }

    public PlayerData(List<String> groups) {
        this.groups = groups;
    }

    public String getPrimaryGroup() {
        return groups.get(0);
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

}
