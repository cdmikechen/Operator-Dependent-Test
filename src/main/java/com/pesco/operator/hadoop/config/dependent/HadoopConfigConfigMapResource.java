package com.pesco.operator.hadoop.config.dependent;

import com.pesco.operator.common.crd.EnvConfig;
import com.pesco.operator.common.exception.ConfigmapException;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.hadoop.config.crd.HadoopConfig;
import com.pesco.operator.hadoop.config.crd.HadoopConfigSpec;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.jboss.logging.Logger;
import org.w3c.dom.*;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

@ApplicationScoped
public class HadoopConfigConfigMapResource extends KubernetesDependentResource<ConfigMap, HadoopConfig> implements
        Creator<ConfigMap, HadoopConfig>,
        Updater<ConfigMap, HadoopConfig>,
        Matcher<ConfigMap, HadoopConfig> {

    private static final Logger LOGGER = Logger.getLogger(HadoopConfigConfigMapResource.class);

    public HadoopConfigConfigMapResource() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(HadoopConfig primary, Context<HadoopConfig> context) {
        final var spec = primary.getSpec();
        final var namespace = primary.getMetadata().getNamespace();
        final var name = primary.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);

        var configmapBuilder = new ConfigMapBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, K8sUtil.hadoopConfigmap(name), labels));
        String content = getContent(spec);

        ConfigMap configMap = configmapBuilder.addToData(spec.getFileName(), content).build();
        LOGGER.infov("??????/?????? configmap {0}/{1}", namespace, configMap.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("?????? {0} yaml = \n{1}", ConfigMap.class.getName(), YamlUtil.toPrettyYaml(configMap));
        }
        return configMap;
    }

    /**
     * ??????????????????????????????
     */
    public static String getContent(HadoopConfigSpec spec) {
        var content = spec.getContent();
        var overwrites = spec.getOverwrites();
        // ???????????????????????????????????????????????????
        if (overwrites != null && !overwrites.isEmpty()) {
            var type = spec.getType();
            switch (type) {
                case hadoop_xml:
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    try (InputStream is = new ByteArrayInputStream(content.replaceAll("\n|\r", "").getBytes())) {
                        DocumentBuilder db = dbf.newDocumentBuilder();
                        Document document = db.parse(is);
                        // configuration
                        Element root = document.getDocumentElement();
                        // property list?????????????????????????????????
                        NodeList propList = root.getChildNodes();
                        // ??????????????????????????????????????? property list
                        for (EnvConfig ow : overwrites) {
                            boolean replaced = false;
                            for (int i = 0, size1 = propList.getLength(); i < size1; i++) {
                                Node prop = propList.item(i);
                                if (prop.getNodeType() != Node.ELEMENT_NODE) continue;
                                NodeList propNodes = prop.getChildNodes();
                                // ????????????????????? name ??? value
                                int nameIndex = 0;
                                int valueIndex = 0;
                                for (int j = 0, size2 = propNodes.getLength(); j < size2; j++) {
                                    Node kv = propNodes.item(j);
                                    if ("name".equals(kv.getNodeName())) {
                                        nameIndex = j;
                                    } else if ("value".equals(kv.getNodeName())) {
                                        valueIndex = j;
                                    }
                                }
                                // ???????????????????????????
                                if (ow.getName().equals(propNodes.item(nameIndex).getChildNodes().item(0).getNodeValue())) {
                                    propNodes.item(valueIndex).getChildNodes().item(0).setNodeValue(ow.getValue());
                                    replaced = true;
                                    break;
                                }
                            }
                            // ????????????????????????????????????
                            if (!replaced) {
                                Element name = document.createElement("name");
                                name.setTextContent(ow.getName());
                                Element value = document.createElement("value");
                                value.setTextContent(ow.getValue());
                                Element property = document.createElement("property");
                                property.appendChild(name);
                                property.appendChild(value);
                                root.appendChild(property);
                            }
                        }
                        // ????????????
                        root.normalize();
                        XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
                        NodeList blankTextNodes = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
                        for (int i = 0; i < blankTextNodes.getLength(); i++) {
                            blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
                        }

                        // ???????????????????????????xml
                        TransformerFactory tf = TransformerFactory.newInstance();
                        Transformer transformer = tf.newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(root), new StreamResult(writer));
                        return writer.getBuffer().toString();
                    } catch (Exception e) {
                        throw new ConfigmapException("??????xml?????????" + e.getMessage(), e);
                    }
                default:
                    throw new ConfigmapException("????????????????????? " + type + " ??????????????????");
            }
        }
        return content;
    }

    @Override
    public Result<ConfigMap> match(ConfigMap actual, HadoopConfig primary, Context<HadoopConfig> context) {
        // ??????????????????configmap?????? ????????????
        var spec = primary.getSpec();
        var content = getContent(spec);
        if (!content.equals(actual.getData().get(spec.getFileName()))) {
            LOGGER.infov("?????? {0} ???????????????????????????????????????", spec.getFileName());
            return Result.nonComputed(false);
        } else return Result.nonComputed(true);
    }
}
