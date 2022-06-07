package com.pesco.operator.ranger.crd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pesco.operator.common.crd.EnvConfig;
import com.pesco.operator.common.crd.host.HostAlias;
import com.pesco.operator.common.crd.resource.Resources;
import com.pesco.operator.common.crd.storage.Storage;
import com.pesco.operator.common.type.ImagePullPolicy;
import com.pesco.operator.ranger.crd.service.InitService;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

/**
 * @Title ranger服务声明信息
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.ranger.crd
 * @Description
 * @Date 2022/6/7 4:32 下午
 * @Version V1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class RangerSpec {

    private String image;

    private String version;

    private Integer replicas = 1;

    private ImagePullPolicy imagePullPolicy = ImagePullPolicy.IfNotPresent;

    private String imagePullSecrets;

    private String serviceAccount;

    private Resources resources;

    private List<HostAlias> hostAliases;

    private Storage storage;

    private List<EnvConfig> env;

    private String hadoopConfigFolder;

    /**
     * install.properties，这个文件在首次安装时候生效
     * 后续的修改如果不手工触发则不会生效
     * 默认情况下，install 文件会转为 ranger-admin-site.xml 文件
     */
    private String installProperties;

    /**
     * 由于编程的局限性，有些变量没有从 install 转化到 ranger-admin-site.xml
     * 这时候，我们就需要手工填写转化规则进行转化
     */
    private List<EnvConfig> overwriteConfigs;

    /**
     * 是否初始化各个ranger里面的组件
     * 每一次ranger重启的时候，都会自动判断一下该组件是否注册过
     * 没有注册则会新建；否则就跳过而不是更新
     */
    private InitService initService;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public ImagePullPolicy getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(ImagePullPolicy imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public String getImagePullSecrets() {
        return imagePullSecrets;
    }

    public void setImagePullSecrets(String imagePullSecrets) {
        this.imagePullSecrets = imagePullSecrets;
    }

    public String getServiceAccount() {
        return serviceAccount;
    }

    public void setServiceAccount(String serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public List<HostAlias> getHostAliases() {
        return hostAliases;
    }

    public void setHostAliases(List<HostAlias> hostAliases) {
        this.hostAliases = hostAliases;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public List<EnvConfig> getEnv() {
        return env;
    }

    public void setEnv(List<EnvConfig> env) {
        this.env = env;
    }

    public String getHadoopConfigFolder() {
        return hadoopConfigFolder;
    }

    public void setHadoopConfigFolder(String hadoopConfigFolder) {
        this.hadoopConfigFolder = hadoopConfigFolder;
    }

    public String getInstallProperties() {
        return installProperties;
    }

    public void setInstallProperties(String installProperties) {
        this.installProperties = installProperties;
    }

    public List<EnvConfig> getOverwriteConfigs() {
        return overwriteConfigs;
    }

    public void setOverwriteConfigs(List<EnvConfig> overwriteConfigs) {
        this.overwriteConfigs = overwriteConfigs;
    }

    public InitService getInitService() {
        return initService;
    }

    public void setInitService(InitService initService) {
        this.initService = initService;
    }

    @Override
    public String toString() {
        return "RangerSpec{" +
                "image='" + image + '\'' +
                ", version='" + version + '\'' +
                ", replicas=" + replicas +
                ", imagePullPolicy=" + imagePullPolicy +
                ", imagePullSecrets='" + imagePullSecrets + '\'' +
                ", serviceAccount='" + serviceAccount + '\'' +
                ", resources=" + resources +
                ", hostAliases=" + hostAliases +
                ", storage=" + storage +
                ", env=" + env +
                ", hadoopConfigFolder='" + hadoopConfigFolder + '\'' +
                ", installProperties='" + installProperties + '\'' +
                ", overwriteConfigs=" + overwriteConfigs +
                ", initService=" + initService +
                '}';
    }
}
