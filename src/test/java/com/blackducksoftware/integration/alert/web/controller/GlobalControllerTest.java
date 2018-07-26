package com.blackducksoftware.integration.alert.web.controller;

import java.nio.charset.Charset;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.TestProperties;
import com.blackducksoftware.integration.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;
import com.blackducksoftware.integration.alert.mock.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;

@Category({ DatabaseConnectionTest.class, ExternalConnectionTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class GlobalControllerTest<GE extends DatabaseEntity, GR extends Config, CR extends JpaRepository<GE, Long>> {

    protected final TestProperties testProperties = new TestProperties();
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected MockMvc mockMvc;

    protected Gson gson;

    protected CR globalEntityRepository;

    protected MockGlobalEntityUtil<GE> globalEntityMockUtil;

    protected MockGlobalRestModelUtil<GR> globalRestModelMockUtil;

    protected String restUrl;

    protected GE entity;

    protected GR restModel;

    public abstract CR getGlobalEntityRepository();

    public abstract MockGlobalEntityUtil<GE> getGlobalEntityMockUtil();

    public abstract MockGlobalRestModelUtil<GR> getGlobalRestModelMockUtil();

    public abstract String getRestControllerUrl();

    @Before
    public void setup() {
        gson = new Gson();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();

        globalEntityRepository = getGlobalEntityRepository();
        globalEntityRepository.deleteAllInBatch();

        globalEntityMockUtil = getGlobalEntityMockUtil();
        globalRestModelMockUtil = getGlobalRestModelMockUtil();

        restModel = globalRestModelMockUtil.createGlobalRestModel();
        entity = globalEntityMockUtil.createGlobalEntity();
        globalEntityRepository.save(entity);

        restUrl = BaseController.BASE_PATH + getRestControllerUrl();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetConfig() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(restUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPostConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(restUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPutConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GE savedEntity = globalEntityRepository.save(entity);
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(restUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        restModel.setId(String.valueOf(savedEntity.getId()));
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteConfig() throws Exception {
        globalEntityRepository.deleteAll();
        final GE savedEntity = globalEntityRepository.save(entity);
        final String deleteUrl = restUrl + "?id=" + savedEntity.getId();
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(deleteUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testTestConfig() throws Exception {
        final String testRestUrl = restUrl + "/test";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidateConfig() throws Exception {
        final String testRestUrl = restUrl + "/validate";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(testRestUrl)
                .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                .with(SecurityMockMvcRequestPostProcessors.csrf());
        ;
        request.content(gson.toJson(restModel));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
