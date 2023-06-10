package net.simplyvanilla.simplyrank.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;

import java.lang.reflect.Type;
import java.util.Locale;

public class TextColorGsonDeserializer
    implements JsonDeserializer<TextColor>, JsonSerializer<TextColor> {

    @Override
    public TextColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        try {
            return TextColor.fromCSSHexString(json.getAsString());
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Could not instantiate ChatColor " + json, e);
        }
    }

    @Override
    public JsonElement serialize(TextColor src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.asHexString());
    }
}
