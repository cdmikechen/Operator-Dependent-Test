package com.pesco.operator.hadoop.config;

import com.pesco.operator.common.utils.JsonUtil;
import com.pesco.operator.hadoop.config.crd.HadoopConfigStatus;
import com.pesco.operator.hadoop.config.dependent.HadoopConfigConfigMapResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.api.reconciler.*;

import com.pesco.operator.hadoop.config.crd.HadoopConfig;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import java.util.Map;

import static com.pesco.operator.common.KubeConstants.*;
import static com.pesco.operator.common.KubeConstants.LABELS_CONTEXT_KEY;

@ControllerConfiguration(dependents = {
        @Dependent(type = HadoopConfigConfigMapResource.class)
})
public class HadoopConfigReconciler implements Reconciler<HadoopConfig>
        , ContextInitializer<HadoopConfig>
        , ErrorStatusHandler<HadoopConfig> {

    private static final Logger LOGGER = Logger.getLogger(HadoopConfigReconciler.class);

    @Override
    public void initContext(HadoopConfig hadoopConfig, Context<HadoopConfig> context) {
        final var name = hadoopConfig.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, name,
                INSTANCE_LABEL, HADOOP_CONFIG_INSTANCE_NAME,
                VERSION_LABEL, DEFAULT_APPLICATION_VERSION,
                COMPONENT_LABEL, OPERATOR_NAME);
        context.managedDependentResourceContext().put(LABELS_CONTEXT_KEY, labels);
    }

    @Override
    public UpdateControl<HadoopConfig> reconcile(HadoopConfig hadoopConfig, Context<HadoopConfig> context) throws Exception {
        final var spec = hadoopConfig.getSpec();
        final var namespace = hadoopConfig.getMetadata().getNamespace();
        final var name = hadoopConfig.getMetadata().getName();
        LOGGER.infov("注册 HadoopConfig 配置 {0}/{1}/{2}", namespace, name, JsonUtil.toJson(spec));

        final var status = new HadoopConfigStatus();
        final var configMap = context.getSecondaryResource(ConfigMap.class);
        if (configMap.isPresent()) {
            status.setSuccess(true);
            final var cm = configMap.get();
            status.setRealContent(cm.getData().get(spec.getFileName()));
            status.setResourceName(cm.getMetadata().getName());
        } else {
            status.setSuccess(false);
            status.setErrorMsg("找不到生成的文件资源，请核对提交的自定义资源内容！");
        }
        hadoopConfig.setStatus(status);
        return UpdateControl.updateStatus(hadoopConfig);
    }

    @Override
    public ErrorStatusUpdateControl<HadoopConfig> updateErrorStatus(HadoopConfig hadoopConfig, Context<HadoopConfig> context, Exception e) {
        // 如果有异常，则更新状态信息
        LOGGER.errorv(e, "创建资源时候发生异常！{0}", e.getMessage());
        var fail = new HadoopConfigStatus();
        fail.setSuccess(false);
        fail.setErrorMsg(ExceptionUtils.getMessage(e));
        fail.setStackTrace(ExceptionUtils.getStackFrames(e));
        hadoopConfig.setStatus(fail);
        return ErrorStatusUpdateControl.updateStatus(hadoopConfig);
    }
}
