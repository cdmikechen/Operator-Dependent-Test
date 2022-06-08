package com.pesco.operator.hadoop.folder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pesco.operator.hadoop.folder.crd.HadoopConfigFolder;
import com.pesco.operator.hadoop.folder.crd.HadoopConfigFolderStatus;
import com.pesco.operator.hadoop.folder.dependent.HadoopConfigFolderConfigMapResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import java.util.Map;

import static com.pesco.operator.common.KubeConstants.*;
import static com.pesco.operator.common.KubeConstants.LABELS_CONTEXT_KEY;

/**
 * @Title HadoopConfig文件夹服务类
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder
 * @Description
 * @Date 2022/6/7 3:31 下午
 * @Version V1.0
 */

@ControllerConfiguration(dependents = {
        @Dependent(type = HadoopConfigFolderConfigMapResource.class)
})
public class HadoopConfigFolderReconciler implements Reconciler<HadoopConfigFolder>
        , ContextInitializer<HadoopConfigFolder>
        , ErrorStatusHandler<HadoopConfigFolder> {

    private static final Logger LOGGER = Logger.getLogger(HadoopConfigFolderReconciler.class);

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void initContext(HadoopConfigFolder hadoopConfigFolder, Context<HadoopConfigFolder> context) {
        final var name = hadoopConfigFolder.getMetadata().getName();
        final var labels = Map.of(APP_LABEL, name,
                INSTANCE_LABEL, HADOOP_CONFIG_FOLDER_INSTANCE_NAME,
                VERSION_LABEL, DEFAULT_APPLICATION_VERSION,
                COMPONENT_LABEL, OPERATOR_NAME);
        context.managedDependentResourceContext().put(LABELS_CONTEXT_KEY, labels);
    }

    @Override
    public UpdateControl<HadoopConfigFolder> reconcile(HadoopConfigFolder hadoopConfigFolder, Context<HadoopConfigFolder> context) throws Exception {
        final var spec = hadoopConfigFolder.getSpec();
        final var namespace = hadoopConfigFolder.getMetadata().getNamespace();
        final var name = hadoopConfigFolder.getMetadata().getName();
        LOGGER.infov("注册 HadoopConfigFolder 配置 {0}/{1}/{2}", namespace, name, objectMapper.writeValueAsString(spec));

        final var status = new HadoopConfigFolderStatus();
        final var configMap = context.getSecondaryResource(ConfigMap.class);
        if (configMap.isPresent()) {
            status.setSuccess(true);
            final var cm = configMap.get();
            // 将每个文件都放入列表里面
            status.setFileList(cm.getData().keySet().toArray(new String[]{}));
            status.setResourceName(cm.getMetadata().getName());
        } else {
            status.setSuccess(false);
            status.setErrorMsg("找不到生成的文件资源，请核对提交的自定义资源内容！");
        }
        hadoopConfigFolder.setStatus(status);
        return UpdateControl.updateStatus(hadoopConfigFolder);
    }

    @Override
    public ErrorStatusUpdateControl<HadoopConfigFolder> updateErrorStatus(HadoopConfigFolder hadoopConfigFolder, Context<HadoopConfigFolder> context, Exception e) {
        // 如果有异常，则更新状态信息
        LOGGER.errorv(e, "创建资源时候发生异常！{0}", e.getMessage());
        var fail = new HadoopConfigFolderStatus();
        fail.setSuccess(false);
        fail.setErrorMsg(ExceptionUtils.getMessage(e));
        fail.setStackTrace(ExceptionUtils.getStackFrames(e));
        hadoopConfigFolder.setStatus(fail);
        return ErrorStatusUpdateControl.updateStatus(hadoopConfigFolder);
    }
}
