package com.artansepet.repository;

import com.artansepet.model.User;

import java.util.List;
import java.util.Optional;

/** Kullanici verisine erisim. Verinin nerede tutuldugunu gizler. */
public interface UserRepository {

    void save(User user);

    Optional<User> findById(String id);

    /** Giris yaparken e-posta ile kullanici bulmak icin. */
    Optional<User> findByEmail(String email);

    List<User> findAll();
}