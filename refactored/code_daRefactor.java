import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

class ValidationResult {
    boolean valid;
    String message;

    ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}

class ErrorHandler {
    static void logError(String msg) {
        System.err.println(msg);
    }
}

class Task {
    int id;
    String title, description, dueDate, priority, status, createdAt, updatedAt;

    Task(int id, String title, String description, String dueDate, String priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = "Chưa hoàn thành";
        this.createdAt = LocalDateTime.now().toString();
        this.updatedAt = this.createdAt;
    }

    JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("due_date", dueDate);
        obj.put("priority", priority);
        obj.put("status", status);
        obj.put("created_at", createdAt);
        obj.put("last_updated_at", updatedAt);
        return obj;
    }
}

class TaskDAO {
    private static final String DB_FILE_PATH = "tasks_database.json";

    static JSONArray loadTasks() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) return (JSONArray) obj;
        } catch (Exception e) {
            ErrorHandler.logError("Lỗi khi đọc DB: " + e.getMessage());
        }
        return new JSONArray();
    }

    static void saveTasks(JSONArray tasks) {
        try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
            writer.write(tasks.toJSONString());
        } catch (IOException e) {
            ErrorHandler.logError("Lỗi khi ghi DB: " + e.getMessage());
        }
    }
}

class TaskValidator {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static ValidationResult validateTitle(String title) {
        if (title == null || title.trim().isEmpty())
            return new ValidationResult(false, "Tiêu đề không được để trống.");
        return new ValidationResult(true, "");
    }

    static ValidationResult validateDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return new ValidationResult(true, "");
        } catch (Exception e) {
            return new ValidationResult(false, "Ngày không hợp lệ. Định dạng: yyyy-MM-dd");
        }
    }

    static ValidationResult validatePriority(String priority) {
        List<String> valid = Arrays.asList("Thấp", "Trung bình", "Cao");
        if (!valid.contains(priority))
            return new ValidationResult(false, "Mức ưu tiên không hợp lệ. Chọn Thấp/Trung bình/Cao.");
        return new ValidationResult(true, "");
    }

    static boolean isDuplicate(JSONArray tasks, String title, String dueDate) {
        for (Object o : tasks) {
            JSONObject t = (JSONObject) o;
            if (t.get("title").toString().equalsIgnoreCase(title) &&
                t.get("due_date").toString().equals(dueDate))
                return true;
        }
        return false;
    }
}

public class PersonalTaskManagerRefactored {
    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        ValidationResult r;

        r = TaskValidator.validateTitle(title);
        if (!r.valid) {
            ErrorHandler.logError(r.message);
            return null;
        }

        r = TaskValidator.validateDate(dueDateStr);
        if (!r.valid) {
            ErrorHandler.logError(r.message);
            return null;
        }

        r = TaskValidator.validatePriority(priority);
        if (!r.valid) {
            ErrorHandler.logError(r.message);
            return null;
        }

        JSONArray tasks = TaskDAO.loadTasks();

        if (TaskValidator.isDuplicate(tasks, title, dueDateStr)) {
            ErrorHandler.logError("Nhiệm vụ đã tồn tại với cùng ngày.");
            return null;
        }

        int id = tasks.size() + 1; // Thay UUID bằng số nguyên tăng dần
        Task task = new Task(id, title, description, dueDateStr, priority);
        tasks.add(task.toJSON());
        TaskDAO.saveTasks(tasks);

        System.out.println("Thêm thành công với ID: " + id);
        return task.toJSON();
    }

    public static void main(String[] args) {
        PersonalTaskManagerRefactored manager = new PersonalTaskManagerRefactored();
        manager.addTask("Mua sách", "Sách Java", "2025-07-20", "Cao");
        manager.addTask("Mua sách", "Sách Java", "2025-07-20", "Cao");
        manager.addTask("", "Không tiêu đề", "2025-07-22", "Thấp");
    }
}
