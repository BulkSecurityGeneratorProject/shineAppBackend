package com.shine.shineappback.web.rest;

import com.shine.shineappback.ShineAppBackendApp;

import com.shine.shineappback.domain.Currency;
import com.shine.shineappback.repository.CurrencyRepository;
import com.shine.shineappback.service.CurrencyService;
import com.shine.shineappback.service.dto.CurrencyDTO;
import com.shine.shineappback.service.mapper.CurrencyMapper;
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
 * Test class for the CurrencyResource REST controller.
 *
 * @see CurrencyResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShineAppBackendApp.class)
public class CurrencyResourceIntTest {

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_LABEL_EN = "AAAAAAAAAA";
    private static final String UPDATED_LABEL_EN = "BBBBBBBBBB";

    private static final String DEFAULT_LABEL_FR = "AAAAAAAAAA";
    private static final String UPDATED_LABEL_FR = "BBBBBBBBBB";

    @Autowired
    private CurrencyRepository currencyRepository;


    @Autowired
    private CurrencyMapper currencyMapper;
    

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCurrencyMockMvc;

    private Currency currency;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CurrencyResource currencyResource = new CurrencyResource(currencyService);
        this.restCurrencyMockMvc = MockMvcBuilders.standaloneSetup(currencyResource)
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
    public static Currency createEntity(EntityManager em) {
        Currency currency = new Currency()
            .code(DEFAULT_CODE)
            .labelEn(DEFAULT_LABEL_EN)
            .labelFr(DEFAULT_LABEL_FR);
        return currency;
    }

    @Before
    public void initTest() {
        currency = createEntity(em);
    }

    @Test
    @Transactional
    public void createCurrency() throws Exception {
        int databaseSizeBeforeCreate = currencyRepository.findAll().size();

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);
        restCurrencyMockMvc.perform(post("/api/currencies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isCreated());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate + 1);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(DEFAULT_CODE);
        assertThat(testCurrency.getLabelEn()).isEqualTo(DEFAULT_LABEL_EN);
        assertThat(testCurrency.getLabelFr()).isEqualTo(DEFAULT_LABEL_FR);
    }

    @Test
    @Transactional
    public void createCurrencyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = currencyRepository.findAll().size();

        // Create the Currency with an existing ID
        currency.setId(1L);
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCurrencyMockMvc.perform(post("/api/currencies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = currencyRepository.findAll().size();
        // set the field null
        currency.setCode(null);

        // Create the Currency, which fails.
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        restCurrencyMockMvc.perform(post("/api/currencies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllCurrencies() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get all the currencyList
        restCurrencyMockMvc.perform(get("/api/currencies?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(currency.getId().intValue())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())))
            .andExpect(jsonPath("$.[*].labelEn").value(hasItem(DEFAULT_LABEL_EN.toString())))
            .andExpect(jsonPath("$.[*].labelFr").value(hasItem(DEFAULT_LABEL_FR.toString())));
    }
    

    @Test
    @Transactional
    public void getCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        // Get the currency
        restCurrencyMockMvc.perform(get("/api/currencies/{id}", currency.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(currency.getId().intValue()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()))
            .andExpect(jsonPath("$.labelEn").value(DEFAULT_LABEL_EN.toString()))
            .andExpect(jsonPath("$.labelFr").value(DEFAULT_LABEL_FR.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingCurrency() throws Exception {
        // Get the currency
        restCurrencyMockMvc.perform(get("/api/currencies/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Update the currency
        Currency updatedCurrency = currencyRepository.findById(currency.getId()).get();
        // Disconnect from session so that the updates on updatedCurrency are not directly saved in db
        em.detach(updatedCurrency);
        updatedCurrency
            .code(UPDATED_CODE)
            .labelEn(UPDATED_LABEL_EN)
            .labelFr(UPDATED_LABEL_FR);
        CurrencyDTO currencyDTO = currencyMapper.toDto(updatedCurrency);

        restCurrencyMockMvc.perform(put("/api/currencies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isOk());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
        Currency testCurrency = currencyList.get(currencyList.size() - 1);
        assertThat(testCurrency.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testCurrency.getLabelEn()).isEqualTo(UPDATED_LABEL_EN);
        assertThat(testCurrency.getLabelFr()).isEqualTo(UPDATED_LABEL_FR);
    }

    @Test
    @Transactional
    public void updateNonExistingCurrency() throws Exception {
        int databaseSizeBeforeUpdate = currencyRepository.findAll().size();

        // Create the Currency
        CurrencyDTO currencyDTO = currencyMapper.toDto(currency);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restCurrencyMockMvc.perform(put("/api/currencies")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(currencyDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Currency in the database
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCurrency() throws Exception {
        // Initialize the database
        currencyRepository.saveAndFlush(currency);

        int databaseSizeBeforeDelete = currencyRepository.findAll().size();

        // Get the currency
        restCurrencyMockMvc.perform(delete("/api/currencies/{id}", currency.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Currency> currencyList = currencyRepository.findAll();
        assertThat(currencyList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Currency.class);
        Currency currency1 = new Currency();
        currency1.setId(1L);
        Currency currency2 = new Currency();
        currency2.setId(currency1.getId());
        assertThat(currency1).isEqualTo(currency2);
        currency2.setId(2L);
        assertThat(currency1).isNotEqualTo(currency2);
        currency1.setId(null);
        assertThat(currency1).isNotEqualTo(currency2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CurrencyDTO.class);
        CurrencyDTO currencyDTO1 = new CurrencyDTO();
        currencyDTO1.setId(1L);
        CurrencyDTO currencyDTO2 = new CurrencyDTO();
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
        currencyDTO2.setId(currencyDTO1.getId());
        assertThat(currencyDTO1).isEqualTo(currencyDTO2);
        currencyDTO2.setId(2L);
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
        currencyDTO1.setId(null);
        assertThat(currencyDTO1).isNotEqualTo(currencyDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(currencyMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(currencyMapper.fromId(null)).isNull();
    }
}
