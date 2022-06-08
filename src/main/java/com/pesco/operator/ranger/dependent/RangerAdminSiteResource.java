package com.pesco.operator.ranger.dependent;

import com.pesco.operator.common.exception.ConfigmapException;
import com.pesco.operator.common.utils.K8sUtil;
import com.pesco.operator.common.utils.YamlUtil;
import com.pesco.operator.ranger.RangerReconciler;
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
import java.io.IOException;
import java.util.TreeMap;

import static com.pesco.operator.ranger.RangerReconciler.RANGER_ADMIN_SITE_FILE;

/**
 * @Title ranger-admin-site.xml资源处理服务
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.dependent
 * @Description
 * @Date 2022/6/8 9:34 上午
 * @Version V1.0
 */

@ApplicationScoped
public class RangerAdminSiteResource extends KubernetesDependentResource<ConfigMap, Ranger> implements
        Creator<ConfigMap, Ranger>,
        Updater<ConfigMap, Ranger>,
        Matcher<ConfigMap, Ranger> {

    private static final Logger LOGGER = Logger.getLogger(RangerAdminSiteResource.class);

    public RangerAdminSiteResource() {
        super(ConfigMap.class);
    }

    public static String getRangerAdminSiteName(String name) {
        return String.format("%s-ranger-site", name);
    }

    @Override
    protected ConfigMap desired(Ranger ranger, Context<Ranger> context) {
        // 重新生成的 xml 文件内容
        var output = getRangerAdminSite(ranger, context);
        final var namespace = ranger.getMetadata().getNamespace();
        final var name = ranger.getMetadata().getName();
        final var labels = K8sUtil.getContextLabels(context);

        // 生成configmap
        var configmapBuilder = new ConfigMapBuilder()
                .withMetadata(K8sUtil.createMetadata(namespace,getRangerAdminSiteName(name), labels));
        ConfigMap configMap = configmapBuilder.addToData(RANGER_ADMIN_SITE_FILE, output).build();

        LOGGER.infov("创建/修改 ranger-admin-site.xml {0}/{1}", namespace, configMap.getMetadata().getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugv("显示 {0} yaml = \n{1}", ConfigMap.class.getName(), YamlUtil.toPrettyYaml(configMap));
        }
        return configMap;
    }

    /**
     * 生成 ranger-admin-site.xml 内容
     */
    private String getRangerAdminSite(Ranger ranger, Context<Ranger> context) {
        var install = context.getSecondaryResource(Secret.class)
                .map(secret -> secret.getData().get(RangerReconciler.INSTALL_FILE))
                .map(K8sUtil::decode)
                .orElseThrow(() -> new ConfigmapException("找不到指定的 install.properties 秘钥文件！"));
        try {
            // 系统生成的 install.properties 文件
            var base = K8sUtil.parsePropertiesString(install);
            // 查找 xml 映射配置
            var mapping = K8sUtil.parsePropertiesFile("/ranger/install-map.properties");
            // 按照顺序存储
            var xml = new TreeMap<String, String>();
            base.forEach((k, v) -> {
                if (mapping.containsKey(k)) {
                    xml.put(mapping.getProperty(String.valueOf(k)), String.valueOf(v));
                }
            });

            // 数据库信息需要转化，使用的配置是 ranger.jpa.jdbc.url, ranger.jpa.jdbc.dialect 等等
            String dbFlavor = base.getProperty("DB_FLAVOR");
            if (StringUtils.isBlank(dbFlavor)) {
                throw new ConfigmapException("找不到支持的数据库类型 DB_FLAVOR！");
            }
            dbFlavor = dbFlavor.toLowerCase();
            String dbhost = base.getProperty("db_host");
            String dbname = base.getProperty("db_name");
            if (StringUtils.isBlank(dbhost)) {
                throw new ConfigmapException("找不到数据库的连接信息 db_host！");
            } else if (StringUtils.isBlank(dbname)) {
                throw new ConfigmapException("找不到数据库的db信息 dbname！");
            }

            if ("mysql".equals(dbFlavor)) {
                xml.put("ranger.jpa.jdbc.url", String.format("jdbc:log4jdbc:mysql://%s/%s", dbhost, dbname));
                xml.put("ranger.jpa.jdbc.dialect", "org.eclipse.persistence.platform.database.MySQLPlatform");
                xml.put("ranger.jpa.audit.jdbc.dialect", "org.eclipse.persistence.platform.database.MySQLPlatform");
                xml.put("ranger.jpa.jdbc.driver", "net.sf.log4jdbc.DriverSpy");
                xml.put("ranger.jpa.audit.jdbc.driver", "net.sf.log4jdbc.DriverSpy");
                xml.put("ranger.jpa.jdbc.preferredtestquery", "select 1");
            } else if ("postgres".equals(dbFlavor)) {
                var dbssl = Boolean.valueOf(base.getProperty("db_ssl_enabled", "false"));
                if (dbssl) {
                    var sslfile = base.getProperty("db_ssl_certificate_file");
                    if (StringUtils.isBlank(sslfile)) {
                        xml.put("ranger.jpa.jdbc.url", String.format("jdbc:postgresql://%s/%s?ssl=true&sslmode=verify-full&sslrootcert=%s", dbhost, dbname, sslfile));
                    } else {
                        xml.put("ranger.jpa.jdbc.url", String.format("jdbc:postgresql://%s/%s?ssl=true&sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory", dbhost, dbname));
                    }
                } else {
                    xml.put("ranger.jpa.jdbc.url", String.format("jdbc:postgresql://%s/%s", dbhost, dbname));
                }
                xml.put("ranger.jpa.jdbc.dialect", "org.eclipse.persistence.platform.database.PostgreSQLPlatform");
                xml.put("ranger.jpa.audit.jdbc.dialect", "org.eclipse.persistence.platform.database.PostgreSQLPlatform");
                xml.put("ranger.jpa.jdbc.driver", "org.postgresql.Driver");
                xml.put("ranger.jpa.audit.jdbc.driver", "org.postgresql.Driver");
                xml.put("ranger.jpa.jdbc.preferredtestquery", "select 1");
            } else {
                throw new ConfigmapException("不支持的数据库类型！" + dbFlavor);
            }

            // 进行覆盖
            var spec = ranger.getSpec();
            var overwrites = spec.getOverwriteConfigs();
            if (overwrites != null && !overwrites.isEmpty()) {
                overwrites.forEach(ow -> xml.put(ow.getName(), ow.getValue()));
            }

            // 最后输出新的xml
            var output = new StringBuilder();
            output.append("<configuration>\n");
            xml.forEach((k, v) -> {
                output.append("  <property>\n");
                output.append("     <name>").append(k).append("</name>\n");
                output.append("     <value>").append(v).append("</value>\n");
                output.append("  </property>\n");
            });
            output.append("</configuration>");

            return output.toString();
        } catch (IOException e) {
            throw new ConfigmapException("解析重新生成的 ranger-admin-site.xml 信息失败！" + e.getMessage(), e);
        }
    }

    @Override
    public Result<ConfigMap> match(ConfigMap actual, Ranger primary, Context<Ranger> context) {
        // 这里主要判断configmap的值 是否一致
        var content = getRangerAdminSite(primary, context);
        if (!content.equals(actual.getData().get(RangerReconciler.RANGER_ADMIN_SITE_FILE))) {
            LOGGER.info("当前 ranger-admin-site.xml 发生了修改，需要进行更新！");
            return Result.nonComputed(false);
        } else return Result.nonComputed(true);
    }

}
