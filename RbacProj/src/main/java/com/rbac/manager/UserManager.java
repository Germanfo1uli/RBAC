package com.rbac.manager;

import com.rbac.filter.user.UserFilter;
import com.rbac.model.User;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @Override
    public void add(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        synchronized (lock) {
            if (users.containsKey(user.username())) {
                throw new IllegalArgumentException("User with username " + user.username() + " already exists");
            }
            users.put(user.username(), user);
        }
    }

    @Override
    public boolean remove(User user) {
        if (user == null) return false;
        synchronized (lock) {
            return users.remove(user.username()) != null;
        }
    }

    @Override
    public Optional<User> findById(String id) {
        return findByUsername(id);
    }

    @Override
    public List<User> findAll() {
        synchronized (lock) {
            return new ArrayList<>(users.values());
        }
    }

    @Override
    public int count() {
        synchronized (lock) {
            return users.size();
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            users.clear();
        }
    }

    public Optional<User> findByUsername(String username) {
        synchronized (lock) {
            return Optional.ofNullable(users.get(username));
        }
    }

    public Optional<User> findByEmail(String email) {
        synchronized (lock) {
            return users.values().stream()
                    .filter(u -> u.email().equalsIgnoreCase(email))
                    .findFirst();
        }
    }

    public List<User> findByFilter(UserFilter filter) {
        synchronized (lock) {
            return users.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        }
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        synchronized (lock) {
            return users.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        }
    }

    public boolean exists(String username) {
        synchronized (lock) {
            return users.containsKey(username);
        }
    }

    public void update(String username, String newFullName, String newEmail) {
        synchronized (lock) {
            User existingUser = users.get(username);
            if (existingUser == null) {
                throw new NoSuchElementException("User not found: " + username);
            }

            User updatedUser = User.validate(username, newFullName, newEmail);
            users.put(username, updatedUser);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
