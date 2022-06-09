package com.pesco.operator.ranger.dependent;

import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.ranger.crd.Ranger;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

import static com.pesco.operator.common.KubeConstants.ClusterIP;

/**
 * @Title ranger构建的service
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.dependent
 * @Description
 * @Date 2022/6/8 12:58 下午
 * @Version V1.0
 */

@ApplicationScoped
public class RangerServiceResource extends KubernetesDependentResource<Service, Ranger> implements
        Creator<Service, Ranger>,
        Updater<Service, Ranger>,
        Matcher<Service, Ranger> {

    public static final Integer DEFAULT_HTTP_PORT = 6181;
    public static final Integer DEFAULT_HTTPS_PORT = 6182;

    private static final Logger LOGGER = Logger.getLogger(RangerServiceResource.class);

    public RangerServiceResource() {
        super(Service.class);
    }

    @Override
    protected Service desired(Ranger ranger, Context<Ranger> context) {
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);

        Service service = new ServiceBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, String.format("%s-ranger-service", name, name), labels))
                .withNewSpec()
                // http
                .addNewPort()
                .withName("ranger-admin-http")
                .withPort(DEFAULT_HTTP_PORT)
                .withNewTargetPort().withStrVal("http").endTargetPort()
                .endPort()
                // https
                .addNewPort()
                .withName("ranger-admin-https")
                .withPort(DEFAULT_HTTPS_PORT)
                .withNewTargetPort().withStrVal("https").endTargetPort()
                .endPort()
                .withSelector(labels)
                .withType(ClusterIP)
                .endSpec()
                .build();

        LOGGER.infov("创建/修改 service {0}/{1}", namespace, name);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", Service.class.getName(), YamlUtil.toPrettyYaml(service));
        }
        return service;
    }

    @Override
    public Result<Service> match(Service actual, Ranger ranger, Context<Ranger> context) {
        final var ports = actual.getSpec().getPorts();
        // 端口映射是否正确
        boolean match = true;
        for (ServicePort port : ports) {
            if ("ranger-admin-http".equals(port.getName())) {
                if (!Objects.equals(port.getPort(), DEFAULT_HTTP_PORT) || !"http".equals(port.getTargetPort().getStrVal())) {
                    match = false;
                    break;
                }
            } else if ("ranger-admin-https".equals(port.getName())) {
                if (!Objects.equals(port.getPort(), DEFAULT_HTTPS_PORT) || !"https".equals(port.getTargetPort().getStrVal())) {
                    match = false;
                    break;
                }
            } else {
                match = false;
            }
        }
        // 是不是 ClusterIP
        if (!actual.getSpec().getType().equals(ClusterIP)) match = false;
        if (!match) LOGGER.info("当前 Service 发生了修改，需要进行更新！");
        return Result.nonComputed(match);
    }

}
