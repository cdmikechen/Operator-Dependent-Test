package com.pesco.operator.hadoop.folder;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.*;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @Title hadoop配置文件夹，相当于组合不同的config到一个文件夹使用
 * @Company 尚源智慧科技有限公司
 * @Author 陈翔
 * @Package com.pesco.operator.hadoop.folder
 * @Description
 * @Date 2022/6/7 9:17 上午
 * @Version V1.0
 */

@Group("com.pesco.operator")
@Kind("HadoopConfigFolder")
@Plural("hadoopconfigfolders")
@Singular("hadoopconfigfolder")
@ShortNames("hdcf")
@Version("v1")
@RegisterForReflection
public class HadoopConfigFolder extends CustomResource<HadoopConfigFolderSpec, HadoopConfigFolderStatus> implements Namespaced {

}
