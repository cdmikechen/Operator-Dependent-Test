package com.pesco.operator.ranger.dependent;

import com.pesco.operator.common.exception.SecretException;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.ranger.RangerReconciler;
import com.pesco.operator.ranger.crd.Ranger;
import com.pesco.operator.ranger.crd.RangerSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.Creator;
import io.javaoperatorsdk.operator.processing.dependent.Matcher;
import io.javaoperatorsdk.operator.processing.dependent.Updater;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependentResource;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;


/**
 * @Title install.properties处理
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.dependent
 * @Description
 * @Date 2022/6/8 8:59 上午
 * @Version V1.0
 */

@ApplicationScoped
public class InstallPropertiesResource extends KubernetesDependentResource<Secret, Ranger> implements
        Creator<Secret, Ranger>,
        Updater<Secret, Ranger>,
        Matcher<Secret, Ranger> {

    private static final Logger LOGGER = Logger.getLogger(InstallPropertiesResource.class);

    public InstallPropertiesResource() {
        super(Secret.class);
    }

    public static String getInstallProperties(String name) {
        return String.format("%s-install-properties", name);
    }

    @Override
    protected Secret desired(Ranger primary, Context<Ranger> context) {
        final var spec = primary.getSpec();
        final var namespace = primary.getMetadata().getNamespace();
        final var name = primary.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);

        SecretBuilder builder = new SecretBuilder()
                .withNewMetadata()
                .withName(getInstallProperties(name))
                .withLabels(labels)
                .withNamespace(namespace)
                .endMetadata();
        var secret = builder.addToData(RangerReconciler.INSTALL_FILE, K8sUtil.encode(getInstallProperties(spec))).build();
        LOGGER.infov("创建/修改 Secret {0}/{1}", namespace, secret.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", Secret.class.getName(), YamlUtil.toPrettyYaml(secret));
        }
        return secret;
    }

    public String getInstallProperties(RangerSpec spec) {
        var install = spec.getInstallProperties();
        try {
            // 这里解析properties主要是为了判断文件的可读性和正确性，由于properties读取后是乱序的，所以我们如果添加值，会在源文件上进行处理
            Properties properties = K8sUtil.parsePropertiesString(install);
            // 不能删除的配置
            Properties unremove = K8sUtil.parsePropertiesFile("/ranger/unremove.properties");
            // 新增行
            var appends = new ArrayList<String>();
            // todo 2.3 开始支持从rds中直接获取数据库连接信息，所以这里可以不写数据库配置，会由系统自动给你做拼接
            var version = spec.getVersion();
            if (StringUtils.isNotBlank(version) && version.startsWith("2.3.0")) {

            }
            // 补充一些必须保留的信息，这里其实有些配置不需要，但是 ranger 里面有些问题，所以我们这边只能先新增进去
            unremove.forEach((k, v) -> {
                if (!properties.containsKey(k)) {
                    appends.add(String.format("%s=%s", k, v));
                }
            });

            if (!appends.isEmpty()) {
                var add = new StringBuilder();
                add.append("\n").append("## Add missing required configuration");
                for (String append : appends) {
                    add.append("\n\n").append(append);
                }
                install += add.toString();
            }

            return install;
        } catch (IOException e) {
            throw new SecretException("解析 install.properties 失败！" + e.getMessage(), e);
        }
    }

    @Override
    public Result<Secret> match(Secret actual, Ranger primary, Context<Ranger> context) {
        // 这里主要判断secret的值 是否一致
        var spec = primary.getSpec();
        var content = K8sUtil.encode(getInstallProperties(spec));
        if (!content.equals(actual.getData().get(RangerReconciler.INSTALL_FILE))) {
            LOGGER.info("当前 install.properties 发生了修改，需要进行更新！");
            return Result.nonComputed(false);
        } else return Result.nonComputed(true);
    }
}
