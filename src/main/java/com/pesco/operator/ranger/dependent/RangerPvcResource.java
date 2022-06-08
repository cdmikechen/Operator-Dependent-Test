package com.pesco.operator.ranger.dependent;

import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.ranger.crd.Ranger;
import io.fabric8.kubernetes.api.model.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

/**
 * @Title ranger配置的pvc资源
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.dependent
 * @Description
 * @Date 2022/6/8 1:14 下午
 * @Version V1.0
 */

@ApplicationScoped
public class RangerPvcResource extends KubernetesDependentResource<PersistentVolumeClaim, Ranger> implements
        Creator<PersistentVolumeClaim, Ranger>,
        Updater<PersistentVolumeClaim, Ranger>,
        Matcher<PersistentVolumeClaim, Ranger> {

    private static final Logger LOGGER = Logger.getLogger(RangerPvcResource.class);

    public RangerPvcResource() {
        super(PersistentVolumeClaim.class);
    }

    @Override
    protected PersistentVolumeClaim desired(Ranger ranger, Context<Ranger> context) {
        var pvc = getPvc(ranger, context);
        LOGGER.infov("创建/修改 pvc {0}/{1}", pvc.getMetadata().getNamespace(), pvc.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", PersistentVolumeClaim.class.getName(), YamlUtil.toPrettyYaml(pvc));
        }
        return pvc;
    }

    public PersistentVolumeClaim getPvc(Ranger ranger, Context<Ranger> context) {
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);
        final var storage = ranger.getSpec().getStorage();

        // 如果storage为空，则我们就按照默认值进行创建
        String storageClass = null;
        String accessModes = null;
        String volumeClaimSize = null;
        if (storage != null) {
            storageClass = storage.getStorageClassName();
            accessModes = storage.getAccessModes();
            volumeClaimSize = storage.getVolumeClaimSize();
        }
        if (StringUtils.isBlank(accessModes)) {
            accessModes = "ReadWriteOnce";
        }
        if (StringUtils.isBlank(volumeClaimSize)) {
            volumeClaimSize = "1Gi";
        }

        return new PersistentVolumeClaimBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace, getWorkspacePvc(name), labels))
                .withNewSpec()
                .withAccessModes(accessModes)
                .withStorageClassName(storageClass)
                .withNewResources().addToRequests("storage", Quantity.parse(volumeClaimSize))
                .endResources()
                .endSpec()
                .build();
    }

    public static String getWorkspacePvc(String name) {
        return String.format("%s-ranger-workspace", name);
    }

    @Override
    public Result<PersistentVolumeClaim> match(PersistentVolumeClaim actual, Ranger ranger, Context<Ranger> context) {
        // 如果能扩容，才进行比较，否则不进行处理
        // 目前的话，暂时只处理扩容这一个选项
        var storage = ranger.getSpec().getStorage();
        if (storage != null && storage.getExpansion() && storage.getVolumeClaimSize() != null) {
            var size = Quantity.parse(storage.getVolumeClaimSize());
            var check = !size.equals(actual.getSpec().getResources().getRequests().get("storage"));
            if (check) {
                LOGGER.infov("当前 pvc 发生了容量变化 => {0}，需要进行更新！", size);
                return Result.nonComputed(false);
            }
        }
        return Result.nonComputed(true);
    }
}
