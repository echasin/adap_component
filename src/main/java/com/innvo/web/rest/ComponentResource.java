package com.innvo.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.innvo.domain.Component;

import com.innvo.repository.ComponentRepository;
import com.innvo.repository.search.ComponentSearchRepository;
import com.innvo.web.rest.util.HeaderUtil;
import com.innvo.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Component.
 */
@RestController
@RequestMapping("/api")
public class ComponentResource {

    private final Logger log = LoggerFactory.getLogger(ComponentResource.class);

    private static final String ENTITY_NAME = "component";
        
    private final ComponentRepository componentRepository;

    private final ComponentSearchRepository componentSearchRepository;

    public ComponentResource(ComponentRepository componentRepository, ComponentSearchRepository componentSearchRepository) {
        this.componentRepository = componentRepository;
        this.componentSearchRepository = componentSearchRepository;
    }

    /**
     * POST  /components : Create a new component.
     *
     * @param component the component to create
     * @return the ResponseEntity with status 201 (Created) and with body the new component, or with status 400 (Bad Request) if the component has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/components")
    @Timed
    public ResponseEntity<Component> createComponent(@Valid @RequestBody Component component) throws URISyntaxException {
        log.debug("REST request to save Component : {}", component);
        if (component.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new component cannot already have an ID")).body(null);
        }
        Component result = componentRepository.save(component);
        componentSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/components/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /components : Updates an existing component.
     *
     * @param component the component to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated component,
     * or with status 400 (Bad Request) if the component is not valid,
     * or with status 500 (Internal Server Error) if the component couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/components")
    @Timed
    public ResponseEntity<Component> updateComponent(@Valid @RequestBody Component component) throws URISyntaxException {
        log.debug("REST request to update Component : {}", component);
        if (component.getId() == null) {
            return createComponent(component);
        }
        Component result = componentRepository.save(component);
        componentSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, component.getId().toString()))
            .body(result);
    }

    /**
     * GET  /components : get all the components.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of components in body
     */
    @GetMapping("/components")
    @Timed
    public ResponseEntity<List<Component>> getAllComponents(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Components");
        Page<Component> page = componentRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/components");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /components/:id : get the "id" component.
     *
     * @param id the id of the component to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the component, or with status 404 (Not Found)
     */
    @GetMapping("/components/{id}")
    @Timed
    public ResponseEntity<Component> getComponent(@PathVariable Long id) {
        log.debug("REST request to get Component : {}", id);
        Component component = componentRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(component));
    }

    /**
     * DELETE  /components/:id : delete the "id" component.
     *
     * @param id the id of the component to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/components/{id}")
    @Timed
    public ResponseEntity<Void> deleteComponent(@PathVariable Long id) {
        log.debug("REST request to delete Component : {}", id);
        componentRepository.delete(id);
        componentSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/components?query=:query : search for the component corresponding
     * to the query.
     *
     * @param query the query of the component search 
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/components")
    @Timed
    public ResponseEntity<List<Component>> searchComponents(@RequestParam String query, @ApiParam Pageable pageable) {
        log.debug("REST request to search for a page of Components for query {}", query);
        Page<Component> page = componentSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/components");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


}
