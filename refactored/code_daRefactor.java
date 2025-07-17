import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidPriority(String priority) {
        String[] validPriorities = {"Thấp", "Trung bình", "Cao"};
        for (String p : validPriorities) {
            if (p.equals(priority)) return true;
        }
        return false;
    }

    private JSONArray loadTasksFromDb() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
        }
        return new JSONArray();
    }

    private void saveTasksToDb(JSONArray tasks) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
        }
    }

    private boolean isDuplicateTask(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title) &&
                task.get("due_date").toString().equals(dueDateStr)) {
                return true;
            }
        }
        return false;
    }

    private JSONObject createTaskJson(String title, String description, LocalDate dueDate, String priority) {
        JSONObject task = new JSONObject();
        String id = UUID.randomUUID().toString();
        String dateStr = dueDate.format(DATE_FORMATTER);
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        task.put("id", id);
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dateStr);
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        task.put("created_at", now);
        task.put("last_updated_at", now);
        return task;
    }

    public JSONObject addNewTask(String title, String description, String dueDateStr, String priority) {
        if (isEmpty(title) || isEmpty(dueDateStr)) return null;
        if (!isValidDate(dueDateStr)) return null;
        if (!isValidPriority(priority)) return null;

        LocalDate dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        JSONArray tasks = loadTasksFromDb();

        if (isDuplicateTask(tasks, title, dueDateStr)) return null;

        JSONObject newTask = createTaskJson(title, description, dueDate, priority);
        tasks.add(newTask);
        saveTasksToDb(tasks);
        return newTask;
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao");
        manager.addNewTask("Tập thể dục", "Tập gym 1 tiếng.", "2025-07-21", "Trung bình");
        manager.addNewTask("", "Không có tiêu đề", "2025-07-22", "Thấp");
        manager.addNewTask("Mua sách", "Sách Công nghệ phần mềm.", "2025-07-20", "Cao");
    }
}
// Thêm class ValidationResult để gom lỗi va
