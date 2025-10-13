package au.edu.rmit.cosc1295.carehome.repo;

import java.util.*;

public interface Repository<T> {
    Optional<T> findById(String id);
    List<T> findAll();
    void save(T entity);
    void deleteById(String id);
    int size();
}