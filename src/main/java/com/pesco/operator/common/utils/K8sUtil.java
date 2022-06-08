package com.pesco.operator.common.utils;

import com.pesco.operator.common.crd.resource.Resource;
import com.pesco.operator.common.exception.ConfigmapException;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.pesco.operator.common.KubeConstants.*;

/**
 * @Title k8s工具类
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.common.utils
 * @Description
 * @Date 2022/4/29 11:14 上午
 * @Version V1.0
 */
public class K8sUtil {

    /**
     * 给自定义资源创建 metadata
     */
    public static ObjectMeta createMetadata(String namespace, String name, Map<String, String> labels) {
        var builder = new ObjectMetaBuilder()
                .withName(name)
                .withLabels(labels);
        if (StringUtils.isNotBlank(namespace)) builder.withNamespace(namespace);
        return builder.build();
    }

    /**
     * 获取 context 传输的标签变量
     */
    public static Map<String, String> getContextLabels(Context context) {
        return (Map<String, String>) context.managedDependentResourceContext()
                .getMandatory(LABELS_CONTEXT_KEY, Map.class);
    }

    /**
     * 检测 labels 是否相同
     */
    public static boolean checkLabels(Map<String, String> left, Map<String, String> right) {
        int leftSize = left == null || left.isEmpty() ? 0 : left.size();
        int rightSize = right == null || right.isEmpty() ? 0 : right.size();
        if (leftSize != rightSize) return false;
        return Objects.equals(left, right);
    }

    /**
     * 获取service的域名
     */
    public static String getServiceHost(String namespace, String name) {
        return String.format("%s.%s.svc.cluster.local", name, namespace);
    }

    /**
     * 加密数据
     */
    public static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    /**
     * 解密数据
     */
    public static String decode(String value) {
        return new String(Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    /**
     * 转化configmap的内容为md5
     * 参考链接 https://stackoverflow.com/a/30119004
     */
    public static String getConfigmapMd5(String configmapProps) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(configmapProps));
            return String.format("%032x", new BigInteger(1, md5.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigmapException(e.getMessage(), e);
        }
    }

    /**
     * configmap名称
     */
    public static String hadoopConfigmap(String name) {
        return String.format("%s-configmap", name);
    }

    /**
     * 将文本转化为properties
     */
    public static Properties parsePropertiesString(String s) throws IOException {
        final Properties p = new Properties();
        try (StringReader sr = new StringReader(s)) {
            p.load(sr);
        }
        return p;
    }

    /**
     * 将文件内容转化为properties
     */
    public static Properties parsePropertiesFile(String file) throws IOException {
        final Properties p = new Properties();
        try (InputStream is = K8sUtil.class.getResourceAsStream(file)) {
            p.load(is);
        }
        return p;
    }

    /**
     * 获取资源的配置信息
     */
    public static Map<String, Quantity> getResource(Resource resource) {
        var resources = new HashMap<String, Quantity>();
        if (StringUtils.isNotBlank(resource.getCpu())) {
            resources.put("cpu", Quantity.parse(resource.getCpu()));
        }
        if (StringUtils.isNotBlank(resource.getMemory())) {
            resources.put("memory", Quantity.parse(resource.getMemory()));
        }
        return resources;
    }
}
