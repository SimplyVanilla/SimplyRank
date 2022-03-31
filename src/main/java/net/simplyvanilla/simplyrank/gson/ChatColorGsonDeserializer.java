package net.simplyvanilla.simplyrank.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.Locale;

public class ChatColorGsonDeserializer implements JsonDeserializer<ChatColor> {

    @Override
    public ChatColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return ChatColor.valueOf(json.getAsString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Could not instantiate ChatColor " + json, e);
        }
    }

}
