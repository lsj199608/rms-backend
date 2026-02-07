package rms.dashboard.service;

import rms.dashboard.entity.TestEntity;

import java.util.List;
import java.util.UUID;

public interface TestService {

	List<TestEntity> findAll();

	TestEntity findById(UUID id);

	TestEntity findByCode(String code);

	TestEntity create(TestEntity entity);

	TestEntity update(TestEntity entity);

	void deleteById(UUID id);
}
