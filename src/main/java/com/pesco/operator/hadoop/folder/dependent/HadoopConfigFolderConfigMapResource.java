package com.pesco.operator.hadoop.folder.dependent;

import com.pesco.operator.common.exception.ConfigmapException;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.hadoop.config.crd.HadoopConfig;
import com.pesco.operator.hadoop.folder.crd.HadoopConfigFolder;
import com.pesco.operator.hadoop.folder.crd.HadoopConfigFolderSpec;
import com.pesco.operator.hadoop.folder.crd.HadoopConfigResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Title HadoopConfig文件夹资源处理
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder.dependent
 * @Description 会读取每个资源，并生成一个统一的configmap
 * @Date 2022/6/7 3:37 下午
 * @Version V1.0
 */

@ApplicationScoped
public class HadoopConfigFolderConfigMapResource extends KubernetesDependentResource<ConfigMap, HadoopConfigFolder> implements
        Creator<ConfigMap, HadoopConfigFolder>,
        Updater<ConfigMap, HadoopConfigFolder>,
        Matcher<ConfigMap, HadoopConfigFolder> {

    private static final Logger LOGGER = Logger.getLogger(HadoopConfigFolderConfigMapResource.class);

    public HadoopConfigFolderConfigMapResource() {
        super(ConfigMap.class);
    }

    @Inject
    KubernetesClient client;

    @Override
    protected ConfigMap desired(HadoopConfigFolder primary, Context<HadoopConfigFolder> context) {
        // 找每个content，并进行汇总
        var namespace = primary.getMetadata().getNamespace();
        final var name = primary.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);
        var configmap = String.format("%s-hadoopfolder", name);

        var configmapBuilder = new ConfigMapBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, configmap, labels));
        var datas = getContent(primary.getSpec(), namespace);

        ConfigMap configMap = configmapBuilder.addToData(datas).build();
        LOGGER.infov("创建/修改 configmap {0}/{1}", namespace, configMap.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", Service.class.getName(), YamlUtil.toPrettyYaml(configMap));
        }
        return configMap;
    }

    /**
     * 获取拼接的configmap内容
     */
    private Map<String, String> getContent(HadoopConfigFolderSpec spec, String namespace) {
        var configs = spec.getConfig();
        if (configs == null || configs.isEmpty()) {
            throw new ConfigmapException("找不到对应的 HadoopConfig 资源列表！");
        }
        var datas = new LinkedHashMap<String, String>();
        var api = client.resources(HadoopConfig.class);

        for (HadoopConfigResource resource : configs) {
            // 找资源
            var defaultNamespace = StringUtils.isBlank(resource.getNamespace()) ? namespace : resource.getNamespace();
            var hadoopConfig = api.inNamespace(defaultNamespace)
                    .withName(resource.getName())
                    .get();
            if (hadoopConfig == null) {
                throw new ConfigmapException(String.format("找不到对应的 HadoopConfig 资源信息！%s/%s", resource.getNamespace(), resource.getName()));
            } else if (hadoopConfig.getStatus() == null || !hadoopConfig.getStatus().isSuccess()) {
                throw new ConfigmapException(String.format("对应的 HadoopConfig 资源信息未创建！%s/%s", resource.getNamespace(), resource.getName()));
            }
            // 找configmap
            var configmap = client.configMaps()
                    .inNamespace(hadoopConfig.getMetadata().getNamespace())
                    .withName(hadoopConfig.getStatus().getResourceName())
                    .get();
            if (configmap == null) {
                throw new ConfigmapException(String.format("找不到对应的 Configmap 资源信息！%s/%s",
                        hadoopConfig.getMetadata().getNamespace(), hadoopConfig.getStatus().getResourceName()));
            }
            datas.putAll(configmap.getData());
        }
        return datas;
    }

    @Override
    public Result<ConfigMap> match(ConfigMap actual, HadoopConfigFolder primary, Context<HadoopConfigFolder> context) {
        // 这里主要判断configmap的值 是否一致
        var spec = primary.getSpec();
        var namespace = primary.getMetadata().getNamespace();

        var content = getContent(spec, namespace);
        if (!content.equals(actual.getData())) {
            LOGGER.info("HadoopConfigFolder 内容发生了修改，需要进行更新！");
            return Result.nonComputed(false);
        } else return Result.nonComputed(true);
    }
}
