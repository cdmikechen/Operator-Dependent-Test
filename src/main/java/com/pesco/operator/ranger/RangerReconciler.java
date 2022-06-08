package com.pesco.operator.ranger;

import com.pesco.operator.common.utils.JsonUtil;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.ranger.crd.Ranger;
import com.pesco.operator.ranger.crd.RangerStatus;
import com.pesco.operator.ranger.crd.service.Service;
import com.pesco.operator.ranger.dependent.InstallPropertiesResource;
import com.pesco.operator.ranger.dependent.RangerAdminSiteResource;
import com.pesco.operator.ranger.dependent.RangerDeploymentResource;
import com.pesco.operator.ranger.dependent.RangerPvcResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.pesco.operator.common.KubeConstants.*;

/**
 * @Title Ranger服务处理
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger
 * @Description Ranger的部署包含 install.properties ranger-admin-site.xml deployment service pvc init-service
 * @Date 2022/6/8 8:52 上午
 * @Version V1.0
 */
@ControllerConfiguration(dependents = {
        @Dependent(type = InstallPropertiesResource.class),
        @Dependent(type = RangerAdminSiteResource.class),
        @Dependent(type = RangerPvcResource.class),
        @Dependent(type = RangerDeploymentResource.class),
})
public class RangerReconciler implements Reconciler<Ranger>
        , ContextInitializer<Ranger>
        , ErrorStatusHandler<Ranger>
        , Cleaner<Ranger> {

    private static final Logger LOGGER = Logger.getLogger(RangerReconciler.class);

    public static final String INSTALL_FILE = "install.properties";
    public static final String RANGER_ADMIN_SITE_FILE = "ranger-admin-site.xml";
    public static final String INIT_SERVICE_FILE = "service.json";

    @Inject
    KubernetesClient client;

    @Override
    public void initContext(Ranger ranger, Context<Ranger> context) {
        final var name = ranger.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, name,
                INSTANCE_LABEL, RANGER_INSTANCE_NAME,
                VERSION_LABEL, DEFAULT_APPLICATION_VERSION,
                COMPONENT_LABEL, OPERATOR_NAME);
        context.managedDependentResourceContext().put(LABELS_CONTEXT_KEY, labels);
    }

    @Override
    public UpdateControl<Ranger> reconcile(Ranger ranger, Context<Ranger> context) throws Exception {
        final var spec = ranger.getSpec();
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);
        LOGGER.infov("注册任务 {0}/{1}/{2}", namespace, name, JsonUtil.toJson(spec));

        // 如果有initservice，则手工创建
        if (spec.getInitService() != null && spec.getInitService().getEnable()) {
            createInitService(namespace, name, labels, spec.getInitService().getService());
        }

        var status = new RangerStatus();
        // active replicas
        status.setActiveReplicas(
                context.getSecondaryResource(Deployment.class)
                        .map(deployment -> deployment.getStatus().getAvailableReplicas())
                        .orElse(0));
        // ranger-admin-site.xml 赋值
        final var rangerAdminSite = context.getSecondaryResource(ConfigMap.class)
                .map(configmap -> configmap.getMetadata().getName()).orElse(null);
        status.setConfigMapName(rangerAdminSite);
        // 返回最终状态
        ranger.setStatus(status);
        return UpdateControl.updateStatus(ranger);
    }

    public static String getInitServiceName(String name) {
        return String.format("%s-ranger-initservices", name);
    }

    /**
     * 创建一个initservice的json配置
     */
    private void createInitService(String namespace, String name, Map<String, String> labels, List<Service> services) throws IOException {
        var json = services == null || services.isEmpty() ? "" : JsonUtil.toJson(services);
        var configMap = new ConfigMapBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, getInitServiceName(name), labels))
                .addToData(INIT_SERVICE_FILE, json)
                .build();
        LOGGER.infov("创建/修改 pvc {0}/{1}", configMap.getMetadata().getNamespace(), configMap.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", ConfigMap.class.getName(), YamlUtil.toPrettyYaml(configMap));
        }
        client.configMaps()
                .inNamespace(namespace)
                .createOrReplace(configMap);
    }

    /**
     * 删除initservice的json配置
     */
    private void deleteInitService(String namespace, String name) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("删除任务 InitService Configmap {0}/{1}", namespace, getInitServiceName(name));
        }
        client.configMaps()
                .inNamespace(namespace)
                .withName(getInitServiceName(name))
                .delete();
    }

    @Override
    public ErrorStatusUpdateControl<Ranger> updateErrorStatus(Ranger ranger, Context<Ranger> context, Exception e) {
        // 如果有异常，则更新状态信息
        LOGGER.errorv(e, "创建资源时候发生异常！{0}", e.getMessage());
        var fail = new RangerStatus();
        fail.setErrorMsg(ExceptionUtils.getMessage(e));
        fail.setStackTrace(ExceptionUtils.getStackFrames(e));
        ranger.setStatus(fail);
        return ErrorStatusUpdateControl.updateStatus(ranger);
    }

    @Override
    public DeleteControl cleanup(Ranger ranger, Context<Ranger> context) {
        final var spec = ranger.getSpec();
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        LOGGER.infov("删除任务 {0}/{1}", namespace, name);

        if (spec.getInitService() != null && spec.getInitService().getEnable()) {
            deleteInitService(namespace, name);
        }
        return DeleteControl.defaultDelete();
    }
}
