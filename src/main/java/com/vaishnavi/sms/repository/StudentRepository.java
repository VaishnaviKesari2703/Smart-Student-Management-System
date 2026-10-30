package com.vaishnavi.sms.repository;

import com.vaishnavi.sms.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** JSON-backed repository for students.json. */
@Repository
public class StudentRepository {

    private final JsonFileStore<Student> store;

    public StudentRepository(@Value("${app.data-dir:data}") String dataDir) {
        this.store = new JsonFileStore<>(Path.of(dataDir, "students.json"), Student.class);
    }

    public List<Student> findAll() {
        return store.readAll();
    }

    public Optional<Student> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Optional<Student> findByRegistrationNumber(String regNo) {
        return findAll().stream()
                .filter(s -> s.getRegistrationNumber().equalsIgnoreCase(regNo)).findFirst();
    }

    /** Inserts a new student or replaces the one with the same id, then saves immediately. */
    public synchronized Student save(Student student) {
        List<Student> all = store.readAll();
        all.removeIf(s -> s.getId().equals(student.getId()));
        all.add(student);
        store.writeAll(all);
        return student;
    }

    public synchronized boolean deleteById(String id) {
        List<Student> all = store.readAll();
        boolean removed = all.removeIf(s -> s.getId().equals(id));
        if (removed) {
            store.writeAll(all);
        }
        return removed;
    }

    /** Next sequential id such as STU006. */
    public synchronized String nextId() {
        int max = store.readAll().stream()
                .map(Student::getId)
                .filter(id -> id != null && id.startsWith("STU"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }).max().orElse(0);
        return String.format("STU%03d", max + 1);
    }
}
