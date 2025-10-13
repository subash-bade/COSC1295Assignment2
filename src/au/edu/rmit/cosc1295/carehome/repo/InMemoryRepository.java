package au.edu.rmit.cosc1295.carehome.repo;

import java.io.Serializable;
import java.util.*;

/**
 * Abstract Map-backed repository that is Serializable.
 * Subclasses must provide getId(entity) so we avoid non-serializable lambdas.
 */
public abstract class InMemoryRepository<T> implements Repository<T>, Serializable {
    private static final long serialVersionUID = 1L;

    protected final Map<String, T> store = new LinkedHashMap<>();

    protected abstract String getId(T entity);

    @Override public Optional<T> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<T> findAll() { return new ArrayList<>(store.values()); }
    @Override public void save(T entity) { store.put(getId(entity), entity); }
    @Override public void deleteById(String id) { store.remove(id); }
    @Override public int size() { return store.size(); }
}