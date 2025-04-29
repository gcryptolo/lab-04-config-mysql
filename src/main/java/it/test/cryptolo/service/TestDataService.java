package it.test.cryptolo.service;

import it.test.cryptolo.entity.TestData;
import it.test.cryptolo.repository.TestDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestDataService {
    private final TestDataRepository testDataRepository;

    public TestDataService(TestDataRepository testDataRepository) {
        this.testDataRepository = testDataRepository;
    }

    public List<TestData> findAll() {
        return testDataRepository.findAll();
    }

    public TestData save(TestData testData) {
        return testDataRepository.save(testData);
    }

    public void deleteById(Long id) {
        testDataRepository.deleteById(id);
    }
}
