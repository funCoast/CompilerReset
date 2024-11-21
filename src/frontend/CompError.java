package frontend;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CompError {
    private HashMap<Integer, Character> errors = new HashMap<>();

    public void updateError(int lineCount, char type) {
        errors.put(lineCount, type);
    }

    public void output(){
        // 使用 TreeMap 对 HashMap 的键进行排序
        Map<Integer, Character> sortedErrors = new TreeMap<>(errors);
        try (FileWriter writer = new FileWriter("error.txt", false)) {  // 使用 false 覆盖文件
            for (Map.Entry<Integer, Character> entry : sortedErrors.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }
}
