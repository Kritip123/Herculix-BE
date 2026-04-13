package org.example.herculix.repository;

import org.example.herculix.entity.TrainingSubcategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSubcategoryRepository extends MongoRepository<TrainingSubcategory, String> {

    List<TrainingSubcategory> findByCategoryIdAndActiveTrueOrderByDisplayOrderAsc(String categoryId);

    List<TrainingSubcategory> findByActiveTrueOrderByDisplayOrderAsc();

    List<TrainingSubcategory> findByCategoryIdIn(List<String> categoryIds);

    boolean existsByNameAndCategoryId(String name, String categoryId);
}
