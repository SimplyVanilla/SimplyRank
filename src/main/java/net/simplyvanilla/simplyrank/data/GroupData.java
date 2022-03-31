package net.simplyvanilla.simplyrank.data;

import org.bukkit.ChatColor;

public class GroupData {

    private ChatColor color;
    private String prefix;

    public GroupData() {
    }

    public GroupData(ChatColor color, String prefix) {
        this.color = color;
        this.prefix = prefix;
    }

    public ChatColor getColor() {
        return color;
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
