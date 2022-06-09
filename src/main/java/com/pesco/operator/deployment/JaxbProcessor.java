package com.pesco.operator.deployment;

import java.io.IOError;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import org.jboss.jandex.DotName;

import io.quarkus.deployment.ApplicationArchive;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationArchivesBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageSystemPropertyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyIgnoreWarningBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

/**
 * @Title 处理xml解析问题
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.processor
 * @Description 参考自 quarkus 项目的 jaxb 扩展
 * @Date 2022/6/9 1:44 下午
 * @Version V1.0
 */
public class JaxbProcessor {

    private static final List<Class<?>> JAXB_REFLECTIVE_CLASSES = List.of(XmlAccessOrder.class);

    private static final List<DotName> IGNORE_TYPES = List.of(DotName.createSimple("javax.xml.datatype.XMLGregorianCalendar"));

    @BuildStep
    void ignoreWarnings(BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarningProducer) {
        for (DotName type : IGNORE_TYPES) {
            ignoreWarningProducer.produce(new ReflectiveHierarchyIgnoreWarningBuildItem(type));
        }
    }

    @BuildStep
    void registerClasses(
            BuildProducer<NativeImageSystemPropertyBuildItem> nativeImageProps,
            BuildProducer<ServiceProviderBuildItem> providerItem, final BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            final BuildProducer<NativeImageResourceBundleBuildItem> resourceBundle) {

        addReflectiveClass(reflectiveClass, true, false, "com.sun.xml.bind.v2.ContextFactory");
        addReflectiveClass(reflectiveClass, true, false, "com.sun.xml.internal.bind.v2.ContextFactory");

        addReflectiveClass(reflectiveClass, true, false, "com.sun.xml.internal.stream.XMLInputFactoryImpl");
        addReflectiveClass(reflectiveClass, true, false, "com.sun.xml.internal.stream.XMLOutputFactoryImpl");
        addReflectiveClass(reflectiveClass, true, false, "com.sun.org.apache.xpath.internal.functions.FuncNot");
        addReflectiveClass(reflectiveClass, true, false, "com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace");
        addReflectiveClass(reflectiveClass, true, false, "com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl");
        addResourceBundle(resourceBundle, "javax.xml.bind.Messages");
        addResourceBundle(resourceBundle, "javax.xml.bind.helpers.Messages");

        nativeImageProps
                .produce(new NativeImageSystemPropertyBuildItem("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true"));

        JAXB_REFLECTIVE_CLASSES.stream()
                .map(Class::getName)
                .forEach(className -> addReflectiveClass(reflectiveClass, true, false, className));

        providerItem
                .produce(new ServiceProviderBuildItem(JAXBContext.class.getName(), "com.sun.xml.bind.v2.ContextFactory"));
    }

    private void addReflectiveClass(BuildProducer<ReflectiveClassBuildItem> reflectiveClass, boolean methods, boolean fields,
                                    String... className) {
        reflectiveClass.produce(new ReflectiveClassBuildItem(methods, fields, className));
    }

    private void addResourceBundle(BuildProducer<NativeImageResourceBundleBuildItem> resourceBundle, String bundle) {
        resourceBundle.produce(new NativeImageResourceBundleBuildItem(bundle));
    }
}
