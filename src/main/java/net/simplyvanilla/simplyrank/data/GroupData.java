package net.simplyvanilla.simplyrank.data;

import net.kyori.adventure.text.format.TextColor;

public class GroupData {

    private TextColor color;
    private String prefix;

    public GroupData() {
    }

    public GroupData(TextColor color, String prefix) {
        this.color = color;
        this.prefix = prefix;
    }

    public TextColor getColor() {
        return color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
