package rms.dashboard.mapper;

import rms.dashboard.entity.TestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface TestMapper {

	List<TestEntity> findAll();

	Optional<TestEntity> findById(@Param("id") UUID id);

	Optional<TestEntity> findByCode(@Param("code") String code);

	int insert(TestEntity entity);

	int update(TestEntity entity);

	int deleteById(@Param("id") UUID id);
}
