package ro.ubbcluj.cs.map.repository.PagingRepo;

import ro.ubbcluj.cs.map.domain.Entity;
import ro.ubbcluj.cs.map.repository.Repository;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID, E> {
    Page<E> findAll(Pageable pageable);
}
