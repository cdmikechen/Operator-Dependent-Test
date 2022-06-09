package com.pesco.operator.ranger.dependent;

import com.pesco.operator.common.crd.EnvConfig;
import com.pesco.operator.common.exception.ConfigmapException;
import com.pesco.operator.common.exception.DeploymentException;
import com.pesco.operator.common.utils.CollectionUtils;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.hadoop.folder.dependent.HadoopConfigFolderConfigMapResource;
import com.pesco.operator.ranger.RangerReconciler;
import com.pesco.operator.ranger.crd.Ranger;
import com.pesco.operator.ranger.crd.RangerSpec;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
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
import java.util.*;

import static com.pesco.operator.common.KubeConstants.*;

/**
 * @Title ranger deployment 服务
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.dependent
 * @Description
 * @Date 2022/6/8 12:57 下午
 * @Version V1.0
 */

@ApplicationScoped
public class RangerDeploymentResource extends KubernetesDependentResource<Deployment, Ranger> implements
        Creator<Deployment, Ranger>,
        Updater<Deployment, Ranger>,
        Matcher<Deployment, Ranger> {

    public RangerDeploymentResource() {
        super(Deployment.class);
    }

    private static final Logger LOGGER = Logger.getLogger(RangerDeploymentResource.class);

    @Inject
    KubernetesClient client;

    @Override
    protected Deployment desired(Ranger ranger, Context<Ranger> context) {
        final var spec = ranger.getSpec();
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);
        var deployment = getDeployment(spec, namespace, name, labels, context);

        // 返回
        LOGGER.infov("创建/修改 ranger-admin-site.xml {0}/{1}", namespace, deployment.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", Deployment.class.getName(), YamlUtil.toPrettyYaml(deployment));
        }
        return deployment;
    }

    /**
     * 创建新的deployment
     */
    private Deployment getDeployment(RangerSpec spec, String namespace, String name, Map<String, String> labels, Context<Ranger> context) {
        // hostAliases
        List<HostAlias> hostAliases = new ArrayList<>();
        if (spec.getHostAliases() != null && !spec.getHostAliases().isEmpty()) {
            spec.getHostAliases().forEach(h -> {
                hostAliases.add(new HostAliasBuilder().withHostnames(h.getHostnames()).withIp(h.getIp()).build());
            });
        }

        // 查找configmap，并写入 spec.template.metadata.annotations.xxx-config-hash
        // 这样做的目的是为了能让每次更新配置后自动重启pod，进而保证pod的时效性
        var hashMap = getConfigHashMap(spec, context);

        // 初始化container
        var containerBuilder = new DeploymentBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, String.format("%s-ranger-admin", name), labels))
                .withNewSpec()
                // replicas
                .withReplicas(spec.getReplicas())
                .withNewSelector().withMatchLabels(labels).endSelector()
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(labels)
                .withAnnotations(hashMap)
                .endMetadata()
                .withNewSpec()
                // serviceAccountName
                .withServiceAccountName(spec.getServiceAccount())
                // hostAliases
                .withHostAliases(hostAliases)
                .addNewContainer()
                // container配置
                .withName("ranger")
                .withImage(getImage(spec.getImage(), spec.getVersion()))
                .withImagePullPolicy(spec.getImagePullPolicy().toString())
                .addNewCommand("/opt/mr3-run/ranger/start-ranger.sh");
        // 添加 env 环境变量
        if (spec.getEnv() != null) {
            spec.getEnv().forEach(env -> containerBuilder.addNewEnv()
                    .withName(env.getName())
                    .withValue(env.getValue())
                    .endEnv());
        }
        // 设置初始化initservice配置
        if (spec.getInitService() != null && spec.getInitService().getEnable()) {
            containerBuilder.addNewEnv().withName("RANGER_SYNC_SERVICE").withValue("true").endEnv();
        }
        // resources
        if (spec.getResources() != null) {
            var resources = spec.getResources();
            if (resources.getRequests() != null) {
                containerBuilder.withNewResources()
                        .addToRequests(K8sUtil.getResource(resources.getRequests()))
                        .endResources();
            }
            if (resources.getLimits() != null) {
                containerBuilder.withNewResources()
                        .addToLimits(K8sUtil.getResource(resources.getLimits()))
                        .endResources();
            }
        }
        // 添加端口，然后生成最终的deployment
        // 端口配置为从 ranger.service.http.port 和 ranger.service.https.port 采集的
        // 这个变量从 install.properties 里面获取不到，所以需要从 overwrite 里面找配置，找不到则按照默认启动
        var overwrite = spec.getOverwriteConfigs();
        int httpPort = RangerServiceResource.DEFAULT_HTTP_PORT;
        int httpsPort = RangerServiceResource.DEFAULT_HTTPS_PORT;
        if (overwrite != null && !overwrite.isEmpty()) {
            for (EnvConfig ow : overwrite) {
                if ("ranger.service.http.port".equals(ow.getName())) {
                    httpPort = Integer.parseInt(ow.getValue());
                } else if ("ranger.service.https.port".equals(ow.getName())) {
                    httpsPort = Integer.parseInt(ow.getValue());
                }
            }
        }
        Deployment deployment = containerBuilder
                .addNewPort()
                .withName("http").withProtocol("TCP").withContainerPort(httpPort)
                .endPort()
                .addNewPort()
                .withName("https").withProtocol("TCP").withContainerPort(httpsPort)
                .endPort()
                .endContainer()
                .endSpec()
                .endTemplate()
                .endSpec()
                .build();

        // imagePullSecrets
        if (StringUtils.isNotBlank(spec.getImagePullSecrets())) {
            deployment.getSpec().getTemplate().getSpec()
                    .setImagePullSecrets(
                            List.of(new LocalObjectReferenceBuilder().withName(spec.getImagePullSecrets()).build())
                    );
        }

        // 挂载信息，包括 initservice, ranger-admin-site.xml, install.properties, workspace-pvc, hadoop-folder
        var volumes = new ArrayList<Volume>();
        var volumeMounts = new ArrayList<VolumeMount>();
        // 1. 添加 conf-init-service
        if (spec.getInitService() != null && spec.getInitService().getEnable()) {
            addInitServiceVol(name, volumes, volumeMounts);
        }
        // 2. 添加 ranger-admin-site.xml
        addRangerAdminSiteVol(name, volumes, volumeMounts);
        // 3. 添加 install.properties
        addInstallPropertiesVol(name, volumes, volumeMounts);
        // 4. 添加 workspace-pvc
        addWorkSpaceVol(name, volumes, volumeMounts);
        // 5. 添加 hadoop 配置文件
        if (StringUtils.isNotBlank(spec.getHadoopConfigFolder())) {
            addHadoopConfigsVol(namespace, spec.getHadoopConfigFolder(), volumes, volumeMounts);
        }
        // 挂载deployment
        deployment.getSpec().getTemplate().getSpec().setVolumes(volumes);
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setVolumeMounts(volumeMounts);
        return deployment;
    }

    /**
     * 获取配置hash对应的 annotations
     *
     * @return {"ranger-site-config-hash": "", "initservice-config-hash": ""}
     */
    private Map<String, String> getConfigHashMap(RangerSpec spec, Context<Ranger> context) {
        var hashMap = new LinkedHashMap<String, String>();
        var rangerConfigXml = context.getSecondaryResource(ConfigMap.class)
                .map(configmap -> configmap.getData().get(RangerReconciler.RANGER_ADMIN_SITE_FILE)).orElse("");
        hashMap.put(RANGER_CONFIG_HASH_ANNOTATION, K8sUtil.getConfigmapMd5(rangerConfigXml));
        // initservice的内容
        if (spec.getInitService() != null && spec.getInitService().getEnable()) {
            var initServiceJson = RangerReconciler.getServiceJson(spec.getInitService().getService());
            hashMap.put(RANGER_INITSERVICE_HASH_ANNOTATION, K8sUtil.getConfigmapMd5(initServiceJson));
        }
        return hashMap;
    }

    /**
     * 挂载 initservice
     */
    private void addInitServiceVol(String name, ArrayList<Volume> volumes, ArrayList<VolumeMount> volumeMounts) {
        // 添加 volume
        Volume volume = new VolumeBuilder().withName("conf-init-service")
                .withNewConfigMap()
                .withName(RangerReconciler.getInitServiceName(name)).endConfigMap()
                .build();
        volumes.add(volume);
        // 添加 volumeMounts
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath("/opt/mr3-run/ranger/service.json")
                .withSubPath("service.json")
                .build();
        volumeMounts.add(volumeMount);
    }

    /**
     * 挂载 ranger-admin-site.xml
     */
    private void addRangerAdminSiteVol(String name, ArrayList<Volume> volumes, ArrayList<VolumeMount> volumeMounts) {
        // 添加 volume
        Volume volume = new VolumeBuilder().withName("conf-ranger-site")
                .withNewConfigMap()
                .withName(RangerAdminSiteResource.getRangerAdminSiteName(name)).endConfigMap()
                .build();
        volumes.add(volume);
        // 添加 volumeMounts
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath("/opt/mr3-run/ranger/conf/" + RangerReconciler.RANGER_ADMIN_SITE_FILE)
                .withSubPath(RangerReconciler.RANGER_ADMIN_SITE_FILE)
                .build();
        volumeMounts.add(volumeMount);
    }

    /**
     * 挂载 workspace-pvc
     */
    private void addWorkSpaceVol(String name, ArrayList<Volume> volumes, ArrayList<VolumeMount> volumeMounts) {
        // 1. 添加 workspace
        // 添加 volume
        Volume volume = new VolumeBuilder().withName("work-dir-volume")
                .withNewPersistentVolumeClaim()
                .withClaimName(RangerPvcResource.getWorkspacePvc(name))
                .endPersistentVolumeClaim()
                .build();
        volumes.add(volume);
        // 添加 volumeMounts
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath("/opt/mr3-run/ranger/work-dir/")
                .build();
        volumeMounts.add(volumeMount);
        // 2. 添加本地文件系统存储日志
        Volume local = new VolumeBuilder().withName("work-local-dir-volume")
                .withNewEmptyDir().endEmptyDir().build();
        volumes.add(local);
        // 添加 volumeMounts
        VolumeMount localMount = new VolumeMountBuilder()
                .withName(local.getName())
                .withMountPath("/opt/mr3-run/ranger/work-local-dir/")
                .build();
        volumeMounts.add(localMount);
    }

    /**
     * 挂载 hadoopConfigFolder
     * 可能挂载多个文件，所以需要检索原来的资源然后每个都配对
     */
    private void addHadoopConfigsVol(String namespace, String name, ArrayList<Volume> volumes, ArrayList<VolumeMount> volumeMounts) {
        var configName = HadoopConfigFolderConfigMapResource.getConfigMapName(name);
        var hadoopConfigs = client.configMaps().inNamespace(namespace).withName(configName).get();
        if (hadoopConfigs == null) {
            throw new ConfigmapException("找不到当前命名空间下的 HadoopConfigFolder 对应的资源对象！" + configName);
        }
        var configs = hadoopConfigs.getData().keySet();
        for (String config : configs) {
            // 添加 volume
            Volume volume = new VolumeBuilder()
                    // 替换名称
                    .withName(String.format("conf-hdp-%s", config.replace(".", "-")))
                    .withNewConfigMap()
                    .withName(configName).endConfigMap()
                    .build();
            volumes.add(volume);
            // 添加 volumeMounts
            VolumeMount volumeMount = new VolumeMountBuilder()
                    .withName(volume.getName())
                    .withMountPath("/opt/mr3-run/ranger/conf/" + config)
                    .withSubPath(config)
                    .build();
            volumeMounts.add(volumeMount);
        }
    }

    /**
     * 挂载 install.properties
     */
    private void addInstallPropertiesVol(String name, ArrayList<Volume> volumes, ArrayList<VolumeMount> volumeMounts) {
        Volume volume = new VolumeBuilder().withName("conf-install-properties")
                .withNewSecret()
                .withSecretName(InstallPropertiesResource.getInstallProperties(name)).endSecret()
                .build();
        volumes.add(volume);
        // 添加 volumeMounts
        VolumeMount volumeMount = new VolumeMountBuilder()
                .withName(volume.getName())
                .withMountPath("/opt/mr3-run/ranger/key/" + RangerReconciler.INSTALL_FILE)
                .withSubPath(RangerReconciler.INSTALL_FILE)
                .build();
        volumeMounts.add(volumeMount);
    }

    /**
     * 获取当前的镜像
     */
    private String getImage(String image, String version) {
        if (StringUtils.isBlank(image)) {
            image = "harbor.sypesco.com:5000/bigdata/ranger";
        }
        if (StringUtils.isBlank(version)) {
            // 这里默认为2.3.0版本
            version = "2.3.0-mr3";
        }
        return String.format("%s:%s", image, version);
    }

    @Override
    public Result<Deployment> match(Deployment actual, Ranger ranger, Context<Ranger> context) {
        // 比较deployment的差异
        final var labels = K8sUtil.getContextLabels(context);
        final var spec = ranger.getSpec();
        var deployment = getDeployment(spec, ranger.getMetadata().getNamespace(), ranger.getMetadata().getName(), labels, context);

        // sa名称
        if (!Objects.equals(spec.getServiceAccount(), actual.getSpec().getTemplate().getSpec().getServiceAccount()))
            return Result.nonComputed(false);
        // 副本数量
        if (!actual.getSpec().getReplicas().equals(spec.getReplicas()))
            return Result.nonComputed(false);
        // 标签信息
        if (!K8sUtil.checkLabels(actual.getMetadata().getLabels(), labels) || !K8sUtil.checkLabels(actual.getSpec().getSelector().getMatchLabels(), labels))
            return Result.nonComputed(false);
        // configmap差异
        var hashMap = deployment.getSpec().getTemplate().getMetadata().getAnnotations();
        var annotations = actual.getSpec().getTemplate().getMetadata().getAnnotations();
        if (annotations == null || annotations.isEmpty() || !annotations.equals(hashMap))
            return Result.nonComputed(false);
        // hostAliases
        var newHas = CollectionUtils.filterEmptyCollections(deployment.getSpec().getTemplate().getSpec().getHostAliases());
        var oldHas = CollectionUtils.filterEmptyCollections(actual.getSpec().getTemplate().getSpec().getHostAliases());
        if (newHas.size() != oldHas.size()) {
            return Result.nonComputed(false);
        } else {
            for (int i = 0, size = newHas.size(); i < size; i++) {
                var newHa = newHas.get(i);
                var oldHa = oldHas.get(i);
                if (!newHa.getIp().equals(oldHa.getIp())) {
                    return Result.nonComputed(false);
                } else if (!CollectionUtils.isEqualCollection(newHa.getHostnames(), oldHa.getHostnames())) {
                    return Result.nonComputed(false);
                }
            }
        }

        // container内部的差异
        final var container = actual.getSpec().getTemplate().getSpec().getContainers()
                .stream()
                .findFirst();
        final var originContainer = deployment.getSpec().getTemplate().getSpec().getContainers()
                .stream()
                .findFirst().orElseThrow(() -> new DeploymentException("找不到生成的deployment资源信息！"));
        return Result.nonComputed(
                container.map(c -> {
                    // 镜像名称
                    if (!StringUtils.equals(getImage(spec.getImage(), spec.getVersion()), c.getImage()))
                        return false;
                    // 镜像策略
                    if (!StringUtils.equals(spec.getImagePullPolicy().toString(), c.getImagePullPolicy()))
                        return false;
                    // 环境变量
                    if (!convertEnvVar(originContainer.getEnv()).equals(convertEnvVar(c.getEnv())))
                        return false;
                    // command
                    if (!CollectionUtils.isEqualCollection(originContainer.getCommand(), c.getCommand()))
                        return false;
                    // 两个端口信息，6181，6182
                    if (c.getPorts() == null || c.getPorts().size() != 2) {
                        return false;
                    } else {
                        var newPort = originContainer.getPorts();
                        var oldPort = c.getPorts();
                        if (!Objects.equals(oldPort.get(0).getContainerPort(), newPort.get(0).getContainerPort()))
                            return false;
                        if (!Objects.equals(oldPort.get(1).getContainerPort(), newPort.get(1).getContainerPort()))
                            return false;
                    }
                    // resources
                    if (originContainer.getResources() == null) {
                        if (c.getResources() != null && (c.getResources().getRequests() != null || c.getResources().getLimits() != null)) {
                            return false;
                        }
                    } else {
                        // limits
                        var oldLimits = CollectionUtils.filterEmptyMap(c.getResources().getLimits());
                        var newLimits = CollectionUtils.filterEmptyMap(originContainer.getResources().getLimits());
                        if (!CollectionUtils.isEqualMap(oldLimits, newLimits)) return false;
                        // requests
                        var oldRequests = CollectionUtils.filterEmptyMap(c.getResources().getRequests());
                        var newRequests = CollectionUtils.filterEmptyMap(originContainer.getResources().getRequests());
                        if (!CollectionUtils.isEqualMap(oldRequests, newRequests)) return false;
                    }

                    // 挂载信息，如果挂载有关联问题，会出错，所以我们这边暂时只校验 volumeMounts
                    var newVms = originContainer.getVolumeMounts();
                    var oldVms = c.getVolumeMounts();
                    if (newVms.isEmpty() || newVms.size() != oldVms.size()) {
                        return false;
                    } else {
                        for (int i = 0, size = newVms.size(); i < size; i++) {
                            var newVm = newVms.get(i);
                            var oldVm = oldVms.get(i);
                            if (!newVm.getName().equals(oldVm.getName())) {
                                return false;
                            } else if (!newVm.getMountPath().equals(oldVm.getMountPath())) {
                                return false;
                            } else if (!Objects.equals(newVm.getSubPath(), oldVm.getSubPath())) {
                                return false;
                            }
                        }
                    }
                    return true;
                }).orElse(false)
        );
    }

    private Map<String, String> convertEnvVar(List<EnvVar> envVars) {
        final var result = new HashMap<String, String>(envVars.size());
        envVars.forEach(e -> result.put(e.getName(), e.getValue()));
        return result;
    }
}
