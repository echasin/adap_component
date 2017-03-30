package com.innvo.repository.search;

import com.innvo.domain.Component;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Component entity.
 */
public interface ComponentSearchRepository extends ElasticsearchRepository<Component, Long> {
}
