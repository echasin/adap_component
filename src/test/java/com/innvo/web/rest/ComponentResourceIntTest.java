package com.innvo.web.rest;

import com.innvo.AdapcomponentApp;

import com.innvo.domain.Component;
import com.innvo.repository.ComponentRepository;
import com.innvo.repository.search.ComponentSearchRepository;
import com.innvo.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ComponentResource REST controller.
 *
 * @see ComponentResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdapcomponentApp.class)
public class ComponentResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ComponentSearchRepository componentSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restComponentMockMvc;

    private Component component;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ComponentResource componentResource = new ComponentResource(componentRepository, componentSearchRepository);
        this.restComponentMockMvc = MockMvcBuilders.standaloneSetup(componentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Component createEntity(EntityManager em) {
        Component component = new Component()
            .name(DEFAULT_NAME);
        return component;
    }

    @Before
    public void initTest() {
        componentSearchRepository.deleteAll();
        component = createEntity(em);
    }

    @Test
    @Transactional
    public void createComponent() throws Exception {
        int databaseSizeBeforeCreate = componentRepository.findAll().size();

        // Create the Component
        restComponentMockMvc.perform(post("/api/components")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(component)))
            .andExpect(status().isCreated());

        // Validate the Component in the database
        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeCreate + 1);
        Component testComponent = componentList.get(componentList.size() - 1);
        assertThat(testComponent.getName()).isEqualTo(DEFAULT_NAME);

        // Validate the Component in Elasticsearch
        Component componentEs = componentSearchRepository.findOne(testComponent.getId());
        assertThat(componentEs).isEqualToComparingFieldByField(testComponent);
    }

    @Test
    @Transactional
    public void createComponentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = componentRepository.findAll().size();

        // Create the Component with an existing ID
        component.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restComponentMockMvc.perform(post("/api/components")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(component)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = componentRepository.findAll().size();
        // set the field null
        component.setName(null);

        // Create the Component, which fails.

        restComponentMockMvc.perform(post("/api/components")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(component)))
            .andExpect(status().isBadRequest());

        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllComponents() throws Exception {
        // Initialize the database
        componentRepository.saveAndFlush(component);

        // Get all the componentList
        restComponentMockMvc.perform(get("/api/components?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(component.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getComponent() throws Exception {
        // Initialize the database
        componentRepository.saveAndFlush(component);

        // Get the component
        restComponentMockMvc.perform(get("/api/components/{id}", component.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(component.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingComponent() throws Exception {
        // Get the component
        restComponentMockMvc.perform(get("/api/components/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateComponent() throws Exception {
        // Initialize the database
        componentRepository.saveAndFlush(component);
        componentSearchRepository.save(component);
        int databaseSizeBeforeUpdate = componentRepository.findAll().size();

        // Update the component
        Component updatedComponent = componentRepository.findOne(component.getId());
        updatedComponent
            .name(UPDATED_NAME);

        restComponentMockMvc.perform(put("/api/components")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedComponent)))
            .andExpect(status().isOk());

        // Validate the Component in the database
        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeUpdate);
        Component testComponent = componentList.get(componentList.size() - 1);
        assertThat(testComponent.getName()).isEqualTo(UPDATED_NAME);

        // Validate the Component in Elasticsearch
        Component componentEs = componentSearchRepository.findOne(testComponent.getId());
        assertThat(componentEs).isEqualToComparingFieldByField(testComponent);
    }

    @Test
    @Transactional
    public void updateNonExistingComponent() throws Exception {
        int databaseSizeBeforeUpdate = componentRepository.findAll().size();

        // Create the Component

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restComponentMockMvc.perform(put("/api/components")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(component)))
            .andExpect(status().isCreated());

        // Validate the Component in the database
        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteComponent() throws Exception {
        // Initialize the database
        componentRepository.saveAndFlush(component);
        componentSearchRepository.save(component);
        int databaseSizeBeforeDelete = componentRepository.findAll().size();

        // Get the component
        restComponentMockMvc.perform(delete("/api/components/{id}", component.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean componentExistsInEs = componentSearchRepository.exists(component.getId());
        assertThat(componentExistsInEs).isFalse();

        // Validate the database is empty
        List<Component> componentList = componentRepository.findAll();
        assertThat(componentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchComponent() throws Exception {
        // Initialize the database
        componentRepository.saveAndFlush(component);
        componentSearchRepository.save(component);

        // Search the component
        restComponentMockMvc.perform(get("/api/_search/components?query=id:" + component.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(component.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Component.class);
    }
}
