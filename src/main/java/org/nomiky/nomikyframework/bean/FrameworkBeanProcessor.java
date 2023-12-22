/*
 * Copyright (c) 2022-2032 NOMIKY
 * 不能修改和删除上面的版权声明
 * 此代码属于NOMIKY编写，在未经允许的情况下不得传播复制
 */
package org.nomiky.nomikyframework.bean;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.map.MapUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.nomiky.nomikyframework.entity.TableDefinition;
import org.nomiky.nomikyframework.entity.XmlController;
import org.nomiky.nomikyframework.entity.XmlMapper;
import org.nomiky.nomikyframework.entity.XmlTable;
import org.nomiky.nomikyframework.enums.ExecutorEnum;
import org.nomiky.nomikyframework.executor.DaoExecutor;
import org.nomiky.nomikyframework.util.Checker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 通过xml配置文件，生成controller、executor BEAN，交给spring管理
 *
 * @author nomiky
 * @since 2023年12月21日 11时19分
 */
@Slf4j
public class FrameworkBeanProcessor implements BeanDefinitionRegistryPostProcessor {

    private final JdbcTemplate jdbcTemplate;

    @Lazy
    @Resource
    private RequestMappingHandlerMapping handlerMapping;

    @PostConstruct
    public void initHandlerMapping() {

    }

    public FrameworkBeanProcessor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 读取tableDefine.xml
        List<XmlTable> xmlTables = getXmlTableFromXml();

        // 解析字段
        TableDefinitionBeanProcessor tableDefinitionBeanProcessor = new TableDefinitionBeanProcessor(jdbcTemplate);
        tableDefinitionBeanProcessor.explainTables(xmlTables);

        // 生成DaoExecutor bean
        Map<String, TableDefinition> tableDefinitionMap = tableDefinitionBeanProcessor.getTableDefinitions();
        if (MapUtil.isNotEmpty(tableDefinitionMap)) {
            tableDefinitionMap.forEach((tableName, tableDefinition) -> {
                DaoExecutor daoExecutor = DaoExecutorBeanProcessor.getInstance().createSpecifyExecutor(tableDefinition, jdbcTemplate);
                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DaoExecutor.class, () -> daoExecutor).getRawBeanDefinition();
                // BEAN名称为tableName
                registry.registerBeanDefinition(tableName, beanDefinition);
                log.info("Register DaoExecutor Bean: {}", tableName);
            });
        }

        // 生成controller mapper

    }

    private Pair<List<XmlMapper>, List<XmlController>> getXmlControllerFromXml(){
        URL fileUrl = ResourceUtil.getResource("tableDefine.xml");
        ClassPathResource classPathResource = new ClassPathResource("");
        try {
            File file = classPathResource.getFile();
            File[] files = file.listFiles((dir, name) -> name.endsWith("_controller.xml"));
            for (File xmlFile : files) {

            }
        } catch (Exception e) {
            throw new BeanCreationException("Can not find *_controller.xml!");
        }

        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(fileUrl.getFile());
        } catch (DocumentException e) {
            throw new BeanCreationException("Read tableDefine.xml from classpath fail!!", e);
        }

        return null;
    }

    private List<XmlTable> getXmlTableFromXml() {
        //        File file = ResourceUtils.getFile("tableDefine.xml");
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
            xmlTables.add(xmlTable);
        }

        return xmlTables;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
