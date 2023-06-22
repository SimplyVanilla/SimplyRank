package net.simplyvanilla.simplyrank.gson;

import com.google.gson.*;
import net.kyori.adventure.text.format.TextColor;

import java.lang.reflect.Type;

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
