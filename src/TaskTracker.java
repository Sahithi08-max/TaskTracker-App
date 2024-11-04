import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TaskTracker {

    private static final String FILE_NAME = "tasks.json";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide an action (add, update, delete, mark, list)");
            return;
        }

        String action = args[0];
        switch (action) {
            case "add":
                addTask(args);
                break;
            case "update":
                updateTask(args);
                break;
            case "delete":
                deleteTask(args);
                break;
            case "mark":
                markTask(args);
                break;
            case "list":
                listTasks(args);
                break;
            default:
                System.out.println("Unknown action. Available actions: add, update, delete, mark, list");
                break;
        }
    }

    private static void addTask(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: add <task_description>");
            return;
        }

        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Map<String, String> task = new HashMap<>();
        task.put("id", UUID.randomUUID().toString());
        task.put("description", description);
        task.put("status", "not done");

        List<Map<String, String>> tasks = loadTasks();
        tasks.add(task);
        saveTasks(tasks);

        System.out.println("Task added: " + description);
    }

    private static void updateTask(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: update <task_id> <new_description>");
            return;
        }

        String id = args[1];
        String newDescription = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        List<Map<String, String>> tasks = loadTasks();
        boolean updated = false;
        for (Map<String, String> task : tasks) {
            if (task.get("id").equals(id)) {
                task.put("description", newDescription);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveTasks(tasks);
            System.out.println("Task updated.");
        } else {
            System.out.println("Task not found.");
        }
    }

    private static void deleteTask(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: delete <task_id>");
            return;
        }

        String id = args[1];
        List<Map<String, String>> tasks = loadTasks();
        boolean removed = tasks.removeIf(task -> task.get("id").equals(id));

        if (removed) {
            saveTasks(tasks);
            System.out.println("Task deleted.");
        } else {
            System.out.println("Task not found.");
        }
    }

    private static void markTask(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: mark <task_id> <status>");
            return;
        }

        String id = args[1];
        String status = args[2];

        if (!status.equals("in progress") && !status.equals("done") && !status.equals("not done")) {
            System.out.println("Invalid status. Use 'in progress', 'done', or 'not done'");
            return;
        }

        List<Map<String, String>> tasks = loadTasks();
        boolean marked = false;
        for (Map<String, String> task : tasks) {
            if (task.get("id").equals(id)) {
                task.put("status", status);
                marked = true;
                break;
            }
        }

        if (marked) {
            saveTasks(tasks);
            System.out.println("Task marked as " + status + ".");
        } else {
            System.out.println("Task not found.");
        }
    }

    private static void listTasks(String[] args) {
        String statusFilter = args.length > 1 ? args[1] : "";

        List<Map<String, String>> tasks = loadTasks();
        for (Map<String, String> task : tasks) {
            String status = task.get("status");

            if (statusFilter.isEmpty() || status.equals(statusFilter)) {
                System.out.println("ID: " + task.get("id") + ", Description: " + task.get("description") + ", Status: " + status);
            }
        }
    }

    private static List<Map<String, String>> loadTasks() {
        List<Map<String, String>> tasks = new ArrayList<>();
        try {
            if (Files.exists(Paths.get(FILE_NAME))) {
                String content = new String(Files.readAllBytes(Paths.get(FILE_NAME)));
                if (!content.isEmpty()) {
                    tasks = parseTasks(content);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
        return tasks;
    }

    private static void saveTasks(List<Map<String, String>> tasks) {
        try (FileWriter file = new FileWriter(FILE_NAME)) {
            file.write(tasksToJson(tasks));
            file.flush();
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    private static List<Map<String, String>> parseTasks(String content) {
        List<Map<String, String>> tasks = new ArrayList<>();
        String[] entries = content.split("},\\{");
        for (String entry : entries) {
            entry = entry.replaceAll("[\\[\\]{}]", ""); // Clean up JSON brackets
            Map<String, String> task = new HashMap<>();
            String[] pairs = entry.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                task.put(keyValue[0].replaceAll("\"", "").trim(), keyValue[1].replaceAll("\"", "").trim());
            }
            tasks.add(task);
        }
        return tasks;
    }

    private static String tasksToJson(List<Map<String, String>> tasks) {
        StringBuilder json = new StringBuilder("[");
        for (Map<String, String> task : tasks) {
            json.append("{");
            task.forEach((key, value) -> json.append("\"").append(key).append("\":\"").append(value).append("\","));
            json.deleteCharAt(json.length() - 1); // Remove trailing comma
            json.append("},");
        }
        if (tasks.size() > 0) {
            json.deleteCharAt(json.length() - 1); // Remove trailing comma
        }
        json.append("]");
        return json.toString();
    }
}
