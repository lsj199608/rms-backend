package rms.dashboard.service.impl;

import rms.dashboard.entity.TestEntity;
import rms.dashboard.mapper.TestMapper;
import rms.dashboard.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestServiceImpl implements TestService {

	private final TestMapper testMapper;

	@Override
	public List<TestEntity> findAll() {
		return testMapper.findAll();
	}

	@Override
	public TestEntity findById(UUID id) {
		return testMapper.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Not found: id=" + id));
	}

	@Override
	public TestEntity findByCode(String code) {
		return testMapper.findByCode(code)
			.orElseThrow(() -> new IllegalArgumentException("Not found: code=" + code));
	}

	@Override
	@Transactional
	public TestEntity create(TestEntity entity) {
		testMapper.insert(entity);
		return testMapper.findById(entity.getId()).orElse(entity);
	}

	@Override
	@Transactional
	public TestEntity update(TestEntity entity) {
		testMapper.update(entity);
		return findById(entity.getId());
	}

	@Override
	@Transactional
	public void deleteById(UUID id) {
		testMapper.deleteById(id);
	}
}
