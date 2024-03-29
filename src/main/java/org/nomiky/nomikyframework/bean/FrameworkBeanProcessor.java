/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.BiMap;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.nomiky.nomikyframework.constant.DaoConstants;
import org.nomiky.nomikyframework.entity.*;
import org.nomiky.nomikyframework.enums.ExecutorEnum;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.executor.SpelParameterConvertor;
import org.nomiky.nomikyframework.util.Checker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * 通过解析xml配置文件，生成controller、executor BEAN，交给spring管理
 *
 * @author nomiky
 * @since 2023年12月21日 11时19分
 */
@Slf4j
public class FrameworkBeanProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationListener<ApplicationStartedEvent> {

    private final JdbcTemplate jdbcTemplate;

    private final FrameworkConfig frameworkConfig;

    private final List<ControllerBeanProcessor> controllerBeanProcessorList = new ArrayList<>();

    public FrameworkBeanProcessor(JdbcTemplate jdbcTemplate, FrameworkConfig frameworkConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.frameworkConfig = frameworkConfig;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        // 生成根据表定义的DaoExecutor，提供CURD基础能力
        Map<String, DaoExecutor> executorMap = initDaoExecutor(registry);

        // 生成controller mapper bean
        initControllerBeans(executorMap);
    }

    private Map<String, DaoExecutor> initDaoExecutor(BeanDefinitionRegistry registry) {
        // 读取tableDefine.xml
        List<XmlTable> xmlTables = getXmlTableFromXml();

        // 解析字段
        TableDefinitionBeanProcessor tableDefinitionBeanProcessor = new TableDefinitionBeanProcessor(jdbcTemplate);
        tableDefinitionBeanProcessor.explainTables(xmlTables);

        // 生成DaoExecutor bean
        Map<String, TableDefinition> tableDefinitionMap = tableDefinitionBeanProcessor.getTableDefinitions();
        Map<String, DaoExecutor> executorMap = new HashMap<>();
        if (MapUtil.isNotEmpty(tableDefinitionMap)) {
            tableDefinitionMap.forEach((tableName, tableDefinition) -> {
                DaoExecutor daoExecutor = DaoExecutorBeanProcessor.getInstance().createSpecifyExecutor(tableDefinition, jdbcTemplate, frameworkConfig);
                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DaoExecutor.class, () -> daoExecutor).getRawBeanDefinition();
                // BEAN名称为tableName
                registry.registerBeanDefinition(tableName + DaoConstants.DAO_EXECUTOR_BEAN_NAME_SUFFIX, beanDefinition);
                executorMap.put(tableName, daoExecutor);
                log.info("Register DaoExecutor Bean: {} with xml file {}", tableName, tableDefinition.getFileName());
            });
        }

        return executorMap;
    }

    private void initControllerBeans(Map<String, DaoExecutor> executorMap) {
        ClassPathResource classPathResource = new ClassPathResource("");
        try {
            File file = classPathResource.getFile();
            File[] files = file.listFiles((dir, name) -> name.endsWith("_controller.xml"));
            if (ArrayUtil.isEmpty(files)) {
                return;
            }

            for (File xmlFile : files) {
                Document document;
                SAXReader reader = new SAXReader();
                document = reader.read(xmlFile);
                // <mapper>
                Map<String, XmlMapper> mappers = fillXmlMapper(document, xmlFile.getAbsolutePath());
                // <interceptors>
                Set<String> interceptors = fillXmlInterceptor(document);
                // <controller>
                List<XmlController> controllers = fillXmlController(document,  xmlFile.getAbsolutePath());
                ControllerBeanProcessor controllerBeanProcessor = new ControllerBeanProcessor();
                controllerBeanProcessor.setControllers(controllers);
                controllerBeanProcessor.setExecutorMap(executorMap);
                controllerBeanProcessor.setInterceptors(interceptors);
                controllerBeanProcessor.setMappers(mappers);
                controllerBeanProcessor.setParameterConverter(new SpelParameterConvertor());
                controllerBeanProcessor.setJdbcTemplate(jdbcTemplate);
                controllerBeanProcessorList.add(controllerBeanProcessor);
            }
        } catch (DocumentException de) {
            throw new BeanCreationException(de.getMessage(), de);
        } catch (Exception e) {
            throw new BeanCreationException("Can not find *_controller.xml!");
        }
    }

    private List<XmlController> fillXmlController(Document document, String xmlFile) {
        List<Node> controllerNodes = document.selectNodes("/controller/controller");
        if (CollUtil.isEmpty(controllerNodes)) {
            return new ArrayList<>(0);
        }

        Node rootNode = document.selectSingleNode("/controller");
        String parentPath = ((DefaultElement) rootNode).attributeValue("path", StrUtil.EMPTY);
        List<XmlController> controllers = new ArrayList<>(controllerNodes.size());
        for (Node controllerNode : controllerNodes) {
            DefaultElement controllerElement = (DefaultElement) controllerNode;
            XmlController controller = fillXmlControllerList(controllerElement, parentPath);
            controller.setFileName(xmlFile);
            controllers.add(controller);
        }
        return controllers;
    }

    private Set<String> fillXmlInterceptor(Document document) {
        Node interceptorNode = document.selectSingleNode("/controller/interceptors");
        if (ObjectUtil.isEmpty(interceptorNode)) {
            return new HashSet<>(0);
        }

        Set<String> interceptorSet = new LinkedHashSet<>();
        List<Node> interceptorNodes = interceptorNode.selectNodes("interceptor");
        if (CollUtil.isNotEmpty(interceptorNodes)) {
            for (Node node : interceptorNodes) {
                interceptorSet.add(node.getText());
            }
        }

        return interceptorSet;
    }

    private Map<String, XmlMapper> fillXmlMapper(Document document, String xmlFile) {
        List<Node> mapperNodes = document.selectNodes("/controller/mapper");
        if (CollUtil.isEmpty(mapperNodes)) {
            return new HashMap<>(0);
        }

        Map<String, XmlMapper> mappers = new HashMap<>(mapperNodes.size());
        for (Node mapperNode : mapperNodes) {
            XmlMapper mapper = new XmlMapper();
            DefaultElement element = (DefaultElement) mapperNode;
            mapper.setId(element.attributeValue("id"));
            mapper.setFileName(xmlFile);
            List<Node> propertyNodes = mapperNode.selectNodes("property");
            if (CollUtil.isEmpty(propertyNodes)) {
                continue;
            }

            BiMap<String, String> propertyMap = new BiMap<>(new HashMap<>());
            for (Node propertyNode : propertyNodes) {
                DefaultElement propertyElement = (DefaultElement) propertyNode;
                propertyMap.put(propertyElement.attributeValue("name"), propertyElement.getText());
            }

            mapper.setAttrToFieldMapper(propertyMap);
            mappers.put(mapper.getId(), mapper);
        }

        return mappers;
    }

    private XmlController fillXmlControllerList(DefaultElement controllerElement, String parentPath) {
        XmlController controller = new XmlController();
        controller.setMethod(controllerElement.attributeValue("method"));
        controller.setPath(parentPath + StrUtil.emptyToDefault(controllerElement.attributeValue("path"), StrUtil.EMPTY));
        controller.setUseTransaction(Boolean.valueOf(controllerElement.attributeValue("useTransaction", "false")));
        List<Node> executorNodes = controllerElement.selectNodes("executor");
        if (CollUtil.isEmpty(executorNodes)) {
            return controller;
        }

        List<XmlExecutor> executors = new ArrayList<>();
        for (Node executorNode : executorNodes) {
            XmlExecutor executor = new XmlExecutor();
            DefaultElement executorElement = (DefaultElement) executorNode;
            // 使用默认DaoExecutor时需要配置的属性
            executor.setParams(executorElement.attributeValue("params"));
            executor.setRef(executorElement.attributeValue("ref"));
            executor.setSchema(executorElement.attributeValue("schema"));
            executor.setMapperRef(executorElement.attributeValue("mapperRef"));

            // 使用自定义SQL语句时需要配置的属性
            executor.setType(executorElement.attributeValue("type", XmlExecutor.TYPE_DEFAULT));
            executor.setEngine(executorElement.attributeValue("engine", XmlExecutor.ENGINE_DEFAULT));
            executor.setOperator(executorElement.attributeValue("operator", XmlExecutor.OPERATOR_SELECT));
            executor.setSqlDefinition(executorElement.getText());
            executors.add(executor);
        }

        controller.setExecutors(executors);
        return controller;
    }

    private List<XmlTable> getXmlTableFromXml() {
        URL fileUrl = ResourceUtil.getResource("tableDefine.xml");
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(fileUrl.getFile());
        } catch (DocumentException e) {
            throw new BeanCreationException("Read tableDefine.xml from classpath fail!!", e);
        }

        // <TableExecutors>
        List<Node> nodes = document.selectNodes("/TableExecutors/table");
        List<XmlTable> xmlTables = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            DefaultElement element = (DefaultElement) node;
            XmlTable xmlTable = new XmlTable();
            String tableName = element.attributeValue("name");
            Checker.checkEmpty(ExecutorEnum.TABLE_NAME_NOT_SPECIFY, tableName);
            xmlTable.setName(tableName);
            xmlTable.setSchema(element.attributeValue("schema"));
            xmlTable.setPrimaryKeyGenerator(element.attributeValue("keyGenerator"));
            xmlTable.setFileName(fileUrl.getFile());
            xmlTables.add(xmlTable);
        }

        return xmlTables;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (CollUtil.isEmpty(this.controllerBeanProcessorList)) {
            return;
        }

        RequestMappingHandlerMapping handlerMapping = event.getApplicationContext().getBean(RequestMappingHandlerMapping.class);
        DataSourceTransactionManager transactionManager = event.getApplicationContext().getBean(DataSourceTransactionManager.class);
        // 生成映射
        for (ControllerBeanProcessor controllerBeanProcessor : this.controllerBeanProcessorList) {
            controllerBeanProcessor.setHandlerMapping(handlerMapping);
            controllerBeanProcessor.setTransactionManager(transactionManager);
            try {
                controllerBeanProcessor.mapHandlerMapping();
            } catch (NoSuchMethodException e) {
                log.error("Mapping handler error: No such method!", e);
            }
        }
    }
}
