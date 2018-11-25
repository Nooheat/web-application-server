package template;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Templater {
    private String contents;
    private Map<String, String> tagMap;

    public Templater(String path) throws IOException {
        this.contents = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        this.tagMap = Maps.newHashMap();
    }

    public <T> Templater addObject(String tag, String s) {
        tagMap.put(tag, s);
        return this;
    }

    public String template() {
        for (Map.Entry<String, String> tagEntry : tagMap.entrySet()) {
            this.contents = this.contents.replaceAll("\\{\\{\\s*" + tagEntry.getKey() + "\\s*\\}\\}", tagEntry.getValue());
        }
        return this.contents;
    }
}
