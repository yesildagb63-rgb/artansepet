package com.artansepet.repository;

import com.artansepet.model.User;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** UserRepository'nin bellekte (DataStore) calisan hali. */
public class InMemoryUserRepository implements UserRepository {

    private final DataStore store = DataStore.getInstance();

    @Override
    public void save(User user) {
        Objects.requireNonNull(user);
        // Ayni e-posta ile ikinci bir hesap acilamaz
        findByEmail(user.getEmail())
                .filter(existing -> !existing.equals(user))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Bu e-posta zaten kayitli: " + user.getEmail());
                });
        store.users().put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(store.users().get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String aranan = email.toLowerCase(Locale.ROOT);
        return store.users().values().stream()
                .filter(u -> u.getEmail().equals(aranan))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(store.users().values());
    }
}