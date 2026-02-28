package com.multiverse.pixel.repository;

import com.multiverse.pixel.entity.PixelHistoryEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PixelHistoryRepository extends CassandraRepository<PixelHistoryEntity, String> {
    // Spring Data will auto-implement basic CRUD operations
}