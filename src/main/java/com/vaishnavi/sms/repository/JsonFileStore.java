package com.vaishnavi.sms.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes a JSON array file using Jackson.
 * <ul>
 *   <li>Creates the directory and file (as {@code []}) if missing.</li>
 *   <li>Empty or corrupted files are recovered: the bad file is renamed to {@code *.corrupt-<time>}
 *       and an empty list is used.</li>
 *   <li>Writes are synchronized and go through a temp file followed by a move, so a crash
 *       cannot leave a half-written file.</li>
 * </ul>
 */
public class JsonFileStore<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonFileStore.class);

    private final Path file;
    private final ObjectMapper mapper;
    private final JavaType listType;

    public JsonFileStore(Path file, Class<T> type) {
        this.file = file;
        this.mapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        this.listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        initialise();
    }

    public Path getFile() {
        return file;
    }

    private synchronized void initialise() {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(file) || Files.size(file) == 0) {
                Files.writeString(file, "[]", StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot initialise data file " + file, e);
        }
    }

    public synchronized List<T> readAll() {
        try {
            if (!Files.exists(file) || Files.size(file) == 0) {
                initialise();
                return new ArrayList<>();
            }
            List<T> items = mapper.readValue(file.toFile(), listType);
            return items == null ? new ArrayList<>() : new ArrayList<>(items);
        } catch (IOException e) {
            recoverCorruptFile(e);
            return new ArrayList<>();
        }
    }

    public synchronized void writeAll(List<T> items) {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            mapper.writeValue(tmp.toFile(), items);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write data file " + file, e);
        }
    }

    private void recoverCorruptFile(IOException cause) {
        Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + System.currentTimeMillis());
        log.warn("Data file {} is corrupted ({}). Backing up to {} and starting empty.",
                file, cause.getMessage(), backup.getFileName());
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(file, "[]", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot recover corrupted data file " + file, e);
        }
    }
}
