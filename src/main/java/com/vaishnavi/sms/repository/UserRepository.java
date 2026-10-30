package com.vaishnavi.sms.repository;

import com.vaishnavi.sms.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** JSON-backed repository for users.json. */
@Repository
public class UserRepository {

    private final JsonFileStore<User> store;

    public UserRepository(@Value("${app.data-dir:data}") String dataDir) {
        this.store = new JsonFileStore<>(Path.of(dataDir, "users.json"), User.class);
    }

    public List<User> findAll() {
        return store.readAll();
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return findAll().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public Optional<User> findByStudentId(String studentId) {
        return findAll().stream().filter(u -> studentId != null && studentId.equals(u.getStudentId())).findFirst();
    }

    public synchronized User save(User user) {
        List<User> all = store.readAll();
        all.removeIf(u -> u.getId().equals(user.getId()));
        all.add(user);
        store.writeAll(all);
        return user;
    }

    public synchronized void deleteByStudentId(String studentId) {
        List<User> all = store.readAll();
        if (all.removeIf(u -> studentId.equals(u.getStudentId()))) {
            store.writeAll(all);
        }
    }
}
