package com.shine.shineappback.web.rest;

import com.shine.shineappback.ShineAppBackendApp;

import com.shine.shineappback.domain.ActivityRejection;
import com.shine.shineappback.domain.User;
import com.shine.shineappback.repository.ActivityRejectionRepository;
import com.shine.shineappback.service.ActivityRejectionService;
import com.shine.shineappback.service.dto.ActivityRejectionDTO;
import com.shine.shineappback.service.mapper.ActivityRejectionMapper;
import com.shine.shineappback.web.rest.errors.ExceptionTranslator;

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


import static com.shine.shineappback.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ActivityRejectionResource REST controller.
 *
 * @see ActivityRejectionResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShineAppBackendApp.class)
public class ActivityRejectionResourceIntTest {

    private static final Boolean DEFAULT_REJECTED = false;
    private static final Boolean UPDATED_REJECTED = true;

    private static final Integer DEFAULT_WEEK_NUMBER = 1;
    private static final Integer UPDATED_WEEK_NUMBER = 2;

    private static final Integer DEFAULT_YEAR = 1;
    private static final Integer UPDATED_YEAR = 2;

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

    @Autowired
    private ActivityRejectionRepository activityRejectionRepository;


    @Autowired
    private ActivityRejectionMapper activityRejectionMapper;
    

    @Autowired
    private ActivityRejectionService activityRejectionService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restActivityRejectionMockMvc;

    private ActivityRejection activityRejection;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ActivityRejectionResource activityRejectionResource = new ActivityRejectionResource(activityRejectionService);
        this.restActivityRejectionMockMvc = MockMvcBuilders.standaloneSetup(activityRejectionResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ActivityRejection createEntity(EntityManager em) {
        ActivityRejection activityRejection = new ActivityRejection()
            .rejected(DEFAULT_REJECTED)
            .weekNumber(DEFAULT_WEEK_NUMBER)
            .year(DEFAULT_YEAR)
            .comment(DEFAULT_COMMENT);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        activityRejection.setUser(user);
        return activityRejection;
    }

    @Before
    public void initTest() {
        activityRejection = createEntity(em);
    }

    @Test
    @Transactional
    public void createActivityRejection() throws Exception {
        int databaseSizeBeforeCreate = activityRejectionRepository.findAll().size();

        // Create the ActivityRejection
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(activityRejection);
        restActivityRejectionMockMvc.perform(post("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isCreated());

        // Validate the ActivityRejection in the database
        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeCreate + 1);
        ActivityRejection testActivityRejection = activityRejectionList.get(activityRejectionList.size() - 1);
        assertThat(testActivityRejection.isRejected()).isEqualTo(DEFAULT_REJECTED);
        assertThat(testActivityRejection.getWeekNumber()).isEqualTo(DEFAULT_WEEK_NUMBER);
        assertThat(testActivityRejection.getYear()).isEqualTo(DEFAULT_YEAR);
        assertThat(testActivityRejection.getComment()).isEqualTo(DEFAULT_COMMENT);
    }

    @Test
    @Transactional
    public void createActivityRejectionWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = activityRejectionRepository.findAll().size();

        // Create the ActivityRejection with an existing ID
        activityRejection.setId(1L);
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(activityRejection);

        // An entity with an existing ID cannot be created, so this API call must fail
        restActivityRejectionMockMvc.perform(post("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ActivityRejection in the database
        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkWeekNumberIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRejectionRepository.findAll().size();
        // set the field null
        activityRejection.setWeekNumber(null);

        // Create the ActivityRejection, which fails.
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(activityRejection);

        restActivityRejectionMockMvc.perform(post("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isBadRequest());

        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkYearIsRequired() throws Exception {
        int databaseSizeBeforeTest = activityRejectionRepository.findAll().size();
        // set the field null
        activityRejection.setYear(null);

        // Create the ActivityRejection, which fails.
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(activityRejection);

        restActivityRejectionMockMvc.perform(post("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isBadRequest());

        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllActivityRejections() throws Exception {
        // Initialize the database
        activityRejectionRepository.saveAndFlush(activityRejection);

        // Get all the activityRejectionList
        restActivityRejectionMockMvc.perform(get("/api/activity-rejections?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(activityRejection.getId().intValue())))
            .andExpect(jsonPath("$.[*].rejected").value(hasItem(DEFAULT_REJECTED.booleanValue())))
            .andExpect(jsonPath("$.[*].weekNumber").value(hasItem(DEFAULT_WEEK_NUMBER)))
            .andExpect(jsonPath("$.[*].year").value(hasItem(DEFAULT_YEAR)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT.toString())));
    }
    

    @Test
    @Transactional
    public void getActivityRejection() throws Exception {
        // Initialize the database
        activityRejectionRepository.saveAndFlush(activityRejection);

        // Get the activityRejection
        restActivityRejectionMockMvc.perform(get("/api/activity-rejections/{id}", activityRejection.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(activityRejection.getId().intValue()))
            .andExpect(jsonPath("$.rejected").value(DEFAULT_REJECTED.booleanValue()))
            .andExpect(jsonPath("$.weekNumber").value(DEFAULT_WEEK_NUMBER))
            .andExpect(jsonPath("$.year").value(DEFAULT_YEAR))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingActivityRejection() throws Exception {
        // Get the activityRejection
        restActivityRejectionMockMvc.perform(get("/api/activity-rejections/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateActivityRejection() throws Exception {
        // Initialize the database
        activityRejectionRepository.saveAndFlush(activityRejection);

        int databaseSizeBeforeUpdate = activityRejectionRepository.findAll().size();

        // Update the activityRejection
        ActivityRejection updatedActivityRejection = activityRejectionRepository.findById(activityRejection.getId()).get();
        // Disconnect from session so that the updates on updatedActivityRejection are not directly saved in db
        em.detach(updatedActivityRejection);
        updatedActivityRejection
            .rejected(UPDATED_REJECTED)
            .weekNumber(UPDATED_WEEK_NUMBER)
            .year(UPDATED_YEAR)
            .comment(UPDATED_COMMENT);
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(updatedActivityRejection);

        restActivityRejectionMockMvc.perform(put("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isOk());

        // Validate the ActivityRejection in the database
        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeUpdate);
        ActivityRejection testActivityRejection = activityRejectionList.get(activityRejectionList.size() - 1);
        assertThat(testActivityRejection.isRejected()).isEqualTo(UPDATED_REJECTED);
        assertThat(testActivityRejection.getWeekNumber()).isEqualTo(UPDATED_WEEK_NUMBER);
        assertThat(testActivityRejection.getYear()).isEqualTo(UPDATED_YEAR);
        assertThat(testActivityRejection.getComment()).isEqualTo(UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void updateNonExistingActivityRejection() throws Exception {
        int databaseSizeBeforeUpdate = activityRejectionRepository.findAll().size();

        // Create the ActivityRejection
        ActivityRejectionDTO activityRejectionDTO = activityRejectionMapper.toDto(activityRejection);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restActivityRejectionMockMvc.perform(put("/api/activity-rejections")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(activityRejectionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ActivityRejection in the database
        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteActivityRejection() throws Exception {
        // Initialize the database
        activityRejectionRepository.saveAndFlush(activityRejection);

        int databaseSizeBeforeDelete = activityRejectionRepository.findAll().size();

        // Get the activityRejection
        restActivityRejectionMockMvc.perform(delete("/api/activity-rejections/{id}", activityRejection.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ActivityRejection> activityRejectionList = activityRejectionRepository.findAll();
        assertThat(activityRejectionList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ActivityRejection.class);
        ActivityRejection activityRejection1 = new ActivityRejection();
        activityRejection1.setId(1L);
        ActivityRejection activityRejection2 = new ActivityRejection();
        activityRejection2.setId(activityRejection1.getId());
        assertThat(activityRejection1).isEqualTo(activityRejection2);
        activityRejection2.setId(2L);
        assertThat(activityRejection1).isNotEqualTo(activityRejection2);
        activityRejection1.setId(null);
        assertThat(activityRejection1).isNotEqualTo(activityRejection2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ActivityRejectionDTO.class);
        ActivityRejectionDTO activityRejectionDTO1 = new ActivityRejectionDTO();
        activityRejectionDTO1.setId(1L);
        ActivityRejectionDTO activityRejectionDTO2 = new ActivityRejectionDTO();
        assertThat(activityRejectionDTO1).isNotEqualTo(activityRejectionDTO2);
        activityRejectionDTO2.setId(activityRejectionDTO1.getId());
        assertThat(activityRejectionDTO1).isEqualTo(activityRejectionDTO2);
        activityRejectionDTO2.setId(2L);
        assertThat(activityRejectionDTO1).isNotEqualTo(activityRejectionDTO2);
        activityRejectionDTO1.setId(null);
        assertThat(activityRejectionDTO1).isNotEqualTo(activityRejectionDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(activityRejectionMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(activityRejectionMapper.fromId(null)).isNull();
    }
}
