package com.vaishnavi.sms.repository;

import com.vaishnavi.sms.model.Attendance;
import com.vaishnavi.sms.model.AttendanceHistory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** JSON-backed repository for attendance.json and attendance_history.json. */
@Repository
public class AttendanceRepository {

    private final JsonFileStore<Attendance> store;
    private final JsonFileStore<AttendanceHistory> historyStore;

    public AttendanceRepository(@Value("${app.data-dir:data}") String dataDir) {
        this.store = new JsonFileStore<>(Path.of(dataDir, "attendance.json"), Attendance.class);
        this.historyStore = new JsonFileStore<>(Path.of(dataDir, "attendance_history.json"), AttendanceHistory.class);
    }

    public List<Attendance> findAll() {
        return store.readAll();
    }

    public Optional<Attendance> findByStudentId(String studentId) {
        return findAll().stream().filter(a -> a.getStudentId().equals(studentId)).findFirst();
    }

    public synchronized Attendance save(Attendance attendance) {
        List<Attendance> all = store.readAll();
        all.removeIf(a -> a.getStudentId().equals(attendance.getStudentId()));
        all.add(attendance);
        store.writeAll(all);
        return attendance;
    }

    /** Saves the new attendance value and its history entry together under one lock. */
    public synchronized void saveWithHistory(Attendance attendance, AttendanceHistory history) {
        save(attendance);
        saveHistory(history);
    }

    public synchronized void saveHistory(AttendanceHistory history) {
        List<AttendanceHistory> all = historyStore.readAll();
        all.add(history);
        historyStore.writeAll(all);
    }

    /** History of one student, newest first. */
    public List<AttendanceHistory> findHistory(String studentId) {
        return historyStore.readAll().stream()
                .filter(h -> h.getStudentId().equals(studentId))
                .sorted(Comparator.comparing(AttendanceHistory::getTimestamp).reversed())
                .toList();
    }

    public synchronized void deleteByStudentId(String studentId) {
        List<Attendance> all = store.readAll();
        if (all.removeIf(a -> a.getStudentId().equals(studentId))) {
            store.writeAll(all);
        }
        List<AttendanceHistory> history = historyStore.readAll();
        if (history.removeIf(h -> h.getStudentId().equals(studentId))) {
            historyStore.writeAll(history);
        }
    }
}
